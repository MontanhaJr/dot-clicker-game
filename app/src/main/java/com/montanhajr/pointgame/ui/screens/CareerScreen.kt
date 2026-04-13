package com.montanhajr.pointgame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.montanhajr.pointgame.logic.CareerManager
import com.montanhajr.pointgame.models.BoardStyle
import com.montanhajr.pointgame.ui.components.BoardBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerScreen(onLevelSelected: (Int) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val careerManager = remember { CareerManager(context) }
    val currentLevel = careerManager.getCurrentLevel()

    Box(modifier = Modifier.fillMaxSize()) {
        BoardBackground(style = BoardStyle.GALAXY)
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Career Journey", color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black.copy(alpha = 0.5f))
                )
            }
        ) { padding ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items((1..50).toList()) { level ->
                    LevelNode(
                        level = level,
                        isUnlocked = level <= currentLevel,
                        isCurrent = level == currentLevel,
                        onClick = { if (level <= currentLevel) onLevelSelected(level) }
                    )
                }
            }
        }
    }
}

@Composable
fun LevelNode(level: Int, isUnlocked: Boolean, isCurrent: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = when {
                isCurrent -> Color(0xFF00FFFF)
                isUnlocked -> Color(0xFF4CAF50)
                else -> Color.Gray.copy(alpha = 0.3f)
            },
            modifier = Modifier.size(64.dp),
            border = if (isCurrent) androidx.compose.foundation.BorderStroke(4.dp, Color.White) else null,
            shadowElevation = if (isUnlocked) 8.dp else 0.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isUnlocked) {
                    Text(
                        text = level.toString(),
                        color = if (isCurrent) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                } else {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
                }
            }
        }
        
        if (isCurrent) {
            Text(
                text = "CURRENT",
                color = Color(0xFF00FFFF),
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
