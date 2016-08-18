package co.riiid.foodchooser.deep

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import co.riiid.foodchooser.R
import com.google.android.gms.appinvite.AppInvite
import com.google.android.gms.appinvite.AppInviteReferral
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient


class DeepLinkActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deep_link)

        // Build GoogleApiClient with AppInvite API for receiving deep links
        val googleApiClient: GoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(AppInvite.API)
                .build();

        val autoLaunchDeepLink = true
        AppInvite.AppInviteApi.getInvitation(googleApiClient, this, autoLaunchDeepLink)
                .setResultCallback { result ->
                    println("AppInviteReferral.getDeepLink(result.invitationIntent) " +
                            "${AppInviteReferral.getDeepLink(result.invitationIntent)}")


                }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
//        throw UnsupportedOperationException()
    }
}
