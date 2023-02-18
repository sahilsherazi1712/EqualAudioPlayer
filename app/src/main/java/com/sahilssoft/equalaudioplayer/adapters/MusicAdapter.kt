package com.sahilssoft.equalaudioplayer.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sahilssoft.equalaudioplayer.classes.Music
import com.sahilssoft.equalaudioplayer.R
import com.sahilssoft.equalaudioplayer.activities.MainActivity
import com.sahilssoft.equalaudioplayer.activities.PlayerActivity
import com.sahilssoft.equalaudioplayer.activities.PlaylistActivity
import com.sahilssoft.equalaudioplayer.activities.PlaylistDetailsActivity
import com.sahilssoft.equalaudioplayer.databinding.ItemMusicBinding
import com.sahilssoft.equalaudioplayer.classes.formatDuration

class MusicAdapter(
    private val context: Context,
    private var list: ArrayList<Music>,
    private var playlistDetails: Boolean = false,
    private var selectionActivity: Boolean = false
    ): RecyclerView.Adapter<MusicAdapter.ViewHolder>() {

    inner class ViewHolder(val binding:ItemMusicBinding): RecyclerView.ViewHolder(binding.root) {
        val image = binding.imgSong
        val title = binding.tvSongName
        val album = binding.tvSongAlbum
        val duration = binding.tvSongDuration
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemMusicBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.tvSongName.text = list.get(position).title
        holder.binding.tvSongAlbum.text = list.get(position).album
        holder.binding.tvSongDuration.text = formatDuration(list.get(position).duration)
        Glide.with(context)
            .load(list.get(position).artUri)
            .apply(RequestOptions.placeholderOf(R.drawable.logo).fitCenter())
            .into(holder.binding.imgSong)

        when{
            playlistDetails ->{
                holder.itemView.setOnClickListener {
                    sendIntent(pos = position, ref = "PlaylistDetailsAdapter")
                }
            }

            selectionActivity ->{
                holder.itemView.setOnClickListener {
                    if (addSong(list[position])){
                        holder.binding.root.setBackgroundColor(ContextCompat.getColor(context,
                            R.color.cool_pink
                        ))
                    }else{
                        holder.binding.root.setBackgroundColor(ContextCompat.getColor(context,
                            R.color.white
                        ))
                    }
                }
            }
            else ->{
            holder.itemView.setOnClickListener {
                if(MainActivity.isSearch){
                    sendIntent(pos = position, ref = "MusicAdapterSearch")
                }else if (list[position].id == PlayerActivity.nowPlayingId){
                    sendIntent(pos = PlayerActivity.songPosition, ref = "NowPlaying")
                } else{
                    sendIntent(pos = position, ref = "MusicAdapter")
                }
            }
            }
        }
    }

    private fun addSong(song: Music): Boolean {
        PlaylistActivity.musicPlaylist.ref[PlaylistDetailsActivity.currentPlaylistPos].playlist.forEachIndexed { index, music ->
            if (song.id == music.id){
                PlaylistActivity.musicPlaylist.ref[PlaylistDetailsActivity.currentPlaylistPos].playlist.removeAt(index)
                return false
            }
        }
        PlaylistActivity.musicPlaylist.ref[PlaylistDetailsActivity.currentPlaylistPos].playlist.add(song)
        return true
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateMusicList(searchList: ArrayList<Music>){
        list = ArrayList()
        list.addAll(searchList)
        notifyDataSetChanged()
    }

    private fun sendIntent(pos: Int, ref: String) {
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("index", pos)
        intent.putExtra("class",ref)
        ContextCompat.startActivity(context,intent,null)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshPlaylist(){
        list = ArrayList()
        list = PlaylistActivity.musicPlaylist.ref[PlaylistDetailsActivity.currentPlaylistPos].playlist
        notifyDataSetChanged()
    }
}