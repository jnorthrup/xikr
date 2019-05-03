/******************************************************************************
 * Compilation:  javac edu.princeton.cs.algs4.BinaryStdIn.java
 * Execution:    java edu.princeton.cs.algs4.BinaryStdIn < input > output
 * Dependencies: none
 *
 * Supports reading binary data from standard input.
 *
 * % java edu.princeton.cs.algs4.BinaryStdIn < input.jpg > output.jpg
 * % diff input.jpg output.jpg
 *
 */

package edu.princeton.cs.algs4

/******************************************************************************
 *  Compilation:  javac edu.princeton.cs.algs4.BinaryStdOut.java
 *  Execution:    java edu.princeton.cs.algs4.BinaryStdOut
 *  Dependencies: none
 *
 *  Write binary data to standard output, either one 1-bit boolean,
 *  one 8-bit char, one 32-bit int, one 64-bit double, one 32-bit float,
 *  or one 64-bit long at a time.
 *
 *  The bytes written are not aligned.
 *
 ******************************************************************************/


/*************************************************************************
 *  Compilation:  javac edu.princeton.cs.algs4.BurrowsWheeler.java
 *  Execution:     java edu.princeton.cs.algs4.BurrowsWheeler - < input.txt   (encode)
 *  Execution:     java edu.princeton.cs.algs4.BurrowsWheeler + < input.txt   (decode)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Encode or decode a text file using Burrows-Wheeler transform.
 *
 *************************************************************************/

/*************************************************************************
 * Compilation:  javac MoveToFront.java
 * Execution:     java MoveToFront - < input.txt   (encode)
 * Execution:     java MoveToFront + < input.txt   (decode)
 * Dependencies: BinaryIn.java BinaryOut.java
 *
 * Move-to-front encode or decode a text file.
 *
 */


object MoveToFront {

    private const val R = 256

    /**
     *
     */// Move-to-front encoding
    fun encode() {
        // initialize ordered char array
        val a = CharArray(R)
        for (i in 0 until R)
            a[i] = i.toChar()
        // read the input
        val s = BinaryStdIn.readString()
        val input = s.toCharArray()
        var index = 0
        for (i in input.indices) {
            index = 0
            // look for index where input[i] appears
            while (a[index] != input[i])
                index++
            BinaryStdOut.write(index)
            // move to front
            while (index > 0) {
                a[index] = a[index - 1]
                index--
            }
            a[0] = input[i]
        }
        // close output
        BinaryStdOut.close()
    }

    /**
     *
     */// Move-to-front decoding
    fun decode() {

        // initialize ordered char array
        val a = CharArray(R)
        for (i in 0 until R)
            a[i] = i.toChar()
        // read the input
        val s = BinaryStdIn.readString()
        val input = s.toCharArray()
        var index = 0
        for (i in input.indices) {
            index = input[i].toInt()
            BinaryStdOut.write(a[index])
            // move to front
            val a0 = a[index]
            while (index > 0) {
                a[index] = a[index - 1]
                index--
            }
            a[0] = a0
        }
        // close output
        BinaryStdOut.close()
    }

    /**
     *
     */// if args[0] is '-', apply Move-to-front encoding
    // if args[0] is '+', apply Move-to-front decoding
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

/******************************************************************************
 *  Copyright 2002-2018, Robert Sedgewick and Kevin Wayne.
 *
 *  This file is part of algs4.jar, which accompanies the textbook
 *
 *      Algorithms, 4th edition by Robert Sedgewick and Kevin Wayne,
 *      Addison-Wesley Professional, 2011, ISBN 0-321-57351-X.
 *      http://algs4.cs.princeton.edu
 *
 *
 *  algs4.jar is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  algs4.jar is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with algs4.jar.  If not, see http://www.gnu.org/licenses.
 ******************************************************************************/

/******************************************************************************
 * Copyright 2002-2018, Robert Sedgewick and Kevin Wayne.
 *
 * This file is part of algs4.jar, which accompanies the textbook
 *
 * Algorithms, 4th edition by Robert Sedgewick and Kevin Wayne,
 * Addison-Wesley Professional, 2011, ISBN 0-321-57351-X.
 * http://algs4.cs.princeton.edu
 *
 *
 * algs4.jar is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * algs4.jar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with algs4.jar.  If not, see http://www.gnu.org/licenses.
 */