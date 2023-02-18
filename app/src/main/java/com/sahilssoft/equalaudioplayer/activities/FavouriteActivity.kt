package com.sahilssoft.equalaudioplayer.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.sahilssoft.equalaudioplayer.adapters.FavouriteAdapter
import com.sahilssoft.equalaudioplayer.classes.Music
import com.sahilssoft.equalaudioplayer.classes.checkPlaylist
import com.sahilssoft.equalaudioplayer.databinding.ActivityFavouriteBinding

class FavouriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavouriteBinding
    private lateinit var favouriteAdapter: FavouriteAdapter

    companion object{
        var favouriteSongs: ArrayList<Music> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themIndex])
        favouriteSongs = checkPlaylist(favouriteSongs)
        binding = ActivityFavouriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgBackFA.setOnClickListener { finish() }

        favouriteAdapter = FavouriteAdapter(this, favouriteSongs)
        binding.rvFavorite.adapter = favouriteAdapter

        if(favouriteSongs.size <1) binding.fabShuffleFA.visibility = View.INVISIBLE
        binding.fabShuffleFA.setOnClickListener {
            val intent = Intent(this@FavouriteActivity, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class","FavouriteActivity")
            startActivity(intent)
        }
    }
}