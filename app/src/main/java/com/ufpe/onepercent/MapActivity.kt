package com.ufpe.onepercent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BATTERY_CHANGED
import android.content.Intent.ACTION_POWER_CONNECTED
import android.content.IntentFilter
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.BatteryManager
import android.os.BatteryManager.BATTERY_STATUS_CHARGING
import android.os.BatteryManager.BATTERY_STATUS_FULL
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.maps.android.PolyUtil
import com.ufpe.onepercent.model.Outlet
import com.ufpe.onepercent.model.User
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.add_outlet_dialog.view.*
import org.json.JSONObject
import kotlin.collections.ArrayList
import kotlin.properties.Delegates


class MapActivity : AppCompatActivity() {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
    val ref = FirebaseDatabase.getInstance().getReference("markers")


    val users_ref = FirebaseDatabase.getInstance().getReference("users")
    lateinit var pl: Polyline

    lateinit var mapFragment: SupportMapFragment
    lateinit var googleMap: GoogleMap
    lateinit var lastLocation: Location
    lateinit var fusedLocationClient: FusedLocationProviderClient

    var users = ArrayList<User>()



    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        // https://www.youtube.com/watch?v=suwq7Nta3oM
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        try{createMap()}
        catch(e:Throwable){
            println(e.message)
        }

        var extras_data = intent.extras
        var logged_username:String? = ""
        var photoUrl:String? = ""
        var userId:String? = ""

        if (extras_data != null) {
            logged_username = extras_data.getString("username")
            photoUrl = extras_data.getString("photoUrl")
            userId = extras_data.getString("id")
            Toast.makeText(this, logged_username + " Logged In", Toast.LENGTH_LONG).show()
        }

        var logged_user = User(
            name = logged_username!!,
            id = userId!!,
            photoUrl = photoUrl!!,
            score = 0
        )
        getUsers(logged_user)

        val add_outlet_button = addOutletButton
        add_outlet_button.setOnClickListener {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.add_outlet_dialog, null)

            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Add Outlet Form")

            val mAlertDialog = mBuilder.show()

            mDialogView.dialogAddButton.setOnClickListener {
                mAlertDialog.dismiss()
                val outlet = Outlet(
                    place = mDialogView.dialogPlaceText.text.toString(),
                    description = mDialogView.dialogDescriptionText.text.toString()
                )
                val debugText: String = "Outlet added!\nplace: %s\ndesc: %s".format(outlet.place, outlet.description)

                addMarkeratDatabase(lastLocation.latitude,lastLocation.longitude,outlet.place as String,outlet.description as String)
                Toast.makeText(this, debugText, Toast.LENGTH_LONG).show()
            }
        }


        val score_button = scoreButton
        score_button.setOnClickListener {
            startActivity(Intent(this, ScoreActivity::class.java))

        }

    }



    private fun getUsers(logged_user: User){
        users_ref.addListenerForSingleValueEvent(object : ValueEventListener {
            var found: Boolean = false
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (i in dataSnapshot.children){
                    var child: Map<String, Object> = (i.getValue() as Map<String, Object>)
                    var username:String = child["username"] as String
                    var score:Long = child["score"] as Long
                    var photoUrl:String = child["photoUrl"] as String
                    var id: String = child["id"] as String
                    var user = User(
                        name = username,
                        score = score,
                        photoUrl = photoUrl,
                        id = id
                    )
                    users.add(user)

                    if (username == logged_user.name) {
                        found = true
                        logged_user.score = score
                        println("************************************** user already in DB")

                    }
                }

                if (!found) {
                    println("\n*************************************** adding user " + logged_user.name)
                    addUserDatabase(logged_user)
                }
            }


            override fun onCancelled(databaseError: DatabaseError) {
                println("Error!!!")
            }
        })
    }

    private fun addUserDatabase(user: User){

        val db_user:MutableMap<String, Any> = mutableMapOf()
        db_user["username"] = user.name
        db_user["score"] = user.score
        db_user["photoUrl"] = user.photoUrl
        db_user["id"] = user.id
        val k:String = users_ref.push().key as String
        users_ref.child(k).setValue(db_user)


    }


    private fun addMarkeratDatabase(lat:Double,lng:Double,desc:String,loc:String){

        val coord = listOf(lat,lng)
        val marker:MutableMap<String, Any> = mutableMapOf()
        marker["description"] = desc
        marker["location"] = loc
        marker["coord"] = coord
        val k:String = ref.push().key as String
        try{
            ref.child(k).setValue(marker)
            val latLng = LatLng(lat, lng)
            val markerOptions: MarkerOptions =
                MarkerOptions().position(latLng).title(loc).snippet(desc)
            googleMap!!.addMarker(markerOptions)
        }
        catch(e:Throwable){
            println(e.message)
        }


    }
    private fun getChargeInfo(){
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            this.registerReceiver(null, ifilter)
        }
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        var isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BATTERY_STATUS_FULL
        while(isCharging){
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status == BATTERY_STATUS_FULL
        }
    }
    private fun createMap() {

        setUpMap()
                mapFragment.getMapAsync(OnMapReadyCallback {
                    googleMap = it
                    //it.setMinZoomPreference(16.5f)
                    val zoomLevel = 18.5f //This goes up to 21
                    googleMap.let {
                        getMarkers()
                        val latLng = LatLng(-8.151968, -34.916035)
                        val markerOptions: MarkerOptions =
                            MarkerOptions().position(latLng).title("Testing")
                        it!!.addMarker(markerOptions)
                        googleMap.isMyLocationEnabled = true
                        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                            if (location != null) {
                                lastLocation = location
                                val currentLatLng = LatLng(location.latitude, location.longitude)
                                googleMap.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        currentLatLng,
                                        12f
                                    )
                                )
                            }
                        }
                        googleMap!!.setOnMarkerClickListener { marker ->
                            val destiny: LatLng = marker.position
                            if (::pl.isInitialized) {
                                pl.remove()
                            }
                            routeMaker(destiny, LatLng(lastLocation.latitude,lastLocation.longitude))
                            false
                        }
                    }
                })

    }
        private fun getMarkers(){
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (i in dataSnapshot.children){
                        val child: Map<String, Object> = (i.getValue() as Map<String, Object>)
                        val coords:ArrayList<Double> = child["coord"] as ArrayList<Double>
                        val latLng = LatLng(coords[0],coords[1])
                        val markerOptions: MarkerOptions =
                            MarkerOptions().position(latLng).title(child["description"] as String).snippet(child["location"] as String)
                        googleMap!!.addMarker(markerOptions)

                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    println("Error!!!")
                }
            })
        }
        private fun setUpMap() {
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
                return
            }
        }
        private fun routeMaker(A:LatLng,B:LatLng){
            val path: MutableList<List<LatLng>> = ArrayList()
            //val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?origin=${A.latitude},${A.longitude}&destination=${B.latitude},${B.longitude}&key=AIzaSyCDIevLmcizmFp-3ET12-JLAOoOdIpAEig"
            val urlDirections ="https://maps.googleapis.com/maps/api/directions/json?origin=${A.latitude},${A.longitude}&destination=${B.latitude},${B.longitude}&key=AIzaSyCDIevLmcizmFp-3ET12-JLAOoOdIpAEig"
            val directionsRequest = object : StringRequest(Request.Method.GET, urlDirections, Response.Listener<String> {
                    response ->
                val jsonResponse = JSONObject(response)
                // Get routes
                val routes = jsonResponse.getJSONArray("routes")
                val legs = routes.getJSONObject(0).getJSONArray("legs")
                val steps = legs.getJSONObject(0).getJSONArray("steps")
                for (i in 0 until steps.length()) {
                    val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                    path.add(PolyUtil.decode(points))
                }
                for (i in 0 until path.size) {
                    pl = this.googleMap!!.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
                }
            }, Response.ErrorListener {
                println(it.networkResponse)
            }){}
            val requestQueue = Volley.newRequestQueue(this)
            try{
                requestQueue.add(directionsRequest)
            }
            catch(e:Throwable){
                println(e.message)
            }

        }
    }









