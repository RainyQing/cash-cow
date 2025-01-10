package utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat

val numberRegex = """-?\d+(\.\d+)?""".toRegex()

private val digits3NumberFormatter = NumberFormat.getNumberInstance().apply {
    maximumFractionDigits = 3
    roundingMode = RoundingMode.DOWN
}

private val digits2NumberFormatter = NumberFormat.getNumberInstance().apply {
    maximumFractionDigits = 2
    roundingMode = RoundingMode.DOWN
}

fun String.isNumber(): Boolean {
    if (isEmpty() || isBlank()) {
        return false
    }
    return numberRegex.find(this) != null
}

/**
 * first group number
 */
fun String.getNumber(): Double? {
    val matchResult = numberRegex.find(this)
    return if (matchResult != null) {
        matchResult.groupValues[0].toDouble()
    } else {
        null
    }
}

fun String.beautySmallNum(digits: Int = 2) = (getNumber() ?: 0).beautySmallNum(digits)

fun Number.beautySmallNum(digits: Int = 2): String =
    when (digits) {
        2 -> {
            digits2NumberFormatter
        }

        3 -> {
            digits3NumberFormatter
        }

        else -> {
            NumberFormat.getNumberInstance().apply {
                maximumFractionDigits = digits
                roundingMode = RoundingMode.DOWN
            }
        }
    }.format(BigDecimal(toString()))