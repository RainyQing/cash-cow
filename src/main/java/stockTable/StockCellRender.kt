package stockTable

import base.Config
import com.intellij.ui.JBColor
import java.awt.Component
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer

class StockCellRender(private val tableModel: StockTableModel) : DefaultTableCellRenderer() {

    private val defaultForegroundColor = JBColor.foreground()

    override fun getTableCellRendererComponent(
        table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
    ): Component {
        val cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
        val stock = tableModel.tableData[row]
        cell.foreground =
            if (Config.colorful) {
                if (column == Config.incomeIdx || column == Config.incomePercentIdx) {
                    if (stock.income.isEmpty()) {
                        defaultForegroundColor
                    } else if (stock.income.startsWith('-')) {
                        JBColor.GREEN
                    } else {
                        JBColor.RED
                    }
                } else if (column == Config.fluctuationIdx || column == Config.fluctuationPercentIdx) {
                    if (stock.change.isEmpty()) {
                        defaultForegroundColor
                    } else if (stock.change.startsWith('-')) {
                        JBColor.GREEN
                    } else {
                        JBColor.RED
                    }
                } else {
                    defaultForegroundColor
                }
            } else {
                defaultForegroundColor
            }
        return cell
    }
}