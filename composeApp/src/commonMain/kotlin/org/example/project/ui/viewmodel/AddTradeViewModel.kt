package org.example.project.ui.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.project.domain.model.MarketType
import org.example.project.domain.model.Trade
import org.example.project.domain.model.TradeDirection
import org.example.project.domain.model.TradeStatus
import org.example.project.domain.repository.TradeRepository
import kotlin.random.Random

class AddTradeViewModel(
    private val repository: TradeRepository
) {
    private val _tradeId = MutableStateFlow<String?>(null)
    val tradeId: StateFlow<String?> = _tradeId.asStateFlow()

    private val _instrument = MutableStateFlow("")
    val instrument: StateFlow<String> = _instrument.asStateFlow()

    private val _entryPrice = MutableStateFlow("")
    val entryPrice: StateFlow<String> = _entryPrice.asStateFlow()

    private val _stopLoss = MutableStateFlow("")
    val stopLoss: StateFlow<String> = _stopLoss.asStateFlow()

    private val _target = MutableStateFlow("")
    val target: StateFlow<String> = _target.asStateFlow()

    private val _strategy = MutableStateFlow("")
    val strategy: StateFlow<String> = _strategy.asStateFlow()

    private val _emotion = MutableStateFlow("")
    val emotion: StateFlow<String> = _emotion.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _direction = MutableStateFlow(TradeDirection.BUY)
    val direction: StateFlow<TradeDirection> = _direction.asStateFlow()

    suspend fun loadTrade(tradeId: String?) {
        _tradeId.value = tradeId
        if (tradeId == null) {
            _instrument.value = ""
            _entryPrice.value = ""
            _stopLoss.value = ""
            _target.value = ""
            _strategy.value = ""
            _emotion.value = ""
            _notes.value = ""
            _direction.value = TradeDirection.BUY
            return
        }
        val trade = repository.getTradeById(tradeId) ?: return
        _instrument.value = trade.instrument
        _entryPrice.value = trade.entryPrice.toString()
        _stopLoss.value = trade.stopLoss?.toString().orEmpty()
        _target.value = trade.target?.toString().orEmpty()
        _strategy.value = trade.strategy.orEmpty()
        _emotion.value = trade.emotion.orEmpty()
        _notes.value = trade.notes.orEmpty()
        _direction.value = trade.direction
    }

    fun onInstrumentChange(value: String) { _instrument.value = value }
    fun onEntryPriceChange(value: String) { _entryPrice.value = value }
    fun onStopLossChange(value: String) { _stopLoss.value = value }
    fun onTargetChange(value: String) { _target.value = value }
    fun onStrategyChange(value: String) { _strategy.value = value }
    fun onEmotionChange(value: String) { _emotion.value = value }
    fun onNotesChange(value: String) { _notes.value = value }
    fun onDirectionChange(value: TradeDirection) { _direction.value = value }

    fun canSave(): Boolean =
        instrument.value.isNotBlank() && entryPrice.value.toDoubleOrNull() != null

    suspend fun saveTrade() {
        val nowDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val parsedEntry = entryPrice.value.toDoubleOrNull() ?: return

        val existingTrade = tradeId.value?.let { repository.getTradeById(it) }
        val tradeToSave = Trade(
            id = existingTrade?.id ?: Random.nextLong().toString(),
            date = existingTrade?.date ?: nowDate,
            instrument = instrument.value,
            marketType = existingTrade?.marketType ?: MarketType.FNO,
            direction = direction.value,
            entryPrice = parsedEntry,
            exitPrice = existingTrade?.exitPrice,
            quantity = existingTrade?.quantity ?: 1,
            stopLoss = stopLoss.value.toDoubleOrNull(),
            target = target.value.toDoubleOrNull(),
            riskPercent = existingTrade?.riskPercent,
            pnl = existingTrade?.pnl,
            status = existingTrade?.status ?: TradeStatus.OPEN,
            strategy = strategy.value.ifBlank { null },
            mistakes = existingTrade?.mistakes ?: emptyList(),
            emotion = emotion.value.ifBlank { null },
            notes = notes.value.ifBlank { null }
        )

        if (existingTrade == null) {
            repository.insertTrade(tradeToSave)
        } else {
            repository.updateTrade(tradeToSave)
        }
    }
}
