package handler

import base.Urls
import bean.Stock
import utils.getNumber
import utils.httpGet
import java.math.BigDecimal
import java.math.RoundingMode

class TencentStockHandler() {
    suspend fun fetchStockData(stocks: List<Stock>?): List<Stock>? {
        if (stocks.isNullOrEmpty()) {
            return null
        }

        val query = stocks.joinToString(",") { it.code }
        val result = "${Urls.tencentStock}$query".httpGet()

        if (result.isEmpty()) {
            return null
        }

        return parseDate(result, stocks)
    }

    private fun parseDate(result: String, currentStocks: List<Stock>): List<Stock> {
        val lines = result.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
        val stockData = currentStocks.toMutableList()
        for (line in lines) {
            val code = line.substring(line.indexOf("_") + 1, line.indexOf("="))
            currentStocks.find { it.code == code }?.also { stock ->
                val dataStr = line.substring(line.indexOf("=") + 2, line.length - 2)
                val values = dataStr.split("~".toRegex()).dropLastWhile { it.isEmpty() }

                stock.name = values[1]
                stock.now = values[3]
                stock.change = values[31]
                stock.changePercent = values[32]
                stock.time = values[30]
                stock.max = values[33]
                stock.min = values[34]
                stock.buyOne = values[10]
                stock.sellOne = values[20]

                stock.calculatePrice()
            }?.let {
                stockData.add(it)
            }
        }
        return stockData
    }
}
