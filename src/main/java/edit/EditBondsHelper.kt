package edit

import base.getHoldingDialogLocation
import base.getStockConfig
import base.saveHoldingDialogLocation
import base.updateStockConfig
import bean.Stock
import com.intellij.ide.util.PropertiesComponent
import utils.beautySmallNum
import utils.getNumber
import utils.isNumber
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.GridLayout
import java.awt.Point
import java.awt.event.ComponentEvent
import java.awt.event.FocusEvent
import javax.swing.*

fun Stock.showEditBondsDialog(
    updateRequest: (Stock) -> Unit,
    presetLocation: Point
) {
    val targetStock = this
    val dialog = JDialog(JFrame(), targetStock.name, true)

    val frame = JPanel()
    frame.layout = GridLayout(3, 1)

    // 创建持仓输入框
    val holdingPanel = JPanel(FlowLayout(FlowLayout.LEFT))
    val holdingTextField = JTextField(if (targetStock.own.isNumber()) targetStock.own else null, 10)
    holdingPanel.add(JLabel("持仓:"))
    holdingPanel.add(holdingTextField)
    frame.add(holdingPanel)

    // 创建成本输入框
    val costPanel = JPanel(FlowLayout(FlowLayout.LEFT))
    val costTextField = JTextField(if (targetStock.costPrice.isNumber()) targetStock.costPrice else null, 10)
    costPanel.add(JLabel("成本:"))
    costPanel.add(costTextField)
    frame.add(costPanel)

    // 创建按钮
    val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER))
    val button = JButton("确定").apply {
        addActionListener {
            val holding = holdingTextField.text.trim()
            val cost = costTextField.text.trim()

            if (holding.isNotEmpty() && cost.isNotEmpty()) {
                val storage = PropertiesComponent.getInstance()
                val followedStocks = storage.getStockConfig().toMutableList()
                val idx = followedStocks.indexOfFirst { it.code == targetStock.code }
                if (idx != -1) {
                    val newStock = targetStock.copy(
                        costPrice = cost.beautySmallNum(3),
                        own = ((holding.getNumber()?.toInt() ?: 0) / 100 * 100).toString()
                    )
                    newStock.calculatePrice()

                    followedStocks[idx] = newStock
                    storage.updateStockConfig(followedStocks)
                    updateRequest(newStock)
                }
                dialog.isVisible = false
            }
        }
    }
    buttonPanel.add(button)
    frame.add(buttonPanel)

    dialog.apply {
        add(frame, BorderLayout.CENTER)
        pack()

        addFocusListener(object : java.awt.event.FocusAdapter() {
            override fun focusLost(event: FocusEvent?) {
                super.focusLost(event)
                dialog.isVisible = false
            }
        })

        addComponentListener(object : java.awt.event.ComponentAdapter() {
            override fun componentHidden(p0: ComponentEvent?) {
                super.componentHidden(p0)
                PropertiesComponent.getInstance().saveHoldingDialogLocation(location)
            }
        })

        setLocationRelativeTo(null)
        defaultCloseOperation = JDialog.HIDE_ON_CLOSE
        location = PropertiesComponent.getInstance().getHoldingDialogLocation() ?: presetLocation
//        location = presetLocation
        isVisible = true
    }
}