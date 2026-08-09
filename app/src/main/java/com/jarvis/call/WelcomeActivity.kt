package com.jarvis.call

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("jarvis_prefs", MODE_PRIVATE)
        val alreadySetUp = prefs.getBoolean("setup_complete", false)

        if (alreadySetUp) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_welcome)

        val nameInput = findViewById<EditText>(R.id.nameInput)
        val phoneInput = findViewById<EditText>(R.id.phoneInput)
        val voiceGroup = findViewById<RadioGroup>(R.id.voiceGroup)
        val filterGroup = findViewById<RadioGroup>(R.id.filterGroup)
        val doneBtn = findViewById<Button>(R.id.doneBtn)

        doneBtn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Type your name first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (phone.isEmpty()) {
                Toast.makeText(this, "Type your phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val voice = if (voiceGroup.checkedRadioButtonId == R.id.voiceMale) "male" else "female"

            val filter = when (filterGroup.checkedRadioButtonId) {
                R.id.filterStrangersOnly -> "strangers_only"
                R.id.filterContactsOnly -> "contacts_only"
                R.id.filterOff -> "off"
                else -> "everyone"
            }

            prefs.edit()
                .putString("owner_name", name)
                .putString("owner_phone", phone)
                .putString("voice_gender", voice)
                .putString("call_filter", filter)
                .putBoolean("setup_complete", true)
                .apply()

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
