package com.sahilssoft.equalaudioplayer.classes

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

//before it the service class has been made and audio is played through it
class ApplicationClass: Application() {
    companion object{
        const val CHANNEL_ID = "channel1"
        const val PLAY = "play"
        const val PREVIOUS = "previous"
        const val NEXT = "next"
        const val EXIT = "exit"
    }
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(CHANNEL_ID, "Now Playing Song", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = "This is an important channel for showing song!!"
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}