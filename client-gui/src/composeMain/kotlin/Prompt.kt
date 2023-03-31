import androidx.compose.runtime.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.jvm.JvmName

class Prompt(prompt: String, value: String) {
    var prompt: String by mutableStateOf(prompt)
    var value: String by mutableStateOf(value)
    var textFieldValue: TextFieldValue by mutableStateOf(TextFieldValue(prompt + value))

    var isEnable by mutableStateOf(false)

    /**
     * @param newValue 新しい[TextFieldValue]
     * @param onValueChanged [newValue]が有効で値が更新されたときに呼ばれる。
     */
    fun updateTextFieldValue(
        newValue: TextFieldValue,
        onValueChanged: ((value: String, prompt: String) -> Unit)? = null
    ) {
        if (newValue.text.startsWith(prompt) && isEnable) {
            textFieldValue = newValue
            onValueChanged?.invoke(getValue(), prompt)
        } else {
            textFieldValue = textFieldValue.copy(selection = TextRange(prompt.length))
        }
    }

    fun updateValue(newValue: String) {
        value = newValue
        textFieldValue = textFieldValue.copy(text = prompt + value, selection = TextRange((prompt + value).length))
    }

    fun newPrompt(promptText: String, defaultValue: String = ""/*onValue:(String)->Unit={}*/) {
        /*//Event消化
        callback?.invoke("")
        callback=null*/
        isEnable = true
        prompt = promptText
        value = defaultValue
        textFieldValue = textFieldValue.copy(text = prompt + value, selection = TextRange((prompt + value).length))

    }

    fun reset() {
        prompt = ""
        value = ""
        textFieldValue = textFieldValue.copy(text = prompt + value)
        isEnable = false
    }

    //valueのgetterと名前が被るので回避
    @JvmName("getValueWithoutPrompt")
    fun getValue(): String = textFieldValue.text.removePrefix(prompt)

}

@Composable
fun rememberPrompt(prompt: String, value: String = "") = remember(prompt, value) {
    mutableStateOf(
        Prompt(
            prompt,
            value
        )
    )
}