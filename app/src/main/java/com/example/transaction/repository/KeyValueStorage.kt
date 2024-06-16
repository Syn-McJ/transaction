package com.example.transaction.repository

import java.util.Stack
import java.util.concurrent.ConcurrentHashMap

sealed class CommitCommand {
    data class Set(val key: String, val value: String, val oldValue: String?) : CommitCommand()
    data class Delete(val key: String, val value: String?) : CommitCommand()
}

class KeyValueStorage {
    private val dataStore = ConcurrentHashMap<String, String>()
    private val threadLocalTransactionStack = ThreadLocal.withInitial { Stack<HashMap<String, CommitCommand>>() }
    private val transactionStack: Stack<HashMap<String, CommitCommand>> get() {
        return threadLocalTransactionStack.get()!!
    }

    fun set(key: String, value: String) {
        if (transactionStack.isEmpty()) {
            dataStore[key] = value
        } else {
            var oldValue = dataStore[key]

            for (map in transactionStack.asReversed()) {
                if (map.containsKey(key)) {
                    oldValue = map[key]?.let {
                        when (it) {
                            is CommitCommand.Set -> it.value
                            is CommitCommand.Delete -> null
                        }
                    }
                    break
                }
            }

            transactionStack.peek()[key] = CommitCommand.Set(key, value, oldValue)
        }
    }

    fun get(key: String): String? {
        if (transactionStack.isEmpty()) {
            return dataStore[key]
        } else {
            for (map in transactionStack.asReversed()) {
                return when (val command = map[key]) {
                    is CommitCommand.Set -> command.value
                    is CommitCommand.Delete -> null
                    else -> continue
                }
            }

            return dataStore[key]
        }
    }

    fun delete(key: String) {
        if (transactionStack.isEmpty()) {
            dataStore.remove(key)
        } else {
            var oldValue = dataStore[key]

            for (map in transactionStack.asReversed()) {
                if (map.containsKey(key)) {
                    oldValue = map[key]?.let {
                        when (it) {
                            is CommitCommand.Set -> it.value
                            is CommitCommand.Delete -> null
                        }
                    }
                    break
                }
            }

            transactionStack.peek()[key] = CommitCommand.Delete(key, oldValue)
        }
    }

    fun count(value: String): Int {
        var count = dataStore.values.count { it == value }

        for (map in transactionStack) {
            for (command in map.values) {
                when (command) {
                    is CommitCommand.Set -> {
                        if (command.value == value) {
                            count++
                        }

                        if (command.oldValue == value) {
                            count--
                        }
                    }
                    is CommitCommand.Delete -> {
                        if (command.value == value) {
                            count--
                        }
                    }
                }
            }
        }

        return count
    }

    fun beginTransaction() {
        transactionStack.push(HashMap())
    }

    fun commitTransaction(): Boolean {
        val cache = HashMap(dataStore)

        return try {
            val stack = transactionStack.pop()

            if (transactionStack.isEmpty()) {
                // Outermost transaction, apply changes to the storage
                for (command in stack.values) {
                    when (command) {
                        is CommitCommand.Set -> dataStore[command.key] = command.value
                        is CommitCommand.Delete -> dataStore.remove(command.key)
                    }
                }
            } else {
                // Nested transaction, unwrap into the parent
                transactionStack.peek().putAll(stack)
            }

            true
        } catch (e: Exception) {
            dataStore.clear()
            dataStore.putAll(cache)
            false
        }
    }

    fun rollbackTransaction(): Boolean {
        return if (transactionStack.isNotEmpty()) {
            transactionStack.pop()
            true
        } else {
            false
        }
    }
}