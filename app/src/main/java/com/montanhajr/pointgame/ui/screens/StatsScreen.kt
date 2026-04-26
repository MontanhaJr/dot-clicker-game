package com.montanhajr.pointgame.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.montanhajr.pointgame.R
import com.montanhajr.pointgame.logic.StatisticsManager
import com.montanhajr.pointgame.ui.components.AchievementDialog
import com.montanhajr.pointgame.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val statsManager = remember { StatisticsManager(context) }
    val stats = remember { statsManager.getStats() }
    var showAchievementDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // App Background Image
        Image(
            painter = painterResource(id = R.drawable.app_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Camada de contraste Pop!
        Box(modifier = Modifier.fillMaxSize().background(PopDarkBlue.copy(alpha = 0.6f)))
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            stringResource(R.string.stats_title), 
                            color = PopWhite, 
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = PopWhite)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showAchievementDialog = true }) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = stringResource(R.string.achievements_title), tint = PopYellow)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Card de Triângulos Totais (Destaque Principal)
                PopStatsCard(
                    title = stringResource(R.string.global_performance),
                    content = {
                        Text(
                            text = "${stats.totalTriangles}",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PopCyan,
                            textAlign = TextAlign.Center,
                            lineHeight = 64.sp
                        )
                        Text(
                            text = stringResource(R.string.triangles_formed), 
                            color = PopWhite.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                )

                // Row para tempo médio
                PopStatsCard(
                    title = stringResource(R.string.avg_time_per_match),
                    content = {
                        val minutes = (stats.avgTimeMs / 1000) / 60
                        val seconds = (stats.avgTimeMs / 1000) % 60
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PopYellow,
                            textAlign = TextAlign.Center
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Difficulty Grid
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        stringResource(R.string.win_rate_vs_cpu),
                        color = PopWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Start
                    )
                    Text(
                        stringResource(R.string.training_only_label),
                        color = PopWhite.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Start
                    )
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PopWinRateCard(modifier = Modifier.weight(1f), label = stringResource(R.string.difficulty_easy), rate = stats.easyStats.winRate, color = PopGreen)
                    PopWinRateCard(modifier = Modifier.weight(1f), label = stringResource(R.string.difficulty_medium), rate = stats.mediumStats.winRate, color = PopYellow)
                    PopWinRateCard(modifier = Modifier.weight(1f), label = stringResource(R.string.difficulty_hard), rate = stats.hardStats.winRate, color = PopRed)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showAchievementDialog) {
        AchievementDialog(
            achievements = statsManager.getAchievements(),
            onDismiss = { showAchievementDialog = false }
        )
    }
}

@Composable
fun PopStatsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PopDeepBlue.copy(alpha = 0.8f)),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title, 
                color = PopWhite.copy(alpha = 0.5f), 
                fontSize = 12.sp, 
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun PopWinRateCard(modifier: Modifier, label: String, rate: Int, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PopDeepBlue.copy(alpha = 0.8f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = PopWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { rate.toFloat() / 100f },
                    color = color,
                    trackColor = color.copy(alpha = 0.1f),
                    strokeWidth = 6.dp,
                    modifier = Modifier.size(50.dp)
                )
                Text(
                    text = "$rate%", 
                    color = color, 
                    fontWeight = FontWeight.ExtraBold, 
                    fontSize = 12.sp
                )
            }
        }
    }
}
