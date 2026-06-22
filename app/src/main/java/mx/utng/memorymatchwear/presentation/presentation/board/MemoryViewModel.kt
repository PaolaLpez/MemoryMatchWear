package mx.utng.memorymatch.presentation.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mx.utng.memorymatch.domain.model.GamePhase
import mx.utng.memorymatch.domain.model.GameState
import mx.utng.memorymatch.domain.usecase.*

class MemoryViewModel(
    // Dependencias inyectadas (NO Context)
    private val shuffleBoard: ShuffleBoardUseCase,
    private val flipCard: FlipCardUseCase,
    private val checkMatch: CheckMatchUseCase,
    private val saveBestTime: SaveBestTimeUseCase,
    private val getBestTime: GetBestTimeUseCase
) : ViewModel() {

    // STATE
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _effects = Channel<GameEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var timerJob: Job? = null

    init {
        startNewGame()
    }

    fun startNewGame() {
        timerJob?.cancel()

        val board = shuffleBoard()
        val bestTime = runBlocking { getBestTime() } // ⚠️ Considera usar suspend en vez de runBlocking

        _state.value = GameState(
            board = board,
            phase = GamePhase.SELECTING_FIRST,
            bestTime = bestTime
        )

        startTimer()
    }

    fun onCardTapped(index: Int) {
        val current = _state.value

        if (current.phase == GamePhase.CHECKING ||
            current.phase == GamePhase.WON
        ) return

        val afterFlip = flipCard(current, index)
        _state.value = afterFlip

        if (afterFlip.phase == GamePhase.CHECKING) {
            evaluate(afterFlip)
        }
    }

    private fun evaluate(state: GameState) {
        viewModelScope.launch {
            delay(800L)

            when (checkMatch(state)) {

                MatchResult.HIT -> {
                    val newState = applyMatch(state)
                    _state.value = newState
                    _effects.send(GameEffect.HapticMatch)

                    if (newState.isComplete) onWin(newState)
                }

                MatchResult.MISS -> {
                    _state.value = flipBack(state)
                    _effects.send(GameEffect.HapticMiss)
                }

                MatchResult.PENDING -> Unit
            }
        }
    }

    private fun applyMatch(state: GameState): GameState {
        val first = state.firstSelected!!
        val second = state.secondSelected!!

        val newBoard = state.board.mapIndexed { i, c ->
            if (i == first || i == second)
                c.copy(isMatched = true)
            else c
        }

        val newMatches = state.matchesFound + 1

        return state.copy(
            board = newBoard,
            matchesFound = newMatches,
            firstSelected = null,
            secondSelected = null,
            phase = if (newMatches == GameState.TOTAL_PAIRS)
                GamePhase.WON else GamePhase.SELECTING_FIRST
        )
    }

    private fun flipBack(state: GameState): GameState {
        val first = state.firstSelected!!
        val second = state.secondSelected!!

        val newBoard = state.board.mapIndexed { i, c ->
            if (i == first || i == second)
                c.copy(isFlipped = false)
            else c
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
                _state.update {
                    it.copy(elapsedSeconds = it.elapsedSeconds + 1)
                }
            }
        }
    }

    private suspend fun onWin(state: GameState) {
        timerJob?.cancel()
        saveBestTime(state.elapsedSeconds)
        _effects.send(GameEffect.HapticVictory)
    }

    override fun onCleared() {
        timerJob?.cancel()
    }
}

sealed class GameEffect {
    object HapticMatch : GameEffect()
    object HapticMiss : GameEffect()
    object HapticVictory : GameEffect()
}