package mx.utng.memorymatch.domain.repository

/**
 * Interfaz del repositorio para el mejor tiempo
 */
interface BestTimeRepository {
    suspend fun getBestTime(): Long
    suspend fun saveBestTime(seconds: Long)
}