package com.example.abdiazizmpesaphishing

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SmsDetectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms_detection)

        val etSmsInput = findViewById<EditText>(R.id.etSmsInput)
        val btnAnalyzeSms = findViewById<Button>(R.id.btnAnalyzeSms)
        val btnClearSms = findViewById<Button>(R.id.btnClearSms)
        val progressBar = findViewById<ProgressBar>(R.id.progressBarSms)
        val tvCharCount = findViewById<TextView>(R.id.tvCharCount)

        // Character Counter Logic
        etSmsInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tvCharCount.text = "${s?.length ?: 0} characters"
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Clear Button Logic
        btnClearSms.setOnClickListener {
            etSmsInput.text.clear()
            Toast.makeText(this, "Cleared", Toast.LENGTH_SHORT).show()
        }

        btnAnalyzeSms.setOnClickListener {
            val smsText = etSmsInput.text.toString().trim()

            // No Internet Check
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            if (activeNetwork == null || !activeNetwork.isConnected) {
                Toast.makeText(this, "No internet connection! Please check your data/Wi-Fi.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (smsText.isEmpty()) {
                Toast.makeText(this, "Please paste a message first", Toast.LENGTH_SHORT).show()
            } else {
                progressBar.visibility = View.VISIBLE
                btnAnalyzeSms.isEnabled = false

                ApiClient.apiService.analyzeSms(SmsRequest(smsText)).enqueue(object : Callback<AnalysisResponse> {
                    override fun onResponse(call: Call<AnalysisResponse>, response: Response<AnalysisResponse>) {
                        if (isFinishing || isDestroyed) return
                        progressBar.visibility = View.GONE
                        btnAnalyzeSms.isEnabled = true

                        if (response.isSuccessful && response.body() != null) {
                            val result = response.body()!!
                            showResultDialog(result, smsText)
                        } else {
                            Toast.makeText(this@SmsDetectionActivity, "API Error: ${response.message()}", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<AnalysisResponse>, t: Throwable) {
                        if (isFinishing || isDestroyed) return
                        progressBar.visibility = View.GONE
                        btnAnalyzeSms.isEnabled = true
                        Toast.makeText(this@SmsDetectionActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
            }
        }
    }

    private fun showResultDialog(result: AnalysisResponse, originalText: String) {
        if (isFinishing || isDestroyed) return

        FirebaseAuthHelper(this).saveAnalysisToHistory(
            type = "SMS",
            input = originalText,
            classification = result.classification,
            riskScore = result.risk_score,
            explanation = result.explanation,
            recommendation = result.recommendation
        )

        val message = "Classification: ${result.classification}\n\n" +
                "Risk Score: ${result.risk_score}/100\n\n" +
                "Explanation: ${result.explanation}\n\n" +
                "Recommendation: ${result.recommendation}"

        AlertDialog.Builder(this)
            .setTitle("AI Analysis Result")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}