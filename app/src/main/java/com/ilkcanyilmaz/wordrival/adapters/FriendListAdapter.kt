package com.ilkcanyilmaz.wordrival.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ilkcanyilmaz.wordrival.databases.DatabaseManager
import com.ilkcanyilmaz.wordrival.databinding.ItemFriendBinding
import com.ilkcanyilmaz.wordrival.models.Friend


class FriendListAdapter(userList: List<Friend>, context: Context) :
    RecyclerView.Adapter<FriendListAdapter.UserViewHolder>() {
    var userList: List<Friend>
    var itemListener: ItemListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val productRowBinding: ItemFriendBinding =
            ItemFriendBinding.inflate(layoutInflater, parent, false)
        return UserViewHolder(productRowBinding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bindto(userList[position])
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    interface ItemListener {
        fun onFriendRequestResponse(friend: Friend, isFriend: Int)
    }

    inner class UserViewHolder(productRowBinding: ItemFriendBinding) :
        RecyclerView.ViewHolder(productRowBinding.getRoot()) {
        var productRowBinding: ItemFriendBinding

        init {
            this.productRowBinding = productRowBinding
            /* productRowBinding.activeButton.setOnClickListener(View.OnClickListener {
                 Log.d(
                     TAG,
                     "onClick: " + userList[adapterPosition]
                 )
             })*/
        }

        fun bindto(data: Friend?) {
            productRowBinding.setData(data)
            productRowBinding.executePendingBindings()

            if (data?.isFriend == 0) {
                productRowBinding.llFriendRequest.visibility = View.VISIBLE
            } else {
                productRowBinding.llFriendRequest.visibility = View.GONE
            }
            productRowBinding.btnCancel.setOnClickListener {
                itemListener?.onFriendRequestResponse(data!!, 2)
            }
            productRowBinding.btnAccept.setOnClickListener {
                itemListener?.onFriendRequestResponse(data!!, 1)
            }
        }

        fun updateIsFriend(friendMail: String, isFriend: Int) {
            val db: DatabaseManager? =
                DatabaseManager.getDatabaseManager(context = itemView.context)
            Firebase.firestore.collection("Users")
                .document(db?.userDao()?.getUser()?.userMail.toString())
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