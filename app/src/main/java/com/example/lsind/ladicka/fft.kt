package com.example.lsind.ladicka

import java.util.*

/*
 * Free FFT and convolution (Java)
 *
 * Copyright (c) 2017 Project Nayuki. (MIT License)
 * https://www.nayuki.io/page/free-small-fft-in-multiple-languages
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * - The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 * - The Software is provided "as is", without warranty of any kind, express or
 *   implied, including but not limited to the warranties of merchantability,
 *   fitness for a particular purpose and noninfringement. In no event shall the
 *   authors or copyright holders be liable for any claim, damages or other
 *   liability, whether in an action of contract, tort or otherwise, arising from,
 *   out of or in connection with the Software or the use or other dealings in the
 *   Software.
 */   object Fft {

    /*
     * Computes the discrete Fourier transform (DFT) of the given complex vector, storing the result back into the vector.
     * The vector's length must be a power of 2. Uses the Cooley-Tukey decimation-in-time radix-2 algorithm.
     */
    fun transform(real: DoubleArray, imag: DoubleArray) { // Length variables
        val n = real.size
        require(n == imag.size) { "Mismatched lengths" }
        val levels = 31 - Integer.numberOfLeadingZeros(n) // Equal to floor(log2(n))
        require(1 shl levels == n) { "Length is not a power of 2" }
        // Trigonometric tables
        val cosTable = DoubleArray(n / 2)
        val sinTable = DoubleArray(n / 2)
        for (i in 0 until n / 2) {
            cosTable[i] = Math.cos(2 * Math.PI * i / n)
            sinTable[i] = Math.sin(2 * Math.PI * i / n)
        }
        // Bit-reversed addressing permutation
        for (i in 0 until n) {
            val j = Integer.reverse(i) ushr 32 - levels
            if (j > i) {
                var temp = real[i]
                real[i] = real[j]
                real[j] = temp
                temp = imag[i]
                imag[i] = imag[j]
                imag[j] = temp
            }
        }
        // Cooley-Tukey decimation-in-time radix-2 FFT
        var size = 2
        while (size <= n) {
            val halfsize = size / 2
            val tablestep = n / size
            var i = 0
            while (i < n) {
                var j = i
                var k = 0
                while (j < i + halfsize) {
                    val l = j + halfsize
                    val tpre = real[l] * cosTable[k] + imag[l] * sinTable[k]
                    val tpim = -real[l] * sinTable[k] + imag[l] * cosTable[k]
                    real[l] = real[j] - tpre
                    imag[l] = imag[j] - tpim
                    real[j] += tpre
                    imag[j] += tpim
                    j++
                    k += tablestep
                }
                i += size
            }
            if (size == n) // Prevent overflow in 'size *= 2'
                break
            size *= 2
        }
    }

    fun autocorrelation(real: DoubleArray) {
        val imag = DoubleArray(real.size)
        Arrays.fill(imag, 0.0)
        transform(real, imag)
        for (i in real.indices) {
            real[i] = real[i] * real[i] + imag[i] * imag[i]
        }
        Arrays.fill(imag, 0.0)
        transform(real, imag)
    }
}