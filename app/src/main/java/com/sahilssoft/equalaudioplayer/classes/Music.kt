package com.sahilssoft.equalaudioplayer.classes

import android.media.MediaMetadataRetriever
import com.sahilssoft.equalaudioplayer.activities.FavouriteActivity
import com.sahilssoft.equalaudioplayer.activities.PlayerActivity
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

data class Music(
    val id: String,
    val title: String,
    val album: String,
    val artist: String,
    val path: String,
    val duration: Long = 0,
    val artUri: String
)

fun formatDuration(duration: Long): String{
    val minutes = TimeUnit.MINUTES.convert(duration,TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration,TimeUnit.MILLISECONDS) -
            minutes*TimeUnit.SECONDS.convert(1,TimeUnit.MINUTES))
    return String.format("%02d:%02d", minutes,seconds)
}

fun getImgArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

fun setSongPosition(increment: Boolean){
    if (!PlayerActivity.repeat){
        if (increment){
            if (PlayerActivity.musicListPA.size - 1 == PlayerActivity.songPosition){
                PlayerActivity.songPosition = 0
            }else{
                ++PlayerActivity.songPosition
            }
        }else{
            if (0 == PlayerActivity.songPosition){
                PlayerActivity.songPosition = PlayerActivity.musicListPA.size - 1
            }else{
                --PlayerActivity.songPosition
            }
        }
    }
}

fun exitApplication(){
    if (PlayerActivity.musicService != null){
        PlayerActivity.musicService!!.audioManager.abandonAudioFocus(PlayerActivity.musicService)
        PlayerActivity.musicService!!.stopForeground(true)
        PlayerActivity.musicService!!.mediaPlayer!!.release()
        PlayerActivity.musicService = null
    }
    exitProcess(1)
}

fun favouriteChecker(id: String): Int{
    PlayerActivity.isFavorite = false
    FavouriteActivity.favouriteSongs.forEachIndexed { index, music ->
        if (id == music.id){
            PlayerActivity.isFavorite = true
            return index
        }
    }
    return -1
}

////////////////////Playlist/////////////////////
class Playlist{
    lateinit var name: String
    lateinit var playlist: ArrayList<Music>
    lateinit var createdBy: String
    lateinit var createdOn: String
}

class MusicPlaylist{
    var ref: ArrayList<Playlist> = ArrayList()
}

//////////function to make sure that, if any song is deleted from root directory (File Manager) or not
fun checkPlaylist(playlist: ArrayList<Music>): ArrayList<Music>{
    playlist.forEachIndexed { index, music ->
        val  file = File(music.path)
        if (!file.exists()){
            playlist.removeAt(index)
        }
    }
    return playlist
}