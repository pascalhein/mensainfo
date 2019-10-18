package mensainfo.app.openmensa_api

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

internal fun <T> JSONArray.map(transform: (JSONArray) -> (Int) -> T): List<T>
        = (0 until this.length()).map(transform(this))

internal fun <T> JSONArray.objMap(transform: (JSONObject) -> T): List<T>
        = this.map { it::getJSONObject }.map(transform)

internal fun JSONArray.toDoubles() = this.map { it::getDouble }

internal fun JSONArray.toStrings() = this.map { it::getString }

internal fun <S, T> ((S) -> T).tryGet(s: S): T? {
    return try {
        this(s)
    } catch (ex: JSONException) {
        null
    }
}

const val API_ENDPOINT = "https://openmensa.org/api/v2/"
const val PREF_KEY_CANTEENS = "CanteenList"
const val PREF_KEY_SEL_CANTEEN = "SelectedCanteen"
const val PREF_KEY_LOCATIONS = "SelectedLocations"
const val PREF_KEY_WIDGET_DAYS = "WidgetDays"
const val PREF_KEY_WIDGET_SEL_DAY = "WidgetSelDay"
const val PREF_KEY_WIDGET_DIRTY = "WidgetDirty"