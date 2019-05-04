package org.jsuffixarrays
class TRBudget(var chance: Int, var remain: Int) {
    var incval: Int = 0
    var count: Int = 0

    init {
        this.incval = remain
    }

    fun check(size: Int): Int {
        if (size <= this.remain) {
            this.remain -= size
            return 1
        }
        if (this.chance == 0) {
            this.count += size
            return 0
        }
        this.remain += this.incval - size
        this.chance -= 1
        return 1
    }
}