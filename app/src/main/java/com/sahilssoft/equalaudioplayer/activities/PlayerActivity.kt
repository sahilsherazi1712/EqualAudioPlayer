package com.sahilssoft.equalaudioplayer.activities

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sahilssoft.equalaudioplayer.*
import com.sahilssoft.equalaudioplayer.classes.*
import com.sahilssoft.equalaudioplayer.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity(), ServiceConnection {

    companion object{
        lateinit var musicListPA: ArrayList<Music>
        var songPosition: Int = 0
//        var mediaPlayer: MediaPlayer? = null
        var isPlaying: Boolean = false
        var musicService: MusicService? = null
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding
        var repeat: Boolean = false
        var min15: Boolean = false
        var min30: Boolean = false
        var min60: Boolean = false

        //if we play a song then press back button and again click on same song to play then it starts from starts.
        // To solve it
        var nowPlayingId: String = ""

        ////////////favourite///////////////
        var isFavorite: Boolean = false
        var favIndex: Int = -1
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themIndex])
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        //for starting service
//        val intent = Intent(this, MusicService::class.java)
//        bindService(intent,this, BIND_AUTO_CREATE)
//        startService(intent)

        ///////When we select an audio file from file manager, it will show this app in choice to open audio
        if(intent.data?.scheme.contentEquals("content")){
            val intentService = Intent(this, MusicService::class.java)
            bindService(intentService,this, BIND_AUTO_CREATE)
            startService(intentService)
            musicListPA = ArrayList()
            musicListPA.add(getMusicDetails(intent.data!!))
            Glide.with(this)
                .load(getImgArt(musicListPA[songPosition].path))
                .apply(RequestOptions.placeholderOf(R.drawable.logo).centerCrop())
                .into(binding.imgSongPA)
            binding.tvTitlePA.text = musicListPA[songPosition].title
        }else{
            initializeLayout()
        }
        binding.imgBackPA.setOnClickListener { finish() }
        binding.fabPlayPausePA.setOnClickListener {
            if (isPlaying) pauseMusic()
            else playMusic()
        }
        binding.fabNextPA.setOnClickListener { preNextSong(increment = true) }
        binding.fabPreviousPA.setOnClickListener { preNextSong(increment = false) }
        binding.seekbarPA.setOnSeekBarChangeListener(object: OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser){
                    musicService!!.mediaPlayer!!.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}

        })
        binding.imgRepeatPA.setOnClickListener {
            if (!repeat){
                repeat = true
                binding.imgRepeatPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            }else{
                repeat = false
                binding.imgRepeatPA.setColorFilter(ContextCompat.getColor(this, R.color.cool_pink))
            }
        }
        binding.imgEqualizerPA.setOnClickListener {
            try {
                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent,12)
            }catch (e: Exception){
                Toast.makeText(this, "Equalizer not supported!", Toast.LENGTH_SHORT).show()}
        }
        binding.imgTimerPA.setOnClickListener {
            val timer = min15 || min30 || min60
            if (!timer){
                showBottomSheetDialog()
            }else{
                val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle("Stop Timer")
                    .setMessage("Do you want to stop timer?")
                    .setPositiveButton("Yes"){ dialogInterface: DialogInterface, i: Int ->
                        min15 = false
                        min30 = false
                        min60 = false
                        binding.imgTimerPA.setColorFilter(ContextCompat.getColor(this,
                            R.color.cool_pink
                        ))
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
        binding.imgSharePA.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
            startActivity(Intent.createChooser(shareIntent,"Share Music File!!"))
        }
        binding.imgFavoritePA.setOnClickListener {
            if(isFavorite){
                isFavorite = false
                binding.imgFavoritePA.setImageResource(R.drawable.ic_favourite_border)
                FavouriteActivity.favouriteSongs.removeAt(favIndex)
            }else{
                isFavorite = true
                binding.imgFavoritePA.setImageResource(R.drawable.ic_favorite)
                FavouriteActivity.favouriteSongs.add(musicListPA[songPosition])
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun initializeLayout() {
        songPosition = intent.getIntExtra("index",0)
        when(intent.getStringExtra("class")){
            "FavouriteAdapter" ->{
                val intent = Intent(this, MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(FavouriteActivity.favouriteSongs)
                setLayout()
            }
            "NowPlaying" ->{
                setLayout()
                Log.e("startTimePA", "startTimePA: "+ musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.tvStartTimePA.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.tvEndTimePA.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekbarPA.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekbarPA.max = musicService!!.mediaPlayer!!.duration

                if(isPlaying) binding.fabPlayPausePA.setIconResource(R.drawable.ic_pause)
                else binding.fabPlayPausePA.setIconResource(R.drawable.ic_play)
            }
            "MusicAdapter" ->{
                //for starting service
                val intent = Intent(this, MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                setLayout()
            }
            "MainActivity" ->{
                val intent = Intent(this, MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                musicListPA.shuffle()
                setLayout()
            }
            "MusicAdapterSearch" ->{
                val intent = Intent(this, MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.musicSearchList)
                setLayout()
            }
            "FavouriteActivity" ->{
                val intent = Intent(this, MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(FavouriteActivity.favouriteSongs)
                musicListPA.shuffle()
                setLayout()
            }
            "PlaylistDetailsAdapter" ->{
                val intent = Intent(this, MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetailsActivity.currentPlaylistPos].playlist)
                setLayout()
            }
            "PlaylistDetailsActivity" ->{
                val intent = Intent(this, MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetailsActivity.currentPlaylistPos].playlist)
                musicListPA.shuffle()
                setLayout()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun createMediaPlayer() {
        try {
            if (musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPA.get(songPosition).path)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isPlaying = true
            binding.fabPlayPausePA.setIconResource(R.drawable.ic_pause)
            musicService!!.showNotification(R.drawable.ic_pause, 1F)
            binding.tvStartTimePA.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.tvEndTimePA.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekbarPA.progress = 0
            binding.seekbarPA.max = musicService!!.mediaPlayer!!.duration

            musicService!!.mediaPlayer!!.setOnCompletionListener {
                setSongPosition(increment = true)
                createMediaPlayer()
                try {setLayout()}catch (e:Exception){e.printStackTrace()}
            }

            nowPlayingId = musicListPA[songPosition].id
        }catch (e: Exception){
            return
        }
    }

    private fun setLayout() {
        Glide.with(this)
            .load(musicListPA.get(songPosition).artUri)
            .apply(RequestOptions.placeholderOf(R.drawable.logo).centerCrop())
            .into(binding.imgSongPA)
        binding.tvTitlePA.text = musicListPA.get(songPosition).title

        if (repeat) binding.imgRepeatPA.setColorFilter(ContextCompat.getColor(this,
            R.color.purple_500
        ))
        if (min15 || min30 || min60){
            binding.imgTimerPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))}

        ///////////favourite work//////////////
        favIndex = favouriteChecker(musicListPA[songPosition].id)
        if(isFavorite) binding.imgFavoritePA.setImageResource(R.drawable.ic_favorite)
        else binding.imgFavoritePA.setImageResource(R.drawable.ic_favourite_border)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun playMusic(){
        isPlaying = true
        binding.fabPlayPausePA.setIconResource(R.drawable.ic_pause)
        musicService!!.showNotification(R.drawable.ic_pause,1F)
        musicService!!.mediaPlayer!!.start()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun pauseMusic(){
        isPlaying = false
        binding.fabPlayPausePA.setIconResource(R.drawable.ic_play)
        musicService!!.showNotification(R.drawable.ic_play, 0F)
        musicService!!.mediaPlayer!!.pause()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun preNextSong(increment: Boolean){
        if(increment){
            setSongPosition(increment = true)
            setLayout()
            createMediaPlayer()
        }else{
            setSongPosition(increment = false)
            setLayout()
            createMediaPlayer()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
        musicService!!.seekBarSetup()

        //////////pause/play music on call incoming //////////
        musicService!!.audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        musicService!!.audioManager.requestAudioFocus(musicService,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 12 || resultCode == RESULT_OK)
            return
    }

    private fun showBottomSheetDialog(){
        val dialog = BottomSheetDialog(this@PlayerActivity)
        dialog.setContentView(R.layout.dialog_timer_bottom)
        dialog.show()
        dialog.findViewById<LinearLayout>(R.id.ll_15)?.setOnClickListener {
            Toast.makeText(this@PlayerActivity, "Music will stop after 15 minutes", Toast.LENGTH_SHORT).show()
            binding.imgTimerPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min15 = true
            Thread{Thread.sleep(15 * 60000)
                if (min15) exitApplication()
            }.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.ll_30)?.setOnClickListener {
            Toast.makeText(this@PlayerActivity, "Music will stop after 30 minutes", Toast.LENGTH_SHORT).show()
            binding.imgTimerPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min30 = true
            Thread{Thread.sleep(30 * 60000)
                if (min30) exitApplication()
            }.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.ll_60)?.setOnClickListener {
            Toast.makeText(this@PlayerActivity, "Music will stop after 60 minutes", Toast.LENGTH_SHORT).show()
            binding.imgTimerPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min60 = true
            Thread{Thread.sleep(60 * 60000)
                if (min60) exitApplication()
            }.start()
            dialog.dismiss()
        }
    }

    private fun getMusicDetails(contentUri: Uri): Music {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION)
            cursor = this.contentResolver.query(contentUri,projection, null, null, null)
            val dataColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            cursor!!.moveToFirst()
            val path = dataColumn?.let { cursor.getString(it) }
            val duration = durationColumn?.let { cursor.getLong(it) }
            return Music(id = "Unknown", title = path.toString(), album = "Unknown", artist = "Unknown",
                duration = duration!!.toLong(), artUri = "Unknown", path = path.toString())
        }
        finally {
            cursor?.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (musicListPA[songPosition].id == "Unknown" && !isPlaying) exitApplication()
    }
}