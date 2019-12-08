package com.ufpe.onepercent

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.add_outlet_dialog.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.util.*
import kotlin.collections.ArrayList


class MapActivity : AppCompatActivity() {
    lateinit var mapFragment: SupportMapFragment
    lateinit var googleMap: GoogleMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        // https://www.youtube.com/watch?v=suwq7Nta3oM
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        createMap()


        val add_outlet_button = addOutletButton
        add_outlet_button.setOnClickListener {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.add_outlet_dialog, null)

            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Add Outlet Form")

            val mAlertDialog = mBuilder.show()

            mDialogView.dialogAddButton.setOnClickListener {
                mAlertDialog.dismiss()
                val outlet = Outlet(place = mDialogView.dialogPlaceText.text.toString(), description = mDialogView.dialogDescriptionText.text.toString())
                val debugText: String = "NEW OUTLET\nplace: %s\ndesc: %s".format(outlet.place, outlet.description)

                Toast.makeText(this, debugText, Toast.LENGTH_LONG).show()

                addMarkeratDatabase(-1.0,-1.0,outlet.place,outlet.description)



            }



        }
    }
    private fun addMarkeratDatabase(lat:Double,lng:Double,desc:String,loc:String){
        val ref = FirebaseDatabase.getInstance().getReference("markers")
        val coord = arrayOf(lat,lng)
        val marker:MutableMap<String, Any> = mutableMapOf()
        marker["description"] = desc
        marker["location"] = loc
        marker["coord"] = coord
        val k:String = ref.push().key as String
        ref.child(k).setValue(marker)
    }
    private suspend fun createMap() {

        val coords : MutableList<ArrayList<Double>> = runBlocking {
            getMarkers()
        }

        delay(10_000)

        try {
            mapFragment.getMapAsync(OnMapReadyCallback {
                googleMap = it
                it.setMinZoomPreference(16.5f)
                val zoomLevel = 18.5f //This goes up to 21
                googleMap.let {
                    for (i in coords) {
                        val latLng = LatLng(i[0], i[1])
                        val markerOptions: MarkerOptions =
                            MarkerOptions().position(latLng).title(coords.toString())
                        it!!.addMarker(markerOptions)

                    }
                    val latLng = LatLng(-8.055788, -34.951489)
                    val markerOptions: MarkerOptions =
                        MarkerOptions().position(latLng).title(coords.toString())
                    it!!.addMarker(markerOptions)
                    it.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))

                }
            })
            } catch (e: Throwable) {
                println(e.message)
            }



    }
        private suspend fun getMarkers(): MutableList<ArrayList<Double>> {

            val ref = FirebaseDatabase.getInstance().getReference("markers")
            val listOfCoords = mutableListOf<ArrayList<Double>>()
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (i in dataSnapshot.children){
                        val child: Map<String, Object> = (i.getValue() as Map<String, Object>)
                        val coords:ArrayList<Double> = child["coord"] as ArrayList<Double>
                        listOfCoords.add(coords)
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    println("Error!!!")
                }

            })

            return listOfCoords
        }
    }









