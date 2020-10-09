package com.ilkcanyilmaz.wordrival

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.ilkcanyilmaz.wordrival.views.MainActivity

class FirestoreOperation() {
    private var firestore: FirebaseFirestore
    private val TAG = "GoogleActivity"
    private val userId:String
    init {
        userId=FirebaseAuth.getInstance().currentUser?.uid.toString()
        firestore=Firebase.firestore
    }
    fun InsertNewUserFirestore(
        userMail: String,
        userNickName: String,
        userFullName: String,
        userLevel: Int,
        userPhoto: String,
        activity: Activity
    ) {


        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val user = hashMapOf(
                    "userToken" to task.result?.token.toString(),
                    "userMail" to userMail,
                    "userNickName" to userNickName,
                    "userFullName" to userFullName,
                    "userLevel" to userLevel,
                    "userPhoto" to userPhoto,
                    "userScore" to 0
                )
                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
                val uId=mAuth.currentUser?.uid.toString()
                firestore.collection("Users").document(uId)
                    .set(user)
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot successfully written!")
                        val intent = Intent(activity.applicationContext, MainActivity::class.java)
                        activity.startActivity(intent)
                    }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

                Toast.makeText(activity, "Kayıt başarılı", Toast.LENGTH_SHORT)
                    .show()
                // Log and toast
                //val msg = getString(R.string.msg_token_fmt, token)
                // Log.d(TAG, msg)
                //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()

            })

    }

    fun AddFriend(
        friendId: String,
        friendNickName: String,
        friendToken: String,
        friendPhotoUrl: String
    ) {
        val friend = hashMapOf(
            "friendNickName" to friendNickName,
            "friendToken" to friendToken,
            "friendPhotoUrl" to friendPhotoUrl,
            "isFriend" to 0
        )
        firestore.collection("Users").document(userId).collection("Friends")
            .document(friendId)
            .set(friend)
            .addOnSuccessListener {
                //Toast.makeText(activity.applicationContext, "Arkadaşlık isteği gönderildi", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }

    fun FriendRequestResponseUpdate(friendId: String, isFriend:Int){
        firestore.collection("Users").document(userId).collection("Friends")
            .document(friendId)
            .update("isFriend", isFriend)
            .addOnSuccessListener {
                //Toast.makeText(activity.applicationContext, "Arkadaşlık isteği gönderildi", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }
}