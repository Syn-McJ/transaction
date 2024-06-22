package com.example.transaction.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.transaction.repository.KeyValueStorage

data class KeyValueInputState(
    val key: String = "",
    val value: String = "",
    val keyError: Boolean = false,
    val valueError: Boolean = false
)

class MainViewModel(
    private val storage: KeyValueStorage
): ViewModel() {
    var inputState by mutableStateOf(KeyValueInputState())
        private set

    var pickedCommand by mutableStateOf<CommandView>(CommandView.Get)
        private set

    var history by mutableStateOf("")

    fun executeCommand() {
        val key = inputState.key
        val value = inputState.value

        try {
            inputState = inputState.copy(
                key = "",
                value = "",
            )
            val result = runCommand(pickedCommand, key.trim(), value.trim())
            val resultRow = result?.let { "\n$it" } ?: ""
            history += "\n> ${pickedCommand.name} $key $value$resultRow"
        } catch (ex: IllegalArgumentException) {
            inputState = if (pickedCommand.hasKey && key.isEmpty()) {
                inputState.copy(keyError = true)
            } else {
                inputState.copy(valueError = true)
            }
        }
    }

    fun pickCommand(command: CommandView) {
        pickedCommand = command
    }

    fun setKey(key: String) {
        inputState = inputState.copy(key = key, keyError = false)
    }

    fun setValue(value: String) {
        inputState = inputState.copy(value = value, valueError = false)
    }

    private fun runCommand(command: CommandView, key: String, value: String): String? {
        when (command) {
            is CommandView.Set -> {
                require(key.isNotEmpty())
                require(value.isNotEmpty())
                storage.set(key, value)
            }
            is CommandView.Get -> {
                require(key.isNotEmpty())
                return storage.get(key) ?: "key not set"
            }
            is CommandView.Delete -> {
                require(key.isNotEmpty())
                storage.delete(key)
            }
            is CommandView.Count -> {
                require(value.isNotEmpty())
                return storage.count(value).toString()
            }
            CommandView.Begin -> storage.beginTransaction()
            CommandView.Commit -> {
                if (!storage.commitTransaction()) {
                    return "no transaction"
                }
            }
            CommandView.Rollback -> {
                if (!storage.rollbackTransaction()) {
                    return "no transaction"
                }
            }
        }

        return null
    }
}