import base.Config
import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

class SettingsWindow : Configurable {
    private lateinit var settingsPanel: JPanel
    private lateinit var pinyinModeCheckBox: JCheckBox
    private lateinit var colorfulModeCheckBox: JCheckBox

    override fun getDisplayName(): @Nls String {
        return "BoBo_Leeks"
    }

    override fun createComponent(): JComponent {
        pinyinModeCheckBox.isSelected = Config.pinyinMode
        colorfulModeCheckBox.isSelected = Config.colorful
        return settingsPanel
    }

    override fun isModified(): Boolean {
        return pinyinModeCheckBox.isSelected != Config.pinyinMode
                || colorfulModeCheckBox.isSelected != Config.colorful
    }

    override fun apply() {
        Config.pinyinMode = pinyinModeCheckBox.isSelected
        Config.colorful = colorfulModeCheckBox.isSelected
    }
}
