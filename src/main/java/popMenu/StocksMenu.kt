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
    private val deleteRequest: (Stock) -> Unit,
    private val updateRequest: (Stock) -> Unit,
    private val selectedStock: Stock
) {

    @JvmOverloads
    fun showAsRightButton(
        location: Point,
        title: String = "",
        buttons: List<Menu> = listOf(
            Menu.Delete,
            Menu.Edit
        )
    ) {
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

                        else -> {}
                    }
                }

                return super.onChosen(selectedValue, finalChoice)
            }
        }).show(RelativePoint.fromScreen(location))
    }

    private fun deleteStock() {
        val storage = PropertiesComponent.getInstance()
        val stocks = storage.getStockConfig().toMutableList()
        if (stocks.removeIf { it.code == selectedStock.code }) {
            storage.updateStockConfig(stocks)
            deleteRequest.invoke(selectedStock)
        }
    }

    private fun editStock(location: Point) {
        AppScope.launch {
            val stock = PropertiesComponent.getInstance().getStockConfig().find { it.code == selectedStock.code }
            stock?.showEditBondsDialog(updateRequest, location)
        }
    }
}

sealed class Menu {
    data object Delete : Menu()
    data object Edit : Menu()
}