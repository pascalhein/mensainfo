package mensainfo.app.openmensa_api

import mensainfo.app.ui.tools.DateFormatter
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class Canteens(val canteens: List<Canteen>) {
    constructor(s: String) : this(JSONArray(s).objMap(::Canteen))

    override fun toString() = JSONArray(canteens.map(Canteen::toJSON)).toString()
}

class Canteen(jo: JSONObject) {
    val id = jo.getInt("id")
    val name: String? = jo.getString("name")
    val city: String? = jo.getString("city")
    val address: String? = jo.getString("address")
    val coordinates: List<Double>? = jo.optJSONArray("coordinates")?.toDoubles()

    fun toJSON() = JSONObject()
            .put("id", id)
            .put("name", name)
            .put("city", city)
            .put("address", address)
            .put("coordinates", coordinates?.toTypedArray())
}

class Day(jo: JSONObject) {
    val date = jo.getString("date")
    val parsedDate = DateFormatter.iso.parse(date)
    val closed = jo.getBoolean("closed")
}

class Meal(jo: JSONObject) {
    val name = jo.getString("name")
    val category = jo.getString("category")
    val prices = Prices(jo.getJSONObject("prices"))
    val notes = jo.getJSONArray("notes").toStrings()
}

class Prices(jo: JSONObject) {
    val students = jo::getDouble.tryGet("students")
    val employees = jo::getDouble.tryGet("employees")
    val pupils = jo::getDouble.tryGet("pupils")
    val others = jo::getDouble.tryGet("others")
}