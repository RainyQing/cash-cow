package bean

import utils.getNumber
import java.math.BigDecimal
import java.math.RoundingMode

private const val srcTimerReg = "(\\d{2})(\\d{2})(\\d{2})"
private const val timePlacement = "$1:$2:$3"

data class Stock(
    var code: String = "",
    var name: String = "",
    var now: String = "",

    /**
     * 当日涨跌
     */
    var change: String = "",
    var changePercent: String = "",
    var time: String = "",
    var buyOne: String = "",
    var sellOne: String = "",

    /**
     * 最高价
     */
    var max: String = "",

    /**
     * 最低价
     */
    var min: String = "",

    var costPrice: String = "--", //成本价

    /**
     * 持仓
     */
    var bonds: String = "--",

    var incomePercent: String = "", //收益率
    var income: String = "", //收益
) {

    fun calculatePrice() {
        if (costPrice.getNumber() != null
            && bonds.getNumber() != null
        ) {
            val now = BigDecimal(now)
            val costPriceDec = BigDecimal(costPrice)
            val incomeDiff = now.add(costPriceDec.negate())
            if (costPriceDec <= BigDecimal.ZERO) {
                incomePercent = "0"
            } else {
                val incomePercentDec = incomeDiff.divide(costPriceDec, 5, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.TEN)
                    .multiply(BigDecimal.TEN)
                    .setScale(3, RoundingMode.HALF_UP)
                incomePercent = incomePercentDec.toString()
            }

            val bondDec = BigDecimal(bonds)
            val incomeDec = incomeDiff.multiply(bondDec)
                .setScale(2, RoundingMode.HALF_UP)
            income = incomeDec.toString()
        }
    }

    fun getValueByColumn(columName: String?, colorful: Boolean = false): String {
        return when (columName) {
            "编码" -> code
            "股票名称" -> //                return colorful ? this.getName() : PinYinUtils.toPinYin(this.getName());
                name

            "当前价" -> now
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

            "最高价" -> max
            "最低价" -> min
            "成本价" -> costPrice
            "持仓" -> bonds
            "收益率" -> if (costPrice.getNumber() == null) "" else "$incomePercent%"
            "收益" -> income
            "更新时间" ->
                try {
                    time.substring(8).replace(srcTimerReg.toRegex(), timePlacement)
                } catch (e: Exception) {
                    "--"
                }

            else -> ""
        }
    }
}