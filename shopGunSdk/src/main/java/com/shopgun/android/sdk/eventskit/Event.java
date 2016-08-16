package com.shopgun.android.sdk.eventskit;

import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.utils.SgnUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import io.realm.RealmModel;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

@RealmClass
public class Event implements RealmModel {

    public static final String TAG = Event.class.getSimpleName();
    public static final String VERSION = "1.0.0";

    public static Event fromJson(JSONObject object) {
        Event e = new Event();
        e.setVersion(object.optString("version"));
        e.setId(object.optString("id"));
        e.setType(object.optString("type"));
        // TODO Fix the dates
        e.setRecordedAt(SgnUtils.stringToDate(object.optString("recordedAt")));
        e.setSentAt(SgnUtils.stringToDate(object.optString("sentAt")));
        e.setReceivedAt(SgnUtils.stringToDate(object.optString("receivedAt")));
        e.setClient(object.optJSONObject("client"));
        e.setContext(object.optJSONObject("context"));
        e.setProperties(object.optJSONObject("properties"));
        return e;
    }

    /* The event version scheme to use */
    private String mVersion = VERSION;
    /* A uuid that uniquely identifies the event */
    @PrimaryKey
    private String mId;
    /* This determines what will be valid in "properties" key */
    private String mType;
    /* Time of the event, according to the client */
    private Date mRecordedAt;
    /* time of transmission according to the client */
    private Date mSentAt;
    /* time the event arrived at the server according to the server */
    private Date mReceivedAt;
    /* information about the client sending the event. */
    @Ignore
    private JSONObject mClient;
    /* Contextual event information about sender, viewport, etc. */
    @Ignore
    private JSONObject mContext;
    /* What ever properties goes with the event type */
    @Ignore
    private JSONObject mProperties;
    /* number of retries performed */
    private int mRetryCount;

    private String mJsonClient;
    private String mJsonContext;
    private String mJsonProperties;

    public Event() {
        mRecordedAt = new Date();
        mId = SgnUtils.createUUID();
    }

    public Event(String type, JSONObject properties) {
        this();
        mType = type;
        mProperties = properties;
    }

    public String getVersion() {
        return mVersion;
    }

    public void setVersion(String version) {
        mVersion = version;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public Date getRecordedAt() {
        return mRecordedAt;
    }

    public void setRecordedAt(Date recordedAt) {
        mRecordedAt = recordedAt;
    }

    public Date getSentAt() {
        return mSentAt;
    }

    public void setSentAt(Date sentAt) {
        mSentAt = sentAt;
    }

    public Date getReceivedAt() {
        return mReceivedAt;
    }

    public void setReceivedAt(Date receivedAt) {
        mReceivedAt = receivedAt;
    }

    public JSONObject getClient() {
        if (mClient == null) {
            try {
                mClient = new JSONObject(mJsonClient);
            } catch (JSONException e) {
                SgnLog.e(TAG, "Client wasn't set as a JSON string.", e);
            }
        }
        return mClient;
    }

    public void setClient(JSONObject client) {
        mClient = client;
        if (mClient != null) {
            mJsonClient = client.toString();
        }
    }

    public JSONObject getContext() {
        if (mContext == null) {
            try {
                mContext = new JSONObject(mJsonContext);
            } catch (JSONException e) {
                SgnLog.e(TAG, "Context wasn't set as a JSON string.", e);
            }
        }
        return mContext;
    }

    public void setContext(JSONObject context) {
        mContext = context;
        if (mContext!= null) {
            mJsonContext = context.toString();
        }
    }

    public JSONObject getProperties() {
        if (mProperties == null) {
            try {
                mProperties = new JSONObject(mJsonProperties);
            } catch (JSONException e) {
                SgnLog.e(TAG, "Properties wasn't set as a JSON string.", e);
            }
        }
        return mProperties;
    }

    public void setProperties(JSONObject eventProperty) {
        mProperties = eventProperty;
        if (mProperties != null) {
            mJsonProperties = eventProperty.toString();
        }
    }

    public void setRetryCount(int retryCount) {
        mRetryCount = retryCount;
    }

    public void incrementRetryCount() {
        mRetryCount++;
    }

    public int getRetryCount() {
        return mRetryCount;
    }

    public JSONObject toJson() {
        JsonMap map = new JsonMap();
        map.put("version", mVersion);
        map.put("id", mId);
        map.put("type", mType);
        map.put("recordedAt", mRecordedAt);
        map.put("sentAt", mSentAt);
        map.put("receivedAt", mReceivedAt);
        map.put("client", mClient);
        map.put("context", mContext);
        map.put("properties", mProperties);
        return new JSONObject(map);
    }

    @Override
    public String toString() {
        return toJson().toString();
    }
}
