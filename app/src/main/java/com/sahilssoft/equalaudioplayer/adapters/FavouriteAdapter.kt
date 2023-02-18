package com.sahilssoft.equalaudioplayer.adapters

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
import com.sahilssoft.equalaudioplayer.activities.PlayerActivity
import com.sahilssoft.equalaudioplayer.databinding.ItemFavouriteBinding

class FavouriteAdapter(
    private val context: Context,
    private var list: ArrayList<Music>
    ): RecyclerView.Adapter<FavouriteAdapter.ViewHolder>() {

    inner class ViewHolder(val binding:ItemFavouriteBinding): RecyclerView.ViewHolder(binding.root) {
        val image = binding.imgItemFA
        val title = binding.tvTitleItemFA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemFavouriteBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.tvTitleItemFA.text = list[position].title
        Glide.with(context)
            .load(list[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.logo).fitCenter())
            .into(holder.binding.imgItemFA)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra("index", position)
            intent.putExtra("class","FavouriteAdapter")
            ContextCompat.startActivity(context,intent,null)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}