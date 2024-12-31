import base.SettingsKeys
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

class SettingsWindow : Configurable {
    private lateinit var settingsPanel: JPanel
    private lateinit var pinyinModeCheckBox: JCheckBox

    override fun getDisplayName(): @Nls String {
        return "BoBo_Leeks"
    }

    override fun createComponent(): JComponent {
        pinyinModeCheckBox.isSelected = PropertiesComponent.getInstance().getBoolean(SettingsKeys.pinyinMode)
        return settingsPanel
    }

    override fun isModified(): Boolean {
        return true
    }

    override fun apply() {
        PropertiesComponent.getInstance().setValue(SettingsKeys.pinyinMode, !pinyinModeCheckBox.isSelected)
    }
}
