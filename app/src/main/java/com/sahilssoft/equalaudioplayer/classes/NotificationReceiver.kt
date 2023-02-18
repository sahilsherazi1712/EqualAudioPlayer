package com.sahilssoft.equalaudioplayer.classes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sahilssoft.equalaudioplayer.R
import com.sahilssoft.equalaudioplayer.activities.PlayerActivity
import com.sahilssoft.equalaudioplayer.fragments.NowPlayingFragment

class NotificationReceiver: BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            ApplicationClass.PREVIOUS -> prevNextSong(increment = false, context = context!!)
            ApplicationClass.PLAY -> if(PlayerActivity.isPlaying) pauseMusic() else playMusic()
            ApplicationClass.NEXT -> prevNextSong(increment = true, context = context!!)
            ApplicationClass.EXIT -> {
                exitApplication()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun playMusic(){
        PlayerActivity.isPlaying = true
        PlayerActivity.binding.fabPlayPausePA.setIconResource(R.drawable.ic_pause)
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_pause, 1F)
        NowPlayingFragment.binding.fabPlayPauseNP.setIconResource(R.drawable.ic_pause)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun pauseMusic(){
        PlayerActivity.isPlaying = false
        PlayerActivity.binding.fabPlayPausePA.setIconResource(R.drawable.ic_play)
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_play, 0F)
        NowPlayingFragment.binding.fabPlayPauseNP.setIconResource(R.drawable.ic_play)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun prevNextSong(increment: Boolean, context: Context){
        setSongPosition(increment = increment)
        PlayerActivity.musicService!!.createMediaPlayer()
        Glide.with(context)
            .load(PlayerActivity.musicListPA.get(PlayerActivity.songPosition).artUri)
            .apply(RequestOptions.placeholderOf(R.drawable.logo).fitCenter())
            .into(PlayerActivity.binding.imgSongPA)
        PlayerActivity.binding.tvTitlePA.text = PlayerActivity.musicListPA.get(PlayerActivity.songPosition).title
        Glide.with(context)
            .load(PlayerActivity.musicListPA.get(PlayerActivity.songPosition).artUri)
            .apply(RequestOptions.placeholderOf(R.drawable.logo).centerCrop())
            .into(NowPlayingFragment.binding.imgSongNP)
        NowPlayingFragment.binding.tvSongNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        playMusic()

        ///////////favourite work ///////////////
        PlayerActivity.favIndex = favouriteChecker(PlayerActivity.musicListPA[PlayerActivity.songPosition].id)
        if (PlayerActivity.isFavorite) PlayerActivity.binding.imgFavoritePA.setImageResource(R.drawable.ic_favorite)
        else PlayerActivity.binding.imgFavoritePA.setImageResource(R.drawable.ic_favourite_border)
    }
}