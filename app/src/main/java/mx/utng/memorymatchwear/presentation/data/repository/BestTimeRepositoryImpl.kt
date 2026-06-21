package mx.utng.memorymatch.data.repository

import mx.utng.memorymatch.data.datasource.BestTimeDataSource
import mx.utng.memorymatch.domain.repository.BestTimeRepository

/**
 * Implementación del repositorio para el mejor tiempo
 */
class BestTimeRepositoryImpl(
    private val dataSource: BestTimeDataSource
) : BestTimeRepository {
    override suspend fun getBestTime(): Long = dataSource.getBestTime()
    override suspend fun saveBestTime(seconds: Long) = dataSource.saveBestTime(seconds)
}