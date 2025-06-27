package com.example.androidcodexone

/**
 * Simple utility demonstrating a basic for loop.
 */
object ForLoopExample {
    /**
     * Returns a list of integers from 0 until [count] - 1.
     */
    fun generateSequence(count: Int): List<Int> {
        val result = mutableListOf<Int>()
        for (i in 0 until count) {
            result.add(i)
        }
        return result
    }
}
