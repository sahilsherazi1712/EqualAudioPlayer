package com.sahilssoft.equalaudioplayer.activities

import android.content.DialogInterface
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sahilssoft.equalaudioplayer.BuildConfig
import com.sahilssoft.equalaudioplayer.databinding.ActivitySettingsBinding
import com.sahilssoft.equalaudioplayer.classes.exitApplication

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setTheme(MainActivity.currentThemeNav[MainActivity.themIndex])
        setContentView(binding.root)
        supportActionBar?.title = "Settings"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        when(MainActivity.themIndex){
            0 -> binding.imgPinkSA.setBackgroundColor(Color.YELLOW)
            1 -> binding.imgBlueSA.setBackgroundColor(Color.YELLOW)
            2 -> binding.imgPurpleSA.setBackgroundColor(Color.YELLOW)
            3 -> binding.imgGreenSA.setBackgroundColor(Color.YELLOW)
            4 -> binding.imgBlackSA.setBackgroundColor(Color.YELLOW)
        }
        binding.imgPinkSA.setOnClickListener { saveTheme(index = 0) }
        binding.imgBlueSA.setOnClickListener { saveTheme(index = 1) }
        binding.imgPurpleSA.setOnClickListener { saveTheme(index = 2) }
        binding.imgGreenSA.setOnClickListener { saveTheme(index = 3) }
        binding.imgBlackSA.setOnClickListener { saveTheme(index = 4) }

        binding.tvVersionNameSA.text = setVersionDetails()
        binding.imgSortBySA.setOnClickListener {
            val menuList = arrayOf("Recently Added", "Song Title", "Song Size")
            var currentSort = MainActivity.sortOrder
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Sorting")
                .setPositiveButton("OK"){ dialog: DialogInterface, i: Int ->
                    val editor = getSharedPreferences("SORTING", MODE_PRIVATE).edit()
                    editor.putInt("sortOrder",currentSort)
                    editor.apply()
                }
                .setSingleChoiceItems(menuList, currentSort){ dialog: DialogInterface, position: Int ->
                    currentSort = position
                }
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
        }
    }

    private fun saveTheme(index: Int){
        if(MainActivity.themIndex != index){
            val editor = getSharedPreferences("THEMES", MODE_PRIVATE).edit()
            editor.putInt("themeIndex",index)
            editor.apply()
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Apply Theme")
                .setMessage("Do you want to apply theme?")
                .setPositiveButton("Yes"){ dialog: DialogInterface, i: Int ->
                    exitApplication()
                }
                .setNegativeButton("No"){ dialog: DialogInterface, i: Int ->
                    dialog.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        }
    }

    private fun setVersionDetails(): String{
        return "Version Name: ${BuildConfig.VERSION_NAME}"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}