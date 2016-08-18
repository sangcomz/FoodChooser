package co.riiid.foodchooser.main


import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.riiid.foodchooser.R
import co.riiid.foodchooser.result.ResultActivity

class MainListAdapter(val activity: MainActivity) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var list = listOf("4, 5, 6번", "7, 8번", "3, 2번", "1 번")

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as? Holder)?.setItem(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup?,
                                    viewType: Int): RecyclerView.ViewHolder? {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.item_main, parent, false)
        view.setTag(R.id.cv, view.findViewById(R.id.cv))
        view.setTag(R.id.txt_exit, view.findViewById(R.id.txt_exit))

        return Holder(view)
    }

    override fun getItemCount(): Int = list.size

    inner class Holder(val view: View) : RecyclerView.ViewHolder(view) {

        fun setItem(position: Int) {

            var cv: CardView = view.getTag(R.id.cv) as CardView
            val txtExit: TextView = view.getTag(R.id.txt_exit) as TextView
            txtExit.text = list[position]
            cv.setOnClickListener {
                val intent = Intent(activity, ResultActivity::class.java)
                intent.putExtra("section", position + 1)
                activity.startActivity(intent)
            }

        }

    }

}