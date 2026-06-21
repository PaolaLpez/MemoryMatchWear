package mx.utng.memorymatch.domain.usecase

import mx.utng.memorymatch.domain.model.Card
import mx.utng.memorymatch.domain.model.CardSymbol

/**
 * Crea el tablero mezclado con 12 tarjetas (2 de cada símbolo)
 */
class ShuffleBoardUseCase {
    /**
     * Crea 12 tarjetas (2 de cada símbolo) mezcladas aleatoriamente.
     */
    operator fun invoke(): List<Card> =
        CardSymbol.values()
            .flatMap { symbol -> listOf(symbol, symbol) } // duplicar cada símbolo
            .shuffled()                                   // mezclar
            .mapIndexed { index, symbol ->                // asignar id
                Card(id = index, symbol = symbol)
            }
}