package mx.utng.memorymatch.presentation.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.utng.memorymatch.data.datasource.BestTimeDataSource
import mx.utng.memorymatch.data.repository.BestTimeRepositoryImpl
import mx.utng.memorymatch.domain.usecase.*
import mx.utng.memorymatch.presentation.board.MemoryViewModel

class MemoryViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MemoryViewModel::class.java)) {
            // Data Layer
            val dataSource = BestTimeDataSource(context)
            val repository = BestTimeRepositoryImpl(dataSource)

            // Domain Layer - Use Cases
            val shuffleBoard = ShuffleBoardUseCase()
            val flipCard = FlipCardUseCase()
            val checkMatch = CheckMatchUseCase()
            val saveBestTime = SaveBestTimeUseCase(repository)
            val getBestTime = GetBestTimeUseCase(repository)

            // ViewModel con dependencias inyectadas
            return MemoryViewModel(
                shuffleBoard = shuffleBoard,
                flipCard = flipCard,
                checkMatch = checkMatch,
                saveBestTime = saveBestTime,
                getBestTime = getBestTime
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}