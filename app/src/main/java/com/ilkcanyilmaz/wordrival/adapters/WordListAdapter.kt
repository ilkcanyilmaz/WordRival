package com.ilkcanyilmaz.wordrival.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ilkcanyilmaz.wordrival.databinding.ItemSaveWordBinding
import com.ilkcanyilmaz.wordrival.models.Question

class WordListAdapter(userList: List<Question>, context: Context) :
    RecyclerView.Adapter<WordListAdapter.WordViewHolder>() {
    var userList: List<Question>
    var itemListener: ItemListener? = null
    var playButtonListener: PlayButtonListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val productRowBinding: ItemSaveWordBinding =
            ItemSaveWordBinding.inflate(layoutInflater, parent, false)
        return WordViewHolder(productRowBinding)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bindto(userList[position])
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    interface ItemListener {
        fun onFriendRequestResponse(friend: Question, isFriend: Int)
    }

    interface PlayButtonListener {
        fun onPlayButtonClick(friend: Question)
    }

    inner class WordViewHolder(private var productRowBinding: ItemSaveWordBinding) :
        RecyclerView.ViewHolder(productRowBinding.root) {

        init {
            /* productRowBinding.activeButton.setOnClickListener(View.OnClickListener {
                 Log.d(
                     TAG,
                     "onClick: " + userList[adapterPosition]
                 )
             })*/
        }

        fun bindto(data: Question?) {
            if (data != null) {
                productRowBinding.txtWord1.text = data.word
                productRowBinding.txtWord2.text = data.answerTrue
            }


        }

        fun updateIsFriend(friendMail: String, isFriend: Int) {

            Firebase.firestore.collection("Users")
                .document(FirebaseAuth.getInstance().uid.toString())
                .collection("Friends")
                .document(friendMail)
                .update("isFriend", isFriend)
                .addOnFailureListener { e ->
                    Log.w("TAG", "Error writing document", e)
                }
        }
    }

    companion object {
        private const val TAG = "UserRecyclerAdapter"
    }

    init {
        this.userList = userList
    }
}