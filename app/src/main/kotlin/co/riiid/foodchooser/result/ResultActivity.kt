package co.riiid.foodchooser.result

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import co.riiid.foodchooser.R
import co.riiid.foodchooser.add.AddActivity
import co.riiid.foodchooser.model.RestaurantEntry
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_result.*
import kotlinx.android.synthetic.main.layout_result.*


import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class ResultActivity : AppCompatActivity() {

    val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    val databaseReference = firebaseDatabase.getReferenceFromUrl("https://foodchooser-cf58f.firebaseio.com")
    val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        initToolbar()
        getRestaurant(intent.getIntExtra("section", -1))
        fab.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            intent.putExtra("section", this.intent.getIntExtra("section", -1))
            startActivity(intent)
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

    fun getRestaurant(section: Int) {
        if (section == -1) return
        getRestaurantObservable(section)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    it?.let {
                        if (it.children.count() > 0) {
                            val rand = System.nanoTime() % it.children.count()
                            val list = arrayListOf<RestaurantEntry>()
                            it.children.forEach {
                                list.add(it.getValue(RestaurantEntry::class.java))
                            }
                            showRestaurant(list[rand.toInt()])
                        } else {
                            noResult()
                        }
                    }
                }

    }

    fun noResult() {
        finish()
    }

    fun showRestaurant(restaurantEntry: RestaurantEntry) {
        Glide
                .with(this)
                .load(restaurantEntry.imageUrl)
                .centerCrop()
                .into(img_post)

        txt_content.text = restaurantEntry.content

    }

    fun getRestaurantObservable(section: Int): Observable<DataSnapshot> {
        val TAG = "GET_RESTAURANT"
        val restaurantRef = databaseReference.child("restaurant")
        return Observable
                .create { subscriber ->
                    restaurantRef
                            .orderByChild("section")
                            .startAt(section.toDouble() - 1, "section")
                            .endAt(section.toDouble(), "section")
                            .addListenerForSingleValueEvent(object : ValueEventListener {

                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    subscriber.onNext(dataSnapshot)
                                }

                                override fun onCancelled(firebaseError: DatabaseError?) {
                                    Log.w(TAG, "Failed to read value.", firebaseError?.toException());
                                }
                            })
                }
    }
}
