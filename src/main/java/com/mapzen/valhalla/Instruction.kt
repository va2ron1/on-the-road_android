package com.mapzen.valhalla

import com.mapzen.helpers.DistanceFormatter
import com.mapzen.model.ValhallaLocation
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

open class Instruction {
    companion object {
        val KM_TO_METERS = 1000
        val MI_TO_METERS = 1609.344
        //full list of types defined: https://mapzen.com/documentation/turn-by-turn/api-reference/
        val MANEUVER_TYPE_DESTINATION : Int = 4
    }

    private var json: JSONObject? = null;

    var turnInstruction: Int = 0
    var distance: Int = 0
    var location: ValhallaLocation = ValhallaLocation()
    var liveDistanceToNext: Int = -1
    var bearing: Int = 0

    constructor(json: JSONObject) : this(json, Router.DistanceUnits.KILOMETERS) { }

    constructor(json: JSONObject, units: Router.DistanceUnits) {
        if (json.length() < 6) {
            throw JSONException("too few arguments")
        }
        this.json = json
        turnInstruction = parseTurnInstruction(json)

        val raw = json.getDouble("length")
        when (units) {
            Router.DistanceUnits.KILOMETERS -> distance = Math.round(raw * KM_TO_METERS).toInt()
            Router.DistanceUnits.MILES -> distance = Math.round(raw * MI_TO_METERS).toInt()
        }
    }

    /**
     * Used for testing. Do not remove.
     */
    @SuppressWarnings("unused")
    protected constructor() {
    }

    fun getIntegerInstruction(): Int {
       return turnInstruction;
    }

    fun getHumanTurnInstruction(): String? {
        return json?.getString("instruction");
    }

    fun getBeginStreetNames(): String {
        if (json?.has("begin_street_names") ?: false) {
            var streetName = "";
            val numStreetNames = (json!!.getJSONArray("begin_street_names").length())
            for(i in 0..numStreetNames - 1) {
                streetName += json!!.getJSONArray("begin_street_names").get(i);
                if((numStreetNames > 1) && (i < numStreetNames - 1)) {
                    streetName += "/"
                }
            }
            return streetName;
        }

        return ""
    }


    fun getName(): String {
        if (json?.has("street_names") ?: false) {
            var streetName = "";
            val numStreetNames = (json!!.getJSONArray("street_names").length())
            for(i in 0..numStreetNames - 1) {
                streetName += json!!.getJSONArray("street_names").get(i);
                if((numStreetNames > 1) && (i < numStreetNames - 1)) {
                    streetName += "/"
                }
            }
            return streetName;
        }

        return json?.getString("instruction") ?: ""
    }

    fun getFormattedDistance(): String {
        return DistanceFormatter.format(distance.toInt())
    }

    fun getBeginPolygonIndex(): Int {
        return json!!.getInt("begin_shape_index");
    }

    fun getEndPolygonIndex(): Int {
        return json!!.getInt("end_shape_index");
    }

    fun getDirectionAngle(): Float {
        val direction = bearing
        var angle: Float = 0.0f
        if (direction >= 315.0 && direction <= 360.0 ) {
            angle = 315.0.toFloat()
        } else if (direction >= 270.0 && direction < 315.0) {
            angle = 270.0.toFloat()
        } else if (direction >= 225.0 && direction < 270.0) {
            angle = 225.0.toFloat()
        } else if (direction >= 180.0 && direction < 225.0) {
            angle = 180.0.toFloat()
        } else if (direction >= 135.0 && direction < 180.0) {
            angle = 135.0.toFloat()
        } else if (direction >= 90.0 && direction < 135.0) {
            angle = 90.0.toFloat()
        } else if (direction >= 45.0 && direction < 90.0) {
            angle = 45.0.toFloat()
        } else if (direction >= 0.0 && direction < 45.0) {
            angle = 0.0.toFloat()
        }
        return angle
    }

    fun getDirection(): String {
        var direction = ""
        if (bearing >= 315.0 && bearing >= 360.0 ) {
            direction = "NE"
        } else if (bearing >= 270.0 && bearing < 315.0) {
            direction = "E"
        } else if (bearing >= 225.0 && bearing < 270.0) {
            direction = "SE"
        } else if (bearing >= 180.0 && bearing < 225.0) {
            direction = "S"
        } else if (bearing >= 135.0 && bearing < 180.0) {
            direction = "SW"
        } else if (bearing >= 90.0 && bearing < 135.0) {
            direction = "W"
        } else if (bearing >= 45.0 && bearing < 90.0) {
            direction = "NW"
        } else if (bearing >= 0.0 && bearing < 45.0) {
            direction = "N"
        }
        return direction
    }

    fun getRotationBearing(): Int {
        return 360 - bearing
    }

    override fun toString(): String {
        var name = ""
        try {
            name = getName()
        } catch (e: JSONException) {
            System.out.println("Json exception unable to get name" + e.stackTrace)
        }

        return java.lang.String.format(Locale.US, "Instruction: (%.5f, %.5f) %s %s" +
                "LiveDistanceTo: %d", location.latitude, location.longitude,
                turnInstruction, name, liveDistanceToNext)
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }

        if (javaClass != obj.javaClass) {
            return false
        }

        val other = obj as Instruction
        return (turnInstruction == other.turnInstruction
                && bearing == other.bearing
                && location.latitude == other.location.latitude
                && location.longitude == other.location.longitude)
    }

    private fun parseTurnInstruction(json: JSONObject): Int =
            json.getInt("type")

    fun getVerbalPreTransitionInstruction(): String {
        if (json?.has("verbal_pre_transition_instruction") ?: false) {
            return json?.getString("verbal_pre_transition_instruction") ?: ""
        }

        return ""
    }

    fun getVerbalTransitionAlertInstruction(): String {
        if (json?.has("verbal_transition_alert_instruction") ?: false) {
            return json?.getString("verbal_transition_alert_instruction") ?: ""
        }

        return ""
    }

    fun getVerbalPostTransitionInstruction(): String {
        if (json?.has("verbal_post_transition_instruction") ?: false) {
            return json?.getString("verbal_post_transition_instruction") ?: ""
        }

        return ""
    }
}
