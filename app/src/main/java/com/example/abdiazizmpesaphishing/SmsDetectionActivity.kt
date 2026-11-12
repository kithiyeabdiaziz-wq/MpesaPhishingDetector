package com.example.abdiazizmpesaphishing

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
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
        val progressBar = findViewById<ProgressBar>(R.id.progressBarSms)

        btnAnalyzeSms.setOnClickListener {
            val smsText = etSmsInput.text.toString().trim()

            if (smsText.isEmpty()) {
                Toast.makeText(this, "Please paste a message first", Toast.LENGTH_SHORT).show()
            } else {
                progressBar.visibility = View.VISIBLE
                btnAnalyzeSms.isEnabled = false

                ApiClient.apiService.analyzeSms(SmsRequest(smsText)).enqueue(object : Callback<AnalysisResponse> {
                    override fun onResponse(call: Call<AnalysisResponse>, response: Response<AnalysisResponse>) {
                        // SAFE CHECK: Ensure screen is still open before updating UI
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
                        // SAFE CHECK: Ensure screen is still open before updating UI
                        if (isFinishing || isDestroyed) return

                        progressBar.visibility = View.GONE
                        btnAnalyzeSms.isEnabled = true

                        // Show the EXACT error so we know why it failed
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