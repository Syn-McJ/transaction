package com.example.transaction.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.transaction.repository.KeyValueStorage

data class MainScreenUIState(
    val key: String = "",
    val value: String = "",
    val pickedCommand: CommandView = CommandView.all.first(),
    val history: String = "",
    val keyError: Boolean = false,
    val valueError: Boolean = false
)

class MainViewModel(
    private val storage: KeyValueStorage
): ViewModel() {
    var uiState by mutableStateOf(MainScreenUIState())
        private set

    fun executeCommand() {
        val key = uiState.key
        val value = uiState.value

        try {
            val result = runCommand(uiState.pickedCommand, key.trim(), value.trim())
            val resultRow = result?.let { "\n$it" } ?: ""
            uiState = uiState.copy(
                key = "",
                value = "",
                history = uiState.history + "\n> ${uiState.pickedCommand.name} $key $value$resultRow"
            )
        } catch (ex: IllegalArgumentException) {
            uiState = if (uiState.pickedCommand.hasKey && key.isEmpty()) {
                uiState.copy(keyError = true)
            } else {
                uiState.copy(valueError = true)
            }
        }
    }

    fun pickCommand(command: CommandView) {
        uiState = uiState.copy(pickedCommand = command)
    }

    fun setKey(key: String) {
        uiState = uiState.copy(key = key, keyError = false)
    }

    fun setValue(value: String) {
        uiState = uiState.copy(value = value, valueError = false)
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