package com.sahilssoft.equalaudioplayer.classes

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.sahilssoft.equalaudioplayer.R
import com.sahilssoft.equalaudioplayer.activities.MainActivity
import com.sahilssoft.equalaudioplayer.activities.PlayerActivity
import com.sahilssoft.equalaudioplayer.fragments.NowPlayingFragment

class MusicService: Service(), AudioManager.OnAudioFocusChangeListener {

    private var myBinder = MyBinder() //1
    var mediaPlayer: MediaPlayer? = null //1
    private lateinit var mediaSession: MediaSessionCompat //2
    private lateinit var runnable: Runnable
    lateinit var audioManager: AudioManager

    override fun onBind(p0: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext,"My Music")
        return myBinder
    }

    inner class MyBinder: Binder(){ //1
        fun currentService(): MusicService {
            return this@MusicService
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun showNotification(playPauseBtn: Int, playbackSpeed: Float){     //2

        val intent = Intent(baseContext, MainActivity::class.java)
//        intent.putExtra("index", PlayerActivity.songPosition)
//        intent.putExtra("class","NowPlaying")
        val contentIntent = PendingIntent.getActivity(this, 0, intent,PendingIntent.FLAG_MUTABLE)

        //3
        val prevIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(
            ApplicationClass.PREVIOUS
        )
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext,0, prevIntent,PendingIntent.FLAG_MUTABLE)

        val playIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(
            ApplicationClass.PLAY
        )
        val playPendingIntent = PendingIntent.getBroadcast(baseContext,0, playIntent,PendingIntent.FLAG_MUTABLE)

        val nextIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(
            ApplicationClass.NEXT
        )
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext,0, nextIntent,PendingIntent.FLAG_MUTABLE)

        val exitIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(
            ApplicationClass.EXIT
        )
        val exitPendingIntent = PendingIntent.getBroadcast(baseContext,0, exitIntent,PendingIntent.FLAG_MUTABLE)

        val imgArt = getImgArt(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
        val image = if(imgArt != null){
            BitmapFactory.decodeByteArray(imgArt,0,imgArt.size)
        }else{
            BitmapFactory.decodeResource(resources, R.drawable.logo)
        }

        val notification = NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
            .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].title)
            .setContentText(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)
            .setContentIntent(contentIntent)
            .setSmallIcon(R.drawable.ic_music)
            .setLargeIcon(image)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.ic_nav_back,"Previous",prevPendingIntent)
            .addAction(playPauseBtn,"Play",playPendingIntent)
            .addAction(R.drawable.ic_nav_next,"Next",nextPendingIntent)
            .addAction(R.drawable.ic_exit,"Exit",exitPendingIntent)
            .build()

        //////////seekbar notification //////////
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            mediaSession.setMetadata(MediaMetadataCompat.Builder()
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer!!.duration.toLong())
                .build())
            mediaSession.setPlaybackState(PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer!!.currentPosition.toLong(), playbackSpeed)
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                .build())
        }

        startForeground(13,notification)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun createMediaPlayer() {
        try {
            if (PlayerActivity.musicService!!.mediaPlayer == null) PlayerActivity.musicService!!.mediaPlayer = MediaPlayer()
            PlayerActivity.musicService!!.mediaPlayer!!.reset()
            PlayerActivity.musicService!!.mediaPlayer!!.setDataSource(PlayerActivity.musicListPA.get(PlayerActivity.songPosition).path)
            PlayerActivity.musicService!!.mediaPlayer!!.prepare()
            PlayerActivity.binding.fabPlayPausePA.setIconResource(R.drawable.ic_pause)
            PlayerActivity.musicService!!.showNotification(R.drawable.ic_pause, 0F)
            PlayerActivity.binding.tvStartTimePA.text = formatDuration(PlayerActivity.musicService!!.mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.tvEndTimePA.text = formatDuration(PlayerActivity.musicService!!.mediaPlayer!!.duration.toLong())
            PlayerActivity.binding.seekbarPA.progress = 0
            PlayerActivity.binding.seekbarPA.max = PlayerActivity.musicService!!.mediaPlayer!!.duration
            PlayerActivity.nowPlayingId = PlayerActivity.musicListPA[PlayerActivity.songPosition].id
        }catch (e: Exception){
            return
        }
    }

    fun seekBarSetup(){
        runnable = Runnable {
            PlayerActivity.binding.tvStartTimePA.text = formatDuration(PlayerActivity.musicService!!.mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.seekbarPA.progress = PlayerActivity.musicService!!.mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable,200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable,0)
    }

    //////////pause/play music on call incoming //////////
    @SuppressLint("NewApi")
    override fun onAudioFocusChange(focusChange: Int) {
        if(focusChange <= 0){
            PlayerActivity.isPlaying = false
            PlayerActivity.binding.fabPlayPausePA.setIconResource(R.drawable.ic_play)
            NowPlayingFragment.binding.fabPlayPauseNP.setIconResource(R.drawable.ic_play)
            showNotification(R.drawable.ic_play, 0F)
            mediaPlayer!!.pause()
        }else{
            PlayerActivity.isPlaying = true
            PlayerActivity.binding.fabPlayPausePA.setIconResource(R.drawable.ic_pause)
            NowPlayingFragment.binding.fabPlayPauseNP.setIconResource(R.drawable.ic_pause)
            showNotification(R.drawable.ic_pause, 1F)
            mediaPlayer!!.start()
        }
    }
}