package com.shopgun.android.sdk.eventskit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventResponse {

    List<Item> mItems = new ArrayList<>();

    public static EventResponse fromJson(JSONObject object) {
        JSONArray events = object.optJSONArray("events");
        return fromJson(events);
    }

    public static EventResponse fromJson(JSONArray array) {
        EventResponse response = new EventResponse();
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.optJSONObject(i);
            Item event = Item.fromJson(object);
            response.mItems.add(event);
        }
        return response;
    }

    enum Status {
        ACK, NACK, ERROR;
        public static Status fromString(String status) {
            if ("ack".equalsIgnoreCase(status)) {
                return ACK;
            } else if ("nack".equalsIgnoreCase(status)) {
                return NACK;
            } else {
                return ERROR;
            }
        }
    }

    public static class Item {

        public String id;
        public Status status;
        public List<JSONObject> errors = new ArrayList<>();

        public static Item fromJson(JSONObject object) {
            Item r = new Item();
            r.id = object.optString("id");
            r.status = Status.fromString(object.optString("status"));
            JSONArray jErrors = object.optJSONArray("errors");
            if (jErrors != null) {
                for (int i = 0; i < jErrors.length(); i++) {
                    JSONObject error = jErrors.optJSONObject(i);
                    r.errors.add(error);
                }
            }
            return r;
        }

    }

    public List<Item> getAck() {
        return get(Status.ACK);
    }

    public List<Item> getNack() {
        return get(Status.NACK);
    }

    public List<Item> getError() {
        return get(Status.ERROR);
    }

    private List<Item> get(Status status) {
        List<Item> list = new ArrayList<>();
        for (Item item : mItems) {
            if (status == item.status) {
                list.add(item);
            }
        }
        return list;
    }

    public Set<String> getAckIds() {
        return getIds(Status.ACK);
    }

    public Set<String> getNackIds() {
        return getIds(Status.NACK);
    }

    public Set<String> getErrorIds() {
        return getIds(Status.ERROR);
    }

    public Set<String> getIds(Status status) {
        Set<String> ids = new HashSet<>();
        for (Item item : mItems) {
            if (status == item.status) {
                ids.add(item.id);
            }
        }
        return ids;
    }

}
