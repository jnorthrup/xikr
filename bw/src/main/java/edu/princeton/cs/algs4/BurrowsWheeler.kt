package edu.princeton.cs.algs4

import java.util.*

/**
 *
 */
object BurrowsWheeler {

    private const val R = 256

    /**
     *
     */// apply Burrows-Wheeler encoding, reading from standard input and
    // writing to standard output
    fun encode() {
        // read the input
        val s = BinaryStdIn.readString()
        val N = s.length
        // concatenate the string to itself
        val ss = s + s
        val strs = arrayOfNulls<String>(N)
        for (i in 0 until N)
            strs[i] = ss.substring(i, i + N)
        // using system sort
        Arrays.sort(strs)
        val index = Arrays.binarySearch(strs, s)
        BinaryStdOut.write(index)
        for (i in 0 until N) {
            val s1 = strs[i]!!
            BinaryStdOut.write(s1[N - 1])
        }
        // close output
        BinaryStdOut.close()
    }

    /**
     *
     */// apply Burrows-Wheeler decoding, reading from standard input
    // and writing to standard output
    fun decode() {
        // read first and t[]
        val first = BinaryStdIn.readInt()
        val s = BinaryStdIn.readString()
        val N = s.length
        // allocate the ending array
        val t = CharArray(N)
        for (i in 0 until N)
            t[i] = s[i]
        // allocate an array to store the next array
        val next = IntArray(N)
        // allocate an array to store 1st char of the sorted suffixes
        val f = CharArray(N)
        // an array to store the total count for each character
        val count = IntArray(R + 1)
        // do key-index counting, but store values in the next[] array
        for (i in 0 until N)
            count[t[i].toInt() + 1]++
        for (r in 0 until R)
            count[r + 1] += count[r]
        for (i in 0 until N) {
            val c = t[i] as Int
            next[count[c]] = i
            f[count[c]++] = t[i]
        }
        // write out
        var current = first
        for (i in 0 until N) {
            BinaryStdOut.write(f[current])
            current = next[current]
        }
        // close output
        BinaryStdOut.close()
    }

    /**
     *
     */// if args[0] is '-', apply Burrows-Wheeler encoding
    // if args[0] is '+', apply Burrows-Wheeler decoding
    @JvmStatic
    fun main(args: Array<String>) {
        if (args[0] == "-")
            encode()
        else if (args[0] == "+")
            decode()
        else
            throw RuntimeException("Illegal command line argument")
    }

}