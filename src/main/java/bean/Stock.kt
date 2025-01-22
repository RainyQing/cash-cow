package bean

import base.Config
import utils.getNumber
import utils.toPinYin
import java.math.BigDecimal
import java.math.RoundingMode

private const val srcTimerReg = "(\\d{2})(\\d{2})(\\d{2})"
private const val timePlacement = "$1:$2:$3"

data class Stock(
    var code: String = "",
    var name: String = "",

    var openPrice: String = "",
    var latestPrice: String = "",

    /**
     * 当日涨跌
     */
    var change: String = "",
    var changePercent: String = "",

    var updateTime: String = "",

    var buyOne: String = "",
    var sellOne: String = "",

    /**
     * 最高价
     */
    var highestPrice: String = "",

    /**
     * 最低价
     */
    var lowestPrice: String = "",

    /**
     * 成本价
     */
    var costPrice: String = "--",

    /**
     * 持仓
     */
    var own: String = "--",

    var incomePercent: String = "",
    var income: String = "",
) {

    fun clearHold(): Boolean {
        if (costPrice.getNumber() == null || own.getNumber() == null) {
            return false
        }

        costPrice = "--"
        own = "--"

        income = ""
        incomePercent = ""
        return true
    }

    /**
     * 方法是计算收益，放在对应字段
     *
     */
    fun calculatePrice() {
        if (costPrice.getNumber() == null || own.getNumber() == null) {
            income = ""
            incomePercent = ""
            return
        }

        val latestPriceValue = BigDecimal(latestPrice).setScale(3, RoundingMode.HALF_UP)
        val costPriceValue = BigDecimal(costPrice).setScale(3, RoundingMode.HALF_UP)
        val incomePriceValue = latestPriceValue.subtract(costPriceValue)

        if (incomePriceValue.compareTo(BigDecimal.ZERO) == 0) {
            income = "0"
            incomePercent = "0"
            return
        }

        val ownValue = BigDecimal(own)
        val incomeTotalValue = incomePriceValue.multiply(ownValue)

        income = incomeTotalValue.setScale(2, RoundingMode.HALF_UP).toString()

        if (costPriceValue.compareTo(BigDecimal.ZERO) == 0) {
            incomePercent = "*"
            return
        }

        val incomePercentValue = incomePriceValue.divide(costPriceValue, 5, RoundingMode.HALF_UP)
            .multiply(BigDecimal("100"))
            .setScale(3, RoundingMode.HALF_UP)

        incomePercent = "$incomePercentValue%"
    }

    fun getValueByColumn(columName: String?): String {
        return when (columName) {
            "编码" -> code
            "股票" -> if (Config.pinyinMode) name.toPinYin() else name
            "开盘" -> openPrice

            "当前价" -> latestPrice
            "买一" -> buyOne
            "卖一" -> sellOne
            "涨跌" -> {
                if (change.startsWith('-')) {
                    change
                } else {
                    "+$change"
                }
            }

            "涨跌幅" -> {
                if (changePercent.startsWith('-')) {
                    changePercent
                } else {
                    "+$changePercent"
                } + "%"
            }

            "最高价" -> highestPrice
            "最低价" -> lowestPrice
            "成本价" -> costPrice
            "持仓" -> own
            "收益率" -> incomePercent
            "收益" -> income
            "更新时间" ->
                try {
                    updateTime.substring(8).replace(srcTimerReg.toRegex(), timePlacement)
                } catch (e: Exception) {
                    "--"
                }

            else -> ""
        }
    }
}