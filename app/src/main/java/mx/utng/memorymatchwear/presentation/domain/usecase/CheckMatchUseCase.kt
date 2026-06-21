package mx.utng.memorymatch.domain.usecase

import mx.utng.memorymatch.domain.model.GameState

/**
 * Verifica si las dos tarjetas seleccionadas hacen par
 */
class CheckMatchUseCase {
    /**
     * Evalúa si las dos tarjetas seleccionadas hacen par.
     * Retorna MatchResult: HIT (par correcto) o MISS (no coinciden).
     */
    operator fun invoke(state: GameState): MatchResult {
        val first = state.firstSelected ?: return MatchResult.PENDING
        val second = state.secondSelected ?: return MatchResult.PENDING

        val cardA = state.board[first]
        val cardB = state.board[second]

        return if (cardA.symbol == cardB.symbol) MatchResult.HIT
        else MatchResult.MISS
    }
}

enum class MatchResult { HIT, MISS, PENDING }