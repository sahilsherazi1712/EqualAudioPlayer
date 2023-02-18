package com.sahilssoft.equalaudioplayer.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sahilssoft.equalaudioplayer.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentThemeNav[MainActivity.themIndex])
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "About"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.tvAbout.text = "Developed By: Sahal Sherazi" +
                "\n\nIf you give feedback. It will like it so much."
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}