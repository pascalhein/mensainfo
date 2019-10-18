package mensainfo.app.ui

import android.content.Context
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_main.view.*
import kotlinx.android.synthetic.main.fragment_waiting.view.*
import kotlinx.android.synthetic.main.item_menu.view.*
import mensainfo.app.R
import mensainfo.app.openmensa_api.Meal
import mensainfo.app.openmensa_api.OpenmensaApi

class MainFragment : Fragment() {

    companion object {
        private const val ID_ARG = "mainfragment_id_arg"
        private const val DATE_ARG = "mainfragment_date_arg"

        private lateinit var meals: List<Meal>

        fun newInstance(canteenId: Int, date: String): MainFragment {
            val args = Bundle()
            args.putInt(ID_ARG, canteenId)
            args.putString(DATE_ARG, date)
            return MainFragment().apply { arguments = args }
        }

        private fun LayoutInflater.inflateMenuEntry(container: ViewGroup, meal: Meal, context: Context) {
            container.addView(inflate(R.layout.item_menu, container, false).apply {
                item_menu_content.text = meal.name
                item_menu_category.text = if (meal.category == "null") {
                    context.getString(R.string.item_category_empty)
                } else {
                    meal.category
                }

                item_menu_price.apply {
                    if (meal.prices.students == null) {
                        visibility = View.GONE
                    } else {
                        text = context.getString(R.string.price_format_currency, meal.prices.students)
                    }
                }

                if (meal.notes.isEmpty()) {
                    item_menu_info_symbol.visibility = View.GONE
                } else {
                    item_menu_allergens.text = meal.notes.joinToString(prefix = context.getString(R.string.allergen_title) + " ", separator = " • ")

                    setOnClickListener {
                        TransitionManager.beginDelayedTransition(container, AutoTransition().setDuration(200))
                        if (item_menu_expansion.visibility == View.VISIBLE) {
                            item_menu_expansion.visibility = View.GONE
                            item_menu_info_symbol.setImageResource(R.drawable.info)
                        } else {
                            item_menu_expansion.visibility = View.VISIBLE
                            item_menu_info_symbol.setImageResource(R.drawable.chevron_up)
                        }
                    }
                }
            })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val parent = inflater.inflate(R.layout.fragment_waiting, container, false) as ConstraintLayout

        OpenmensaApi.Requests.getMeals(arguments!!.getInt(ID_ARG), arguments!!.getString(DATE_ARG)!!) {
            activity!!.runOnUiThread {
                val root = inflater.inflate(R.layout.fragment_main, parent, true)
                meals = it
                with(root.fragment_main_menu_container) {
                    it.forEach {
                        inflater.inflateMenuEntry(this, it, context)
                    }
                }
                parent.progress.visibility = View.GONE
            }
        }

        return parent
    }
}