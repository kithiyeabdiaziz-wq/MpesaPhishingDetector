package com.example.abdiazizmpesaphishing

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val cardSms = findViewById<CardView>(R.id.cardSms)
        val cardUrl = findViewById<CardView>(R.id.cardUrl)
        val cardHistory = findViewById<CardView>(R.id.cardHistory)

        cardSms.setOnClickListener {
            val intent = Intent(this, SmsDetectionActivity::class.java)
            startActivity(intent)
        }

        cardUrl.setOnClickListener {
            val intent = Intent(this, UrlDetectionActivity::class.java)
            startActivity(intent)
        }

        cardHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
        val tvProfile = findViewById<TextView>(R.id.tvProfile)

        tvProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}