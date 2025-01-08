package stockTable

import base.Config
import bean.Stock
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
        return columnNames[columnIndex]
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        return tableData[rowIndex].getValueByColumn(columnNames[columnIndex])
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
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