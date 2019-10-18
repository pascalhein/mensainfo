package mensainfo.app.ui

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.preference.PreferenceManager
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import mensainfo.app.openmensa_api.*
import mensainfo.app.ui.tools.CanteenSpinnerAdapter
import mensainfo.app.ui.tools.MenuViewpagerAdapter
import mensainfo.app.ui.tools.WaitingAdapter
import mensainfo.app.widget.CanteenWidgetProvider

class CanteenModel(activity: FragmentActivity, private val spinner: Spinner, pager: ViewPager, tabs: TabLayout) {
    private var ids: List<Int> = emptyList()

    init {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val list: String? = prefs.getString(PREF_KEY_CANTEENS, null)
        val selected: Int = prefs.getInt(PREF_KEY_SEL_CANTEEN, 0)
        val filter: Set<String> = prefs.getStringSet(PREF_KEY_LOCATIONS, setOf())!!

        if (list == null) {
            spinner.adapter = CanteenSpinnerAdapter(spinner.context, listOf("Loading..."))
        } else {
            updateSpinner(Canteens(list), selected, filter)
        }


        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            @SuppressLint("ApplySharedPref")
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (ids.isEmpty()) {
                    return
                }

                prefs.edit().putInt(PREF_KEY_SEL_CANTEEN, ids[position]).commit()
                val item = pager.currentItem
                val adapter = pager.adapter

                pager.adapter = WaitingAdapter(activity.supportFragmentManager)
                OpenmensaApi.Requests.getDays(ids[position]) {
                    activity.runOnUiThread {
                        MenuViewpagerAdapter(activity.supportFragmentManager, ids[position], it).setupTabs(pager, tabs, item, adapter)
                    }
                }

                prefs.edit().putBoolean(PREF_KEY_WIDGET_DIRTY, true).commit()
                activity.sendBroadcast(Intent(activity, CanteenWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(
                            AppWidgetManager.EXTRA_APPWIDGET_IDS,
                            AppWidgetManager.getInstance(activity.application).getAppWidgetIds(
                                    ComponentName(activity.application, CanteenWidgetProvider::class.java)
                            ))

                })
            }
        }

        OpenmensaApi.Requests.getCanteens {
            activity.runOnUiThread {
                prefs.edit().putString(PREF_KEY_CANTEENS, it.toString()).apply()
                activity.invalidateOptionsMenu()
                updateSpinner(it, selected, filter)
            }
        }
    }

    fun updateSpinner(canteens: Canteens, selected: Int, filter: Collection<String>?) {
        val canteenList = if (filter == null || filter.isEmpty()) {
            canteens.canteens
        } else {
            canteens.canteens.filter { filter.contains(it.city) }
        }
        ids = canteenList.map(Canteen::id)
        val names = canteenList.map(Canteen::name)

        val selectedPos = ids.indexOf(selected)
        spinner.adapter = CanteenSpinnerAdapter(spinner.context, names)
        if (selectedPos >= 0) {
            spinner.setSelection(selectedPos, true)
        }
    }
}