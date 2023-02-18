package com.sahilssoft.equalaudioplayer.activities

import android.annotation.SuppressLint
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sahilssoft.equalaudioplayer.classes.MusicPlaylist
import com.sahilssoft.equalaudioplayer.classes.Playlist
import com.sahilssoft.equalaudioplayer.adapters.PlaylistAdapter
import com.sahilssoft.equalaudioplayer.R
import com.sahilssoft.equalaudioplayer.databinding.ActivityPlaylistBinding
import com.sahilssoft.equalaudioplayer.databinding.DialogAddPlaylistBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PlaylistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistBinding
    private lateinit var playlistAdapter: PlaylistAdapter

    companion object{
        var musicPlaylist: MusicPlaylist = MusicPlaylist()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themIndex])
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgBackPLA.setOnClickListener { finish() }

        val list = ArrayList<String>()
        list.add("My Songs")
        list.add("My Travelling Songs")
        list.add("Night Listening Songs")
        list.add("Raps Songs Collection")
        list.add("Travelling Songs")
        list.add("Free Time Songs")
        playlistAdapter = PlaylistAdapter(this@PlaylistActivity, musicPlaylist.ref)
        binding.rvPlaylist.adapter = playlistAdapter

        binding.fabAddPlaylist.setOnClickListener {
            showPlaylistDialog()
        }
    }

    private fun showPlaylistDialog(){
        val view = LayoutInflater.from(this@PlaylistActivity).inflate(R.layout.dialog_add_playlist,binding.root,false)
        val binder = DialogAddPlaylistBinding.bind(view)
        val builder = MaterialAlertDialogBuilder(this)
        builder.setView(view)
            .setTitle("Playlist Details")
            .setPositiveButton("ADD"){ dialog: DialogInterface, i: Int ->
                val playlistName = binder.etAddPlaylistNameDPL.text.toString()
                val createdBy = binder.etAddPersonNameDPL.text.toString()
                if (playlistName.isNotEmpty() && createdBy.isNotEmpty()){
                    addPlaylist(name = playlistName, createdBy = createdBy)
                }
                dialog.dismiss()
            }.show()
    }

    private fun addPlaylist(name: String, createdBy: String) {
        var playlistExists = false
        for (i in musicPlaylist.ref){
            if (name == i.name){
                playlistExists = true
                break
            }
        }
        if (playlistExists){
            Toast.makeText(this@PlaylistActivity, "Playlist Exists!!", Toast.LENGTH_SHORT).show()
        }else{
            val tempPlaylist = Playlist()
            tempPlaylist.name = name
            tempPlaylist.playlist = ArrayList()
            tempPlaylist.createdBy = createdBy
            val calendar = Calendar.getInstance().time
            val simpleDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            tempPlaylist.createdOn = simpleDateFormat.format(calendar)
            musicPlaylist.ref.add(tempPlaylist)
            playlistAdapter.refreshPlaylist()
        }
    }

    //coding to update the ui of playlist at same time, as a song is added in it
    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        playlistAdapter.notifyDataSetChanged()
    }
}