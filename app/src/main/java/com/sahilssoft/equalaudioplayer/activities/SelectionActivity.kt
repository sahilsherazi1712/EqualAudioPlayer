package com.sahilssoft.equalaudioplayer.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import com.sahilssoft.equalaudioplayer.adapters.MusicAdapter
import com.sahilssoft.equalaudioplayer.databinding.ActivitySelectionBinding

class SelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectionBinding
    private lateinit var musicAdapter: MusicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themIndex])
        binding = ActivitySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvSelection.setHasFixedSize(true)
        binding.rvSelection.setItemViewCacheSize(10)
        musicAdapter = MusicAdapter(this, MainActivity.MusicListMA, selectionActivity = true)
        binding.rvSelection.adapter = musicAdapter

        binding.searchSA.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { return true }
            override fun onQueryTextChange(newText: String?): Boolean {
                MainActivity.musicSearchList = ArrayList()
                if(newText != null){
                    val userInput = newText.lowercase()
                    for (song in MainActivity.MusicListMA)
                        if (song.title.lowercase().contains(userInput))
                            MainActivity.musicSearchList.add(song)
                    MainActivity.isSearch = true
                    musicAdapter.updateMusicList(searchList = MainActivity.musicSearchList)
                }
                return true
            }
        })
        binding.imgBackSA.setOnClickListener { finish() }
    }
}