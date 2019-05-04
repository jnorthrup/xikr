package org.jsuffixarrays

import org.jsuffixarrays.StackElement.Companion.SENIL

/**
 *
 *
 * Straightforward reimplementation of the divsufsort algorithm given in: <pre>`
 * Yuta Mori, Short description of improved two-stage suffix sorting
 * algorithm, 2005.
 * http://homepage3.nifty.com/wpage/software/itssort.txt
`</pre> *
 *
 *
 * This implementation is basically a translation of the C version given by Yuta Mori:
 * <tt>libdivsufsort-2.0.0, http://code.google.com/p/libdivsufsort/</tt>
 *
 *
 * The implementation of this algorithm makes some assumptions about the input. See
 * [.buildSuffixArray] for details.
 */
class DivSufSort : ISuffixArrayBuilder {

    /* fields */
    var ALPHABET_SIZE: Int
    var BUCKET_A_SIZE: Int
    var BUCKET_B_SIZE: Int
    lateinit var SA: IntArray
    lateinit var T: IntArray
    var start: Int = 0

    constructor() {
        ALPHABET_SIZE = DEFAULT_ALPHABET_SIZE
        BUCKET_A_SIZE = ALPHABET_SIZE
        BUCKET_B_SIZE = ALPHABET_SIZE * ALPHABET_SIZE
    }

    /**
     * @param alphabetSize
     */
    constructor(alphabetSize: Int) {
        ALPHABET_SIZE = alphabetSize
        BUCKET_A_SIZE = ALPHABET_SIZE
        BUCKET_B_SIZE = ALPHABET_SIZE * ALPHABET_SIZE
    }

    /**
     * {@inheritDoc}
     *
     *
     * Additional constraints enforced by DivSufSort algorithm:
     *
     *  * non-negative (0) symbols in the input
     *  * symbols limited by alphabet size passed in the constructor.
     *  * length >= 2
     *
     *
     *
     */
    override fun buildSuffixArray(input: IntArray, start: Int, length: Int): IntArray {
        assert(input != null, { "input must not be null" })
        assert(length >= 2, { "input length must be >= 2" })
        var mm = minmax(input, start, length)
        assert(mm.min >= 0, { "input must not be negative" })
        assert(mm.max < ALPHABET_SIZE, { "max alphabet size is $ALPHABET_SIZE" })

        var ret = IntArray(length)
        this.SA = ret
        this.T = input
        var bucket_A = IntArray(BUCKET_A_SIZE)
        var bucket_B = IntArray(BUCKET_B_SIZE)
        this.start = start
        /* Suffixsort. */
        var m = sortTypeBstar(bucket_A, bucket_B, length)
        constructSuffixArray(bucket_A, bucket_B, length, m)
        return ret
    }


    /**
     * Constructs the suffix array by using the sorted order of type B* suffixes.
     */
    fun constructSuffixArray(bucket_A: IntArray, bucket_B: IntArray, n: Int, m: Int) {
        var i: Int
        var j: Int
        var k: Int // ptr
        var s: Int
        var c0: Int
        var c1: Int
        var c2: Int
        // (_c1)])
        if (0 < m) {
            /*
                        * Construct the sorted order of type B suffixes by using the sorted order of
                        * type B suffixes.
                        */
            c1 = ALPHABET_SIZE - 2
            while (0 <= c1) {
                /* Scan the suffix array from right to left. */
                i = bucket_B[c1 * ALPHABET_SIZE + (c1 + 1)]
                j = bucket_A[c1 + 1] - 1
                k = 0
                c2 = -1
                while (i <= j) {
                    if (0 < (SA[j].also { s = it })) {
                        // Tools.assert(T[s] == c1, "");
                        // Tools.assert(((s + 1) < n) && (T[s] <= T[s +
                        // 1]),
                        // "");
                        // Tools.assert(T[s - 1] <= T[s], "");
                        SA[j] = s.inv()
                        c0 = T[start + --s]
                        if (0 < s && T[start + s - 1] > c0) {
                            s = s.inv()
                        }
                        if (c0 != c2) {
                            if (0 <= c2) {
                                bucket_B[c1 * ALPHABET_SIZE + c2] = k
                            }
                            k = bucket_B[c1 * ALPHABET_SIZE + (c0.also { c2 = it })]
                        }
                        // Tools.assert(k < j, "");
                        SA[k--] = s
                    } else {
                        // Tools.assert(((s == 0) && (T[s] == c1))
                        // || (s < 0), "");
                        SA[j] = s.inv()
                    }
                    --j
                }
                --c1
            }
        }

        /*
                * Construct the suffix array by using the sorted order of type B suffixes.
                */
        k = bucket_A[T[start + n - 1].apply { c2 = this }]
        SA[k++] = if (T[start + n - 2] < c2) (n - 1).inv() else n - 1
        /* Scan the suffix array from left to right. */
        i = 0
        j = n
        while (i < j) {
            if (0 < (SA[i].also { s = it })) {
                // Tools.assert(T[s - 1] >= T[s], "");
                c0 = T[start + --s]
                if (s == 0 || T[start + s - 1] < c0) {
                    s = s.inv()
                }
                if (c0 != c2) {
                    bucket_A[c2] = k
                    k = bucket_A[c0.also { c2 = it }]
                }
                // Tools.assert(i < k, "");
                SA[k++] = s
            } else {
                // Tools.assert(s < 0, "");
                SA[i] = s.inv()
            }
            ++i
        }
    }

    /**
     *
     */
    fun sortTypeBstar(bucket_A: IntArray, bucket_B: IntArray, n: Int): Int {
        var PAb: Int
        var ISAb: Int
        var buf: Int

        var i: Int
        var j: Int
        var k: Int
        var t: Int
        var m: Int
        var bufsize: Int
        var c0: Int
        var c1 = 0

        /*
                * Count the number of occurrences of the first one or two characters of each type
                * A, B and B suffix. Moreover, store the beginning position of all type B
                * suffixes into the array SA.
                */
        i = n - 1
        m = n
        c0 = T[start + n - 1]
        while (0 <= i) {
            /* type A suffix. */
            do {
                ++bucket_A[c0.also { c1 = it }]
            } while (0 <= --i && (T[start + i].also { c0 = it }) >= c1)
            if (0 <= i) {
                /* type B suffix. */
                ++bucket_B[c0 * ALPHABET_SIZE + c1]
                SA[--m] = i
                /* type B suffix. */
                --i
                c1 = c0
                while (0 <= i && (T[start + i].also { c0 = it }) <= c1) {
                    ++bucket_B[c1 * ALPHABET_SIZE + c0]
                    --i
                    c1 = c0
                }
            }
        }
        m = n - m

        // note:
        // A type B* suffix is lexicographically smaller than a type B suffix
        // that
        // begins with the same first two characters.

        // Calculate the index of start/end point of each bucket.
        c0 = 0
        i = 0
        j = 0
        while (c0 < ALPHABET_SIZE) {
            t = i + bucket_A[c0]
            bucket_A[c0] = i + j /* start point */
            i = t + bucket_B[c0 * ALPHABET_SIZE + c0]
            c1 = c0 + 1
            while (c1 < ALPHABET_SIZE) {
                j += bucket_B[c0 * ALPHABET_SIZE + c1]
                bucket_B[c0 * ALPHABET_SIZE + c1] = j // end point
                i += bucket_B[c1 * ALPHABET_SIZE + c0]
                ++c1
            }
            ++c0
        }

        if (0 < m) {
            // Sort the type B* suffixes by their first two characters.
            PAb = n - m// SA
            ISAb = m// SA
            i = m - 2
            while (0 <= i) {
                t = SA[PAb + i]
                c0 = T[start + t]
                c1 = T[start + t + 1]
                SA[--bucket_B[c0 * ALPHABET_SIZE + c1]] = i
                --i
            }
            t = SA[PAb + m - 1]
            c0 = T[start + t]
            c1 = T[start + t + 1]
            SA[--bucket_B[c0 * ALPHABET_SIZE + c1]] = m - 1

            // Sort the type B* substrings using sssort.

            buf = m// SA
            bufsize = n - 2 * m

            c0 = ALPHABET_SIZE - 2
            j = m
            while (0 < j) {
                c1 = ALPHABET_SIZE - 1
                while (c0 < c1) {
                    i = bucket_B[c0 * ALPHABET_SIZE + c1]
                    if (1 < j - i) {
                        ssSort(PAb, i, j, buf, bufsize, 2, n, SA[i] == m - 1)
                    }
                    j = i
                    --c1
                }
                --c0
            }

            // Compute ranks of type B* substrings.
            i = m - 1
            while (0 <= i) {
                if (0 <= SA[i]) {
                    j = i
                    do {
                        SA[ISAb + SA[i]] = i
                    } while (0 <= --i && 0 <= SA[i])
                    SA[i + 1] = i - j
                    if (i <= 0) {
                        break
                    }
                }
                j = i
                do SA[ISAb + (SA[i].inv().also { SA[i] = it })] = j
                while (SA[--i] < 0)
                SA[ISAb + SA[i]] = j
                --i
            }
            // Construct the inverse suffix array of type B* suffixes using
            // trsort.
            trSort(ISAb, m, 1)
            // Set the sorted order of type B* suffixes.
            i = n - 1
            j = m
            c0 = T[start + n - 1]
            while (0 <= i) {
                --i
                c1 = c0
                while (0 <= i && (T[start + i].also { c0 = it }) >= c1) {
                    --i
                    c1 = c0
                }
                if (0 <= i) {
                    t = i
                    --i
                    c1 = c0
                    while (0 <= i && (T[start + i].also { c0 = it }) <= c1) {
                        --i
                        c1 = c0
                    }
                    SA[SA[ISAb + --j]] = if (t == 0 || 1 < t - i) t else t.inv()
                }
            }

            // Calculate the index of start/end point of each bucket.
            bucket_B[(ALPHABET_SIZE - 1) * ALPHABET_SIZE + (ALPHABET_SIZE - 1)] = n // end
            // point
            c0 = ALPHABET_SIZE - 2
            k = m - 1
            while (0 <= c0) {
                i = bucket_A[c0 + 1] - 1
                c1 = ALPHABET_SIZE - 1
                while (c0 < c1) {
                    t = i - bucket_B[c1 * ALPHABET_SIZE + c0]
                    bucket_B[c1 * ALPHABET_SIZE + c0] = i // end point

                    // Move all type B* suffixes to the correct position.
                    i = t
                    j = bucket_B[c0 * ALPHABET_SIZE + c1]
                    while (j <= k) {
                        SA[i] = SA[k]
                        --i
                        --k
                    }
                    --c1
                }
                bucket_B[c0 * ALPHABET_SIZE + (c0 + 1)] = i - bucket_B[c0 * ALPHABET_SIZE + c0] + 1 //
                bucket_B[c0 * ALPHABET_SIZE + c0] = i // end point
                --c0
            }
        }

        return m
    }

    /**
     *
     */
    fun ssSort(PA: Int, first: Int, last: Int, buf: Int, bufsize: Int,
               depth: Int, n: Int, lastsuffix: Boolean) {
        var first = first
        var buf = buf
        var bufsize = bufsize
        var a: Int
        var b: Int
        var middle: Int
        var curbuf: Int// SA pointer

        var j: Int
        var k: Int
        var curbufsize: Int
        var limit = 0

        var i: Int

        if (lastsuffix) {
            ++first
        }

        if (bufsize < SS_BLOCKSIZE && bufsize < last - first
                && bufsize < (ssIsqrt(last - first).also { limit = it })) {
            if (SS_BLOCKSIZE < limit) {
                limit = SS_BLOCKSIZE
            }
            middle = last - limit
            buf = middle
            bufsize = limit
        } else {
            middle = last
            limit = 0
        }
        a = first
        i = 0
        while (SS_BLOCKSIZE < middle - a) {
            ssMintroSort(PA, a, a + SS_BLOCKSIZE, depth)
            curbufsize = last - (a + SS_BLOCKSIZE)
            curbuf = a + SS_BLOCKSIZE
            if (curbufsize <= bufsize) {
                curbufsize = bufsize
                curbuf = buf
            }
            b = a
            k = SS_BLOCKSIZE
            j = i
            while (j and 1 != 0) {
                ssSwapMerge(PA, b - k, b, b + k, curbuf, curbufsize, depth)
                b -= k
                k = k shl 1
                j = j shr 1
            }
            a += SS_BLOCKSIZE
            ++i
        }
        ssMintroSort(PA, a, middle, depth)
        k = SS_BLOCKSIZE
        while (i != 0) {
            if (i and 1 != 0) {
                ssSwapMerge(PA, a - k, a, middle, buf, bufsize, depth)
                a -= k
            }
            k = k shl 1
            i = i shr 1
        }
        if (limit != 0) {
            ssMintroSort(PA, middle, last, depth)
            ssInplaceMerge(PA, first, middle, last, depth)
        }

        if (lastsuffix) {
            var p1 = SA[PA + SA[first - 1]]
            var p11 = n - 2
            a = first
            i = SA[first - 1]
            while (a < last && (SA[a] < 0 || 0 < ssCompare(p1, p11, PA + SA[a], depth))) {
                SA[a - 1] = SA[a]
                ++a
            }
            SA[a - 1] = i
        }

    }

    /**
     * special version of ss_compare for handling
     * `ss_compare(T, &(PAi[0]), PA + *a, depth)` situation.
     */
    fun ssCompare(pa: Int, pb: Int, p2: Int, depth: Int): Int {
        var U1: Int
        var U2: Int
        var U1n: Int
        var U2n: Int// pointers to T

        U1 = depth + pa
        U2 = depth + SA[p2]
        U1n = pb + 2
        U2n = SA[p2 + 1] + 2
        while (U1 < U1n
                && U2 < U2n && T[start + U1] == T[start + U2]) {
            ++U1
            ++U2
        }

        return if (U1 < U1n)
            if (U2 < U2n) T[start + U1] - T[start + U2] else 1
        else
            if (U2 < U2n)
                -1
            else
                0
    }

    /**
     *
     */
    fun ssCompare(p1: Int, p2: Int, depth: Int): Int {
        var U1: Int
        var U2: Int
        var U1n: Int
        var U2n: Int// pointers to T

        U1 = depth + SA[p1]
        U2 = depth + SA[p2]
        U1n = SA[p1 + 1] + 2
        U2n = SA[p2 + 1] + 2
        while (((U1 < U1n)
                        && (U2 < U2n) && (T[start + U1] == T[start + U2]))) {
            ++U1
            ++U2
        }

        return if (U1 < U1n)
            (if (U2 < U2n) T[start + U1] - T[start + U2] else 1)
        else
            (if (U2 < U2n)
                -1
            else
                0)

    }

    /**
     *
     */
    fun ssInplaceMerge(PA: Int, first: Int, middle: Int, last: Int, depth: Int) {
        var middle = middle
        var last = last
        // PA, middle, first, last are pointers to SA
        var p: Int
        var a: Int
        var b: Int// pointer to SA
        var len: Int
        var half: Int
        var q: Int
        var r: Int
        var x: Int

        while (true) {
            if (SA[last - 1] < 0) {
                x = 1
                p = PA + SA[last - 1].inv()
            } else {
                x = 0
                p = PA + SA[last - 1]
            }
            a = first
            len = middle - first
            half = len shr 1
            r = -1
            while (0 < len) {
                b = a + half
                q = ssCompare(PA + (if ((0 <= SA[b])) SA[b] else SA[b].inv()), p, depth)
                if (q < 0) {
                    a = b + 1
                    half -= (len and 1) xor 1
                } else {
                    r = q
                }
                len = half
                half = half shr 1
            }
            if (a < middle) {
                if (r == 0) {
                    SA[a] = SA[a].inv()
                }
                ssRotate(a, middle, last)
                last -= middle - a
                middle = a
                if (first == middle) {
                    break
                }
            }
            --last
            if (x != 0) {
                while (SA[--last] < 0) {
                    // nop
                }
            }
            if (middle == last) {
                break
            }
        }

    }

    /**
     *
     */
    fun ssRotate(first: Int, middle: Int, last: Int) {
        var first = first
        var last = last
        // first, middle, last are pointers in SA
        var a: Int
        var b: Int
        var t: Int// pointers in SA
        var l: Int
        var r: Int
        l = middle - first
        r = last - middle
        while ((0 < l) && (0 < r)) {
            if (l == r) {
                ssBlockSwap(first, middle, l)
                break
            }
            if (l < r) {
                a = last - 1
                b = middle - 1
                t = SA[a]
                do {
                    SA[a--] = SA[b]
                    SA[b--] = SA[a]
                    if (b < first) {
                        SA[a] = t
                        last = a
                        if ((l + 1).also { r -= it } <= l) {
                            break
                        }
                        a -= 1
                        b = middle - 1
                        t = SA[a]
                    }
                } while (true)
            } else {
                a = first
                b = middle
                t = SA[a]
                do {
                    SA[a++] = SA[b]
                    SA[b++] = SA[a]
                    if (last <= b) {
                        SA[a] = t
                        first = a + 1
                        if ((r + 1).also { l -= it } <= r) {
                            break
                        }
                        a += 1
                        b = middle
                        t = SA[a]
                    }
                } while (true)
            }
        }
    }

    /**
     *
     */
    fun ssBlockSwap(a: Int, b: Int, n: Int) {
        var a = a
        var b = b
        var n = n
        // a, b -- pointer to SA
        var t: Int
        while (0 < n) {
            t = SA[a]
            SA[a] = SA[b]
            SA[b] = t
            --n
            ++a
            ++b
        }
    }

    /**
     * D&C based merge.
     */
    fun ssSwapMerge(PA: Int, first: Int, middle: Int, last: Int, buf: Int,
                    bufsize: Int, depth: Int) {
        var first = first
        var middle = middle
        var last = last
        // Pa, first, middle, last and buf - pointers in SA array

        var STACK_SIZE = SS_SMERGE_STACKSIZE
        var stack = Array<StackElement>(STACK_SIZE) { SENIL }
        var l: Int
        var r: Int
        var lm: Int
        var rm: Int// pointers in SA
        var m: Int
        var len: Int
        var half: Int
        var ssize: Int
        var check: Int
        var next: Int

        check = 0
        ssize = 0
        while (true) {

            if ((last - middle) <= bufsize) {
                if ((first < middle) && (middle < last)) {
                    ssMergeBackward(PA, first, middle, last, buf, depth)
                }
                if ((((check and 1) != 0) || ((((check and 2) != 0) && ((ssCompare(PA + getIDX(SA[first - 1]), (PA + SA[first]), depth) == 0)))))) {
                    SA[first] = SA[first].inv()
                }
                if ((((check and 4) != 0) && ((ssCompare(PA + getIDX(SA[last - 1]), PA + SA[last], depth) == 0)))) {
                    SA[last] = SA[last].inv()
                }

                if (ssize > 0) {
                    var se = stack[--ssize]
                    first = se.a
                    middle = se.b
                    last = se.c
                    check = se.d
                } else {
                    return
                }
                continue
            }

            if ((middle - first) <= bufsize) {
                if (first < middle) {
                    ssMergeForward(PA, first, middle, last, buf, depth)
                }
                if ((((check and 1) != 0) || ((((check and 2) != 0) && ((ssCompare(PA + getIDX(SA[first - 1]), (PA + SA[first]), depth) == 0)))))) {
                    SA[first] = SA[first].inv()
                }
                if ((((check and 4) != 0) && ((ssCompare(PA + getIDX(SA[last - 1]), PA + SA[last], depth) == 0)))) {
                    SA[last] = SA[last].inv()
                }

                if (ssize > 0) {
                    var se = stack[--ssize]
                    first = se.a
                    middle = se.b
                    last = se.c
                    check = se.d
                } else {
                    return
                }

                continue
            }

            m = 0
            len = min(middle - first, last - middle)
            half = len shr 1
            while (0 < len) {
                if ((ssCompare(PA + getIDX(SA[middle + m + half]), (PA + getIDX(SA[middle - m - half - 1])), depth) < 0)) {
                    m += half + 1
                    half -= (len and 1) xor 1
                }
                len = half
                half = half shr 1
            }

            if (0 < m) {
                lm = middle - m
                rm = middle + m
                ssBlockSwap(lm, middle, m)
                r = middle
                l = r
                next = 0
                if (rm < last) {
                    if (SA[rm] < 0) {
                        SA[rm] = SA[rm].inv()
                        if (first < lm) {
                            while (SA[--l] < 0) {
                            }
                            next = next or 4
                        }
                        next = next or 1
                    } else if (first < lm) {
                        while (SA[r] < 0) {
                            ++r
                        }
                        next = next or 2
                    }
                }

                if ((l - first) <= (last - r)) {
                    stack[ssize++] = StackElement(r, rm, last, ((next and 3) or (check and 4)))

                    middle = lm
                    last = l
                    check = (check and 3) or (next and 4)
                } else {
                    if (((next and 2) != 0) && (r == middle)) {
                        next = next xor 6
                    }
                    stack[ssize++] = StackElement(first, lm, l, ((check and 3) or (next and 4)))

                    first = r
                    middle = rm
                    check = (next and 3) or (check and 4)
                }
            } else {
                if (ssCompare(PA + getIDX(SA[middle - 1]), PA + SA[middle], depth) == 0) {
                    SA[middle] = SA[middle].inv()
                }

                if ((((check and 1) != 0) || ((((check and 2) != 0) && ((ssCompare(PA + getIDX(SA[first - 1]), (PA + SA[first]), depth) == 0)))))) {
                    SA[first] = SA[first].inv()
                }
                if ((((check and 4) != 0) && ((ssCompare(PA + getIDX(SA[last - 1]), PA + SA[last], depth) == 0)))) {
                    SA[last] = SA[last].inv()
                }

                if (ssize > 0) {
                    var se = stack[--ssize]
                    first = se.a
                    middle = se.b
                    last = se.c
                    check = se.d
                } else {
                    return
                }

            }

        }

    }

    /**
     * Merge-forward with public buffer.
     */
    fun ssMergeForward(PA: Int, first: Int, middle: Int, last: Int, buf: Int,
                       depth: Int) {
        // PA, first, middle, last, buf are pointers to SA
        var a: Int
        var b: Int
        var c: Int
        var bufend: Int// pointers to SA
        var t: Int
        var r: Int

        bufend = buf + (middle - first) - 1
        ssBlockSwap(buf, first, middle - first)

        t = SA[first.also { a = it }]
        b = buf
        c = middle
        while (true) {
            r = ssCompare(PA + SA[b], PA + SA[c], depth)
            if (r < 0) {
                do {
                    SA[a++] = SA[b]
                    if (bufend <= b) {
                        SA[bufend] = t
                        return
                    }
                    SA[b++] = SA[a]
                } while (SA[b] < 0)
            } else if (r > 0) {
                do {
                    SA[a++] = SA[c]
                    SA[c++] = SA[a]
                    if (last <= c) {
                        while (b < bufend) {
                            SA[a++] = SA[b]
                            SA[b++] = SA[a]
                        }
                        SA[a] = SA[b]
                        SA[b] = t
                        return
                    }
                } while (SA[c] < 0)
            } else {
                SA[c] = SA[c].inv()
                do {
                    SA[a++] = SA[b]
                    if (bufend <= b) {
                        SA[bufend] = t
                        return
                    }
                    SA[b++] = SA[a]
                } while (SA[b] < 0)

                do {
                    SA[a++] = SA[c]
                    SA[c++] = SA[a]
                    if (last <= c) {
                        while (b < bufend) {
                            SA[a++] = SA[b]
                            SA[b++] = SA[a]
                        }
                        SA[a] = SA[b]
                        SA[b] = t
                        return
                    }
                } while (SA[c] < 0)
            }
        }

    }

    /**
     * Merge-backward with public buffer.
     */
    fun ssMergeBackward(PA: Int, first: Int, middle: Int, last: Int, buf: Int,
                        depth: Int) {
        // PA, first, middle, last, buf are pointers in SA
        var p1: Int
        var p2: Int// pointers in SA
        var a: Int
        var b: Int
        var c: Int
        var bufend: Int// pointers in SA
        var t: Int
        var r: Int
        var x: Int

        bufend = buf + (last - middle) - 1
        ssBlockSwap(buf, middle, last - middle)

        x = 0
        if (SA[bufend] < 0) {
            p1 = PA + SA[bufend].inv()
            x = x or 1
        } else {
            p1 = PA + SA[bufend]
        }
        if (SA[middle - 1] < 0) {
            p2 = PA + SA[middle - 1].inv()
            x = x or 2
        } else {
            p2 = PA + SA[middle - 1]
        }
        t = SA[last - 1].also { a = it }
        b = bufend
        c = middle - 1
        while (true) {
            r = ssCompare(p1, p2, depth)
            if (0 < r) {
                if ((x and 1) != 0) {
                    do {
                        SA[a--] = SA[b]
                        SA[b--] = SA[a]
                    } while (SA[b] < 0)
                    x = x xor 1
                }
                SA[a--] = SA[b]
                if (b <= buf) {
                    SA[buf] = t
                    break
                }
                SA[b--] = SA[a]
                if (SA[b] < 0) {
                    p1 = PA + SA[b].inv()
                    x = x or 1
                } else {
                    p1 = PA + SA[b]
                }
            } else if (r < 0) {
                if ((x and 2) != 0) {
                    do {
                        SA[a--] = SA[c]
                        SA[c--] = SA[a]
                    } while (SA[c] < 0)
                    x = x xor 2
                }
                SA[a--] = SA[c]
                SA[c--] = SA[a]
                if (c < first) {
                    while (buf < b) {
                        SA[a--] = SA[b]
                        SA[b--] = SA[a]
                    }
                    SA[a] = SA[b]
                    SA[b] = t
                    break
                }
                if (SA[c] < 0) {
                    p2 = PA + SA[c].inv()
                    x = x or 2
                } else {
                    p2 = PA + SA[c]
                }
            } else {
                if ((x and 1) != 0) {
                    do {
                        SA[a--] = SA[b]
                        SA[b--] = SA[a]
                    } while (SA[b] < 0)
                    x = x xor 1
                }
                SA[a--] = SA[b].inv()
                if (b <= buf) {
                    SA[buf] = t
                    break
                }
                SA[b--] = SA[a]
                if ((x and 2) != 0) {
                    do {
                        SA[a--] = SA[c]
                        SA[c--] = SA[a]
                    } while (SA[c] < 0)
                    x = x xor 2
                }
                SA[a--] = SA[c]
                SA[c--] = SA[a]
                if (c < first) {
                    while (buf < b) {
                        SA[a--] = SA[b]
                        SA[b--] = SA[a]
                    }
                    SA[a] = SA[b]
                    SA[b] = t
                    break
                }
                if (SA[b] < 0) {
                    p1 = PA + SA[b].inv()
                    x = x or 1
                } else {
                    p1 = PA + SA[b]
                }
                if (SA[c] < 0) {
                    p2 = PA + SA[c].inv()
                    x = x or 2
                } else {
                    p2 = PA + SA[c]
                }
            }
        }
    }

    /**
     * Insertionsort for small size groups
     */
    fun ssInsertionSort(PA: Int, first: Int, last: Int, depth: Int) {
        // PA, first, last are pointers in SA
        var i: Int
        var j: Int// pointers in SA
        var t: Int
        var r: Int

        i = last - 2
        while (first <= i) {
            t = SA[i]
            j = i + 1
            while (0 < (ssCompare(PA + t, PA + SA[j], depth)).also { r = it }) {
                do {
                    SA[j - 1] = SA[j]
                } while ((++j < last) && (SA[j] < 0))
                if (last <= j) {
                    break
                }
            }
            if (r == 0) {
                SA[j] = SA[j].inv()
            }
            SA[j - 1] = t
            --i
        }

    }

    /* Multikey introsort for medium size groups. */
    fun ssMintroSort(PA: Int, first: Int, last: Int, depth: Int) {
        var first = first
        var last = last
        var depth = depth
        var STACK_SIZE = SS_MISORT_STACKSIZE
        var Td: Int// T ptr
        var a: Int
        var b: Int
        var c: Int
        var d: Int
        var e: Int
        var f: Int// SA ptr
        var s: Int
        var t: Int
        var ssize: Int
        var limit: Int
        var v: Int
        var x = 0
        ssize = 0
        limit = ssIlg(last - first)
        var stack = Array(STACK_SIZE) { SENIL }

        while (true) {

            if ((last - first) <= SS_INSERTIONSORT_THRESHOLD) {
                if (1 < (last - first)) {
                    ssInsertionSort(PA, first, last, depth)
                }
                if (ssize > 0) {
                    var se: StackElement = stack[--ssize]
                    first = se.a
                    last = se.b
                    depth = se.c
                    limit = se.d
                } else {
                    return
                }

                continue
            }

            Td = depth
            if (limit-- == 0) {
                ssHeapSort(Td, PA, first, last - first)

            }
            if (limit < 0) {
                a = first + 1
                v = T[start + Td + SA[PA + SA[first]]]
                while (a < last) {
                    if ((T[start + Td + SA[PA + SA[a]]].also { x = it }) != v) {
                        if (1 < (a - first)) {
                            break
                        }
                        v = x
                        first = a
                    }
                    ++a
                }

                if (T[start + Td + SA[PA + SA[first]] - 1] < v) {
                    first = ssPartition(PA, first, a, depth)
                }
                if ((a - first) <= (last - a)) {
                    if (1 < (a - first)) {
                        stack[ssize++] = StackElement(a, last, depth, -1)
                        last = a
                        depth += 1
                        limit = ssIlg(a - first)
                    } else {
                        first = a
                        limit = -1
                    }
                } else {
                    if (1 < (last - a)) {
                        stack[ssize++] = StackElement(first, a, depth + 1, ssIlg((a - first)))
                        first = a
                        limit = -1
                    } else {
                        last = a
                        depth += 1
                        limit = ssIlg(a - first)
                    }
                }
                continue
            }

            // choose pivot
            a = ssPivot(Td, PA, first, last)
            v = T[start + Td + SA[PA + SA[a]]]
            swapInSA(first, a)

            // partition
            b = first
            while ((++b < last) && ((T[start + Td + SA[PA + SA[b]]].also { x = it }) == v)) {
            }
            if (((b.also { a = it }) < last) && (x < v)) {
                while ((++b < last) && ((T[start + Td + SA[PA + SA[b]]].also { x = it }) <= v)) {
                    if (x == v) {
                        swapInSA(b, a)
                        ++a
                    }
                }
            }

            c = last
            while ((b < --c) && ((T[start + Td + SA[PA + SA[c]]].also { x = it }) == v)) {
            }
            if ((b < (c.also { d = it })) && (x > v)) {
                while ((b < --c) && ((T[start + Td + SA[PA + SA[c]]].also { x = it }) >= v)) {
                    if (x == v) {
                        swapInSA(c, d)
                        --d
                    }
                }
            }

            while (b < c) {
                swapInSA(b, c)
                while ((++b < c) && ((T[start + Td + SA[PA + SA[b]]].also { x = it }) <= v)) {
                    if (x == v) {
                        swapInSA(b, a)
                        ++a
                    }
                }
                while ((b < --c) && ((T[start + Td + SA[PA + SA[c]]].also { x = it }) >= v)) {
                    if (x == v) {
                        swapInSA(c, d)
                        --d
                    }
                }
            }

            if (a <= d) {
                c = b - 1

                if ((a - first.also { s = it }) > (b - a.also { t = it })) {
                    s = t
                }
                e = first
                f = b - s
                while (0 < s) {
                    swapInSA(e, f)
                    --s
                    ++e
                    ++f
                }
                if ((d - c.also { s = it }) > (last - d - 1.also { t = it })) {
                    s = t
                }
                e = b
                f = last - s
                while (0 < s) {
                    swapInSA(e, f)
                    --s
                    ++e
                    ++f
                }

                a = first + (b - a)
                c = last - (d - c)
                b = if ((v <= T[start + Td + SA[PA + SA[a]] - 1]))
                    a
                else
                    ssPartition(PA, a, c,
                            depth)

                if ((a - first) <= (last - c)) {
                    if ((last - c) <= (c - b)) {
                        stack[ssize++] = StackElement(b, c, depth + 1, ssIlg(c - b))
                        stack[ssize++] = StackElement(c, last, depth, limit)
                        last = a
                    } else if ((a - first) <= (c - b)) {
                        stack[ssize++] = StackElement(c, last, depth, limit)
                        stack[ssize++] = StackElement(b, c, depth + 1, ssIlg(c - b))
                        last = a
                    } else {
                        stack[ssize++] = StackElement(c, last, depth, limit)
                        stack[ssize++] = StackElement(first, a, depth, limit)
                        first = b
                        last = c
                        depth += 1
                        limit = ssIlg(c - b)
                    }
                } else {
                    if ((a - first) <= (c - b)) {
                        stack[ssize++] = StackElement(b, c, depth + 1, ssIlg(c - b))
                        stack[ssize++] = StackElement(first, a, depth, limit)
                        first = c
                    } else if ((last - c) <= (c - b)) {
                        stack[ssize++] = StackElement(first, a, depth, limit)
                        stack[ssize++] = StackElement(b, c, depth + 1, ssIlg(c - b))
                        first = c
                    } else {
                        stack[ssize++] = StackElement(first, a, depth, limit)
                        stack[ssize++] = StackElement(c, last, depth, limit)
                        first = b
                        last = c
                        depth += 1
                        limit = ssIlg(c - b)
                    }
                }

            } else {
                limit += 1
                if (T[start + Td + SA[PA + SA[first]] - 1] < v) {
                    first = ssPartition(PA, first, last, depth)
                    limit = ssIlg(last - first)
                }
                depth += 1
            }

        }

    }

    /**
     * Returns the pivot element.
     */
    fun ssPivot(Td: Int, PA: Int, first: Int, last: Int): Int {
        var first = first
        var last = last
        var middle: Int// SA pointer
        var t = last - first
        middle = first + t / 2

        if (t <= 512) {
            if (t <= 32) {
                return ssMedian3(Td, PA, first, middle, last - 1)
            } else {
                t = t shr 2
                return ssMedian5(Td, PA, first, first + t, middle, last - 1 - t, last - 1)
            }
        }
        t = t shr 3
        first = ssMedian3(Td, PA, first, first + t, first + (t shl 1))
        middle = ssMedian3(Td, PA, middle - t, middle, middle + t)
        last = ssMedian3(Td, PA, last - 1 - (t shl 1), last - 1 - t, last - 1)
        return ssMedian3(Td, PA, first, middle, last)
    }

    /**
     * Returns the median of five elements
     */
    fun ssMedian5(Td: Int, PA: Int, v1: Int, v2: Int, v3: Int, v4: Int, v5: Int): Int {
        var v1 = v1
        var v2 = v2
        var v3 = v3
        var v4 = v4
        var v5 = v5
        var t: Int
        if (T[start + Td + SA[PA + SA[v2]]] > T[start + Td + SA[PA + SA[v3]]]) {
            t = v2
            v2 = v3
            v3 = t

        }
        if (T[start + Td + SA[PA + SA[v4]]] > T[start + Td + SA[PA + SA[v5]]]) {
            t = v4
            v4 = v5
            v5 = t
        }
        if (T[start + Td + SA[PA + SA[v2]]] > T[start + Td + SA[PA + SA[v4]]]) {
            t = v2
            v2 = v4
            v4 = t
            t = v3
            v3 = v5
            v5 = t
        }
        if (T[start + Td + SA[PA + SA[v1]]] > T[start + Td + SA[PA + SA[v3]]]) {
            t = v1
            v1 = v3
            v3 = t
        }
        if (T[start + Td + SA[PA + SA[v1]]] > T[start + Td + SA[PA + SA[v4]]]) {
            t = v1
            v1 = v4
            v4 = t
            t = v3
            v3 = v5
            v5 = t
        }
        return if (T[start + Td + SA[PA + SA[v3]]] > T[start + Td + SA[PA + SA[v4]]]) {
            v4
        } else v3
    }

    /**
     * Returns the median of three elements.
     */
    fun ssMedian3(Td: Int, PA: Int, v1: Int, v2: Int, v3: Int): Int {
        var v1 = v1
        var v2 = v2
        if (T[start + Td + SA[PA + SA[v1]]] > T[start + Td + SA[PA + SA[v2]]]) {
            var t = v1
            v1 = v2
            v2 = t
        }
        if (T[start + Td + SA[PA + SA[v2]]] > T[start + Td + SA[PA + SA[v3]]]) {
            return if (T[start + Td + SA[PA + SA[v1]]] > T[start + Td + SA[PA + SA[v3]]]) {
                v1
            } else {
                v3
            }
        }
        return v2
    }

    /**
     * Binary partition for substrings.
     */
    fun ssPartition(PA: Int, first: Int, last: Int, depth: Int): Int {
        var a: Int
        var b: Int// SA pointer
        var t: Int
        a = first - 1
        b = last
        while (true) {
            while ((++a < b) && ((SA[PA + SA[a]] + depth) >= (SA[PA + SA[a] + 1] + 1))) {
                SA[a] = SA[a].inv()
            }
            while ((a < --b) && ((SA[PA + SA[b]] + depth) < (SA[PA + SA[b] + 1] + 1))) {
            }
            if (b <= a) {
                break
            }
            t = SA[b].inv()
            SA[b] = SA[a]
            SA[a] = t
        }
        if (first < a) {
            SA[first] = SA[first].inv()
        }
        return a
    }

    /**
     * Simple top-down heapsort.
     */
    fun ssHeapSort(Td: Int, PA: Int, sa: Int, size: Int) {
        var i: Int
        var m: Int
        var t: Int

        m = size
        if ((size % 2) == 0) {
            m--
            if ((T[start + Td + SA[PA + SA[sa + (m / 2)]]] < T[(start + Td
                            + SA[PA + SA[sa + m]])])) {
                swapInSA(sa + m, sa + (m / 2))
            }
        }

        i = m / 2 - 1
        while (0 <= i) {
            ssFixDown(Td, PA, sa, i, m)
            --i
        }
        if ((size % 2) == 0) {
            swapInSA(sa, sa + m)
            ssFixDown(Td, PA, sa, 0, m)
        }
        i = m - 1
        while (0 < i) {
            t = SA[sa]
            SA[sa] = SA[sa + i]
            ssFixDown(Td, PA, sa, 0, i)
            SA[sa + i] = t
            --i
        }

    }

    /**
     *
     */
    fun ssFixDown(Td: Int, PA: Int, sa: Int, i: Int, size: Int) {
        var i = i
        var j: Int
        var k: Int
        var v: Int
        var c: Int
        var d: Int
        var e: Int

        v = SA[sa + i]
        c = T[start + Td + SA[PA + v]]
        while ((2 * i + 1).also { j = it } < size) {
            d = T[start + Td + SA[PA + SA[sa + (j++.also { k = it })]]]
            if (d < (T[start + Td + SA[PA + SA[sa + j]]].also { e = it })) {
                k = j
                d = e
            }
            if (d <= c) {
                break
            }
            SA[(sa + i)] = SA[sa + k]
            i = k
        }
        SA[i + sa] = v

    }

    /**
     *
     */
    fun swapInSA(a: Int, b: Int) {
        var tmp = SA[a]
        SA[a] = SA[b]
        SA[b] = tmp
    }

    /**
     * Tandem repeat sort
     */
    fun trSort(ISA: Int, n: Int, depth: Int) {
        var budget = TRBudget(trIlg(n) * 2 / 3, n)
        var ISAd: Int
        var first: Int
        var last: Int// SA pointers
        var t: Int
        var skip: Int
        var unsorted: Int
        ISAd = ISA + depth
        while (-n < SA[0]) {
            first = 0
            skip = 0
            unsorted = 0
            do {
                if ((SA[first].also { t = it }) < 0) {
                    first -= t
                    skip += t
                } else {
                    if (skip != 0) {
                        SA[first + skip] = skip
                        skip = 0
                    }
                    last = SA[ISA + t] + 1
                    if (1 < (last - first)) {
                        budget.count = 0
                        trIntroSort(ISA, ISAd, first, last, budget)
                        if (budget.count != 0) {
                            unsorted += budget.count
                        } else {
                            skip = first - last
                        }
                    } else if ((last - first) == 1) {
                        skip = -1
                    }
                    first = last
                }
            } while (first < n)
            if (skip != 0) {
                SA[first + skip] = skip
            }
            if (unsorted == 0) {
                break
            }
            ISAd += ISAd - ISA
        }
    }

    /**
     *
     */
    fun trPartition(ISAd: Int, first: Int, middle: Int,
                    last: Int, pa: Int, pb: Int, v: Int): TRPartitionResult {
        var first = first
        var last = last
        var a: Int
        var b: Int
        var c: Int
        var d: Int
        var e: Int
        var f: Int// ptr
        var t: Int
        var s: Int
        var x = 0

        b = middle - 1
        while ((++b < last) && ((SA[ISAd + SA[b]].also { x = it }) == v)) {
        }
        if (((b.also { a = it }) < last) && (x < v)) {
            while ((++b < last) && ((SA[ISAd + SA[b]].also { x = it }) <= v)) {
                if (x == v) {
                    swapInSA(a, b)
                    ++a
                }
            }
        }
        c = last
        while ((b < --c) && ((SA[ISAd + SA[c]].also { x = it }) == v)) {
        }
        if ((b < (c.also { d = it })) && (x > v)) {
            while ((b < --c) && ((SA[ISAd + SA[c]].also { x = it }) >= v)) {
                if (x == v) {
                    swapInSA(c, d)
                    --d
                }
            }
        }
        while (b < c) {
            swapInSA(c, b)
            while ((++b < c) && ((SA[ISAd + SA[b]].also { x = it }) <= v)) {
                if (x == v) {
                    swapInSA(a, b)
                    ++a
                }
            }
            while ((b < --c) && ((SA[ISAd + SA[c]].also { x = it }) >= v)) {
                if (x == v) {
                    swapInSA(c, d)
                    --d
                }
            }
        }

        if (a <= d) {
            c = b - 1
            if ((a - first.also { s = it }) > (b - a.also { t = it })) {
                s = t
            }
            e = first
            f = b - s
            while (0 < s) {
                swapInSA(e, f)
                --s
                ++e
                ++f
            }
            if ((d - c.also { s = it }) > (last - d - 1.also { t = it })) {
                s = t
            }
            e = b
            f = last - s
            while (0 < s) {
                swapInSA(e, f)
                --s
                ++e
                ++f
            }
            first += (b - a)
            last -= (d - c)
        }
        return TRPartitionResult(first, last)
    }

    fun trIntroSort(ISA: Int, ISAd: Int, first: Int, last: Int, budget: TRBudget) {
        var ISAd = ISAd
        var first = first
        var last = last
        var STACK_SIZE = TR_STACKSIZE
        var stack = Array<StackElement>(STACK_SIZE) { SENIL }
        var a = 0
        var b = 0
        var c: Int// pointers
        var v: Int
        var x = 0
        var incr = ISAd - ISA
        var limit: Int
        var next: Int
        var ssize: Int
        var trlink = -1
        ssize = 0
        limit = trIlg(last - first)
        while (true) {
            if (limit < 0) {
                if (limit == -1) {
                    /* tandem repeat partition */
                    var res = trPartition(ISAd - incr, first, first, last,
                            a, b, last - 1)
                    a = res.a
                    b = res.b
                    /* update ranks */
                    if (a < last) {
                        c = first
                        v = a - 1
                        while (c < a) {
                            SA[ISA + SA[c]] = v
                            ++c
                        }
                    }
                    if (b < last) {
                        c = a
                        v = b - 1
                        while (c < b) {
                            SA[ISA + SA[c]] = v
                            ++c
                        }
                    }

                    /* push */
                    if (1 < (b - a)) {
                        stack[ssize++] = StackElement(0, a, b, 0, 0)
                        stack[ssize++] = StackElement(ISAd - incr, first, last, -2,
                                trlink)
                        trlink = ssize - 2
                    }
                    if ((a - first) <= (last - b)) {
                        if (1 < (a - first)) {
                            stack[ssize++] = StackElement(ISAd, b, last, trIlg((last - b)), trlink)
                            last = a
                            limit = trIlg(a - first)
                        } else if (1 < (last - b)) {
                            first = b
                            limit = trIlg(last - b)
                        } else {
                            if (ssize > 0) {
                                var se = stack[--ssize]
                                ISAd = se.a
                                first = se.b
                                last = se.c
                                limit = se.d
                                trlink = se.e
                            } else {
                                return
                            }

                        }
                    } else {
                        if (1 < (last - b)) {
                            stack[ssize++] = StackElement(ISAd, first, a, trIlg((a - first)), trlink)
                            first = b
                            limit = trIlg(last - b)
                        } else if (1 < (a - first)) {
                            last = a
                            limit = trIlg(a - first)
                        } else {
                            if (ssize > 0) {
                                var se = stack[--ssize]
                                ISAd = se.a
                                first = se.b
                                last = se.c
                                limit = se.d
                                trlink = se.e
                            } else {
                                return
                            }
                        }
                    }
                } else if (limit == -2) {
                    /* tandem repeat copy */
                    var se = stack[--ssize]
                    a = se.b
                    b = se.c
                    if (stack[ssize].d == 0) {
                        trCopy(ISA, first, a, b, last, ISAd - ISA)
                    } else {
                        if (0 <= trlink) {
                            stack[trlink].d = -1
                        }
                        trPartialCopy(ISA, first, a, b, last, ISAd - ISA)
                    }
                    if (ssize > 0) {
                        se = stack[--ssize]
                        ISAd = se.a
                        first = se.b
                        last = se.c
                        limit = se.d
                        trlink = se.e
                    } else {
                        return
                    }
                } else {
                    /* sorted partition */
                    if (0 <= SA[first]) {
                        a = first
                        do {
                            SA[ISA + SA[a]] = a
                        } while ((++a < last) && (0 <= SA[a]))
                        first = a
                    }
                    if (first < last) {
                        a = first
                        do {
                            SA[a] = SA[a].inv()
                        } while (SA[++a] < 0)
                        next = if ((SA[ISA + SA[a]] != SA[ISAd + SA[a]]))
                            trIlg((a - first + 1))
                        else
                            -1
                        if (++a < last) {
                            b = first
                            v = a - 1
                            while (b < a) {
                                SA[ISA + SA[b]] = v
                                ++b
                            }
                        }

                        /* push */
                        if (budget.check(a - first) != 0) {
                            if ((a - first) <= (last - a)) {
                                stack[ssize++] = StackElement(ISAd, a, last, -3,
                                        trlink)
                                ISAd += incr
                                last = a
                                limit = next
                            } else {
                                if (1 < (last - a)) {
                                    stack[ssize++] = StackElement(ISAd + incr, first,
                                            a, next, trlink)
                                    first = a
                                    limit = -3
                                } else {
                                    ISAd += incr
                                    last = a
                                    limit = next
                                }
                            }
                        } else {
                            if (0 <= trlink) {
                                stack[trlink].d = -1
                            }
                            if (1 < (last - a)) {
                                first = a
                                limit = -3
                            } else {
                                if (ssize > 0) {
                                    var se = stack[--ssize]
                                    ISAd = se.a
                                    first = se.b
                                    last = se.c
                                    limit = se.d
                                    trlink = se.e
                                } else {
                                    return
                                }
                            }
                        }
                    } else {
                        if (ssize > 0) {
                            var se = stack[--ssize]
                            ISAd = se.a
                            first = se.b
                            last = se.c
                            limit = se.d
                            trlink = se.e
                        } else {
                            return
                        }
                    }
                }
                continue
            }

            if ((last - first) <= TR_INSERTIONSORT_THRESHOLD) {
                trInsertionSort(ISAd, first, last)
                limit = -3
                continue
            }

            if (limit-- == 0) {
                trHeapSort(ISAd, first, last - first)
                a = last - 1
                while (first < a) {
                    x = SA[ISAd + SA[a]]
                    b = a - 1
                    while (((first <= b) && (SA[ISAd + SA[b]] == x))) {
                        SA[b] = SA[b].inv()
                        --b
                    }
                    a = b
                }
                limit = -3
                continue
            }
            // choose pivot
            a = trPivot(ISAd, first, last)
            swapInSA(first, a)
            v = SA[ISAd + SA[first]]

            // partition
            var res = trPartition(ISAd, first, first + 1, last, a, b, v)
            a = res.a
            b = res.b

            if ((last - first) != (b - a)) {
                next = if ((SA[ISA + SA[a]] != v)) trIlg(b - a) else -1

                /* update ranks */
                c = first
                v = a - 1
                while (c < a) {
                    SA[ISA + SA[c]] = v
                    ++c
                }
                if (b < last) {
                    c = a
                    v = b - 1
                    while (c < b) {
                        SA[ISA + SA[c]] = v
                        ++c
                    }
                }

                /* push */
                if ((1 < (b - a)) && ((budget.check(b - a) != 0))) {
                    if ((a - first) <= (last - b)) {
                        if ((last - b) <= (b - a)) {
                            if (1 < (a - first)) {
                                stack[ssize++] = StackElement(ISAd + incr, a, b,
                                        next, trlink)
                                stack[ssize++] = StackElement(ISAd, b, last, limit,
                                        trlink)
                                last = a
                            } else if (1 < (last - b)) {
                                stack[ssize++] = StackElement(ISAd + incr, a, b,
                                        next, trlink)
                                first = b
                            } else {
                                ISAd += incr
                                first = a
                                last = b
                                limit = next
                            }
                        } else if ((a - first) <= (b - a)) {
                            if (1 < (a - first)) {
                                stack[ssize++] = StackElement(ISAd, b, last, limit,
                                        trlink)
                                stack[ssize++] = StackElement(ISAd + incr, a, b,
                                        next, trlink)
                                last = a
                            } else {
                                stack[ssize++] = StackElement(ISAd, b, last, limit,
                                        trlink)
                                ISAd += incr
                                first = a
                                last = b
                                limit = next
                            }
                        } else {
                            stack[ssize++] = StackElement(ISAd, b, last, limit,
                                    trlink)
                            stack[ssize++] = StackElement(ISAd, first, a, limit,
                                    trlink)
                            ISAd += incr
                            first = a
                            last = b
                            limit = next
                        }
                    } else {
                        if ((a - first) <= (b - a)) {
                            if (1 < (last - b)) {
                                stack[ssize++] = StackElement(ISAd + incr, a, b,
                                        next, trlink)
                                stack[ssize++] = StackElement(ISAd, first, a, limit,
                                        trlink)
                                first = b
                            } else if (1 < (a - first)) {
                                stack[ssize++] = StackElement(ISAd + incr, a, b,
                                        next, trlink)
                                last = a
                            } else {
                                ISAd += incr
                                first = a
                                last = b
                                limit = next
                            }
                        } else if ((last - b) <= (b - a)) {
                            if (1 < (last - b)) {
                                stack[ssize++] = StackElement(ISAd, first, a, limit,
                                        trlink)
                                stack[ssize++] = StackElement(ISAd + incr, a, b,
                                        next, trlink)
                                first = b
                            } else {
                                stack[ssize++] = StackElement(ISAd, first, a, limit,
                                        trlink)
                                ISAd += incr
                                first = a
                                last = b
                                limit = next
                            }
                        } else {
                            stack[ssize++] = StackElement(ISAd, first, a, limit,
                                    trlink)
                            stack[ssize++] = StackElement(ISAd, b, last, limit,
                                    trlink)
                            ISAd += incr
                            first = a
                            last = b
                            limit = next
                        }
                    }
                } else {
                    if ((1 < (b - a)) && (0 <= trlink)) {
                        stack[trlink].d = -1
                    }
                    if ((a - first) <= (last - b)) {
                        if (1 < (a - first)) {
                            stack[ssize++] = StackElement(ISAd, b, last, limit,
                                    trlink)
                            last = a
                        } else if (1 < (last - b)) {
                            first = b
                        } else {
                            if (ssize > 0) {
                                var se = stack[--ssize]
                                ISAd = se.a
                                first = se.b
                                last = se.c
                                limit = se.d
                                trlink = se.e
                            } else {
                                return
                            }
                        }
                    } else {
                        if (1 < (last - b)) {
                            stack[ssize++] = StackElement(ISAd, first, a, limit,
                                    trlink)
                            first = b
                        } else if (1 < (a - first)) {
                            last = a
                        } else {
                            if (ssize > 0) {
                                var se = stack[--ssize]
                                ISAd = se.a
                                first = se.b
                                last = se.c
                                limit = se.d
                                trlink = se.e
                            } else {
                                return
                            }
                        }
                    }
                }
            } else {
                if (budget.check(last - first) != 0) {
                    limit = trIlg(last - first)
                    ISAd += incr
                } else {
                    if (0 <= trlink) {
                        stack[trlink].d = -1
                    }
                    if (ssize > 0) {
                        var se = stack[--ssize]
                        ISAd = se.a
                        first = se.b
                        last = se.c
                        limit = se.d
                        trlink = se.e
                    } else {
                        return
                    }
                }
            }

        }

    }

    /**
     * Returns the pivot element.
     */
    fun trPivot(ISAd: Int, first: Int, last: Int): Int {
        var first = first
        var last = last
        var middle: Int
        var t: Int

        t = last - first
        middle = first + t / 2

        if (t <= 512) {
            if (t <= 32) {
                return trMedian3(ISAd, first, middle, last - 1)
            } else {
                t = t shr 2
                return trMedian5(ISAd, first, first + t, middle, last - 1 - t, last - 1)
            }
        }
        t = t shr 3
        first = trMedian3(ISAd, first, first + t, first + (t shl 1))
        middle = trMedian3(ISAd, middle - t, middle, middle + t)
        last = trMedian3(ISAd, last - 1 - (t shl 1), last - 1 - t, last - 1)
        return trMedian3(ISAd, first, middle, last)
    }

    /**
     * Returns the median of five elements.
     */
    fun trMedian5(ISAd: Int, v1: Int, v2: Int, v3: Int, v4: Int, v5: Int): Int {
        var v1 = v1
        var v2 = v2
        var v3 = v3
        var v4 = v4
        var v5 = v5
        var t: Int
        if (SA[ISAd + SA[v2]] > SA[ISAd + SA[v3]]) {
            t = v2
            v2 = v3
            v3 = t
        }
        if (SA[ISAd + SA[v4]] > SA[ISAd + SA[v5]]) {
            t = v4
            v4 = v5
            v5 = t
        }
        if (SA[ISAd + SA[v2]] > SA[ISAd + SA[v4]]) {
            t = v2
            v2 = v4
            v4 = t
            t = v3
            v3 = v5
            v5 = t
        }
        if (SA[ISAd + SA[v1]] > SA[ISAd + SA[v3]]) {
            t = v1
            v1 = v3
            v3 = t
        }
        if (SA[ISAd + SA[v1]] > SA[ISAd + SA[v4]]) {
            t = v1
            v1 = v4
            v4 = t
            t = v3
            v3 = v5
            v5 = t
        }
        return if (SA[ISAd + SA[v3]] > SA[ISAd + SA[v4]]) {
            v4
        } else v3
    }

    /**
     * Returns the median of three elements.
     */
    fun trMedian3(ISAd: Int, v1: Int, v2: Int, v3: Int): Int {
        var v1 = v1
        var v2 = v2
        if (SA[ISAd + SA[v1]] > SA[ISAd + SA[v2]]) {
            var t = v1
            v1 = v2
            v2 = t
        }
        if (SA[ISAd + SA[v2]] > SA[ISAd + SA[v3]]) {
            return if (SA[ISAd + SA[v1]] > SA[ISAd + SA[v3]]) {
                v1
            } else {
                v3
            }
        }
        return v2
    }

    /**
     *
     */
    fun trHeapSort(ISAd: Int, sa: Int, size: Int) {
        var i: Int
        var m: Int
        var t: Int

        m = size
        if ((size % 2) == 0) {
            m--
            if (SA[ISAd + SA[sa + m / 2]] < SA[ISAd + SA[sa + m]]) {
                swapInSA(sa + m, sa + m / 2)
            }
        }

        i = m / 2 - 1
        while (0 <= i) {
            trFixDown(ISAd, sa, i, m)
            --i
        }
        if ((size % 2) == 0) {
            swapInSA(sa, sa + m)
            trFixDown(ISAd, sa, 0, m)
        }
        i = m - 1
        while (0 < i) {
            t = SA[sa]
            SA[sa] = SA[sa + i]
            trFixDown(ISAd, sa, 0, i)
            SA[sa + i] = t
            --i
        }

    }

    /**
     *
     */
    fun trFixDown(ISAd: Int, sa: Int, i: Int, size: Int) {
        var i = i
        var j: Int
        var k: Int
        var v: Int
        var c: Int
        var d: Int
        var e: Int

        v = SA[sa + i]
        c = SA[ISAd + v]
        while ((2 * i + 1).also { j = it } < size) {
            d = SA[ISAd + SA[sa + (j++.also { k = it })]]
            if (d < (SA[ISAd + SA[sa + j]].also { e = it })) {
                k = j
                d = e
            }
            if (d <= c) {
                break
            }
            SA[sa + i] = SA[(sa + k)]
            i = k
        }
        SA[sa + i] = v

    }

    /**
     */
    fun trInsertionSort(ISAd: Int, first: Int, last: Int) {
        var a: Int
        var b: Int// SA ptr
        var t: Int
        var r: Int

        a = first + 1
        while (a < last) {
            t = SA[a]
            b = a - 1
            while (0 > (SA[ISAd + t] - SA[ISAd + SA[b]].also { r = it })) {
                do {
                    SA[b + 1] = SA[b]
                } while ((first <= --b) && (SA[b] < 0))
                if (b < first) {
                    break
                }
            }
            if (r == 0) {
                SA[b] = SA[b].inv()
            }
            SA[b + 1] = t
            ++a
        }

    }

    /**
     */
    fun trPartialCopy(ISA: Int, first: Int, a: Int, b: Int, last: Int, depth: Int) {
        var c: Int
        var d: Int
        var e: Int// ptr
        var s: Int
        var v: Int
        var rank: Int
        var lastrank: Int
        var newrank = -1

        v = b - 1
        lastrank = -1
        c = first
        d = a - 1
        while (c <= d) {
            if ((0 <= (SA[c] - depth.also { s = it })) && (SA[ISA + s] == v)) {
                SA[++d] = s
                rank = SA[ISA + s + depth]
                if (lastrank != rank) {
                    lastrank = rank
                    newrank = d
                }
                SA[ISA + s] = newrank
            }
            ++c
        }

        lastrank = -1
        e = d
        while (first <= e) {
            rank = SA[ISA + SA[e]]
            if (lastrank != rank) {
                lastrank = rank
                newrank = e
            }
            if (newrank != rank) {
                SA[ISA + SA[e]] = newrank
            }
            --e
        }

        lastrank = -1
        c = last - 1
        e = d + 1
        d = b
        while (e < d) {
            if ((0 <= (SA[c] - depth.also { s = it })) && (SA[ISA + s] == v)) {
                SA[--d] = s
                rank = SA[ISA + s + depth]
                if (lastrank != rank) {
                    lastrank = rank
                    newrank = d
                }
                SA[ISA + s] = newrank
            }
            --c
        }

    }

    /**
     * sort suffixes of middle partition by using sorted order of suffixes of left and
     * right partition.
     */
    fun trCopy(ISA: Int, first: Int, a: Int, b: Int, last: Int, depth: Int) {
        var c: Int
        var d: Int
        var e: Int// ptr
        var s: Int
        var v: Int

        v = b - 1
        c = first
        d = a - 1
        while (c <= d) {
            s = SA[c] - depth
            if ((0 <= s) && (SA[ISA + s] == v)) {
                SA[++d] = s
                SA[ISA + s] = d
            }
            ++c
        }
        c = last - 1
        e = d + 1
        d = b
        while (e < d) {
            s = SA[c] - depth
            if ((0 <= s) && (SA[ISA + s] == v)) {
                SA[--d] = s
                SA[ISA + s] = d
            }
            --c
        }
    }

    companion object {

        /* constants */

        var DEFAULT_ALPHABET_SIZE = 256
        var SS_INSERTIONSORT_THRESHOLD = 8
        var SS_BLOCKSIZE = 1024
        var SS_MISORT_STACKSIZE = 16
        var SS_SMERGE_STACKSIZE = 32
        var TR_STACKSIZE = 64
        var TR_INSERTIONSORT_THRESHOLD = 8

        var sqq_table = intArrayOf(0, 16, 22, 27, 32, 35, 39, 42, 45, 48, 50, 53, 55, 57, 59, 61, 64, 65, 67, 69, 71, 73, 75, 76, 78, 80, 81, 83, 84, 86, 87, 89, 90, 91, 93, 94, 96, 97, 98, 99, 101, 102, 103, 104, 106, 107, 108, 109, 110, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 128, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 144, 145, 146, 147, 148, 149, 150, 150, 151, 152, 153, 154, 155, 155, 156, 157, 158, 159, 160, 160, 161, 162, 163, 163, 164, 165, 166, 167, 167, 168, 169, 170, 170, 171, 172, 173, 173, 174, 175, 176, 176, 177, 178, 178, 179, 180, 181, 181, 182, 183, 183, 184, 185, 185, 186, 187, 187, 188, 189, 189, 190, 191, 192, 192, 193, 193, 194, 195, 195, 196, 197, 197, 198, 199, 199, 200, 201, 201, 202, 203, 203, 204, 204, 205, 206, 206, 207, 208, 208, 209, 209, 210, 211, 211, 212, 212, 213, 214, 214, 215, 215, 216, 217, 217, 218, 218, 219, 219, 220, 221, 221, 222, 222, 223, 224, 224, 225, 225, 226, 226, 227, 227, 228, 229, 229, 230, 230, 231, 231, 232, 232, 233, 234, 234, 235, 235, 236, 236, 237, 237, 238, 238, 239, 240, 240, 241, 241, 242, 242, 243, 243, 244, 244, 245, 245, 246, 246, 247, 247, 248, 248, 249, 249, 250, 250, 251, 251, 252, 252, 253, 253, 254, 254, 255)

        var lg_table = intArrayOf(-1, 0, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7)

        fun getIDX(a: Int): Int {
            return if ((0 <= (a))) (a) else ((a).inv())
        }

        fun min(a: Int, b: Int): Int {
            return if (a < b) a else b
        }

        /**
         *
         */
        fun ssIsqrt(x: Int): Int {
            var y: Int
            var e: Int

            if (x >= (SS_BLOCKSIZE * SS_BLOCKSIZE)) {
                return SS_BLOCKSIZE
            }
            e = if (((x and -0x10000) != 0))
                (if (((x and -0x1000000) != 0))
                    24 + lg_table[(x shr 24) and 0xff]
                else
                    16 + lg_table[(x shr 16) and 0xff])
            else
                (if (((x and 0x0000ff00) != 0))
                    8 + lg_table[(x shr 8) and 0xff]
                else
                    0 + lg_table[(x shr 0) and 0xff])

            if (e >= 16) {
                y = sqq_table[x shr ((e - 6) - (e and 1))] shl ((e shr 1) - 7)
                if (e >= 24) {
                    y = (y + 1 + x / y) shr 1
                }
                y = (y + 1 + x / y) shr 1
            } else if (e >= 8) {
                y = (sqq_table[x shr ((e - 6) - (e and 1))] shr (7 - (e shr 1))) + 1
            } else {
                return sqq_table[x] shr 4
            }

            return if ((x < (y * y))) y - 1 else y
        }

        /**
         *
         */
        fun ssIlg(n: Int): Int {

            return if (((n and 0xff00) != 0))
                8 + lg_table[(n shr 8) and 0xff]
            else
                0 + lg_table[(n shr 0) and 0xff]
        }

        /**
         *
         */
        fun trIlg(n: Int): Int {
            return if (((n and -0x10000) != 0))
                (if (((n and -0x1000000) != 0))
                    24 + lg_table[(n shr 24) and 0xff]
                else
                    16 + lg_table[(n shr 16) and 0xff])
            else
                (if (((n and 0x0000ff00) != 0))
                    8 + lg_table[(n shr 8) and 0xff]
                else
                    0 + lg_table[(n shr 0) and 0xff])
        }

        val SA: SufAssert = SufAssert()
        fun assert(vf: Boolean, s: () -> Any = { "" }) = SA.sufAssert(vf, s)
    }

    /**
     * Calculate minimum and maximum value for a slice of an array.
     */
    fun minmax(input: IntArray, start: Int, length: Int): MinMax {
        var max = input[start]
        var min = max
        var i = length - 2
        var index = start + 1
        while (i >= 0) {
            val v = input[index]
            if (v > max) {
                max = v
            }
            if (v < min) {
                min = v
            }
            i--
            index++
        }

        return MinMax(min, max)
    }

}

