package co.riiid.foodchooser.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import co.riiid.foodchooser.R
import co.riiid.foodchooser.main.MainActivity
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {


    private val TAG = "LoginActivity"
    private val RC_SIGN_IN = 9001
    private var googleApiClient: GoogleApiClient? = null
    private var auth: FirebaseAuth? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initGoogleApiClient()
        initAuth()
        setTitle(intent.getStringExtra("title"))
        setBackground()
        sign_in_button.setOnClickListener {
            signIn()
        }
    }

    private fun initGoogleApiClient() {
        val gso = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).
                requestEmail()
                .build()
        googleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build()
    }

    private fun setTitle(title: String) {
        txt_login_title.text = title
    }

    private fun setBackground() {
        Glide.with(this)
                .load(Uri.parse(intent.getStringExtra("image_login")))
                .into(img_bg)
    }

    private fun initAuth() {
        auth = FirebaseAuth.getInstance()
    }

    private fun signIn() {
        startActivityForResult(Auth.GoogleSignInApi.getSignInIntent(googleApiClient), RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            println("result.signInAccount ${result.signInAccount}")
            if (result.isSuccess) {
                firebaseAuthWithGoogle(result.signInAccount)
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        val googleSignInAccount = acct ?: return

        Log.d(TAG, "firebaseAuthWithGooogle:" + googleSignInAccount.id)
        val credential = GoogleAuthProvider.getCredential(googleSignInAccount.idToken, null)
        auth?.let {
            it.signInWithCredential(credential).addOnCompleteListener(this, { task ->
                Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful)

                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful) {
                    Log.w(TAG, "signInWithCredential", task.exception)
                    Toast.makeText(this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            })
        }

    }


    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult)
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show()
    }
}
