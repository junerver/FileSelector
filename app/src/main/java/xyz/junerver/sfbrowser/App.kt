package xyz.junerver.sfbrowser

import android.app.Application
import android.content.Context

/**
 * Description:
 * @author Junerver
 * date: 2022/2/17-9:36
 * Email: junerver@gmail.com
 * Version: v1.0
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = this
    }

    companion object {
        lateinit var appContext: Context
    }
}