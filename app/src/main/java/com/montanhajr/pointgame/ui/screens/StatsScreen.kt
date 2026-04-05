package com.montanhajr.pointgame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.montanhajr.pointgame.logic.StatisticsManager
import com.montanhajr.pointgame.models.BoardStyle
import com.montanhajr.pointgame.ui.components.BoardBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val statsManager = remember { StatisticsManager(context) }
    val stats = remember { statsManager.getStats() }

    Box(modifier = Modifier.fillMaxSize()) {
        BoardBackground(style = BoardStyle.GALAXY)
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Statistics", color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black.copy(alpha = 0.5f))
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally // Centraliza o conteúdo da coluna
            ) {
                // Total Triangles Card
                StatsCard(
                    title = "Career Performance",
                    content = {
                        Text(
                            text = "${stats.totalTriangles}",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF00FFFF),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Triangles Formed", 
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                    }
                )

                // Time Card
                StatsCard(
                    title = "Average Match Time",
                    content = {
                        val minutes = (stats.avgTimeMs / 1000) / 60
                        val seconds = (stats.avgTimeMs / 1000) % 60
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                )

                // Difficulty Grid
                Text(
                    "VS CPU Win Rates",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                    textAlign = TextAlign.Start // Mantém o título da grade alinhado à esquerda se preferir, ou Center
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    WinRateCard(modifier = Modifier.weight(1f), label = "Easy", rate = stats.easyStats.winRate, color = Color(0xFF4CAF50))
                    WinRateCard(modifier = Modifier.weight(1f), label = "Medium", rate = stats.mediumStats.winRate, color = Color(0xFFFF9800))
                    WinRateCard(modifier = Modifier.weight(1f), label = "Hard", rate = stats.hardStats.winRate, color = Color(0xFFE91E63))
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun StatsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally // Centraliza o conteúdo dentro do Card
        ) {
            Text(
                text = title, 
                color = Color.Gray, 
                fontSize = 14.sp, 
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun WinRateCard(modifier: Modifier, label: String, rate: Int, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator(
                progress = { rate.toFloat() / 100f },
                color = color,
                strokeWidth = 4.dp,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("$rate%", color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
        }
    }
}
