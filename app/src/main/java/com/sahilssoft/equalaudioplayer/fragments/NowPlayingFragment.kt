package com.sahilssoft.equalaudioplayer.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sahilssoft.equalaudioplayer.R
import com.sahilssoft.equalaudioplayer.activities.MainActivity
import com.sahilssoft.equalaudioplayer.activities.PlayerActivity
import com.sahilssoft.equalaudioplayer.databinding.FragmentNowPlayingBinding
import com.sahilssoft.equalaudioplayer.classes.setSongPosition

class NowPlayingFragment : Fragment() {

    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentNowPlayingBinding
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        requireContext().theme.applyStyle(MainActivity.currentThemeNav[MainActivity.themIndex],true)
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        binding = FragmentNowPlayingBinding.bind(view)
        binding.root.visibility = View.INVISIBLE
        binding.fabPlayPauseNP.setOnClickListener {
            if (PlayerActivity.isPlaying) pauseMusic() else playMusic()
        }
        binding.fabNextNP.setOnClickListener {
            setSongPosition(increment = true)
            PlayerActivity.musicService!!.createMediaPlayer()
            Glide.with(this)
                .load(PlayerActivity.musicListPA.get(PlayerActivity.songPosition).artUri)
                .apply(RequestOptions.placeholderOf(R.drawable.logo).centerCrop())
                .into(binding.imgSongNP)
            binding.tvSongNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            PlayerActivity.musicService!!.showNotification(R.drawable.ic_pause, 1F)
            playMusic()
        }
        binding.root.setOnClickListener {
            val intent = Intent(requireActivity(), PlayerActivity::class.java)
            intent.putExtra("index", PlayerActivity.songPosition)
            intent.putExtra("class","NowPlaying")
            Log.e("startTimeNP", "startTimeNP: "+ PlayerActivity.musicService!!.mediaPlayer!!.currentPosition.toLong())
            startActivity(intent)
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        binding.tvSongNameNP.isSelected = true
        if (PlayerActivity.musicService != null){
            binding.root.visibility = View.VISIBLE
            Glide.with(this)
                .load(PlayerActivity.musicListPA.get(PlayerActivity.songPosition).artUri)
                .apply(RequestOptions.placeholderOf(R.drawable.logo).centerCrop())
                .into(binding.imgSongNP)
            binding.tvSongNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            if (PlayerActivity.isPlaying){
                binding.fabPlayPauseNP.setIconResource(R.drawable.ic_pause)
            }else{
                binding.fabPlayPauseNP.setIconResource(R.drawable.ic_play)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun playMusic(){
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        binding.fabPlayPauseNP.setIconResource(R.drawable.ic_pause)
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_pause, 1F)
        PlayerActivity.binding.fabPlayPausePA.setIconResource(R.drawable.ic_pause)
        PlayerActivity.isPlaying = true
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun pauseMusic(){
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        binding.fabPlayPauseNP.setIconResource(R.drawable.ic_play)
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_play, 0F)
        PlayerActivity.binding.fabPlayPausePA.setIconResource(R.drawable.ic_play)
        PlayerActivity.isPlaying = false
    }
}