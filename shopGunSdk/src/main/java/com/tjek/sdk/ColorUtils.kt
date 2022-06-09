package com.tjek.sdk

import java.lang.NumberFormatException

// Convert the hexadecimal string into an integer
fun String?.getColorFromHexStr(): Int {
    return if (this == null || length < 2 ) 0
    else try {
        Integer.parseInt(substring(1), 16)
    } catch (e: NumberFormatException) {
        0
    }
}