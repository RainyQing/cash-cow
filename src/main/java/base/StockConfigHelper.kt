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

private const val defaultStockTableHeader =
    "编码,股票名称,涨跌,涨跌幅,最高价,最低价,当前价,成本价,持仓,收益率,收益,更新时间"

object Config {
    var fluctuationIdx = 2
        private set
    var fluctuationPercentIdx = 3
        private set

    var incomeIdx = 10
        private set
    var incomePercentIdx = 9
        private set

    var stockTableHeader: List<String> = emptyList()
        get() {
            if (field.isEmpty()) {
                field = (storage.getValue(SettingsKeys.defaultStockTableHeader) ?: defaultStockTableHeader).split(',')
                    .map { it.trim() }
            }
            return field
        }
        set(value) {
            field = value
            fluctuationIdx = value.indexOf("涨跌")
            fluctuationPercentIdx = value.indexOf("涨跌幅")
            incomeIdx = value.indexOf("收益")
            incomePercentIdx = value.indexOf("收益率")
            storage.setValue(SettingsKeys.defaultStockTableHeader, value.joinToString(","))
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
                bonds = array[2]
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

    val configRaw = configs.joinToString(";") { "${it.code},${it.costPrice},${it.bonds}" }
    setValue(SettingsKeys.stockConfig, configRaw)
}