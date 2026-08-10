package com.jarvis.call

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StatusActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)

        val prefs = getSharedPreferences("jarvis_prefs", MODE_PRIVATE)
        val name = prefs.getString("owner_name", "there")
        val filter = when (prefs.getString("call_filter", "everyone")) {
            "strangers_only" -> "unknown numbers only"
            "contacts_only" -> "known contacts only"
            "off" -> "nobody — screening is off"
            else -> "everyone"
        }
        val voice = prefs.getString("voice_gender", "female")

        findViewById<TextView>(R.id.greeting).text = "Hey $name, JarvisHere is on"
        findViewById<TextView>(R.id.summary).text = "Answering for: $filter\nVoice: $voice"

        findViewById<Button>(R.id.editSetupBtn).setOnClickListener {
            startActivity(Intent(this, SetupActivity::class.java))
            finish()
        }
    }
}
