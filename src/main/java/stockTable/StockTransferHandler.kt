package stockTable

import base.Config
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.awt.dnd.DnDConstants
import javax.swing.JComponent
import javax.swing.JTable
import javax.swing.TransferHandler

/**
 * 用它们的编码来传输
 */
class StockTransferHandler(
    private val stockModel: StockTableModel
) : TransferHandler() {
    override fun getSourceActions(c: JComponent): Int {
        return DnDConstants.ACTION_MOVE
    }

    public override fun createTransferable(comp: JComponent?): Transferable {
        val table = comp as JTable
        val row = table.selectedRow

        val dragCode = stockModel.getValueAt(row, Config.codeIdx).toString()
        return StringSelection(dragCode)
    }

    override fun canImport(info: TransferSupport): Boolean {
        return info.isDataFlavorSupported(DataFlavor.stringFlavor)
    }

    override fun importData(support: TransferSupport): Boolean {
        if (!support.isDrop) {
            return false
        }

        if (!canImport(support)) {
            return false
        }

        val hoverLocation = support.dropLocation

        if (hoverLocation is JTable.DropLocation) {
            val targetRow = hoverLocation.row
            val targetStockCode = stockModel.getValueAt(targetRow, Config.codeIdx).toString()
            try {
                val dragCode = support.transferable.getTransferData(DataFlavor.stringFlavor).toString()
                if (dragCode == targetStockCode) {
                    return false
                }

                stockModel.exchangeStockRow(targetStockCode, dragCode)
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
            return true
        } else {
            return false
        }
    }
}
