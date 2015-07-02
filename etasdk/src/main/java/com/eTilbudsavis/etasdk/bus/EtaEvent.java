package com.eTilbudsavis.etasdk.bus;

/**
 * @author Danny Hvam - danny@etilbudsavis.dk
 */
public class EtaEvent {
    
    private Object mTag;

    public Object getTag() {
        return mTag;
    }

    public void setTag(Object tag) {
        this.mTag = tag;
    }

    public String getType() {
        return getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return getType();
    }
}
