package popMenu

import base.AppScope
import base.getStockConfig
import base.updateStockConfig
import bean.Stock
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.ui.awt.RelativePoint
import edit.showEditBondsDialog
import kotlinx.coroutines.launch
import java.awt.Point

class StocksMenu(
    private val deleteRequest: (List<Stock>) -> Unit,
    private val updateRequest: (Stock) -> Unit,
    private val selectedStocks: List<Stock>
) {

    @JvmOverloads
    fun showAsRightButton(
        location: Point,
        title: String = ""
    ) {

        val buttons = mutableListOf<Menu>(Menu.Delete)
        if (selectedStocks.size == 1) {
            buttons.add(Menu.Edit)
            buttons.add(Menu.Clear)
        }

        JBPopupFactory.getInstance().createListPopup(object : BaseListPopupStep<Menu>(title, buttons) {
            override fun onChosen(selectedValue: Menu?, finalChoice: Boolean): PopupStep<*>? {
                if (finalChoice) {
                    when (selectedValue) {
                        Menu.Delete -> {
                            deleteStock()
                        }

                        Menu.Edit -> {
                            editStock(location)
                        }

                        Menu.Clear -> {
                            clearHold()
                        }

                        else -> {}
                    }
                }

                return super.onChosen(selectedValue, finalChoice)
            }
        }).show(RelativePoint.fromScreen(location))
    }

    private fun clearHold() {
        val targetStock = selectedStocks.first()
        if (targetStock.clearHold()) {
            val storage = PropertiesComponent.getInstance()
            val stocks = storage.getStockConfig().toMutableList()

            val idx = stocks.indexOfFirst { it.code == targetStock.code }
            if (idx != -1) {
                stocks[idx] = targetStock
                storage.updateStockConfig(stocks)
                updateRequest.invoke(targetStock)
            }
        }
    }

    private fun deleteStock() {
        val storage = PropertiesComponent.getInstance()
        val stocks = storage.getStockConfig().toMutableList()

        stocks.removeAll { stock -> selectedStocks.any { it.code == stock.code } }
        storage.updateStockConfig(stocks)
        deleteRequest.invoke(selectedStocks)
    }

    private fun editStock(location: Point) {
        val targetStock = selectedStocks.firstOrNull() ?: return

        AppScope.launch {
            val stock = PropertiesComponent.getInstance().getStockConfig().find { it.code == targetStock.code }
            stock?.showEditBondsDialog(updateRequest, location)
        }
    }
}

sealed class Menu {
    data object Delete : Menu()
    data object Edit : Menu()
    data object Clear : Menu()
}