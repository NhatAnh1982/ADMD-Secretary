package com.example.aisecretary

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var inputEditText: EditText
    private lateinit var btnVoice: ImageButton
    private lateinit var btnSend: Button
    private lateinit var tvResponse: TextView
    private lateinit var tts: TextToSpeech

    private val RECORD_AUDIO_REQUEST = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputEditText = findViewById(R.id.inputEditText)
        btnVoice = findViewById(R.id.btnVoice)
        btnSend = findViewById(R.id.btnSend)
        tvResponse = findViewById(R.id.tvResponse)

        tts = TextToSpeech(this, this)

        btnVoice.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_REQUEST)
            } else {
                startVoiceRecognition()
            }
        }

        btnSend.setOnClickListener {
            val text = inputEditText.text.toString().trim()
            if (text.isNotEmpty()) {
                sendToOpenAI(text)
            }
        }
    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        try {
            startActivityForResult(intent, 100)
        } catch (e: Exception) {
            tvResponse.text = "Voice recognition not available"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = results?.get(0) ?: ""
            inputEditText.setText(spoken)
            sendToOpenAI(spoken)
        }
    }

    private fun sendToOpenAI(prompt: String) {
        tvResponse.text = "Đang xử lý..."
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = OpenAIClient.callChatCompletion(prompt)
                runOnUiThread {
                    tvResponse.text = response
                    speak(response)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    tvResponse.text = "Lỗi: ${e.message}"
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.getDefault()
        }
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID")
    }

    override fun onDestroy() {
        tts.shutdown()
        super.onDestroy()
    }
}
