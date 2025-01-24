package base

import bean.Stock
import com.intellij.ide.util.PropertiesComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.awt.Point

val AppScope = CoroutineScope(Dispatchers.Default + Job())
private var followedStocks = listOf<Stock>()

val storage: PropertiesComponent
    get() = PropertiesComponent.getInstance()

private val defaultStockTableHeader = listOf(
    "编码",
    "名称",
    "开盘",
    "涨跌",
    "涨跌幅",
    "最高价",
    "最低价",
//    "振幅",
    "当前价",
    "成本价",
    "持仓",
    "收益率",
    "收益",
    "更新时间"
)

object Config {
    var codeIdx = 0

    var fluctuationIdx = 0
        private set
    var fluctuationPercentIdx = 0
        private set

    var ownIdx = 0
        private set
    var costPriceIdx = 0

    var incomeIdx = 0
        private set
    var incomePercentIdx = 0
        private set

    var stockTableHeader: List<String> = emptyList()
        get() {
            if (field.isEmpty()) {
                val saved = storage.getValue(SettingsKeys.defaultStockTableHeader, "")
                if (saved.isNotEmpty()) {
                    field = saved.split(',').map { it.trim() }
                }

                if (field.size < defaultStockTableHeader.size) {
                    field = defaultStockTableHeader
                }
                saveColumnIndex(field)
            }
            return field
        }
        set(value) {
            field = value
            saveColumnIndex(value)
            storage.setValue(SettingsKeys.defaultStockTableHeader, value.joinToString(","))
        }

    private fun saveColumnIndex(value: List<String>) {
        codeIdx = value.indexOf("编码")

        fluctuationIdx = value.indexOf("涨跌")
        fluctuationPercentIdx = value.indexOf("涨跌幅")

        ownIdx = value.indexOf("持仓")
        costPriceIdx = value.indexOf("成本价")

        incomeIdx = value.indexOf("收益")
        incomePercentIdx = value.indexOf("收益率")
    }

    private var _pinyinMode: Boolean? = null
    var pinyinMode: Boolean
        get() {
            if (_pinyinMode == null) {
                _pinyinMode = storage.getBoolean(SettingsKeys.pinyinMode, false)
            }
            return _pinyinMode!!
        }
        set(value) {
            _pinyinMode = value
            storage.setValue(SettingsKeys.pinyinMode, value)
        }

    private var _colorful: Boolean? = null
    var colorful: Boolean
        get() {
            if (_colorful == null) {
                _colorful = storage.getBoolean(SettingsKeys.colorful, false)
            }
            return _colorful!!
        }
        set(value) {
            _colorful = value
            storage.setValue(SettingsKeys.colorful, value)
        }
}

fun String.readAsStock(): Stock {
    if (isEmpty()) {
        return Stock()
    }

    return if (contains(',')) {
        val array = this.split(',')
        if (array.size > 2) {
            Stock(
                code = array[0],
                costPrice = array[1],
                own = array[2]
            )
        } else if (array.isNotEmpty()) {
            Stock(code = array[0])
        } else Stock()
    } else {
        Stock(this)
    }
}

fun PropertiesComponent.saveHoldingDialogLocation(location: Point) {
    setValue(SettingsKeys.holdingDialogLocation, "${location.x},${location.y}")
}

fun PropertiesComponent.getHoldingDialogLocation(): Point? {
    val location = getValue(SettingsKeys.holdingDialogLocation)
    if (location.isNullOrEmpty()) {
        return null
    }

    val array = location.split(',')
    if (array.size != 2) {
        return null
    }

    return Point(array[0].toInt(), array[1].toInt())
}

fun PropertiesComponent.saveSearchDialogLocation(location: Point) {
    setValue(SettingsKeys.searchDialogLocation, "${location.x},${location.y}")
}

fun PropertiesComponent.getSearchDialogLocation(): Point? {
    val location = getValue(SettingsKeys.searchDialogLocation)
    if (location.isNullOrEmpty()) {
        return null
    }

    val array = location.split(',')
    if (array.size != 2) {
        return null
    }

    return Point(array[0].toInt(), array[1].toInt())
}

/**
 * 原项目按 ‘;’ 分割股, 股内按 ‘成本,持仓’配置(可选)
 */
fun PropertiesComponent.getStockConfig(): List<Stock> {
    if (followedStocks.isNotEmpty()) {
        return followedStocks
    }

    val configRaw = getValue(SettingsKeys.stockConfig)
    if (configRaw.isNullOrEmpty() || configRaw.isBlank()) {
        return emptyList()
    }
    val configs = getValue(SettingsKeys.stockConfig)
        ?.split(";")
        ?.filter { it.isNotEmpty() }
        ?.map { it.readAsStock() }
        ?.filter { it.code.isNotEmpty() }
    return (configs ?: emptyList()).also {
        followedStocks = it
    }
}

fun PropertiesComponent.updateStockConfig(configs: List<Stock>) {
    followedStocks = configs
    if (configs.isEmpty()) {
        setValue(SettingsKeys.stockConfig, "")
        return
    }

    val configRaw = configs.joinToString(";") { "${it.code},${it.costPrice},${it.own}" }
    setValue(SettingsKeys.stockConfig, configRaw)
}