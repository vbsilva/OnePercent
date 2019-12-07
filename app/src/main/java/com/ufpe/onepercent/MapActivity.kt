package com.ufpe.onepercent

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.add_outlet_dialog.view.*


class MapActivity : AppCompatActivity() {

    lateinit var mapFragment: SupportMapFragment
    lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        // https://www.youtube.com/watch?v=suwq7Nta3oM

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(OnMapReadyCallback {
            googleMap = it
        })

        val add_outlet_button = addOutletButton
        add_outlet_button.setOnClickListener {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.add_outlet_dialog, null)

            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Add Outlet Form")

            val mAlertDialog = mBuilder.show()

            mDialogView.dialogAddButton.setOnClickListener {
                mAlertDialog.dismiss()
            }


        }
    }

}
