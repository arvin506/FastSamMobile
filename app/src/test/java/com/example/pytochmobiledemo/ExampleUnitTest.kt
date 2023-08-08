package com.example.pytochmobiledemo

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun test() {
        val l1 = ListNode(1)
        val l2 = ListNode(2)
        val l3 = ListNode(3)
        l1.next = l2
        l2.next = l3
        val result = Solution().reversePrint(l1)
        println(result)
    }

    @Test
    fun getSize(){
        val batchSize = 1
        val channels = 101
        val height = 1024
        val width = 1024

        val array = Array(batchSize) {
            Array(channels) {
                Array(height) {
                    FloatArray(width)
                }
            }
        }

        val memoryUsageBytes = batchSize.toLong() * channels * height * width * java.lang.Float.BYTES

        println("Memory usage of float[1][101][1024][1024]: $memoryUsageBytes bytes")

    }

}

