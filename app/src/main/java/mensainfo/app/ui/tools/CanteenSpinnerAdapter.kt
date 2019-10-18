package mensainfo.app.ui.tools

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.ThemedSpinnerAdapter

class CanteenSpinnerAdapter(context: Context, objects: List<String?>)
    : ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, objects), ThemedSpinnerAdapter {

    private val dropdown = ThemedSpinnerAdapter.Helper(context)

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return (convertView
                ?: dropdown.dropDownViewInflater.inflate(android.R.layout.simple_list_item_1, parent, false)).apply {
            findViewById<TextView>(android.R.id.text1).text = getItem(position)
        }
    }

    override fun getDropDownViewTheme(): Resources.Theme? = dropdown.dropDownViewTheme

    override fun setDropDownViewTheme(theme: Resources.Theme?) {
        dropdown.dropDownViewTheme = theme
    }
}