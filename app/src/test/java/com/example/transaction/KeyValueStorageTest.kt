package com.example.transaction

import com.example.transaction.repository.KeyValueStorage
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

/**
 Chat-GPT generated
 */
class KeyValueStorageTest {
    private lateinit var store: KeyValueStorage

    @Before
    fun setUp() {
        store = KeyValueStorage()
    }

    @Test
    fun testSetAndGet() {
        store.set("key1", "value1")
        assertEquals("value1", store.get("key1"))
    }

    @Test
    fun testDelete() {
        store.set("key1", "value1")
        store.delete("key1")
        assertNull(store.get("key1"))
    }

    @Test
    fun testCount() {
        store.set("key1", "value1")
        store.set("key2", "value1")
        store.set("key3", "value2")
        assertEquals(2, store.count("value1"))
        assertEquals(1, store.count("value2"))
    }

    @Test
    fun testBeginTransactionAndRollback() {
        store.set("key1", "value1")
        store.beginTransaction()
        store.set("key1", "value2")
        assertEquals("value2", store.get("key1"))
        store.rollbackTransaction()
        assertEquals("value1", store.get("key1"))
    }

    @Test
    fun testBeginTransactionAndCommit() {
        store.set("key1", "value1")
        store.beginTransaction()
        store.set("key1", "value2")
        assertEquals("value2", store.get("key1"))
        store.commitTransaction()
        assertEquals("value2", store.get("key1"))
    }

    @Test
    fun testNestedTransactionsRollback() {
        store.set("key1", "value1")
        store.beginTransaction() // T1
        store.set("key1", "value2")
        store.beginTransaction() // T2
        store.set("key1", "value3")
        store.rollbackTransaction() // Rollback T2
        assertEquals("value2", store.get("key1"))
        store.rollbackTransaction() // Rollback T1
        assertEquals("value1", store.get("key1"))
    }

    @Test
    fun testNestedTransactionsCommit() {
        store.set("key1", "value1")
        store.beginTransaction() // T1
        store.set("key1", "value2")
        store.beginTransaction() // T2
        store.set("key1", "value3")
        store.commitTransaction() // Commit T2
        assertEquals("value3", store.get("key1"))
        store.commitTransaction() // Commit T1
        assertEquals("value3", store.get("key1"))
    }

    @Test
    fun testDeleteWithinTransaction() {
        store.set("key1", "value1")
        store.beginTransaction()
        store.delete("key1")
        assertNull(store.get("key1"))
        store.rollbackTransaction()
        assertEquals("value1", store.get("key1"))
    }

    @Test
    fun testSetAndDeleteWithinTransaction() {
        store.set("key1", "value1")
        store.beginTransaction()
        store.set("key1", "value2")
        store.delete("key1")
        assertNull(store.get("key1"))
        store.rollbackTransaction()
        assertEquals("value1", store.get("key1"))
    }

    @Test
    fun testMultipleTransactions() {
        store.set("key1", "value1")
        store.beginTransaction()
        store.set("key1", "value2")
        store.beginTransaction()
        store.set("key1", "value3")
        store.commitTransaction()
        assertEquals("value3", store.get("key1"))
        store.rollbackTransaction()
        assertEquals("value1", store.get("key1"))
    }
}