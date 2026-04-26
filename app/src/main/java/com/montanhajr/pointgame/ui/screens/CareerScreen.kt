package com.montanhajr.pointgame.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.montanhajr.pointgame.R
import com.montanhajr.pointgame.logic.CareerManager
import com.montanhajr.pointgame.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerScreen(onLevelSelected: (Int) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val careerManager = remember { CareerManager(context) }
    val currentLevel = careerManager.getCurrentLevel()

    Box(modifier = Modifier.fillMaxSize()) {
        // Usando o mesmo background da tela inicial
        Image(
            painter = painterResource(id = R.drawable.app_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Camada de contraste para os nós dos níveis
        Box(modifier = Modifier.fillMaxSize().background(PopDarkBlue.copy(alpha = 0.6f)))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            stringResource(R.string.career_title).uppercase(), 
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
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                items((1..100).toList()) { level ->
                    PopLevelNode(
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
fun PopLevelNode(level: Int, isUnlocked: Boolean, isCurrent: Boolean, onClick: () -> Unit) {
    val mainColor = when {
        isCurrent -> PopYellow
        isUnlocked -> PopBlue
        else -> Color.Gray.copy(alpha = 0.3f)
    }
    
    val secondaryColor = when {
        isCurrent -> PopOrange
        isUnlocked -> PopCyan
        else -> Color.DarkGray.copy(alpha = 0.5f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            modifier = Modifier
                .size(72.dp)
                .shadow(if (isUnlocked) 8.dp else 0.dp, CircleShape)
                .border(
                    width = if (isCurrent) 4.dp else 2.dp,
                    color = if (isCurrent) PopWhite else Color.White.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(mainColor, secondaryColor)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    Text(
                        text = level.toString(),
                        color = if (isCurrent) PopDarkBlue else PopWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock, 
                        contentDescription = null, 
                        tint = PopWhite.copy(alpha = 0.4f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        if (isCurrent) {
            Text(
                text = stringResource(R.string.current_level_label),
                color = PopYellow,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
