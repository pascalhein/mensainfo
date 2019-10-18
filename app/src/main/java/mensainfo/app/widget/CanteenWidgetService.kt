package mensainfo.app.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import mensainfo.app.R
import mensainfo.app.openmensa_api.*

class CanteenWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return CanteenRemoteViewsFactory(applicationContext, intent)
    }

    class CanteenRemoteViewsFactory(private val context: Context, intent: Intent) : RemoteViewsFactory {
        // private val appWidgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID)
        private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        // private var days: List<String> = emptyList()
        // private var selectedDay: String? = null
        // private var meals: MutableMap<String, List<Meal>> = mutableMapOf()
        private var meals: List<Meal>? = null
        private var canteen: Int = 0

        override fun onCreate() {
        }

        override fun getLoadingView(): RemoteViews {
            return RemoteViews(context.packageName, R.layout.loading)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong() // + 1_000_000L * days.indexOf(selectedDay)
        }

        @SuppressLint("ApplySharedPref")
        override fun onDataSetChanged() {
            // days = (prefs.getString(PREF_KEY_WIDGET_DAYS, "") ?: "").split(" // ")
            // selectedDay = prefs.getString(PREF_KEY_WIDGET_SEL_DAY, null)
            val day = (prefs.getString(PREF_KEY_WIDGET_DAYS, "") ?: "").split(" // ").firstOrNull()
            canteen = prefs.getInt(PREF_KEY_SEL_CANTEEN, 0)
            if (prefs.getBoolean(PREF_KEY_WIDGET_DIRTY, true)) {
                // meals.clear()
                meals = null
                OpenmensaApi.Requests.synchronised(5000) {
                    /* for (d in days) {
                        getMeals(canteen, d) {
                            meals[d] = it
                        }
                    } */
                    if (day != null) {
                        getMeals(canteen, day) {
                            meals = it
                        }
                    }
                }
                prefs.edit().putBoolean(PREF_KEY_WIDGET_DIRTY, false).commit()
            }
        }

        override fun hasStableIds(): Boolean {
            return false
        }

        override fun getViewAt(position: Int): RemoteViews {
            return RemoteViews(context.packageName, R.layout.widget_meal).apply {
                // val day = days.firstOrNull() // selectedDay
                // if (day == null || day !in meals) return loadingView
                val meals = meals ?: return loadingView
                val m = meals/*[day]!!*/[position]
                val price = m.prices.students
                setTextViewText(android.R.id.text1, when (price) {
                    null -> ""
                    else -> context.resources.getString(R.string.price_format, price)
                })
                setTextViewText(android.R.id.text2, m.name)
            }
        }

        override fun getCount(): Int {
            // val day = days.firstOrNull() // selectedDay
            // if (day == null || day !in meals) {
            //    return 1
            // }
            return meals?.size ?: 1
        }

        override fun getViewTypeCount(): Int {
            return 2
        }

        override fun onDestroy() {
        }
    }
}