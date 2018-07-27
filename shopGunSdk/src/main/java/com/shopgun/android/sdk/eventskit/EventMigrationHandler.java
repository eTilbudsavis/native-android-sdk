package com.shopgun.android.sdk.eventskit;

import android.util.Base64;

import com.fonfon.geohash.GeoHash;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shopgun.android.sdk.SgnLocation;
import com.shopgun.android.sdk.utils.SgnUtils;
import com.shopgun.android.utils.DateUtils;

import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmResults;
import io.realm.RealmSchema;

/**
 * Class to handle the migration of the schema for the events database
 *
 * Reference: https://realm.io/docs/java/latest/#migrations
 */
public class EventMigrationHandler implements RealmMigration {

    private String applicationTrackId;

    public EventMigrationHandler(final String applicationTrackId) {
        this.applicationTrackId = applicationTrackId;
    }

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

        RealmSchema schema = realm.getSchema();

        if(oldVersion == 1L) {
            RealmObjectSchema eventSchema = schema.get("Event");
            if(eventSchema == null) {
                return;
            }

            // change the version
            eventSchema.transform(new RealmObjectSchema.Function() {
                @Override
                public void apply(DynamicRealmObject obj) {
                    obj.set("mVersion", "2");
                }
            });

            // add the application track id
            eventSchema.addField("mApplicationTrackId", String.class)
                    .transform(new RealmObjectSchema.Function() {
                        @Override
                        public void apply(DynamicRealmObject obj) {
                            obj.setString("mApplicationTrackId", applicationTrackId);
                        }
                    });

            // translate recordedAt from Date to UTC in seconds
            mapTimestamp(eventSchema);

            // extract location info from context json string
            mapLocationData(eventSchema);

            // duplicate events paged-publication-page-spread-disappeared
            // because we have to check if there are 2 pages
            addTemporaryPageField(eventSchema);
            duplicatePageSpreadEvents(realm);

            // map the event type from string to int
            // and extract the properties depending on the type
            mapEventType(eventSchema);

            // delete not mappable events (in case there are old events like zoom-in...)
            // that has been marked in the previous step
            realm.where("Event").equalTo("mType", -1).findAll().deleteAllFromRealm();

            // delete old fields
            eventSchema
                    .removeField("page_tmp")
                    .removeField("mStringClient")
                    .removeField("mStringProperties");

            // increment the version in case we have to go through multiple steps in future updates
            oldVersion++;
        }
    }

    private void addTemporaryPageField(RealmObjectSchema eventSchema) {
        eventSchema.addField("page_tmp", int.class)
                .transform(new RealmObjectSchema.Function() {
                    @Override
                    public void apply(DynamicRealmObject obj) {
                        obj.set("page_tmp", 1);
                    }
                });
    }

    private void duplicatePageSpreadEvents(DynamicRealm realm) {
        RealmResults<DynamicRealmObject> spread_page_event = realm
                .where("Event")
                .equalTo("mType", "paged-publication-page-spread-disappeared")
                .findAll();
        if (spread_page_event.size() > 0) {
            for (DynamicRealmObject e : spread_page_event) {
                try {
                    // create and add a new object into the realm with a new id
                    DynamicRealmObject copy = realm.createObject("Event", SgnUtils.createUUID());

                    // copy all the fields modified or added until now
                    copy.setString("mVersion", e.getString("mVersion"));
                    copy.setString("mType", e.getString("mType"));
                    copy.setLong("mTimestamp", e.getLong("mTimestamp"));
                    copy.setString("mApplicationTrackId", e.getString("mApplicationTrackId"));
                    copy.setInt("mRetryCount", e.getInt("mRetryCount"));
                    copy.setString("mGeoHash", e.getString("mGeoHash"));
                    copy.setLong("mLocationTimestamp", e.getLong("mLocationTimestamp"));
                    copy.setString("mCountry", e.getString("mCountry"));
                    copy.setString("mStringClient", e.getString("mStringClient"));
                    copy.setString("mStringProperties", e.getString("mStringProperties"));

                    // this will have to carry the info about the second page of the spread
                    copy.setInt("page_tmp", 2);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void mapEventType(RealmObjectSchema eventSchema) {
        eventSchema.addField("type_tmp", int.class)
                .addField("mViewToken", String.class)
                .addField("mAdditionalPayload", String.class)
                .transform(new TransformPropertiesFunction())
                .removeField("mType")
                .renameField("type_tmp", "mType");
    }

    private void mapTimestamp(RealmObjectSchema eventSchema) {
        eventSchema.addField("mTimestamp", long.class)
                .transform(new RealmObjectSchema.Function() {
                    @Override
                    public void apply(DynamicRealmObject obj) {
                        Date recordedAt = obj.getDate("mRecordedAt");
                        long utc = convertDate(recordedAt);
                        obj.setLong("mTimestamp", utc);
                    }
                })
                .removeField("mRecordedAt")
                .removeField("mSentAt")
                .removeField("mReceivedAt");
    }

    private long convertDate(Date date) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        return TimeUnit.MILLISECONDS.toSeconds(calendar.getTimeInMillis());
    }

    private void mapLocationData(RealmObjectSchema eventSchema) {
        eventSchema.addField("mGeoHash", String.class)
                .addField("mLocationTimestamp", long.class)
                .addField("mCountry", String.class)
                .transform(new RealmObjectSchema.Function() {
                    @Override
                    public void apply(DynamicRealmObject obj) {
                        String eventContext = obj.getString("mStringContext");
                        JsonObject json = parse(eventContext);
                        if (json != null) {
                            JsonObject location = json.getAsJsonObject("location");
                            JsonObject accuracy = location.getAsJsonObject("accuracy");
                            if (accuracy.get("horizontal").getAsFloat() <= 2000) {

                                // take latitude and longitude
                                double lat = location.get("latitude").getAsDouble();
                                double lon = location.get("longitude").getAsDouble();
                                SgnLocation l = new SgnLocation();
                                l.setLatitude(lat);
                                l.setLongitude(lon);
                                GeoHash geoHash = GeoHash.fromLocation(l, SgnLocation.GEO_HASH_PRECISION);
                                obj.setString("mGeoHash", geoHash.toString());

                                // take timestamp
                                String time = location.get("determinedAt").getAsString();
                                long timestamp = 0;
                                try {
                                    timestamp = convertDate(DateUtils.parse(time));
                                }catch (ParseException ignore){}
                                obj.setLong("mLocationTimestamp", timestamp);

                                // set country data
                                String country = Locale.getDefault().getCountry();
                                obj.setString("mCountry", country.length() > 2 ? "" : country);
                            }
                        }
                    }
                })
                .removeField("mStringContext");
    }

    private JsonObject parse(String json) {
        try {
            return (JsonObject) new JsonParser().parse(json);
        } catch (Exception e) {
            return null;
        }
    }

    private class TransformPropertiesFunction implements RealmObjectSchema.Function {

        @Override
        public void apply(DynamicRealmObject obj) {
            String oldType = obj.getString("mType");

            String properties = obj.getString("mStringProperties");
            JsonObject json_properties = parse(properties);

            String client = obj.getString("mStringClient");
            JsonObject json_client = parse(client);
            String clientId = (json_client != null) ? json_client.get("id").getAsString() : SgnUtils.createUUID();

            String vt = "";
            String payload_string = "";
            int type;

            switch (oldType) {

                case "paged-publication-opened": {
                    String pp_id = getPublicationId(json_properties);
                    vt = generateViewToken(clientId + pp_id);
                    JsonObject payload = new JsonObject();
                    payload.addProperty("pp.id", pp_id);
                    payload_string = payload.toString();
                    type = 1;
                    break;
                }
                case "paged-publication-page-spread-disappeared":
                    int pageNumber = getPageNumber(json_properties, obj.getInt("page_tmp"));
                    if (pageNumber == -1) {
                        // we could have a spread with one page. In this case, the copy will have
                        // page_tmp = 2 and can be deleted
                        type = -1;
                    }
                    else {
                        type = 2;
                        String pp_id = getPublicationId(json_properties);
                        vt = generateViewToken(clientId + pp_id + String.valueOf(pageNumber));
                        JsonObject payload = new JsonObject();
                        payload.addProperty("pp.id", pp_id);
                        payload.addProperty("ppp.n", pageNumber);
                        payload_string = payload.toString();
                    }
                    break;

                case "offer-opened": {
                    String of_id = getOfferId(json_properties);
                    vt = generateViewToken(clientId + of_id);
                    JsonObject payload = new JsonObject();
                    payload.addProperty("of.id", of_id);
                    payload_string = payload.toString();
                    type = 3;
                    break;
                }
                case "client-session-opened":
                    // nothing special to do
                    type = 4;
                    break;

                case "searched":
                    if (json_properties == null) {
                        type = -1;
                    }
                    else {
                        JsonObject search = json_properties.getAsJsonObject("search");
                        String query = search.get("query").getAsString();
                        vt = generateViewToken(clientId + query);
                        JsonObject payload = new JsonObject();
                        payload.addProperty("sea.q", query);
                        payload_string = payload.toString();
                        type = 5;
                    }
                    break;

                default:
                    // old events
                    type = -1;
            }

            obj.setString("mViewToken", vt);
            obj.setString("mAdditionalPayload", payload_string);
            obj.setInt("type_tmp", type);
        }

        private int getPageNumber(JsonObject json_properties, int requestedPage) {
            int page = -1;
            if (json_properties != null) {
                try {
                    JsonObject spread = json_properties.getAsJsonObject("pagedPublicationPageSpread");
                    JsonArray pages = spread.getAsJsonArray("pageNumbers");
                    if (requestedPage <= pages.size()) {
                        page = pages.get(requestedPage - 1).getAsInt();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return page;
        }


        private String generateViewToken(String data) {
            try {
                // Create MD5 Hash
                MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
                digest.update(data.getBytes());
                byte digest_result[] = digest.digest();
                byte md5[] = Arrays.copyOfRange(digest_result, 0, 8);

                // encode to base 64
                return Base64.encodeToString(md5, Base64.DEFAULT);

            }catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

        private String getOfferId(JsonObject json_properties) {
            return getId(json_properties, "offer");
        }

        private String getPublicationId(JsonObject json_properties) {
            return getId(json_properties, "pagedPublication");
        }

        private String getId(JsonObject properties, String field) {
            String id = "";
            if (properties != null) {
                try {
                    JsonObject pp = properties.getAsJsonObject(field);
                    JsonArray json_id = pp.getAsJsonArray("id");
                    id = json_id.get(1).getAsString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return id;
        }
    }
}
