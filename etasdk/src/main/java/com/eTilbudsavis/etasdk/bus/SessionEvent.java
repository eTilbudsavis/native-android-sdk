package com.eTilbudsavis.etasdk.bus;

/**
 * Created by Danny Hvam - danny@etilbudsavis.dk on 13/05/15.
 */
public class SessionEvent extends EtaEvent {

    private int mOldUser = 0;
    private int mNewUser = 0;

    public SessionEvent(int oldUser, int newUser) {
        this.mOldUser = oldUser;
        this.mNewUser = newUser;
    }

    public boolean isNewUser() {
        return mOldUser != mNewUser;
    }

    public int getOldUser() {
        return mOldUser;
    }

    public int getNewUser() {
        return mNewUser;
    }

    @Override
    public String toString() {
        return String.format("%s[ oldUser: %s, newUser: %s ]", getType(), mOldUser, mNewUser);
    }
}
