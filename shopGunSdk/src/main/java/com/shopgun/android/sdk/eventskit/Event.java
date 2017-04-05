package com.shopgun.android.sdk.eventskit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shopgun.android.sdk.utils.SgnUtils;
import com.shopgun.android.utils.DateUtils;
import com.shopgun.android.utils.log.L;
import com.shopgun.android.utils.log.LogUtil;

import java.util.Date;

import io.realm.RealmModel;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

@RealmClass
public class Event implements RealmModel {

    public static final String TAG = Event.class.getSimpleName();
    public static final String VERSION = "1.0.0";

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
    /* number of retries performed */
    private int mRetryCount;
    /* information about the client sending the event. */
    @Ignore private JsonObject mJsonClient;
    private String mStringClient;
    /* Contextual event information about sender, viewport, etc. */
    @Ignore private JsonObject mJsonContext;
    private String mStringContext;
    /* What ever properties goes with the event type */
    @Ignore private JsonObject mJsonProperties;
    private String mStringProperties;
    /* Tag for random properties */
    @Ignore private Object mTag;

    public Event() {
        mRecordedAt = new Date();
        mId = SgnUtils.createUUID();
    }

    public Event(String type, JsonObject properties) {
        this();
        setType(type);
        setProperties(properties);
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

    public JsonObject getClient() {
        if (mJsonClient == null) {
            mJsonClient = parse(mStringClient);
        }
        return mJsonClient;
    }

    public void setClient(JsonObject client) {
        mJsonClient = client;
        if (mJsonClient != null) {
            this.mStringClient = client.toString();
        }
    }

    public JsonObject getContext() {
        if (mJsonContext == null) {
            mJsonContext = parse(mStringContext);
        }
        return mJsonContext;
    }

    public void setContext(JsonObject context) {
        mJsonContext = context;
        if (mJsonContext!= null) {
            mStringContext = context.toString();
        }
    }

    public JsonObject getProperties() {
        if (mJsonProperties == null) {
            mJsonProperties = parse(mStringProperties);
        }
        return mJsonProperties;
    }

    public void setProperties(JsonObject eventProperty) {
        mJsonProperties = eventProperty;
        if (mJsonProperties != null) {
            mStringProperties = eventProperty.toString();
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

    private JsonObject parse(String json) {
        try {
            return (JsonObject) new JsonParser().parse(json);
        } catch (Exception e) {
            return null;
        }
    }

    public void setTag(Object tag) {
        mTag = tag;
    }

    public Object getTag() {
        return mTag;
    }

    @Override
    public String toString() {
        return toString(true, true, true);
    }

    public String toString(boolean id, boolean client, boolean context) {
        StringBuilder sb = new StringBuilder();
        if (id) {
            sb.append("id: ").append(mId).append(", ");
        }
        sb.append("type: ").append(mType);
        sb.append(", recordedAt: ").append(DateUtils.format(mRecordedAt, true));
        if (mSentAt != null) {
            sb.append(", sentAt: ").append(DateUtils.format(mSentAt, true));
        }
        if (mReceivedAt != null) {
            sb.append(", receivedAt: ").append(DateUtils.format(mReceivedAt, true));
        }
        sb.append(", properties: ").append(getProperties());
        if (mRetryCount > 0) {
            sb.append(", mRetryCount: ").append(mRetryCount);
        }
        if (client) {
            sb.append(", client: ").append(getClient());
        }
        if (context) {
            sb.append(", context: ").append(getContext());
        }
        return sb.toString();
    }

}
