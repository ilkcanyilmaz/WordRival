package com.ilkcanyilmaz.wordrival.views

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ilkcanyilmaz.wordrival.R
import com.ilkcanyilmaz.wordrival.enums.GameDisplay

class SlidePagerAdapter(var list: List<SlideModel>, var fragment:Fragment) :
    RecyclerView.Adapter<SlidePagerAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var cvNewGame:CardView
        //var tvTitle: TextView
        // var tvDescription: TextView

        init {
            cvNewGame = itemView.findViewById(R.id.cv_newGame)
            //tvTitle = itemView.findViewById(R.id.tvTitle)
            //tvDescription = itemView.findViewById(R.id.tvd)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_slide, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val m = list[position]

        holder.cvNewGame.setOnClickListener{
            val action = HomeFragmentDirections.actionHomeFragmentToOfflineGameFragment()
            findNavController(fragment).navigate(action)
        }
        //holder.tvTitle.text = m.title
        // holder.tvDescription.setText("test61")
    }

    override fun getItemCount(): Int {
        return list.size
    }
}