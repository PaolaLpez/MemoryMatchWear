package mx.utng.memorymatch.presentation.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mx.utng.memorymatch.domain.model.GamePhase
import mx.utng.memorymatch.domain.model.GameState
import mx.utng.memorymatch.domain.usecase.CheckMatchUseCase
import mx.utng.memorymatch.domain.usecase.FlipCardUseCase
import mx.utng.memorymatch.domain.usecase.MatchResult
import mx.utng.memorymatch.domain.usecase.ShuffleBoardUseCase
import mx.utng.memorymatch.domain.usecase.GetBestTimeUseCase
import mx.utng.memorymatch.domain.usecase.SaveBestTimeUseCase

/**
 * ViewModel que orquesta toda la lógica de presentación del juego
 */
class MemoryViewModel(
    private val shuffleBoard: ShuffleBoardUseCase,
    private val flipCard: FlipCardUseCase,
    private val checkMatch: CheckMatchUseCase,
    private val saveBestTime: SaveBestTimeUseCase,
    private val getBestTime: GetBestTimeUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    // Canal de efectos de una sola vez (haptics, sonido)
    private val _effects = Channel<GameEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var timerJob: Job? = null

    init {
        startNewGame()
    }

    fun startNewGame() {
        timerJob?.cancel()
        val board = shuffleBoard()
        val bestTime = runBlocking { getBestTime() }
        _state.value = GameState(
            board = board,
            phase = GamePhase.SELECTING_FIRST,
            bestTime = bestTime
        )
        startTimer()
    }

    fun onCardTapped(cardIndex: Int) {
        val current = _state.value
        if (current.phase == GamePhase.CHECKING || current.phase == GamePhase.WON) return

        val afterFlip = flipCard(current, cardIndex)
        _state.value = afterFlip

        // Solo evaluar si ya hay 2 tarjetas
        if (afterFlip.phase == GamePhase.CHECKING) {
            evaluateMatch(afterFlip)
        }
    }

    private fun evaluateMatch(state: GameState) {
        viewModelScope.launch {
            delay(800L)  // pausa para que el jugador vea las tarjetas
            when (checkMatch(state)) {
                MatchResult.HIT -> {
                    val newState = applyMatch(state)
                    _state.value = newState
                    _effects.send(GameEffect.HapticMatch)
                    if (newState.isComplete) onGameWon(newState)
                }
                MatchResult.MISS -> {
                    _state.value = flipBothBack(state)
                    _effects.send(GameEffect.HapticMiss)
                }
                MatchResult.PENDING -> Unit
            }
        }
    }

    private fun applyMatch(state: GameState): GameState {
        val first = state.firstSelected!!
        val second = state.secondSelected!!
        val newBoard = state.board.mapIndexed { i, card ->
            if (i == first || i == second) card.copy(isMatched = true) else card
        }
        return state.copy(
            board = newBoard,
            matchesFound = state.matchesFound + 1,
            firstSelected = null,
            secondSelected = null,
            phase = if (state.matchesFound + 1 == GameState.TOTAL_PAIRS)
                GamePhase.WON else GamePhase.SELECTING_FIRST
        )
    }

    private fun flipBothBack(state: GameState): GameState {
        val first = state.firstSelected!!
        val second = state.secondSelected!!
        val newBoard = state.board.mapIndexed { i, c ->
            if (i == first || i == second) c.copy(isFlipped = false) else c
        }
        return state.copy(
            board = newBoard,
            firstSelected = null,
            secondSelected = null,
            phase = GamePhase.SELECTING_FIRST
        )
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _state.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    private suspend fun onGameWon(state: GameState) {
        timerJob?.cancel()
        saveBestTime(state.elapsedSeconds)
        _effects.send(GameEffect.HapticVictory)
    }

    override fun onCleared() {
        timerJob?.cancel()
    }
}

/**
 * Efectos de una sola vez que se emiten desde el ViewModel
 */
sealed class GameEffect {
    object HapticMatch : GameEffect()
    object HapticMiss : GameEffect()
    object HapticVictory : GameEffect()
}