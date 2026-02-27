package com.inneredge.di

import android.content.Context
import androidx.room.Room
import com.inneredge.data.local.InnerEdgeDatabase
import com.inneredge.data.local.TradeDao
import com.inneredge.data.repository.RoomTradeRepository
import com.inneredge.domain.repository.TradeRepository
import com.inneredge.domain.usecase.GetTradesUseCase
import com.inneredge.domain.usecase.InsertTradeUseCase
import com.inneredge.domain.usecase.UpdateTradeUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): InnerEdgeDatabase =
        Room.databaseBuilder(context, InnerEdgeDatabase::class.java, "trade.db").build()

    @Provides
    fun provideTradeDao(database: InnerEdgeDatabase): TradeDao = database.tradeDao()

    @Provides
    @Singleton
    fun provideTradeRepository(tradeDao: TradeDao): TradeRepository = RoomTradeRepository(tradeDao)

    @Provides
    fun provideGetTradesUseCase(repository: TradeRepository): GetTradesUseCase = GetTradesUseCase(repository)

    @Provides
    fun provideInsertTradeUseCase(repository: TradeRepository): InsertTradeUseCase = InsertTradeUseCase(repository)

    @Provides
    fun provideUpdateTradeUseCase(repository: TradeRepository): UpdateTradeUseCase = UpdateTradeUseCase(repository)
}
