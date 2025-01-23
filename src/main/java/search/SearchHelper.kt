package search

import base.*
import bean.SearchResult
import bean.SearchedStock
import bean.Stock
import com.alibaba.fastjson2.JSON
import com.intellij.ide.util.PropertiesComponent
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.StartupUiUtil.labelFont
import com.intellij.util.ui.UIUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import utils.httpGet
import java.awt.*
import java.awt.event.*
import javax.swing.*
import javax.swing.border.Border
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

private const val dialogWidth = 600
private const val dialogMinHeight = 50
private const val dialogMaxHeight = 500

private val BackgroundColor = JBColor.background()
private val DefaultBorder = BorderFactory.createEmptyBorder(8, 10, 8, 10)

@OptIn(FlowPreview::class)
class SearchHelper(
    private val addRequest: (Stock) -> Unit,
) {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val searchFlow = MutableSharedFlow<String>()

    private var firstClickPosition = Point()

    private val screenSize: Dimension
        get() = Toolkit.getDefaultToolkit().screenSize

    private val searchFieldChangeListener = object : DocumentListener {
        override fun insertUpdate(event: DocumentEvent) {
            handleKeywordsChanged()
        }

        override fun removeUpdate(event: DocumentEvent) {
            handleKeywordsChanged()
        }

        override fun changedUpdate(event: DocumentEvent) {
            handleKeywordsChanged()
        }
    }

    private val followedStocks: List<Stock>
        get() = PropertiesComponent.getInstance().getStockConfig()

    private val searchField by lazy {
        JTextField(50).apply {
            font = UIUtil.getLabelFont()
            background = BackgroundColor
            border = DefaultBorder
            preferredSize = Dimension(0, 50)
            addMouseListener(object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    firstClickPosition = e.point
                }
            })
            addMouseMotionListener(object : MouseMotionAdapter() {
                override fun mouseDragged(e: MouseEvent) {

                    val offsetX = e.x - firstClickPosition.x
                    val offsetY = e.y - firstClickPosition.y

                    searchDialog.run {
                        location = Point(location.x + offsetX, location.y + offsetY)
                    }
                }
            })

            document.addDocumentListener(searchFieldChangeListener)
        }
    }

    private val searchDialog: JDialog by lazy {
        JDialog().apply {
            isUndecorated = true
            size = Dimension(dialogWidth, dialogMinHeight)

            layout = BorderLayout()
            setLocationRelativeTo(null)
            defaultCloseOperation = JDialog.HIDE_ON_CLOSE

            val contentPanel = JPanel(BorderLayout())
            contentPanel.background = Color(0, 0, 0, 0)

            contentPanel.add(searchField, BorderLayout.NORTH)
            contentPanel.add(resultScrollPane)

            add(contentPanel, BorderLayout.CENTER)

//            location = PropertiesComponent.getInstance().getSearchDialogLocation()
//                ?: Point((screenSize.width - width) / 2, screenSize.height / 3)

            location = Point((screenSize.width - width) / 2, screenSize.height / 3)

            addWindowFocusListener(object : WindowFocusListener {
                override fun windowGainedFocus(p0: WindowEvent?) {

                }

                override fun windowLostFocus(p0: WindowEvent?) {
                    dismiss()
                }
            })

            addKeyListener(object : KeyAdapter() {
                override fun keyPressed(event: KeyEvent) {
                    if (event.keyCode == KeyEvent.VK_ESCAPE) {
                        dismiss()
                    }
                }
            })
        }
    }

    private val resultScrollPane by lazy {
        JBScrollPane(resultList).apply {
            border = BorderFactory.createEmptyBorder()
            isVisible = false
        }
    }

    private val resultList: JList<SearchedStock> by lazy {
        JBList<SearchedStock>().apply {
            font = labelFont
            background = JBColor.background()
            foreground = JBColor.foreground()
            selectionBackground = UIUtil.getListSelectionBackground(true)
            selectionForeground = UIUtil.getListSelectionForeground(true)
            cellRenderer = object : DefaultListCellRenderer() {
                override fun getListCellRendererComponent(
                    list: JList<*>?,
                    obj: Any?,
                    index: Int,
                    isSelected: Boolean,
                    hasFocus: Boolean
                ): Component {
                    val label = (super.getListCellRendererComponent(list, obj, index, isSelected, hasFocus) as JLabel)
//                    label.border = BorderFactory.createEmptyBorder(8, 20, 8, 20)
                    label.text = when (obj) {
                        is SearchedStock -> {
                            obj.displayName
                        }

                        else -> {
                            ""
                        }
                    }
                    label.background = if (isSelected) JBColor.GREEN else BackgroundColor
                    label.border = DefaultBorder
                    return this
                }

                override fun getBorder(): Border {
//                    return super.getBorder()
                    return BorderFactory.createEmptyBorder(8, 20, 8, 20)
                }
            }

            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(event: MouseEvent) {
                    super.mouseClicked(event)
                    if (event.clickCount == 2) {
                        handleDoubleClickSearchedStock(this@apply.selectedValue)
                    }
                }
            })
        }
    }

    init {
        scope.launch {
            searchFlow.debounce(500).collect {
                search(it)
            }
        }
    }

    private fun handleDoubleClickSearchedStock(stock: SearchedStock?) {
        if (stock == null || stock.followed) {
            return
        }
        stock.followed = true
        searchDialog.revalidate()

        val addedStock = Stock(code = stock.code, name = stock.name)
        val configStocks = followedStocks.toMutableList()
        configStocks.add(0, addedStock)
        PropertiesComponent.getInstance().updateStockConfig(configStocks)
        addRequest.invoke(addedStock)
    }

    private fun handleKeywordsChanged() {
        scope.launch {
            searchFlow.emit(searchField.text)
        }
    }

    private suspend fun search(keywords: String?) {
        if (keywords.isNullOrEmpty() || keywords.isEmpty()) {
            updateSearchResult(null)
            return
        }

        val path = Urls.search + keywords

        val httpResult = path.httpGet() ?: return
        val searchResult = try {
            JSON.parseObject(httpResult, SearchResult::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } ?: return

        updateSearchResult(searchResult)
    }

    private fun updateSearchResult(result: SearchResult?) {
        if (result == null) {
            if (resultList.model.size > 0) {
                resultList.setListData(emptyArray())
                resultScrollPane.isVisible = false
                searchDialog.setSize(dialogWidth, dialogMinHeight)
                searchDialog.revalidate()
            }
            return
        }

        val allStockOrFundList = mutableListOf<SearchedStock>()
        result.stock?.let {
            allStockOrFundList.addAll(it)
        }

        result.fund?.let {
            allStockOrFundList.addAll(it)
        }

        allStockOrFundList.forEach { searchItem -> searchItem.followed = followedStocks.any { searchItem.code == it.code } }
        resultList.setListData(allStockOrFundList.toTypedArray())
        resultScrollPane.isVisible = true
        searchDialog.setSize(dialogWidth, dialogMaxHeight)
        searchDialog.revalidate()
    }

    fun show() {
        if (searchDialog.isVisible) {
            return
        }

        searchDialog.isVisible = true
        searchField.requestFocus()
    }

    fun dismiss() {
        if (!searchDialog.isVisible) {
            return
        }
//        PropertiesComponent.getInstance().saveSearchDialogLocation(searchDialog.location)
        searchDialog.isVisible = false
        searchField.text = ""
    }
}