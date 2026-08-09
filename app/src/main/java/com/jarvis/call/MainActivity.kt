package com.jarvis.call

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telecom.TelecomManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.ANSWER_PHONE_CALLS,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.READ_CONTACTS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val status = findViewById<TextView>(R.id.statusText)
        val grantBtn = findViewById<Button>(R.id.grantPermissionsBtn)
        val roleBtn = findViewById<Button>(R.id.becomeScreenerBtn)

        grantBtn.setOnClickListener {
            ActivityCompat.requestPermissions(this, permissions, 100)
        }

        roleBtn.setOnClickListener {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                startActivityForResult(intent, 200)
            }
        }

        val prefs = getSharedPreferences("jarvis_prefs", MODE_PRIVATE)
        val name = prefs.getString("owner_name", "there")
        val filter = prefs.getString("call_filter", "everyone")
        status.text = "Hey $name, CallJarvis is set to handle: $filter\n\n1) Tap 'Grant Permissions'\n2) Tap 'Make CallJarvis my call screener'\n3) Call the phone from another number to test"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        perms: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, perms, grantResults)
        val status = findViewById<TextView>(R.id.statusText)
        val allGranted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        status.text = if (allGranted) "Permissions granted. Now tap 'Make Jarvis my call screener'."
        else "Some permissions were denied — Jarvis needs all of them to work."
    }
}
