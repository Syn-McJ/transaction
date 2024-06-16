package com.example.transaction.ui.main

sealed class CommandView(
    val name: String,
    val hasKey: Boolean,
    val hasValue: Boolean,
    val confirmationRequired: Boolean
) {
    companion object {
        val all = listOf(
            Set,
            Get,
            Delete,
            Count,
            Begin,
            Commit,
            Rollback
        )
    }

    data object Set : CommandView("SET", true, true, false)
    data object Get : CommandView("GET", true, false, false)
    data object Delete : CommandView("DELETE", true, false, true)
    data object Count : CommandView("COUNT", false, true, false)
    data object Begin : CommandView("BEGIN", false, false, false)
    data object Commit : CommandView("COMMIT", false, false, true)
    data object Rollback : CommandView("ROLLBACK", false, false, true)
}
