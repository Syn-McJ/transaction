package com.example.transaction

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.transaction.repository.KeyValueStorage
import com.example.transaction.ui.main.MainScreen
import com.example.transaction.ui.main.MainViewModel
import com.example.transaction.ui.theme.TransactionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            TransactionTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        viewModel = MainViewModel(KeyValueStorage()), // TODO: usually injected
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
