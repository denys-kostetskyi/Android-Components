package com.denyskostetskyi.androidcomponents.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.denyskostetskyi.androidcomponents.ui.theme.AndroidComponentsTheme

class ComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val message = intent.getStringExtra(KEY_MESSAGE) ?: NO_MESSAGE
        setContent {
            AndroidComponentsTheme {
                GreetingScreen(message = message, onReturnResult = ::handleResult)
            }
        }
    }

    private fun handleResult(result: String) {
        val returnIntent = MainActivity.newResultIntent(result)
        setResult(RESULT_OK, returnIntent)
        finish()
    }

    companion object {
        private const val KEY_MESSAGE = "message"
        private const val NO_MESSAGE = "no message"

        fun newIntent(context: Context, message: String) =
            Intent(context, ComposeActivity::class.java).apply {
                putExtra(KEY_MESSAGE, message)
            }
    }
}

@Composable
fun GreetingScreen(message: String, onReturnResult: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Message from MainActivity: $message",
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onReturnResult("Hello from ComposeActivity!") }) {
            Text("Return Result")
        }
    }
}
