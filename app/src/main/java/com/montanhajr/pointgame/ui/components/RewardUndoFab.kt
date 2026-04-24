package com.montanhajr.pointgame.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RewardUndoFab(
    visible: Boolean,
    countdown: Int,
    onUndo: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { countdown / 10f },
                        modifier = Modifier.size(64.dp),
                        color = Color(0xFF00FFFF),
                        strokeWidth = 4.dp,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                    LargeFloatingActionButton(
                        onClick = onUndo,
                        shape = CircleShape,
                        containerColor = Color(0xFF00FFFF),
                        contentColor = Color.Black,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = "Reward Undo")
                    }
                }
                Text(
                    text = "UNDO",
                    color = Color(0xFF00FFFF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
