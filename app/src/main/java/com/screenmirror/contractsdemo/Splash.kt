package com.screenmirror.contractsdemo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.screenmirror.contractsdemo.MainHolderActivity
import com.screenmirror.contractsdemo.utilities.datastore.DataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        checkDarkMode()
        startActivity()

        lifecycleScope.launch(Dispatchers.IO) {
            DataStore.setIsFirstTime(this@Splash, true)
        }
    }

    private fun startActivity() {
        /****** Create Thread that will sleep for 5 seconds */
        val background: Thread = object : Thread() {
            override fun run() {
                try {
                    // Thread will sleep for 5 seconds
                    sleep((3 * 1000).toLong())

                    val i = Intent(baseContext, MainHolderActivity::class.java)
                    startActivity(i)
                    finish()
                } catch (e: Exception) {
                }
            }
        }
        // start thread
        background.start()
    }

    private fun checkDarkMode() {

        this.lifecycleScope.launch(Dispatchers.IO) {
            val darkModeStatus = DataStore.getDarkMode(this@Splash).first()
            if (darkModeStatus) {
                withContext(Dispatchers.Main) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            } else {
                withContext(Dispatchers.Main) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }
    }
}