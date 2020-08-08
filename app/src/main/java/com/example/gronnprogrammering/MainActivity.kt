package com.example.gronnprogrammering


import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.Toast
import com.example.gronnprogrammering.List.CountyFragment
import com.example.gronnprogrammering.dirLocation.LocationFragment
import com.example.gronnprogrammering.Map.MapFragment
import com.example.gronnprogrammering.Settings.SettingsFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //First thing that happens at startup, is disabling of LocationFragment
        disableFragment()
        //Then the system asks for permission to use the users coordinates
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission(permissionsNeeded)) {
                //If permissions are given, the system enables LocationFragment
                enableFragment()
            } else {
                //If permissions not given the system asks for it
                this.requestPermissions(permissionsNeeded, PERMISSION_REQUEST)
            }
        } else {
            enableFragment()
        }

        //When the app first starts, notification channels will be created
        createNotificationChannel()

        //Here the system start the listner for bottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    //Array which contains all permissions needed.
    private var permissionsNeeded =
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    //Navigation listner, when one of menus and clicked, fragment are created and started at the end
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        var fragment:Fragment? = null

        val menu1:MenuItem = navigation.menu.findItem(R.id.navigation_location)
        val menu2:MenuItem = navigation.menu.findItem(R.id.navigation_map)
        val menu3:MenuItem = navigation.menu.findItem(R.id.navigation_list)
        val menu4:MenuItem = navigation.menu.findItem(R.id.navigation_settings)


        when (item.itemId) {
            R.id.navigation_location -> {
                fragment = LocationFragment.newInstance()
                menu1.isChecked = true
            }
            R.id.navigation_map -> {
                fragment = MapFragment.newInstance()
                menu2.isChecked = true
            }
            R.id.navigation_list -> {
                fragment = CountyFragment.newInstance()
                menu3.isChecked = true

            }
            R.id.navigation_settings -> {
                fragment = SettingsFragment.newInstance()
                menu4.isChecked = true
            }

        }

        supportFragmentManager.beginTransaction().replace(R.id.rootLayout, fragment!!, fragment.javaClass.simpleName)
            .commit()
        false
    }

    //Disable menu which shows info based on users location
    private fun disableFragment() {
        val menu1:MenuItem = navigation.menu.findItem(R.id.navigation_location)
        menu1.isEnabled = false
    }

    //Enables menu and starts the fragment
    private fun enableFragment() {
        val menu1:MenuItem = navigation.menu.findItem(R.id.navigation_location)
        menu1.isEnabled = true

        supportFragmentManager.beginTransaction().replace(R.id.rootLayout, LocationFragment.newInstance(), LocationFragment.newInstance().javaClass.simpleName)
            .commit()
    }

    //Checks if all permissions are granted
    private fun checkPermission(permissionArray: Array<String>): Boolean {
        var allPermissionsGranted = true
        for (i in permissionArray.indices) {
            if (checkCallingOrSelfPermission(permissionArray[i]) == PackageManager.PERMISSION_DENIED)
                allPermissionsGranted = false
        }
        return allPermissionsGranted
    }

    //Invoked method from "requestPermissions"
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST) {
            var allPermissionsGranted = true
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    allPermissionsGranted = false
                    val requestAgain =
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(
                            permissions[i]
                        )
                    if (requestAgain) {
                        Toast.makeText(this, "Tilgang ikke gitt, godta for at funksjon skal funke", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Gå til innstillinger å tilatt lokasjon", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            if (allPermissionsGranted)
                enableFragment()

        }
    }

    //Creates notification channel, is needed for making notification later
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel1 = NotificationChannel("CHANNEL_1_ID", "Channel 1", NotificationManager.IMPORTANCE_HIGH)
            channel1.description = "This is channel 1"
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel1)
        }
    }

    //Static val, request code can be any number above 0
    companion object {
        private const val PERMISSION_REQUEST = 10
    }

}
