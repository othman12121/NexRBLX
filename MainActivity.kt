package com.nexrblx.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NexTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Stage1Screen()
                }
            }
        }
    }
}

@Composable
fun Stage1Screen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Text("Nex", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = NexText)
            Text("RBLX", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = NexNeon)
        }
        Spacer(Modifier.height(12.dp))
        Text("Stage 1 ✓  Build works", fontSize = 16.sp, color = NexPurple)
        Spacer(Modifier.height(6.dp))
        Text("Powered by ScriptBlox.com", fontSize = 12.sp, color = NexMuted)
    }
}
