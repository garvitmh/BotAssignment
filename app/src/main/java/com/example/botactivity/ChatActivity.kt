package com.example.botactivity

import ai.ultravox.Transcript
import ai.ultravox.UltravoxSession
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.OnBackPressedCallback
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var session: UltravoxSession
    private lateinit var recyclerView: RecyclerView
    private lateinit var editTextMessage: EditText
    private lateinit var buttonAction: ImageButton
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var progressBar: ProgressBar

    private val messageList = mutableListOf<ChatMessage>()
    private var audioManager: AudioManager? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("Ultravox", "RECORD_AUDIO permission granted. Initializing Ultravox session.")
                startChatSession()
            } else {
                Log.e("Ultravox", "RECORD_AUDIO permission denied. Cannot use Ultravox voice features.")
                Toast.makeText(this, "Microphone permission denied. Voice features will not work.", Toast.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
                editTextMessage.isEnabled = true
                buttonAction.isEnabled = false
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        setupViews()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        session = UltravoxSession(applicationContext, lifecycleScope)
        setupUltravoxListeners()
        checkAndRequestPermissions()
        setupInputListeners()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d("ChatActivity", "System back button pressed, but prevented default action.")
                Toast.makeText(this@ChatActivity, "Please use the 'Back' arrow on toolbar to exit.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupViews() {
        val backButton: ImageButton = findViewById(R.id.backButton)
        recyclerView = findViewById(R.id.recyclerViewChat)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonAction = findViewById(R.id.buttonAction)
        progressBar = findViewById(R.id.chatProgressBar)

        backButton.setOnClickListener { finish() }

        chatAdapter = ChatAdapter(messageList)
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = chatAdapter
    }

    private fun checkAndRequestPermissions() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED -> {
                startChatSession()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startChatSession() {
        progressBar.visibility = View.VISIBLE
        editTextMessage.isEnabled = false
        buttonAction.isEnabled = false

        lifecycleScope.launch {
            try {
                val joinUrl = BackendApi.createCallAndGetJoinUrl()
                session.joinCall(joinUrl)
                Log.d("UltravoxSession", "Successfully joined call with URL: $joinUrl")
            } catch (e: Exception) {
                Log.e("Ultravox", "Error getting joinUrl or joining call: ${e.message}", e)
                Toast.makeText(this@ChatActivity, "Connection Error: ${e.message}", Toast.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
                editTextMessage.isEnabled = true
            }
        }
    }

    private fun setupUltravoxListeners() {
        session.listen("status") {
            runOnUiThread {
                Log.d("UltravoxStatus", "Session status changed: ${session.status.name}")
                if (session.status.live) {
                    progressBar.visibility = View.GONE
                    editTextMessage.isEnabled = true
                    buttonAction.isEnabled = true
                    session.micMuted = true
                    Log.d("UltravoxStatus", "Session is LIVE. Mic muted. Audio routing setup.")

                    audioManager?.let {
                        it.setMode(AudioManager.MODE_NORMAL)
                        it.setSpeakerphoneOn(true)
                        it.setStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            (it.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 0.8).toInt(),
                            0
                        )
                        Log.d("AudioRouting", "Speakerphone ON, Stream MUSIC mode NORMAL.")
                    }

                } else {
                    editTextMessage.isEnabled = false
                    buttonAction.isEnabled = false
                    Log.d("UltravoxStatus", "Session not LIVE. Current status: ${session.status.name}")
                }
            }
        }

        session.listen("transcripts") {
            session.lastTranscript?.let { transcript ->
                Log.d("UltravoxTranscript", "Received transcript: Speaker=${transcript.speaker}, Text='${transcript.text}', Final=${transcript.isFinal}")
                runOnUiThread { updateMessageInUi(transcript) }
            }
        }
    }

    private fun setupInputListeners() {
        editTextMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                buttonAction.setImageResource(if (s.isNullOrEmpty()) R.drawable.bt_micro else R.drawable.send_icon)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        buttonAction.setOnClickListener {
            val messageText = editTextMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                session.setOutputMedium(Transcript.Medium.TEXT)
                session.sendText(messageText)
                editTextMessage.text.clear()
                Log.d("UltravoxInput", "Sent text message: '$messageText'")
            } else {
                toggleMicrophoneInput()
                Log.d("UltravoxInput", "Toggled microphone input.")
            }
        }
    }

    private fun toggleMicrophoneInput() {
        if (session.micMuted) {
            session.micMuted = false
            Toast.makeText(this, "Listening...", Toast.LENGTH_SHORT).show()
            buttonAction.setImageResource(R.drawable.mic_active_icon)
            session.setOutputMedium(Transcript.Medium.VOICE)
            Log.d("UltravoxMic", "Microphone UNMUTED (listening).")
        } else {
            session.micMuted = true
            Toast.makeText(this, "Stopped listening.", Toast.LENGTH_SHORT).show()
            buttonAction.setImageResource(R.drawable.bt_micro)
            Log.d("UltravoxMic", "Microphone MUTED (stopped listening).")
        }
    }

    private fun updateMessageInUi(transcript: Transcript) {
        val isUserSpeaker = transcript.speaker == Transcript.Role.USER
        val message = ChatMessage(transcript.text, isUserSpeaker, transcript.isFinal)

        Log.d("UltravoxUI", "Updating UI for message: Speaker=${transcript.speaker}, Text='${transcript.text}', Final=${transcript.isFinal}")

        val lastMessageIndex = if (messageList.isNotEmpty()) messageList.size - 1 else -1

        if (lastMessageIndex != -1 && messageList[lastMessageIndex].isUser == isUserSpeaker && !messageList[lastMessageIndex].isFinal) {
            messageList[lastMessageIndex] = message
            chatAdapter.notifyItemChanged(lastMessageIndex)
            Log.d("UltravoxUI", "Modified existing message at index $lastMessageIndex.")
        } else {
            messageList.add(message)
            chatAdapter.notifyItemInserted(messageList.size - 1)
            Log.d("UltravoxUI", "Added new message at index ${messageList.size - 1}.")
        }
        recyclerView.scrollToPosition(messageList.size - 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        session.leaveCall()
        audioManager?.let {
            if (it.isSpeakerphoneOn) {
                it.setSpeakerphoneOn(false)
            }
            it.setMode(AudioManager.MODE_NORMAL)
        }
    }
}