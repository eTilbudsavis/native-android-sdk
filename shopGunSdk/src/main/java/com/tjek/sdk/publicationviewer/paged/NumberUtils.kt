package com.tjek.sdk.publicationviewer.paged

import kotlin.math.abs

object NumberUtils {
    const val FLOAT_EPSILON = 0.001f
    const val DOUBLE_EPSILON = 0.001


    /**
     * Method for detecting if two floats are almost equal (epsilon [FLOAT_EPSILON])
     * Inconsistencies are to be expected, due to the nature of float values in java
     *
     * @param lhs  a float
     * @param rhs another float
     * @return true if equal, else false
     */
    @JvmStatic
    fun isEqual(lhs: Float, rhs: Float): Boolean {
        return isEqual(lhs, rhs, FLOAT_EPSILON)
    }


    /**
     * Method for detecting if two floats are almost equal
     * Inconsistencies are to be expected, due to the nature of float values in java
     *
     * @param lhs   a float
     * @param rhs  another float
     * @param epsilon The precision of the measurement
     * @return true if equal, else false
     */
    @JvmStatic
    fun isEqual(lhs: Float, rhs: Float, epsilon: Float): Boolean {
        return lhs.compareTo(rhs) == 0 || abs(lhs - rhs) <= epsilon
    }

    /**
     * Clamp the current value in between a min and max value.
     * @param min the lower bound
     * @param current value to check
     * @param max the upper bound
     * @return `min` if `current` if less than `min`,
     * or `max` if `current` is greater than `max`,
     * else `current`.
     */
    @JvmStatic
    fun clamp(min: Float, current: Float, max: Float): Float {
        return min.coerceAtLeast(current.coerceAtMost(max))
    }
}