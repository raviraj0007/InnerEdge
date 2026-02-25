package com.inneredge.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TradeEntity::class], version = 1, exportSchema = false)
abstract class InnerEdgeDatabase : RoomDatabase() {
    abstract fun tradeDao(): TradeDao
}
