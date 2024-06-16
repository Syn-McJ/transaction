package com.example.transaction

import com.example.transaction.repository.KeyValueStorage
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

/**
Chat-GPT generated
 */
class ThreadSafetyTest {
    private lateinit var store: KeyValueStorage

    @Before
    fun setUp() {
        store = KeyValueStorage()
    }

    @Test
    fun testConcurrentSetAndGet() {
        val threadCount = 10
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        for (i in 1..threadCount) {
            executor.submit {
                try {
                    store.set("key$i", "value$i")
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executor.shutdown()

        for (i in 1..threadCount) {
            assertEquals("value$i", store.get("key$i"))
        }
    }

    @Test
    fun testConcurrentTransactions() {
        val threadCount = 10
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        for (i in 1..threadCount) {
            executor.submit {
                store.beginTransaction()
                try {
                    store.set("key$i", "value$i")
                    store.commitTransaction()
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executor.shutdown()

        for (i in 1..threadCount) {
            assertEquals("value$i", store.get("key$i"))
        }
    }

    @Test
    fun testConcurrentReadWrite() {
        val threadCount = 10
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount * 2)

        for (i in 1..threadCount) {
            executor.submit {
                store.set("key$i", "value$i")
                latch.countDown()
            }
        }

        for (i in 1..threadCount) {
            executor.submit {
                store.get("key$i")
                latch.countDown()
            }
        }

        latch.await()
        executor.shutdown()

        for (i in 1..threadCount) {
            assertEquals("value$i", store.get("key$i"))
        }
    }

    @Test
    fun testConcurrentTransactionRollback() {
        val threadCount = 10
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        for (i in 1..threadCount) {
            executor.submit {
                store.beginTransaction()
                try {
                    store.set("key$i", "value$i")
                    store.rollbackTransaction()
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executor.shutdown()

        for (i in 1..threadCount) {
            assertNull(store.get("key$i"))
        }
    }
}
