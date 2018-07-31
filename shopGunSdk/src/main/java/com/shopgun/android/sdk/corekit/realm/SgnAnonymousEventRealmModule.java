package com.shopgun.android.sdk.corekit.realm;

import com.shopgun.android.sdk.eventskit.AnonymousEventWrapper;

import io.realm.annotations.RealmModule;

@RealmModule(library = true, classes = {AnonymousEventWrapper.class})
public class SgnAnonymousEventRealmModule {
}
