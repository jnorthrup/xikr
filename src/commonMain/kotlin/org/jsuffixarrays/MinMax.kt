package org.jsuffixarrays
/**
 * Holder for minimum and maximum.
 *
 * @see Tools.minmax
 */
class MinMax(val min: Int, val max: Int) {

    fun range(): Int {
        return max - min
    }
}