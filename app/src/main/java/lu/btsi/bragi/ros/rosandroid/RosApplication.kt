package lu.btsi.bragi.ros.rosandroid

import android.app.Application
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.ImageLoader
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RosApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val options = DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .build()

        val config = ImageLoaderConfiguration.Builder(this)
            .defaultDisplayImageOptions(options)
            .build()

        ImageLoader.getInstance().init(config)
    }
}