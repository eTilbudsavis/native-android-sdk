package com.shopgun.android.sdk.corekit.realm;

import com.shopgun.android.sdk.eventskit.Event;

import io.realm.annotations.RealmModule;

@RealmModule(library = true, classes = {Event.class})
public class SgnLegacyEventRealmModule {
}
