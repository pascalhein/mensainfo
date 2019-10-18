package mensainfo.app.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import androidx.appcompat.app.AppCompatActivity
import mensainfo.app.R
import kotlinx.android.synthetic.main.activity_city_select.*
import mensainfo.app.openmensa_api.*

class CitySelectActivity : AppCompatActivity() {

    private lateinit var cities: Array<String>
    private lateinit var filter: BooleanArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_select)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val canteens = Canteens(prefs.getString(PREF_KEY_CANTEENS, null)!!)
        cities = canteens.canteens.mapNotNull(Canteen::city).toSortedSet().toTypedArray()
        val enabled = prefs.getStringSet(PREF_KEY_LOCATIONS, setOf())!!
        filter = cities.map(enabled::contains).toBooleanArray()

        cities_list.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice,
                android.R.id.text1, cities)
        cities_list.setOnItemClickListener { _, view, position, _ ->
            view.findViewById<CheckedTextView>(android.R.id.text1).apply {
                filter[position] = isChecked
            }
        }

        filter.forEachIndexed(cities_list::setItemChecked)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_city_select, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when (item?.itemId) {
        R.id.save -> {
            val enabled = cities.filterIndexed { i, _ -> filter[i] }.toTypedArray()
            setResult(Activity.RESULT_OK, Intent().putExtra(PREF_KEY_LOCATIONS, enabled))
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
