package utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat

val numberRegex = """-?\d+(\.\d+)?""".toRegex()

private val defaultNumberFormatter = NumberFormat.getNumberInstance().apply {
    maximumFractionDigits = 3
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

fun Number.beautySmallNum(): String = defaultNumberFormatter.format(BigDecimal(toString()))