package co.riiid.foodchooser

import android.app.Application

import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Created by sangcomz on 5/20/16.
 */
class AppApplication : Application() {
    var mFirebaseAnalytics: FirebaseAnalytics? = null


    override fun onCreate() {
        super.onCreate()
        initFirebaseAnalytics()
    }

    fun initFirebaseAnalytics() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }


}
