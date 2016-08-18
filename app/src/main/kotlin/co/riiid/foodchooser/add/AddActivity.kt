package co.riiid.foodchooser.add

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import co.riiid.foodchooser.R
import co.riiid.foodchooser.model.RestaurantEntry
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.sangcomz.fishbun.FishBun
import com.sangcomz.fishbun.define.Define
import kotlinx.android.synthetic.main.activity_add.*
import kotlinx.android.synthetic.main.layout_add.*
import rx.Observable
import java.io.ByteArrayOutputStream

class AddActivity : AppCompatActivity() {

    val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    val databaseReference = firebaseDatabase.getReferenceFromUrl("https://foodchooser-cf58f.firebaseio.com")
    val storageReference = firebaseStorage.getReferenceFromUrl("gs://foodchooser-cf58f.appspot.com");
    val auth = FirebaseAuth.getInstance()
    var section: Int? = null

    var isSetImage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        initToolbar()
        section = intent.getIntExtra("section", -1)

        img_post.setOnClickListener {
            FishBun.with(this)
                    .setCamera(true)
                    .setPickerCount(1)
                    .setActionBarColor(ContextCompat.getColor(this, R.color.colorPrimary),
                            ContextCompat.getColor(this, R.color.colorPrimaryDark))
                    .startAlbum()
        }
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        val ab = supportActionBar;
        ab?.let {
            it.setDisplayHomeAsUpEnabled(true);
            it.setDisplayShowHomeEnabled(true);
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Define.ALBUM_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val path = data?.getStringArrayListExtra(Define.INTENT_PATH)
                Glide.with(this)
                        .load(path?.get(0))
                        .centerCrop()
                        .into(img_post)
                isSetImage = true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_add_post, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        when (itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_send -> {
                if (isSetImage && et_content.text.length > 0)
                    send()

                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    fun send() {
        img_post.isDrawingCacheEnabled = true;
        img_post.buildDrawingCache();
        val bitmap = img_post.getDrawingCache(true)
        val baos: ByteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val data = baos.toByteArray()
        sendImage(data)
                .concatMap {
                    sendData(it)
                }.subscribe {
            if (it)
                finish()
            else
                finish()

        }
    }

    fun sendImage(data: ByteArray): Observable<String> {
        val imageStorageRef = storageReference
                .child("images/${System.currentTimeMillis()}_" +
                        "${auth.currentUser?.uid}")

        return Observable.create { subscriber ->
            val uploadTask = imageStorageRef.putBytes(data)
            uploadTask.addOnSuccessListener { it ->
                subscriber.onNext(it.downloadUrl.toString())
                subscriber.onCompleted()
            }.addOnFailureListener {
                subscriber.onNext(null)
                subscriber.onCompleted()
            }
        }
    }

    fun sendData(url: String): Observable<Boolean> {
        val section: Int = this.section ?: return Observable.just(false)
        return Observable.create { subscriber ->
            databaseReference
                    .child("restaurant")
                    .push()
                    .setValue(RestaurantEntry(
                            section,
                            url,
                            auth.currentUser?.displayName!!,
                            et_content.text.toString(),
                            System.currentTimeMillis()), "100")
                    .addOnSuccessListener {
                        subscriber.onNext(true)
                    }
                    .addOnFailureListener {
                        subscriber.onNext(false)
                    }
                    .addOnCompleteListener {
                        subscriber.onCompleted()
                    }
        }
    }

}
