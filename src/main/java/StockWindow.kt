import base.AppScope
import base.Config
import base.getStockConfig
import bean.Stock
import com.intellij.icons.AllIcons
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.AnActionButton
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import edit.showEditBondsDialog
import handler.TencentStockHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import popMenu.StocksMenu
import search.SearchHelper
import stockTable.StockCellRender
import stockTable.StockTableModel
import java.awt.BorderLayout
import java.awt.KeyboardFocusManager
import java.awt.Point
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities

class StockWindow : ToolWindowFactory {

    /**
     * xml 绑定
     */
    lateinit var mPanel: JPanel

    private lateinit var table: JBTable
    private lateinit var refreshTimeLabel: JLabel

    private val scope = AppScope
    private var fetcherJob: Job? = null

    private val searchDialog by lazy {
        SearchHelper {
            stockTableModel.addStock(it)
            startRefreshStockData()
        }
    }

    private var _stockHandler: TencentStockHandler? = null
    private val stockHandler: TencentStockHandler
        get() {
            if (_stockHandler == null) {
                _stockHandler = TencentStockHandler()
            }

            return _stockHandler!!
        }

    private val stockTableModel by lazy {
        StockTableModel()
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()
        val contentStock = contentFactory.createContent(mPanel, "Stock", false)

        val contentManager = toolWindow.contentManager
        contentManager.addContent(contentStock)
    }

    override fun shouldBeAvailable(project: Project): Boolean {
        return true
    }

    override val isDoNotActivateOnStart: Boolean
        get() = true

    override fun init(toolWindow: ToolWindow) {
        if (_stockHandler != null) {
            startRefreshStockData()
            return
        }

        table = JBTable(stockTableModel).apply {
            setDefaultRenderer(Any::class.java, StockCellRender(stockTableModel))
        }
        refreshTimeLabel = JLabel()
        refreshTimeLabel.toolTipText = "刷新时间"
        refreshTimeLabel.border = JBUI.Borders.emptyRight(5)

        val refreshAction: AnActionButton = object : AnActionButton("停止刷新", AllIcons.Actions.Pause) {
            override fun actionPerformed(e: AnActionEvent) {
                stopRefreshStockData()
                this.isEnabled = false
            }
        }

        val toolbarDecorator = ToolbarDecorator.createDecorator(table)
            .addExtraAction(object : AnActionButton("启动刷新", AllIcons.Actions.Refresh) {
                override fun actionPerformed(e: AnActionEvent) {
                    startRefreshStockData()
                    refreshAction.isEnabled = true
                }
            })
            .addExtraAction(refreshAction)
            .setToolbarPosition(ActionToolbarPosition.TOP)

        val toolPanel = toolbarDecorator.createPanel()
        toolbarDecorator.actionsPanel.add(refreshTimeLabel, BorderLayout.EAST)
        toolPanel.border = JBUI.Borders.empty()
        mPanel.add(toolPanel, BorderLayout.CENTER)

        table.tableHeader.addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                Config.stockTableHeader = (0 until table.columnCount).map { columnIdx ->
                    table.getColumnName(columnIdx)
                }
            }
        })

        table.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                val clickLocation = Point(e.xOnScreen, e.yOnScreen)
                if (e.clickCount == 2) {
                    val selectedStock = stockTableModel.tableData[table.selectedRow]
                    PropertiesComponent.getInstance()
                        .getStockConfig()
                        .find { it.code == selectedStock.code }
                        ?.showEditBondsDialog({ stockTableModel.updateStock(it) }, clickLocation)
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    val selectedStocks = table.selectedRows.map { stockTableModel.tableData[it] }
                    StocksMenu(
                        deleteRequest = {
                            stockTableModel.removeStocks(it.map { stock -> stock.code })
                        },
                        updateRequest = {
                            stockTableModel.updateStock(it)
                        },
                        selectedStocks
                    ).showAsRightButton(clickLocation)

                    return
                }
            }
        })

        bindSearchDialog()
        installFollowedStocks()
    }

    private fun refreshTable(stocks: List<Stock>) {
        stockTableModel.resetStock(stocks)
        startRefreshStockData()
    }

    // 全局快捷键监听
    private fun bindSearchDialog() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher { e: KeyEvent ->
            if (e.id == KeyEvent.KEY_PRESSED && e.keyCode == KeyEvent.VK_F7) {
                searchDialog.show()
                return@addKeyEventDispatcher true
            }
            false
        }
    }

    private fun installFollowedStocks() {
        val presetStocks = PropertiesComponent.getInstance().getStockConfig()
        refreshTable(presetStocks)
    }

    private fun startRefreshStockData() {
        if (stockTableModel.tableData.isEmpty()) {
            stopRefreshStockData()
        } else {
            fetcherJob?.cancel()
            fetcherJob = scope.launch {
                while (isActive && stockTableModel.tableData.isNotEmpty()) {
                    doRefreshStockData(stockTableModel.tableData)
                    if (LocalTime.now().isAfter(LocalTime.of(15, 0))) {
                        return@launch
                    }
                    delay(10_000)
                }
            }
        }
    }

    private suspend fun doRefreshStockData(stocks: List<Stock>) {
        val stockData = stockHandler.fetchStockData(stocks)
        stockData?.forEach { stock -> stockTableModel.updateStock(stock) }
        refreshTimeLabel.text =
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    }

    private fun stopRefreshStockData() {
        fetcherJob?.cancel()
        fetcherJob = null
    }
}
