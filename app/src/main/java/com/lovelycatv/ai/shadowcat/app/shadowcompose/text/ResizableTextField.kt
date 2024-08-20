package com.lovelycatv.ai.shadowcat.app.shadowcompose.text

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ResizableTextField() {
    var text by remember { mutableStateOf(TextFieldValue()) }
    val minHeight = 48.dp
    val maxHeight = 96.dp

    // BoxWithConstraints allows access to the constraints of its parent
    BoxWithConstraints {
        // Calculate the height for the OutlinedTextField
        val dynamicHeight = maxOf(minHeight, minHeight + (text.text.length * 0.5.dp)) // Adjust multiplier as needed

        // Ensure the height does not exceed maxHeight
        val effectiveHeight = minOf(maxHeight, dynamicHeight)

        OutlinedTextField(
            value = text,
            onValueChange = { newValue ->
                text = newValue
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(effectiveHeight) // Use calculated height
                .padding(8.dp), // Optional padding
            textStyle = TextStyle(
                fontSize = 16.sp
            ),
            placeholder = { Text("Enter your message") }
        )
    }
}