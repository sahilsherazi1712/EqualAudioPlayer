package com.sahilssoft.equalaudioplayer.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sahilssoft.equalaudioplayer.classes.Playlist
import com.sahilssoft.equalaudioplayer.R
import com.sahilssoft.equalaudioplayer.activities.PlaylistActivity
import com.sahilssoft.equalaudioplayer.activities.PlaylistDetailsActivity
import com.sahilssoft.equalaudioplayer.databinding.ItemPlaylistBinding

class PlaylistAdapter(
    private val context: Context,
    private var list: ArrayList<Playlist>
    ): RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    inner class ViewHolder(val binding:ItemPlaylistBinding): RecyclerView.ViewHolder(binding.root) {
        val image = binding.imgItemPLA
        val title = binding.tvTitleItemPLA
        val delete = binding.imgDeleteItemPLA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemPlaylistBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.tvTitleItemPLA.text = list[position].name
        holder.binding.tvTitleItemPLA.isSelected = true
        holder.binding.imgDeleteItemPLA.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(context)
            builder.setTitle(list[position].name)
                .setMessage("Do you want to delete playlist?")
                .setPositiveButton("Yes"){ dialog: DialogInterface, i: Int ->
                    PlaylistActivity.musicPlaylist.ref.removeAt(position)
                    refreshPlaylist()
                    dialog.dismiss()
                }
                .setNegativeButton("No"){ dialogInterface: DialogInterface, i: Int ->
                    dialogInterface.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PlaylistDetailsActivity::class.java)
            intent.putExtra("index",position)
            ContextCompat.startActivity(context,intent,null)
        }

        if (PlaylistActivity.musicPlaylist.ref[position].playlist.size > 0){
            Glide.with(context)
                .load(PlaylistActivity.musicPlaylist.ref[position].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.logo).centerCrop())
                .into(holder.binding.imgItemPLA)
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshPlaylist(){
        list = ArrayList()
        list.addAll(PlaylistActivity.musicPlaylist.ref)
        notifyDataSetChanged()
    }
}