package com.tjek.sdk.api.remote

internal const val USER_AGENT = "com.tjek.android.sdk/%s (%s/%s)"

enum class EndpointEnvironment(val host: String) {
    PRODUCTION("squid-api.tjek.com"),
    STAGING("squid-api.tjek-staging.com")
}

enum class NetworkLogLevel {
    None, Basic, Full
}
