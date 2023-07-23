package com.screenmirror.contractsdemo

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.screenmirror.contractsdemo.BuildConfig
import com.screenmirror.contractsdemo.R;
import com.screenmirror.contractsdemo.databinding.ActivityMainHolderBinding
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.screenmirror.contractsdemo.dlna_cling.DlnaClingObserver
import com.screenmirror.contractsdemo.utilities.Common
import com.screenmirror.contractsdemo.utilities.extensions.ViewExtensions.hide
import com.screenmirror.contractsdemo.utilities.extensions.ViewExtensions.visible
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.delay
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.registry.DefaultRegistryListener
import org.fourthline.cling.registry.Registry
import org.fourthline.cling.support.avtransport.callback.Stop

class MainHolderActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainHolderBinding
    var isAdLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainHolderBinding.inflate(layoutInflater)
        setContentView(binding.root)



        setSupportActionBar(binding.appBarMainHolder.toolbar)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        //    val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main_holder)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        setDLNA()
        MobileAds.initialize(this) {

            loadBanner(this, navController)


            // setUpInterstitialAd()
        }
        navController.addOnDestinationChangedListener { controller, destination, arguments ->


            when (destination.id) {
                R.id.nav_home -> {
                    binding.appBarMainHolder.contentInclude.relativeLayout.hide()
                    Log.e("destination", "nav_home")
                }

                R.id.webView -> {
                    Log.e("destination", "webView")

                }
                R.id.fragmentMore -> {
                    Log.e("destination", "fragmentMore")

                }
                R.id.fragmentMediaPreview -> {
                    Log.e("destination", "fragmentMediaPreview")

                }
                R.id.fragmentMediaCasting -> {

                }


                else -> {
                    binding.appBarMainHolder.contentInclude.relativeLayout.visible()
                    setUpInterstitialAd()
                    Log.e("destination", "else")
                }
            }
        }
    }

    private fun setDLNA() {
        devices = ArrayList()
        dlnaClingObserver = DlnaClingObserver(this, object : DefaultRegistryListener() {
            override fun deviceAdded(registry: Registry?, device: Device<*, *, *>?) {
                if (device?.details?.modelDetails?.modelDescription?.contains("DMR") == true) {
                    devices?.add(device)
                }
            }

            override fun afterShutdown() {
                super.afterShutdown()
            }

            override fun deviceRemoved(registry: Registry, device: Device<*, *, *>) {
                val position = devices?.indexOf(device)
                devices?.remove(device)
            }
        }, this)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_holder, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main_holder)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        androidWebServer?.stop()
        androidWebServer = null
        try {
            stopCasting { }

        } catch (e: Exception) {

        }
    }

    companion object {
        private var dlnaClingObserver: DlnaClingObserver? = null

        @JvmStatic
        var devices: ArrayList<Device<*, *, *>>? = null

        @JvmStatic
        var androidWebServer: MyServerJava? = null


        @JvmStatic
        fun setSelectionDevice(device: Device<*, *, *>) {
            dlnaClingObserver?.selectionDevice = device
        }

        fun getSelectionDevice(): Device<*, *, *> {
            return dlnaClingObserver?.selectionDevice!!
        }

        @JvmStatic
        fun increaseVolume() {
            dlnaClingObserver?.setVolume(1, object : DlnaClingObserver.SimpleExecuteCallback() {
                override fun callback(success: Boolean) {
                }

            })
        }

        @JvmStatic
        fun decreaseVolume() {
            dlnaClingObserver?.setVolume(-1, object : DlnaClingObserver.SimpleExecuteCallback() {
                override fun callback(success: Boolean) {
                }

            })
        }

        @JvmStatic
        fun stopCasting(stopStatus: (Boolean) -> Unit) {
            val transportService: Service<out Device<*, *, *>, out Service<*, *>> =
                dlnaClingObserver?.transportService ?: return
            dlnaClingObserver?.execute(object : Stop(transportService) {
                override fun success(invocation: ActionInvocation<*>?) {
                    stopStatus(true)
                }

                override fun failure(
                    actionInvocation: ActionInvocation<*>?,
                    upnpResponse: UpnpResponse,
                    s: String
                ) {
                    stopStatus(false)
                }
            })
        }

        @JvmStatic
        fun play(url: String, itemType: Int, playStatus: (Boolean) -> Unit) {
            try {
                stopCasting() {
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            dlnaClingObserver?.autoPlay(
                url,
                itemType,
                object : DlnaClingObserver.SimpleExecuteCallback() {
                    override fun callback(success: Boolean) {
                        playStatus(success)
                    }

                })
        }

        @JvmStatic
        suspend fun startServer(path: String) {
            try {
                androidWebServer?.stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            delay(1000)
            Log.e("path", path)
            androidWebServer = MyServerJava(4000, path)
            try {
                androidWebServer?.start()
            } catch (e: Exception) {
                Log.e("error", e.message!!)
            }
        }
    }

    var adView: AdView? = null
    private fun loadBanner(context: Context, navController: NavController) {
        // Create an ad request.
        adView = AdView(context)
        binding.appBarMainHolder.contentInclude.adView.addView(adView)
        if (BuildConfig.DEBUG) {
            adView?.adUnitId = getString(R.string.testBannerId)
        } else {
            adView?.adUnitId = getString(R.string.bannerId)
        }

        val adSize: AdSize? = Common.getAdSize(this, binding.appBarMainHolder.contentInclude.adView)
        // Step 4 - Set the adaptive ad size on the ad view.
        adView?.adSize = adSize

        val adRequest = AdRequest
            .Builder().build()
        // Step 5 - Start loading the ad in the background.
        adView?.loadAd(adRequest)
        adView?.adListener = object : AdListener() {

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                binding.appBarMainHolder.contentInclude.relativeLayout.hide()

                Log.d("ads", "failed ${p0.message}")
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                if (navController.currentDestination?.id == R.id.nav_home) {
                    Log.e("frag", "let")
                    binding.appBarMainHolder.contentInclude.relativeLayout.hide()
                } else {
                    Log.e("frag", "letnot")
                    binding.appBarMainHolder.contentInclude.relativeLayout.visible()
                }
            }

        }
    }


    private var mInterstitialAd: InterstitialAd? = null
    fun setUpInterstitialAd() {
        if (mInterstitialAd != null) {
            return
        }
        val adRequest = AdRequest.Builder().build()

        if (BuildConfig.DEBUG) {
            InterstitialAd.load(this, getString(R.string.testInterstialId), adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        mInterstitialAd = null
                        Log.e("add", "onAdFailedToLoad")
                        isAdLoaded = false
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        mInterstitialAd = interstitialAd
                        isAdLoaded = true
                        Log.e("add", "onAdLoad 1 ")
                    }
                })
        } else {
            InterstitialAd.load(this, getString(R.string.interstialId), adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        mInterstitialAd = null
                        Log.e("add", "onAdFailedToLoad 2 ")
                        isAdLoaded = false
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        mInterstitialAd = interstitialAd
                        isAdLoaded = true
                        Log.e("add", "onAdLoad 3 ")
                    }
                })
        }
    }

    fun showInterstitialAd(goForword: () -> Unit = {}) {
        if (mInterstitialAd != null) {

            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mInterstitialAd = null
                    goForword()
                    Log.e("add", "onAdDismissedFullScreenContent")
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    //   setUpInterstitialAd()
                    mInterstitialAd = null
                    goForword()
                    Log.e("add", "onAdFailedToShowFullScreenContent")
                }

                override fun onAdShowedFullScreenContent() {
                    //  setUpInterstitialAd()
                    Log.e("add", "onAdShowedFullScreenContent")
                }
            }
            mInterstitialAd?.show(this)
            isAdLoaded = false

        } else {
            goForword()
        }
    }
}