package mensainfo.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.format.DateFormat
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import mensainfo.app.R
import mensainfo.app.openmensa_api.*
import mensainfo.app.ui.tools.DateFormatter
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val cityRequest: Int = 0
    private lateinit var model: CanteenModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        DateFormatter.init(this)

        tabs.setupWithViewPager(pager)

        // OAuthService.init(this)
        OpenmensaApi.init(this)

        model = CanteenModel(this, spinner, pager, tabs)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu!!.findItem(R.id.action_city).isEnabled = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_KEY_CANTEENS, null) != null
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when (item?.itemId) {
        R.id.action_settings -> {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }
        R.id.action_city -> {
            startActivityForResult(Intent(this, CitySelectActivity::class.java), cityRequest)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            cityRequest -> {
                if (resultCode == RESULT_OK) {
                    val result = data?.getStringArrayExtra(PREF_KEY_LOCATIONS) ?: arrayOf()
                    val prefs = PreferenceManager.getDefaultSharedPreferences(this)
                    prefs.edit().putStringSet(PREF_KEY_LOCATIONS, result.toSet()).apply()

                    val list: String? = prefs.getString(PREF_KEY_CANTEENS, null)
                    val selected: Int = prefs.getInt(PREF_KEY_SEL_CANTEEN, 0)
                    if (list != null) {
                        model.updateSpinner(Canteens(list), selected, result.toSet())
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}