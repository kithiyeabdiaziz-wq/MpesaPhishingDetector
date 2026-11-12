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

class UrlDetectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_url_detection)

        val etUrlInput = findViewById<EditText>(R.id.etUrlInput)
        val btnAnalyzeUrl = findViewById<Button>(R.id.btnAnalyzeUrl)
        val progressBar = findViewById<ProgressBar>(R.id.progressBarUrl)

        btnAnalyzeUrl.setOnClickListener {
            val urlText = etUrlInput.text.toString().trim()

            if (urlText.isEmpty()) {
                Toast.makeText(this, "Please enter a URL first", Toast.LENGTH_SHORT).show()
            } else if (!urlText.startsWith("http://") && !urlText.startsWith("https://")) {
                Toast.makeText(this, "URL must start with http:// or https://", Toast.LENGTH_SHORT).show()
            } else {
                progressBar.visibility = View.VISIBLE
                btnAnalyzeUrl.isEnabled = false

                ApiClient.apiService.analyzeUrl(UrlRequest(urlText)).enqueue(object : Callback<UrlAnalysisResponse> {
                    override fun onResponse(call: Call<UrlAnalysisResponse>, response: Response<UrlAnalysisResponse>) {
                        // SAFE CHECK
                        if (isFinishing || isDestroyed) return

                        progressBar.visibility = View.GONE
                        btnAnalyzeUrl.isEnabled = true

                        if (response.isSuccessful && response.body() != null) {
                            val result = response.body()!!
                            showResultDialog(result, urlText)
                        } else {
                            Toast.makeText(this@UrlDetectionActivity, "API Error: ${response.message()}", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<UrlAnalysisResponse>, t: Throwable) {
                        // SAFE CHECK
                        if (isFinishing || isDestroyed) return

                        progressBar.visibility = View.GONE
                        btnAnalyzeUrl.isEnabled = true
                        Toast.makeText(this@UrlDetectionActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
            }
        }
    }

    private fun showResultDialog(result: UrlAnalysisResponse, originalUrl: String) {
        if (isFinishing || isDestroyed) return

        FirebaseAuthHelper(this).saveAnalysisToHistory(
            type = "URL",
            input = originalUrl,
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
            .setTitle("URL Analysis Result")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}