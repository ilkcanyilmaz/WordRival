package com.ilkcanyilmaz.wordrival.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.ilkcanyilmaz.wordrival.RandomGameRepository
import com.ilkcanyilmaz.wordrival.models.Friend
import com.ilkcanyilmaz.wordrival.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "DocSnippets"
    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    val friends = MutableLiveData<List<Friend>>()
    val friendByMail = MutableLiveData<User>()
    val randomGameId = MutableLiveData<String>()

    fun getFriendsFirestore(firestore: FirebaseFirestore, userId: String) {

        val docRef = firestore.collection("Users").document(userId).collection("Friends")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            val objFriend = snapshot?.toObjects(Friend::class.java)
            for ((i, item) in objFriend?.withIndex()!!) {
                item.friendId = snapshot.documents[i].id
                item.isFriend = snapshot.documents[i]["isFriend"].toString().toInt()
            }
            friends.value = objFriend
        }

    }

    fun getRandomGameConnect(data: HashMap<String, Any>) {
        val randomGameRepository: RandomGameRepository = RandomGameRepository(data)
        viewModelScope.launch {
            Dispatchers.IO
            randomGameRepository.getRandomConnect()
            randomGameRepository.gameId.observeForever {
                randomGameId.value = it
            }
        }.let {
        }
    }

    fun getFriendsFristoreByMail(firestore: FirebaseFirestore, name: String) {
        var friend: User = User()
        firestore.collection("Users")
            .whereEqualTo("userNickName", name)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    friend = User()
                } else {
                    for (document in documents) {
                        friend = document.toObject(User::class.java)
                        friend.userId = document.id
                        /* dialog.ll_friend.visibility = View.VISIBLE
                         dialog.txt_userName.text = friend.userNickName
                         dialog.btn_addFriendRequest.setOnClickListener({
                             SendFriendRequest(friend)
                         })*/

                        Log.d(TAG, "${document.id} => ${document.data}")
                    }
                }
                friendByMail.value = friend
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
                friend = User()
            }
    }

    fun UpdateUserStatus(gameId: String, updateField: String, status: Int) {
        firestore.collection("Games").document(gameId)
            .update(updateField, status)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully updated!")
            }
    }
}