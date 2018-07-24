package com.shopgun.android.sdk.eventskit;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Base64;

import com.fonfon.geohash.GeoHash;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shopgun.android.sdk.SgnLocation;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.utils.PackageUtils;

import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

import static com.shopgun.android.sdk.eventskit.Event.META_APPLICATION_TRACK_ID;
import static com.shopgun.android.sdk.eventskit.Event.META_APPLICATION_TRACK_ID_DEBUG;

/**
 * Class to handle the migration of the schema for the events database
 *
 * Reference: https://realm.io/docs/java/latest/#migrations
 */
public class EventMigrationHandler implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

        RealmSchema schema = realm.getSchema();

        if(oldVersion == 1L) {
            migrateToVersionTwo(schema);
            oldVersion++;
        }
    }

    private void migrateToVersionTwo(RealmSchema schema) {
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

        // translate recordedAt from Date to UTC in seconds
        mapTimestamp(eventSchema);

        // add the application track id
        addApplicationTrackId(eventSchema);

        // extract location info from context json string
        mapLocationData(eventSchema);

        // map the event type from string to int
        // and extract the properties depending on the type
        mapEventType(eventSchema);

        // delete not mappable events (in case there are old events like zoom-in...)
        // that has been marked in the previous step

        // delete old fields
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
                });
    }

    private long convertDate(Date date) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        return TimeUnit.MILLISECONDS.toSeconds(calendar.getTimeInMillis());
    }

    private void addApplicationTrackId(RealmObjectSchema eventSchema) {
        Context c = ShopGun.getInstance().getContext();
        Bundle b = PackageUtils.getMetaData(c);
        final String trackerId = b.getString(ShopGun.getInstance().isDevelop() && b.containsKey(META_APPLICATION_TRACK_ID_DEBUG) ?
                META_APPLICATION_TRACK_ID_DEBUG :
                META_APPLICATION_TRACK_ID);
        eventSchema.addField("mApplicationTrackId", String.class)
                .transform(new RealmObjectSchema.Function() {
                    @Override
                    public void apply(DynamicRealmObject obj) {
                        obj.setString("mApplicationTrackId", trackerId);
                    }
                });
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
                            if (location.get("accuracy").getAsInt() <= 2000) {

                                // take latitude and longitude
                                long lat = location.get("latitude").getAsLong();
                                long lon = location.get("longitude").getAsLong();
                                SgnLocation l = new SgnLocation();
                                l.setLatitude(lat);
                                l.setLongitude(lon);
                                GeoHash geoHash = GeoHash.fromLocation(l, SgnLocation.GEO_HASH_PRECISION);
                                obj.setString("mGeoHash", geoHash.toString());

                                // take timestamp
                                String time = location.get("determinedAt").getAsString();
                                long timestamp = convertDate(new Date(time));
                                obj.setLong("mLocationTimestamp", timestamp);

                                // set country data
                                String country = Locale.getDefault().getCountry();
                                obj.setString("mCountry", country.length() > 2 ? "" : country);
                            }
                        }
                    }
                });
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
            String clientId = json_client.get("id").getAsString();

            String vt = "";
            String payload_string = "";
            int type;

            switch (oldType) {

                case "paged-publication-opened": {
                    String pp_id = getPublicationId(json_properties);
                    vt = generateViewToken(client.concat(pp_id));
                    JsonObject payload = new JsonObject();
                    payload.addProperty("pp.id", pp_id);
                    payload_string = payload.toString();
                    type = 1;
                    break;
                }
                case "paged-publication-page-spread-disappeared":
                case "paged-publication-page-disappeared":
                    // todo
                    type = 2;
                    break;

                case "offer-opened": {
                    String of_id = getOfferId(json_properties);
                    vt = generateViewToken(client.concat(of_id));
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
                    JsonObject search = json_properties.getAsJsonObject("search");
                    String query = search.get("query").getAsString();
                    vt = generateViewToken(client.concat(query));
                    JsonObject payload = new JsonObject();
                    payload.addProperty("sea.q", query);
                    payload_string = payload.toString();
                    type = 5;
                    break;

                default:
                    // old events
                    type = -1;
            }

            obj.setString("mViewToken", vt);
            obj.setString("mAdditionalPayload", payload_string);
            obj.setInt("type_tmp", type);
        }



        private String generateViewToken(String data) {
            try {
                // Create MD5 Hash
                MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
                digest.update(data.getBytes());
                byte md5[] = new byte[8];
                digest.digest(md5, 0, 8);

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
            if (properties == null) {
                return "";
            }
            JsonObject pp = properties.getAsJsonObject(field);
            JsonArray id = pp.getAsJsonArray("id");
            return id.get(1).getAsString();
        }
    }
}
