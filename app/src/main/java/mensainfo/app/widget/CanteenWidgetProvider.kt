package mensainfo.app.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.preference.PreferenceManager
import android.view.View
import android.widget.RemoteViews
import mensainfo.app.R
import mensainfo.app.openmensa_api.*
import mensainfo.app.ui.MainActivity
import mensainfo.app.ui.tools.DateFormatter

class CanteenWidgetProvider : AppWidgetProvider() {

    @SuppressLint("ApplySharedPref")
    private fun (RemoteViews).setNewDay(context: Context, id: Int, days: List<String>, index: Int) {
        DateFormatter.init(context)
        /* var i = index
        if (i < 0) {
            i = 0
        }
        if (i >= days.size) {
            i = days.size - 1
        }
        val day = days.getOrNull(i)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putString(PREF_KEY_WIDGET_SEL_DAY, day).commit()
        */
        val day = days.firstOrNull()
        if (day != null) {
            val date = DateFormatter.iso.parse(day)
            setTextViewText(R.id.widget_date, DateFormatter.dateFormat.format(date))
            setTextViewText(R.id.widget_day_of_week, DateFormatter.dowFormat.format(date))
        } else {
            setTextViewText(R.id.widget_date, "")
            setTextViewText(R.id.widget_day_of_week, "")
        }
        // setViewVisibility(R.id.widget_button_left, if (index == 0) View.INVISIBLE else View.VISIBLE)
        // setViewVisibility(R.id.widget_button_right, if (index == days.size - 1) View.INVISIBLE else View.VISIBLE)
        AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(id, R.id.widget_meal_list)
        AppWidgetManager.getInstance(context).partiallyUpdateAppWidget(id, this)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == context.resources.getString(R.string.intent_action_widget_dirty)) {
            val id = intent.getIntExtra(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID)
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val days = prefs.getString(PREF_KEY_WIDGET_DAYS, "")!!.split(" // ")
            val rv = RemoteViews(context.packageName, R.layout.widget_canteens)
            rv.setNewDay(context, id, days, 0)
            /*
            when (intent.action) {
                context.resources.getString(R.string.intent_action_widget_dirty) ->
                    rv.setNewDay(context, id, days, 0)
                context.resources.getString(R.string.intent_action_widget_left) -> {
                    val index = days.indexOf(prefs.getString(PREF_KEY_WIDGET_SEL_DAY, "") ?: "")
                    if (index > 0) {
                        rv.setNewDay(context, id, days, index - 1)
                    }
                }
                context.resources.getString(R.string.intent_action_widget_right) -> {
                    val index = days.indexOf(prefs.getString(PREF_KEY_WIDGET_SEL_DAY, "") ?: "")
                    if (index < days.size - 1) {
                        rv.setNewDay(context, id, days, index + 1)
                    }
                }
                else -> super.onReceive(context, intent)
            }
            */
        } else {
            super.onReceive(context, intent)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        OpenmensaApi.init(context)
        for (id in appWidgetIds) {
            RemoteViews(context.packageName, R.layout.widget_canteens).apply {
                setOnClickPendingIntent(R.id.widget_canteen_name, PendingIntent.getActivity(
                        context, 0, Intent(context, MainActivity::class.java), 0
                ))
                setRemoteAdapter(R.id.widget_meal_list, Intent(context, CanteenWidgetService::class.java).apply {
                    putExtra(EXTRA_APPWIDGET_ID, id)
                    data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
                })
                setEmptyView(R.id.widget_meal_list, R.id.widget_empty)
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                val canteens = Canteens(prefs.getString(PREF_KEY_CANTEENS, null)!!)
                val sel = prefs.getInt(PREF_KEY_SEL_CANTEEN, 0)
                val split = (canteens.canteens.single { it.id == sel }.name ?: "").split(',', limit = 2)
                setTextViewText(R.id.widget_name_2, split[0].trim())
                if (split.size == 2) {
                    setTextViewText(R.id.widget_name_1, split[1].trim())
                }

                val intent = Intent(context, CanteenWidgetProvider::class.java).putExtra(EXTRA_APPWIDGET_ID, id)
                /*
                setOnClickPendingIntent(R.id.widget_button_left, PendingIntent.getBroadcast(
                        context, 0, intent.setAction(context.resources.getString(R.string.intent_action_widget_left)), 0
                ))
                setOnClickPendingIntent(R.id.widget_button_right, PendingIntent.getBroadcast(
                        context, 0, intent.setAction(context.resources.getString(R.string.intent_action_widget_right)), 0
                ))
                */
                appWidgetManager.updateAppWidget(id, this)

                OpenmensaApi.Requests.getDays(sel) @SuppressLint("ApplySharedPref") {
                    val text = it.map(Day::date).joinToString(" // ")
                    if (text != prefs.getString(PREF_KEY_WIDGET_DAYS, "") || prefs.getBoolean(PREF_KEY_WIDGET_DIRTY, true)) {
                        prefs.edit()
                                .putString(PREF_KEY_WIDGET_DAYS, text)
                                .putBoolean(PREF_KEY_WIDGET_DIRTY, true)
                                .commit()
                        context.sendBroadcast(intent.setAction(context.resources.getString(R.string.intent_action_widget_dirty)))
                    }
                }
            }
        }
    }
}