package mensainfo.app.ui

import android.os.Bundle
import android.text.Html.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_licence.view.*
import mensainfo.app.R

class LicenceFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_licence, container, false).apply {
            licence_text.text = fromHtml(
                    getString(R.string.agpl_licence_1) + getString(R.string.agpl_licence_2),
                    FROM_HTML_SEPARATOR_LINE_BREAK_HEADING or FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH
            )
        }
    }
}