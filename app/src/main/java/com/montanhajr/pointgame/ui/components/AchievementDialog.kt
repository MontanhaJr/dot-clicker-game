package com.montanhajr.pointgame.ui.components

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.games.PlayGames
import com.montanhajr.pointgame.R
import com.montanhajr.pointgame.models.Achievement
import com.montanhajr.pointgame.ui.theme.*

@Composable
fun AchievementDialog(
    achievements: List<Achievement>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.achievements_available),
        stringResource(R.string.achievements_completed)
    )

    val filteredAchievements = remember(selectedTab, achievements) {
        if (selectedTab == 0) achievements.filter { !it.isCompleted }
        else achievements.filter { it.isCompleted }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(32.dp),
            color = PopDeepBlue,
            tonalElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.achievements_title).uppercase(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PopWhite
                    )
                }

                // Tabs Pop!
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = PopCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = PopCyan,
                            height = 3.dp
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title.uppercase(),
                                    color = if (selectedTab == index) PopCyan else PopWhite.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                }

                // List
                if (filteredAchievements.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedTab == 0) stringResource(R.string.achievements_empty_available) 
                                   else stringResource(R.string.achievements_empty_completed),
                            color = PopWhite.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredAchievements) { achievement ->
                            PopAchievementItem(achievement)
                        }
                    }
                }

                // Footer Pop!
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        activity?.let {
                            PlayGames.getAchievementsClient(it).achievementsIntent
                                .addOnSuccessListener { intent ->
                                    it.startActivityForResult(intent, 9001)
                                }
                        }
                    }) {
                        Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(18.dp), tint = PopCyan)
                        Spacer(Modifier.width(8.dp))
                        Text("PLAY GAMES", color = PopCyan, fontWeight = FontWeight.ExtraBold)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = PopBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.close).uppercase(), fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
fun PopAchievementItem(achievement: Achievement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        if (achievement.isCompleted) PopYellow.copy(alpha = 0.2f) 
                        else PopWhite.copy(alpha = 0.1f),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (achievement.isCompleted) Icons.Default.CheckCircle else Icons.Default.Star,
                    contentDescription = null,
                    tint = if (achievement.isCompleted) PopYellow else PopWhite.copy(alpha = 0.3f),
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    fontWeight = FontWeight.ExtraBold,
                    color = PopWhite,
                    fontSize = 16.sp
                )
                Text(
                    text = achievement.description,
                    color = PopWhite.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                if (!achievement.isCompleted) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val progress = achievement.currentProgress.toFloat() / achievement.requiredProgress
                    Box {
                         LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = PopCyan,
                            trackColor = PopWhite.copy(alpha = 0.1f),
                            strokeCap = StrokeCap.Round
                        )
                    }
                    Text(
                        text = "${achievement.currentProgress} / ${achievement.requiredProgress}",
                        color = PopCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
