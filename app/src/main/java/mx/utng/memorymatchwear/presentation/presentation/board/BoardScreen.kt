package mx.utng.memorymatch.presentation.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import kotlinx.coroutines.delay
import mx.utng.memorymatch.domain.model.GamePhase

@Composable
fun BoardScreen(
    viewModel: MemoryViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val haptic = LocalHapticFeedback.current
    val scalingLazyListState = rememberScalingLazyListState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                GameEffect.HapticMatch -> {
                    haptic.performHapticFeedback(
                        androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress
                    )
                }

                GameEffect.HapticMiss -> {
                    haptic.performHapticFeedback(
                        androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress
                    )
                }

                GameEffect.HapticVictory -> {
                    repeat(3) {
                        haptic.performHapticFeedback(
                            androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress
                        )
                        delay(150L)
                    }
                }
            }
        }
    }

    if (state.phase == GamePhase.WON) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A1E)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "¡Ganaste!\nTiempo: ${state.elapsedSeconds}s",
                color = Color.White
            )
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A1E))
    ) {

        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = scalingLazyListState,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            item {
                TimeText()
            }

            item {
                Text(
                    text = "${state.elapsedSeconds}s · ${state.moves} mov",
                    color = Color.White
                )
            }

            val rows = state.board.chunked(3)

            items(rows) { rowCards ->

                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    rowCards.forEach { card ->

                        CardItem(
                            card = card,
                            onTap = {
                                val index = state.board.indexOf(card)
                                viewModel.onCardTapped(index)
                            },
                            modifier = Modifier.size(52.dp)
                        )
                    }

                    repeat(3 - rowCards.size) {
                        Box(
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }
            }
        }

        PositionIndicator(
            scalingLazyListState = scalingLazyListState
        )
    }
}