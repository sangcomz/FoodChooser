package co.riiid.foodchooser.main

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.view.Menu
import android.view.MenuItem
import co.riiid.foodchooser.R
import co.riiid.foodchooser.deep.EventActivity
import co.riiid.foodchooser.login.LoginActivity
import com.google.android.gms.appinvite.AppInvite
import com.google.android.gms.appinvite.AppInviteReferral
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.android.synthetic.main.activity_main.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {
    override fun onConnectionFailed(p0: ConnectionResult) {
        println("ConnectionResult :::: ${p0.errorMessage}")
//        throw UnsupportedOperationException()
    }


    var firebaseAuth: FirebaseAuth? = null
    var firebaseUser: FirebaseUser? = null
    var gridLayoutManager: GridLayoutManager? = null

    val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initToolbar()


        val googleApiClient: GoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(AppInvite.API)
                .build();

        val autoLaunchDeepLink = true
        AppInvite.AppInviteApi.getInvitation(googleApiClient, this, autoLaunchDeepLink)
                .setResultCallback { result ->
                    val i = result.invitationIntent
                    val deepLink = AppInviteReferral.getDeepLink(i)
                    if (deepLink != null && deepLink.contains("Spring")) {
                        startActivity(Intent(this, EventActivity::class.java))
                        finish()
                    } else
                        initRemoteConfig()
                }
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
    }

    private fun initRecyclerView() {
        gridLayoutManager = GridLayoutManager(this, 2)
        rv.layoutManager = gridLayoutManager
        rv.adapter = MainListAdapter(this)

    }

    private fun checkAuth() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth?.currentUser
        if (firebaseAuth == null ||
                (firebaseAuth != null && firebaseUser == null))
            goLoginActivity()
        else
            initRecyclerView()


    }

    private fun goLoginActivity() {

        val i = Intent(this, LoginActivity::class.java)
        i.putExtra("title", remoteConfig.getString("app_title"))
        i.putExtra("image_login", remoteConfig.getString("image_login"))
        startActivity(i)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        when (itemId) {
            R.id.action_sign_out -> {
                firebaseAuth?.signOut()
                goLoginActivity()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun initRemoteConfig() {
        remoteConfigFetchObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    checkAuth()
                }
    }

    private fun remoteConfigFetchObservable(): Observable<FirebaseRemoteConfig> {
        remoteConfig.setDefaults(R.xml.remote_config_default)
        return Observable.create { subscriber ->
            remoteConfig.fetch(60L)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful)
                            remoteConfig.activateFetched()
                        subscriber.onNext(remoteConfig)
                    }
        }
    }

}
