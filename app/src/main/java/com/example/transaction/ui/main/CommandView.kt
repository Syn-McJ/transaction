package com.example.transaction.ui.main

import com.example.transaction.ui.main.CommandView.Begin
import com.example.transaction.ui.main.CommandView.Commit
import com.example.transaction.ui.main.CommandView.Count
import com.example.transaction.ui.main.CommandView.Delete
import com.example.transaction.ui.main.CommandView.Get
import com.example.transaction.ui.main.CommandView.Rollback
import com.example.transaction.ui.main.CommandView.Set

val allCommands = listOf(
    Set,
    Get,
    Delete,
    Count,
    Begin,
    Commit,
    Rollback
)

sealed class CommandView(
    val name: String,
    val hasKey: Boolean,
    val hasValue: Boolean,
    val confirmationRequired: Boolean
) {
    data object Set : CommandView("SET", true, true, false)
    data object Get : CommandView("GET", true, false, false)
    data object Delete : CommandView("DELETE", true, false, true)
    data object Count : CommandView("COUNT", false, true, false)
    data object Begin : CommandView("BEGIN", false, false, false)
    data object Commit : CommandView("COMMIT", false, false, true)
    data object Rollback : CommandView("ROLLBACK", false, false, true)
}
