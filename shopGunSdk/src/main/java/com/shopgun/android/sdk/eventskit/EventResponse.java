package com.shopgun.android.sdk.eventskit;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventResponse {

    public static final String TAG = EventResponse.class.getSimpleName();
    
    @SerializedName("events")
    List<Item> mEvents = new ArrayList<>();

    private static final String ACK = "ack";
    private static final String NACK = "nack";

    public static class Item {

        @SerializedName("id")
        private String mId;
        @SerializedName("status")
        private String mStatus;
        @SerializedName("errors")
        private JsonArray mErrors;

        public String getId() {
            return mId;
        }

        public void setId(String id) {
            this.mId = id;
        }

        public String getStatus() {
            return mStatus;
        }

        public void setStatus(String status) {
            this.mStatus = status;
        }

        public JsonArray getErrors() {
            return mErrors;
        }

        public void setErrors(JsonArray errors) {
            this.mErrors = errors;
        }

        public boolean isAck() {
            return ACK.equalsIgnoreCase(mStatus);
        }

        public boolean isNack() {
            return NACK.equalsIgnoreCase(mStatus);
        }

        public boolean isError() {
            return !(isAck() || isNack());
        }

    }

    /**
     * @return Items that have been either of {@code ack}'ed or reported to have some sort of unrecoverable error.
     */
    public Set<String> getRemovableItems() {
        Set<String> ids = new HashSet<>(mEvents.size());
        for (Item item : mEvents) {
            if (item.isAck() || !item.isNack()) {
                ids.add(item.mId);
            }
        }
        return ids;
    }

    public Set<String> getNackItems() {
        Set<String> ids = new HashSet<>(mEvents.size());
        for (Item item : mEvents) {
            if (item.isNack()) {
                ids.add(item.mId);
            }
        }
        return ids;
    }

    public Set<String> getAckItems() {
        Set<String> ids = new HashSet<>(mEvents.size());
        for (Item item : mEvents) {
            if (item.isAck()) {
                ids.add(item.mId);
            }
        }
        return ids;
    }

    public List<Item> getErrors() {
        List<Item> ids = new ArrayList<>(mEvents.size());
        for (Item item : mEvents) {
            if (item.isError()) {
                ids.add(item);
            }
        }
        return ids;
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(EventResponse.this);
    }
}
