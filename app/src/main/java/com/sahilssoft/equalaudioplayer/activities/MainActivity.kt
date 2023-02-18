package com.sahilssoft.equalaudioplayer.activities

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.sahilssoft.equalaudioplayer.*
import com.sahilssoft.equalaudioplayer.adapters.MusicAdapter
import com.sahilssoft.equalaudioplayer.classes.Music
import com.sahilssoft.equalaudioplayer.classes.MusicPlaylist
import com.sahilssoft.equalaudioplayer.classes.exitApplication
import com.sahilssoft.equalaudioplayer.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var musicAdapter: MusicAdapter

    companion object{
        lateinit var MusicListMA: ArrayList<Music>
        lateinit var musicSearchList: ArrayList<Music>
        var isSearch: Boolean = false

        //////////////theme color change /////////
        var themIndex: Int = 0
        val currentTheme = arrayOf(
            R.style.coolPink,
            R.style.coolBlue,
            R.style.coolPurple,
            R.style.coolGreen,
            R.style.coolBlack
        )
        val currentThemeNav = arrayOf(
            R.style.coolPinkNav,
            R.style.coolBlueNav,
            R.style.coolPurpleNav,
            R.style.coolGreenNav,
            R.style.coolBlackNav
        )
        val currentGradient = arrayOf(
            R.drawable.gradient_pink,
            R.drawable.gradient_blue,
            R.drawable.gradient_purple,
            R.drawable.gradient_green,
            R.drawable.gradient_black
        )

        ///////////// sort order //////////////
        var sortOrder: Int = 0
        val sortingList = arrayOf(MediaStore.Audio.Media.DATE_ADDED + " DESC", MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.SIZE + " DESC")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ////////////// Load Changed Theme /////////////
        val themeEditor = getSharedPreferences("THEMES", MODE_PRIVATE)
        themIndex = themeEditor.getInt("themeIndex",0)
        setTheme(currentThemeNav[themIndex])
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //navigation drawer
        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (requestPermission()){
            initializeLayout()

            ////////////////fetching favorites data ////////////////////
            FavouriteActivity.favouriteSongs = ArrayList()
            val editor = getSharedPreferences("FAVOURITE", MODE_PRIVATE)
            val jsonStr = editor.getString("FavouriteSongs", null)
            val typeToken = object: TypeToken<ArrayList<Music>>(){}.type
            if(jsonStr != null){
                val data: ArrayList<Music> = GsonBuilder().create().fromJson(jsonStr,typeToken)
                FavouriteActivity.favouriteSongs.addAll(data)
            }

            /////////////// fetching playlist ///////////////////////
            PlaylistActivity.musicPlaylist = MusicPlaylist()
            val jsonPlaylistString = editor.getString("MusicPlaylist",null)
            if(jsonPlaylistString != null){
                val dataPlaylist: MusicPlaylist = GsonBuilder().create().fromJson(jsonPlaylistString, MusicPlaylist::class.java)
                PlaylistActivity.musicPlaylist = dataPlaylist
            }
        }

        binding.btnShuffle.setOnClickListener {
            val intent = Intent(this@MainActivity, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class","MainActivity")
            startActivity(intent)
        }
        binding.btnFavourite.setOnClickListener {
            val intent = Intent(this@MainActivity, FavouriteActivity::class.java)
            startActivity(intent)
        }
        binding.btnPlaylist.setOnClickListener {
            val intent = Intent(this@MainActivity, PlaylistActivity::class.java)
            startActivity(intent)
        }
        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.navFeedback -> {
                    startActivity(Intent(this@MainActivity, FeedbackActivity::class.java))
                }
                R.id.navSettings -> {
                    startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                }
                R.id.navAbout -> {
                    startActivity(Intent(this@MainActivity, AboutActivity::class.java))
                }
                R.id.navExit -> {
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setTitle("Exit")
                        .setMessage("Do you want to close app?")
                        .setPositiveButton("Yes"){ dialogInterface: DialogInterface, i: Int ->
                            exitApplication()
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
            true
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initializeLayout() {
        isSearch = false
        val sortEditor = getSharedPreferences("SORTING", MODE_PRIVATE)
        sortOrder = sortEditor.getInt("sortOrder", 0)
        MusicListMA = getAllAudios()
        musicAdapter = MusicAdapter(this@MainActivity, MusicListMA)
        binding.musicRv.setHasFixedSize(true)
        binding.musicRv.setItemViewCacheSize(13)
        binding.musicRv.adapter = musicAdapter
        binding.tvSongCount.text = "Total Songs: " + musicAdapter.itemCount
    }

    private fun requestPermission(): Boolean{
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),10)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 10){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                //below two lines, to refresh the activity
//                finish()
//                startActivity(intent)
                initializeLayout()
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),10)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("Range")
    private fun getAllAudios(): ArrayList<Music>{
        val list = ArrayList<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + " !=0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val cursor = this.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,projection,selection, null,
            sortingList[sortOrder], null)

        if (cursor != null){
            if (cursor.moveToFirst()){
                do {
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val albumC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val artistC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val durationC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))

                    val albumIdC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString()
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUriC = Uri.withAppendedPath(uri,albumIdC).toString()

                    val music = Music(id = idC, title = titleC, album = albumC, artist = artistC, path = pathC, duration = durationC, artUri = artUriC)
                    val file = File(music.path)
                    if (file.exists()){
                        list.add(music)
                    }
                }while (cursor.moveToNext())
                cursor.close()
            }
        }
        return list
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!PlayerActivity.isPlaying && PlayerActivity.musicService != null){
            exitApplication()
        }
    }

    override fun onResume() {
        super.onResume()
        ////////////////storing favorites data ////////////////////
        val editor = getSharedPreferences("FAVOURITE", MODE_PRIVATE).edit()
        val jsonStr = GsonBuilder().create().toJson(FavouriteActivity.favouriteSongs)
        editor.putString("FavouriteSongs",jsonStr)
        val jsonPlaylistString = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("MusicPlaylist",jsonPlaylistString)
        editor.apply()

        ////////////for sorting //////////
        val sortEditor = getSharedPreferences("SORTING", MODE_PRIVATE)
        val sortValue = sortEditor.getInt("sortOrder", 0)
        if(sortOrder != sortValue){
            sortOrder = sortValue
            MusicListMA = getAllAudios()
            musicAdapter.updateMusicList(MusicListMA)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu,menu)
        ///////for setting gradient
        findViewById<LinearLayout>(R.id.ll_nav_gradient)?.setBackgroundResource(currentGradient[themIndex])
        val searchView = menu.findItem(R.id.menu_search)?.actionView as SearchView
        searchView.setOnQueryTextListener(object: OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean { return true }
            override fun onQueryTextChange(newText: String?): Boolean {
                musicSearchList = ArrayList()
                if(newText != null){
                    val userInput = newText.lowercase()
                    for (song in MusicListMA)
                        if (song.title.lowercase().contains(userInput))
                            musicSearchList.add(song)
                    isSearch = true
                    musicAdapter.updateMusicList(searchList = musicSearchList)
                }
                return true
            }

        })
        return super.onCreateOptionsMenu(menu)
    }
}