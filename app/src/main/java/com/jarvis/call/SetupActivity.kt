package com.jarvis.call

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SetupActivity : AppCompatActivity() {

    private var page = 0
    private val totalPages = 4

    private val runtimePerms = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.ANSWER_PHONE_CALLS,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.READ_CONTACTS
    )

    private lateinit var prefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("jarvis_prefs", MODE_PRIVATE)

        // If setup was already finished before AND everything is still actually
        // granted, skip straight past onboarding. If something got revoked later
        // (e.g. user turned off a permission in system settings), land back on
        // the permissions page instead of pretending it's still fine.
        if (prefs.getBoolean("setup_complete", false) && allPermsActuallyGranted() && roleActuallyHeld()) {
            startActivity(Intent(this, StatusActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_setup)
        if (prefs.getBoolean("setup_complete", false)) {
            page = 3 // jump straight to the permissions step if only that's missing
        }
        renderPage()

        findViewById<Button>(R.id.nextBtn).setOnClickListener { onNext() }

        findViewById<Button>(R.id.grantPermsBtn).setOnClickListener {
            ActivityCompat.requestPermissions(this, runtimePerms, 100)
        }
        findViewById<Button>(R.id.grantRoleBtn).setOnClickListener {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                startActivityForResult(roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING), 200)
            } else {
                Toast.makeText(this, "This device doesn't support call screening role", Toast.LENGTH_LONG).show()
            }
        }
        findViewById<Button>(R.id.openSettingsBtn).setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.fromParts("package", packageName, null)
            startActivity(intent)
        }
    }

    // Re-check real system state every time the user comes back to this screen —
    // this is what fixes "I granted it but it still says not given": we stop
    // trusting a saved flag and just ask Android directly, every time.
    override fun onResume() {
        super.onResume()
        if (::prefs.isInitialized && page == 3) {
            refreshPermissionStatus()
        }
    }

    private fun allPermsActuallyGranted(): Boolean =
        runtimePerms.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }

    private fun roleActuallyHeld(): Boolean {
        val roleManager = getSystemService(RoleManager::class.java) ?: return false
        return try { roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) } catch (e: Exception) { false }
    }

    private fun refreshPermissionStatus() {
        val statusPerms = findViewById<TextView>(R.id.statusPerms)
        val statusRole = findViewById<TextView>(R.id.statusRole)

        if (allPermsActuallyGranted()) {
            statusPerms.text = "●"
            statusPerms.setTextColor(0xFF4F8C82.toInt())
        } else {
            statusPerms.text = "○"
            statusPerms.setTextColor(0xFF8A5F26.toInt())
        }

        if (roleActuallyHeld()) {
            statusRole.text = "●"
            statusRole.setTextColor(0xFF4F8C82.toInt())
        } else {
            statusRole.text = "○"
            statusRole.setTextColor(0xFF8A5F26.toInt())
        }

        if (allPermsActuallyGranted() && roleActuallyHeld()) {
            findViewById<Button>(R.id.nextBtn).text = "Finish"
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, perms: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, perms, grantResults)
        // Don't trust grantResults alone (some OEMs report it inconsistently) —
        // re-check the real permission state directly instead.
        refreshPermissionStatus()
        if (!allPermsActuallyGranted()) {
            Toast.makeText(this, "Some permissions were denied. If you don't see a popup next time, tap 'Open app settings' and enable them manually.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200) {
            refreshPermissionStatus()
        }
    }

    private fun onNext() {
        when (page) {
            0 -> {
                val name = findViewById<EditText>(R.id.nameInput).text.toString().trim()
                val phone = findViewById<EditText>(R.id.phoneInput).text.toString().trim()
                if (name.isEmpty()) { toast("Type your name first"); return }
                if (phone.isEmpty()) { toast("Type your phone number"); return }
                prefs.edit().putString("owner_name", name).putString("owner_phone", phone).apply()
            }
            1 -> {
                val voice = if (findViewById<RadioButton>(R.id.voiceMale).isChecked) "male" else "female"
                prefs.edit().putString("voice_gender", voice).apply()
            }
            2 -> {
                val filter = when {
                    findViewById<RadioButton>(R.id.filterStrangersOnly).isChecked -> "strangers_only"
                    findViewById<RadioButton>(R.id.filterContactsOnly).isChecked -> "contacts_only"
                    findViewById<RadioButton>(R.id.filterOff).isChecked -> "off"
                    else -> "everyone"
                }
                prefs.edit().putString("call_filter", filter).apply()
            }
            3 -> {
                if (!allPermsActuallyGranted() || !roleActuallyHeld()) {
                    toast("Grant both items above before finishing")
                    return
                }
                prefs.edit().putBoolean("setup_complete", true).apply()
                startActivity(Intent(this, StatusActivity::class.java))
                finish()
                return
            }
        }
        if (page < totalPages - 1) {
            page++
            renderPage()
        }
    }

    private fun renderPage() {
        for (i in 0 until totalPages) {
            val pageView = when (i) {
                0 -> findViewById<View>(R.id.page0)
                1 -> findViewById<View>(R.id.page1)
                2 -> findViewById<View>(R.id.page2)
                else -> findViewById<View>(R.id.page3)
            }
            pageView.visibility = if (i == page) View.VISIBLE else View.GONE

            val dot = when (i) {
                0 -> findViewById<View>(R.id.dot0)
                1 -> findViewById<View>(R.id.dot1)
                2 -> findViewById<View>(R.id.dot2)
                else -> findViewById<View>(R.id.dot3)
            }
            dot.setBackgroundColor(if (i <= page) 0xFFE4A94F.toInt() else 0xFF3A3D45.toInt())
        }

        findViewById<Button>(R.id.nextBtn).text = if (page == totalPages - 1) "Finish" else "Next"
        if (page == 3) refreshPermissionStatus()

        // Selectable cards toggle their radio buttons
        wireCard(R.id.voiceFemaleCard, R.id.voiceFemale, R.id.voiceMale)
        wireCard(R.id.voiceMaleCard, R.id.voiceMale, R.id.voiceFemale)
        wireCard(R.id.filterEveryoneCard, R.id.filterEveryone, R.id.filterStrangersOnly, R.id.filterContactsOnly, R.id.filterOff)
        wireCard(R.id.filterStrangersCard, R.id.filterStrangersOnly, R.id.filterEveryone, R.id.filterContactsOnly, R.id.filterOff)
        wireCard(R.id.filterContactsCard, R.id.filterContactsOnly, R.id.filterEveryone, R.id.filterStrangersOnly, R.id.filterOff)
        wireCard(R.id.filterOffCard, R.id.filterOff, R.id.filterEveryone, R.id.filterStrangersOnly, R.id.filterContactsOnly)
    }

    private fun wireCard(cardId: Int, selectId: Int, vararg othersId: Int) {
        findViewById<View>(cardId).setOnClickListener {
            findViewById<RadioButton>(selectId).isChecked = true
            othersId.forEach { findViewById<RadioButton>(it).isChecked = false }
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
