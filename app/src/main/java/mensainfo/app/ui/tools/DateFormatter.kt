package mensainfo.app.ui.tools

import android.content.Context
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object DateFormatter {
    fun init(ctx: Context) {
        locale = ctx.resources.configuration.locales[0]
    }
    private lateinit var locale: Locale
    val dateFormat: SimpleDateFormat
        get() = SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, "ddMM"), locale)
    val dowFormat: SimpleDateFormat
        get() = SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, "EEE"), locale)
    val iso: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
}