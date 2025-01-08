package stockTable

import com.intellij.ui.JBColor
import utils.getNumber
import java.awt.Component
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer

class StockRowRender(private val tableModel: StockTableModel) : DefaultTableCellRenderer() {

    private val unHoldStockColor = JBColor.LIGHT_GRAY.brighter()

    override fun getTableCellRendererComponent(
        table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
    ): Component {
        val cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
        if (column == 0) {
            val stock = tableModel.tableData[row]
            if (stock.bonds.getNumber() != null) {
                cell.foreground = JBColor.WHITE
            } else {
                cell.foreground = unHoldStockColor
            }
        }

        return cell
    }
}