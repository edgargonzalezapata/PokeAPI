package com.sibb.pokepi.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sibb.pokepi.R

@Composable
fun PokeBallLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Int = 50,
    showText: Boolean = true,
    text: String = "Cargando..."
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pokeball_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(size.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.pokemon),
                contentDescription = "Cargando Pokemon",
                modifier = Modifier
                    .size((size - 5).dp)
                    .rotate(rotation)
                    .clip(CircleShape),
                contentScale = ContentScale.Fit
            )
        }
        
        if (showText) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun FullScreenLoading(
    modifier: Modifier = Modifier,
    text: String = "Cargando..."
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        PokeBallLoadingIndicator(
            size = 80,
            text = text
        )
    }
}

@Composable
fun CenterLoading(
    modifier: Modifier = Modifier,
    text: String = "Cargando..."
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        PokeBallLoadingIndicator(
            size = 60,
            text = text
        )
    }
}