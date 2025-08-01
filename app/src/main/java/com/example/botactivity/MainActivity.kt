package com.example.botactivity

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomBar: LinearLayout = findViewById(R.id.bottom_bar)
        val micIcon: ImageView = findViewById(R.id.bt_micro)

        bottomBar.setOnClickListener {
            startChatActivity()
        }

        micIcon.setOnClickListener {
            startChatActivity()
        }
        val cardHealthyHabits: MaterialCardView = findViewById(R.id.card1)
        val cardSchedule: MaterialCardView = findViewById(R.id.card2)
        val cardEmotionalState: MaterialCardView = findViewById(R.id.card3)
        val cardBetterSleep: MaterialCardView = findViewById(R.id.card4)

        val animation = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in)

        cardHealthyHabits.startAnimation(animation)
        cardSchedule.startAnimation(animation)
        cardEmotionalState.startAnimation(animation)
        cardBetterSleep.startAnimation(animation)
    }
    private fun startChatActivity() {
        val intent = Intent(this, ChatActivity::class.java)
        startActivity(intent)
    }
}