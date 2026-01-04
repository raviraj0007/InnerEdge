import java.time.LocalDate

data class Trade(
    val id: String,
    val date: LocalDate,

    val instrument: String,      // NIFTY, BTCUSDT, AAPL
    //val marketType: MarketType,  // STOCK, CRYPTO, FNO

    //val direction: TradeDirection, // BUY or SELL

    val entryPrice: Double,
    val exitPrice: Double?,
    val quantity: Int,

    val stopLoss: Double?,
    val target: Double?,

    val riskPercent: Double?,    // % risk on capital

    val pnl: Double?,            // calculated later
    //val tradeStatus: TradeStatus // OPEN, CLOSED
)
