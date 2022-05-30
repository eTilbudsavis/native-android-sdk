package com.tjek.sdk.api.models

import com.tjek.sdk.api.HexColor

data class Branding(
    val name: String = "",
    val website: String = "",
    val description: String = "",
    val logoURL: String = "",
    val hexColor: HexColor = 0
)