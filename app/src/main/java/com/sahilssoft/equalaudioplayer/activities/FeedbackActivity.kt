package com.sahilssoft.equalaudioplayer.activities

import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.sahilssoft.equalaudioplayer.databinding.ActivityFeedbackBinding
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class FeedbackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentThemeNav[MainActivity.themIndex])
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Feedback"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSendFA.setOnClickListener {
            val feedbackMsg = binding.etMsgFA.text.toString() + "\n" + binding.etEmailFA.text.toString()
            val subject = binding.etTopicFA.text.toString()
            val username = "sahilsherazi1712@gmail.com"
            val pass = "Sahil@02468"
            val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (feedbackMsg.isNotEmpty() && subject.isNotEmpty() && cm.activeNetworkInfo?.isConnectedOrConnecting == true){
                Thread{
                    try {
                        val properties = Properties()
                        properties["mail.smtp.auth"] = "true"
                        properties["mail.smtp.starttls.enable"] = "true"
                        properties["mail.smtp.host"] = "smtp.gmail.com"
                        properties["mail.smtp.port"] = "587"
                        val session = Session.getInstance(properties,object: Authenticator(){
                            override fun getPasswordAuthentication(): PasswordAuthentication {
                                return PasswordAuthentication(username,pass)
                            }
                        })
                        val mail = MimeMessage(session)
                        mail.subject = subject
                        mail.setText(feedbackMsg)
                        mail.setFrom(InternetAddress(username))
                        mail.setRecipients(Message.RecipientType.TO, InternetAddress.parse(username))
                        Transport.send(mail)

                    }catch (e: Exception){
                        Toast.makeText(this@FeedbackActivity, e.toString(), Toast.LENGTH_SHORT).show()
                    }
                }.start()
                Toast.makeText(this@FeedbackActivity, "Thanks for your feedback!!", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this@FeedbackActivity, "Something went wrong!!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}