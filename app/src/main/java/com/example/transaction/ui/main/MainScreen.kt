package com.example.transaction.ui.main

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        MultiLineTextView(
            text = viewModel.uiState.history,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 20.dp)
        )

        var commandToConfirm by remember { mutableStateOf<CommandView?>(null) }
        val confirmOrExecute = {
            if (viewModel.uiState.pickedCommand.confirmationRequired) {
                commandToConfirm = viewModel.uiState.pickedCommand
            } else {
                viewModel.executeCommand()
                commandToConfirm = null
            }
        }

        Row(
            Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()) {
            CommandPicker(
                viewModel,
                Modifier.weight(1f)
            )
            ExecuteButton(
                confirmOrExecute,
                modifier = Modifier
                    .padding(start = 10.dp)
            )
        }

        KeyValueInput(
            viewModel,
            confirmOrExecute,
            Modifier.padding(vertical = 20.dp)
        )

        commandToConfirm?.let { command ->
            ConfirmationAlert(
                commandName = command.name,
                onConfirm = {
                    viewModel.executeCommand()
                    commandToConfirm = null
                },
                onDismiss = { commandToConfirm = null }
            )
        }
    }
}

@Composable
fun MultiLineTextView(text: String, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState(0)
    LaunchedEffect(text) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Box(modifier = modifier
        .background(Color.Gray.copy(alpha = 0.2f), shape = RoundedCornerShape(10.dp))
        .border(1.dp, Color.Gray, shape = RoundedCornerShape(10.dp))
        .padding(16.dp)
    ) {
        BasicTextField(
            value = text,
            onValueChange = { },
            readOnly = true,
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                lineHeight = 28.sp
            ),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .verticalScroll(scrollState)
        )
    }
}

@Composable
fun CommandPicker(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 0f else 180f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "animateFloatAsState"
    )

    Button(
        modifier = modifier,
        onClick = { expanded = !expanded }
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = viewModel.uiState.pickedCommand.name,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            )
            Box {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier
                        .rotate(rotationAngle)
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    CommandView.all.forEach { command ->
                        DropdownMenuItem(
                            text = { Text(command.name) },
                            onClick = {
                                viewModel.pickCommand(command)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExecuteButton(
    confirmOrExecute: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        modifier = modifier
            .clip(CircleShape),
        onClick = confirmOrExecute,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text("Go")
    }
}

@Composable
fun ConfirmationAlert(
    commandName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
            )
        },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Execute $commandName command?")
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun KeyValueInput(
    viewModel: MainViewModel,
    confirmOrExecute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    Row(modifier = modifier) {
        if (viewModel.uiState.pickedCommand.hasKey) {
            LaunchedEffect(viewModel.uiState.key) {
                if (viewModel.uiState.key.isEmpty()) {
                    focusRequester.requestFocus()
                }
            }

            TextField(
                value = viewModel.uiState.key,
                onValueChange = { viewModel.setKey(it) },
                label = { Text("Key") },
                isError = viewModel.uiState.keyError,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = if (viewModel.uiState.pickedCommand.hasValue) ImeAction.Next else ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { confirmOrExecute() }
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
                    .focusRequester(focusRequester)
            )
        }

        if (viewModel.uiState.pickedCommand.hasValue) {
            TextField(
                value = viewModel.uiState.value,
                onValueChange = { viewModel.setValue(it) },
                label = { Text("Value") },
                isError = viewModel.uiState.valueError,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        confirmOrExecute()
                    }
                ),
                modifier = Modifier
                    .weight(1f)
            )
        }
    }
}