package com.sahilssoft.equalaudioplayer.activities

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.sahilssoft.equalaudioplayer.adapters.MusicAdapter
import com.sahilssoft.equalaudioplayer.R
import com.sahilssoft.equalaudioplayer.classes.checkPlaylist
import com.sahilssoft.equalaudioplayer.databinding.ActivityPlaylistDetailsBinding

class PlaylistDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistDetailsBinding
    private lateinit var musicAdapter: MusicAdapter

    companion object{
        var currentPlaylistPos: Int = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themIndex])
        binding = ActivityPlaylistDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.imgBackPDA.setOnClickListener { finish() }

        currentPlaylistPos = intent.extras?.get("index") as Int
        try {
            PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist =
                checkPlaylist(playlist = PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist)
        }catch (e: Exception){e.printStackTrace()}
//        PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist.addAll(MainActivity.MusicListMA)
//        PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist.shuffle()
        musicAdapter = MusicAdapter(this,
            PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist, playlistDetails = true)
        binding.rvPDA.adapter = musicAdapter
        binding.fabShufflePDA.setOnClickListener {
            val intent = Intent(this@PlaylistDetailsActivity, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class","PlaylistDetailsActivity")
            startActivity(intent)
        }
        binding.btnAddSongPDA.setOnClickListener {
            startActivity(Intent(this, SelectionActivity::class.java))
        }
        binding.btnRemoveAllPDA.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Remove")
                .setMessage("Do you want to remove all songs from playlist?")
                .setPositiveButton("Yes"){ dialogInterface: DialogInterface, i: Int ->
                    PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist.clear()
                    musicAdapter.refreshPlaylist()
                    dialogInterface.dismiss()
                }
                .setNegativeButton("No"){ dialogInterface: DialogInterface, i: Int ->
                    dialogInterface.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        binding.tvPlaylistNamePDA.text = PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].name
        binding.tvDetailsPDA.text = "Total ${musicAdapter.itemCount} Songs. \n\n" +
                "Created On:\n ${PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].createdOn}\n\n" +
                " -- ${PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].createdBy}"
        if (musicAdapter.itemCount > 0){
            Glide.with(this)
                .load(PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.logo).centerCrop())
                .into(binding.imgPDA)
            binding.fabShufflePDA.visibility = View.VISIBLE
        }

        musicAdapter.notifyDataSetChanged()

        ////////////////storing favorites data ////////////////////
        val editor = getSharedPreferences("FAVOURITE", MODE_PRIVATE).edit()
        val jsonPlaylistString = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("MusicPlaylist",jsonPlaylistString)
        editor.apply()
    }
}