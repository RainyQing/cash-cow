package stockTable

import base.Config
import base.updateStockConfig
import bean.Stock
import com.intellij.ide.util.PropertiesComponent
import utils.beautySmallNum
import utils.toPinYin
import javax.swing.table.AbstractTableModel

class StockTableModel(val tableData: MutableList<Stock> = mutableListOf()) : AbstractTableModel() {
    private val columnNames = Config.stockTableHeader

    override fun getRowCount(): Int {
        return tableData.size
    }

    override fun getColumnCount(): Int {
        return columnNames.size
    }

    override fun getColumnName(columnIndex: Int): String {
        return if (Config.pinyinMode) columnNames[columnIndex].toPinYin() else columnNames[columnIndex]
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        return tableData[rowIndex].getValueByColumn(columnNames[columnIndex])
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return columnIndex == Config.ownIdx
                || columnIndex == Config.costPriceIdx
    }

    override fun setValueAt(newValue: Any, rowIndex: Int, columnIndex: Int) {
        if (rowIndex !in tableData.indices) {
            return
        }

        val targetStock = tableData[rowIndex]

        if (columnIndex == Config.ownIdx) {
            targetStock.own = newValue.toString().beautySmallNum(3)
        } else if (columnIndex == Config.costPriceIdx) {
            targetStock.costPrice = newValue.toString().beautySmallNum(3)
        }

        targetStock.calculatePrice()
        fireTableRowsUpdated(rowIndex, rowIndex)

        val storage = PropertiesComponent.getInstance()
        storage.updateStockConfig(tableData)
    }

    fun addStock(stock: Stock) {
        tableData.add(0, stock)
        fireTableRowsInserted(tableData.size - 1, tableData.size - 1)
    }

    fun updateStock(stock: Stock) {
        val rowIndex = tableData.indexOfFirst { it.code == stock.code }
        if (rowIndex != -1) {
            tableData[rowIndex] = stock
            fireTableRowsUpdated(rowIndex, rowIndex)
        }
    }

    fun resetStock(stocks: List<Stock>?) {
        tableData.clear()
        if (!stocks.isNullOrEmpty()) {
            tableData.addAll(stocks)
        }
        fireTableDataChanged()
    }

    fun removeStocks(stockCodes: List<String>) {
        stockCodes.forEach { stockCode ->
            val rowIndex = tableData.indexOfFirst { it.code == stockCode }
            if (rowIndex != -1) {
                tableData.removeAt(rowIndex)
                fireTableRowsDeleted(rowIndex, rowIndex)
            }
        }
    }
}