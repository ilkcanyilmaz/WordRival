package com.ilkcanyilmaz.wordrival

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.ilkcanyilmaz.wordrival.enums.SendFcmType
import com.ilkcanyilmaz.wordrival.repositories.FirestoreRepository
import com.ilkcanyilmaz.wordrival.views.MainActivity
import org.json.JSONObject


class MyFirebaseInstanceIDService : FirebaseMessagingService() {
    private val TAG = "MyFirebaseMsgService"
    val CHAR_SPLIT = "$!"

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val db: FirebaseFirestore = Firebase.firestore

        val docRef = db.collection("Users").document(mAuth.currentUser?.uid.toString())

        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    if (document.data?.get("userToken") != p0) {
                        docRef.update("userToken", p0)
                            .addOnSuccessListener {
                                Log.d("FirestoreUpdate:", "Token is updated")
                            }
                            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
                    }
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
        getSharedPreferences("_", MODE_PRIVATE).edit().putString("token", p0).apply();

        Log.e("NEW_TOKEN", p0)
    }//


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val params = remoteMessage.data
        if (params != null && params.size > 0) {
            createNotification(remoteMessage)
        }
    }

    companion object {
        fun getToken(context: Context): String? {
            return context.getSharedPreferences("_", MODE_PRIVATE).getString("token", "empty")
        }
    }


    private fun createNotification(remoteMessage: RemoteMessage) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("text", remoteMessage.data["message_data_text"])
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val params = remoteMessage.data
        val gson = Gson()
        val parser = JsonParser()
        val data =
            parser.parse(JSONObject(params as Map<String, String>).toString()) as JsonObject // response will be the json String

        showNotification(
            applicationContext,
            data["title"].toString().replace("\"", ""), data["data"].toString().replace("\"", ""),
            intent
        )
    }

    fun showNotification(context: Context, title: String?, body: String?, intent: Intent?) {
        var mTitle = ""
        var mContentText = ""
        val firebaseOperatation = FirestoreRepository(applicationContext)
        if (title == SendFcmType.FRIEND_REQUEST.getTypeID().toString()) {
            mTitle = "Arkadaşlık isteği"
            val bodyData = body?.split(CHAR_SPLIT)
            val friendToken = bodyData?.get(0).toString()
            val friendId = bodyData?.get(1).toString()
            val friendNickName = bodyData?.get(2).toString()
            val friendPhotoUrl = bodyData?.get(3).toString()
            mContentText = friendNickName
            firebaseOperatation.addFriend(
                friendId,
                friendNickName,
                friendToken,
                friendPhotoUrl,
                null
            )

        } else if (title == SendFcmType.FRIEND_REQUEST_RESPONSE.getTypeID().toString()) {
            val bodyData = body?.split(CHAR_SPLIT)
            val friendId = bodyData?.get(0).toString()
            val friendNickName = bodyData?.get(1).toString()
            val isFriend = bodyData?.get(2)?.toInt()
            mContentText = friendNickName
            val firestore: FirebaseFirestore = Firebase.firestore
            firebaseOperatation.FriendRequestResponseUpdate(friendId, isFriend!!)
            firestore.collection("Users")
                .document(FirebaseAuth.getInstance().currentUser?.uid.toString())
                .collection("Friends")
                .document(friendId)
                .update("isFriend", isFriend)
                .addOnSuccessListener {
                    //Toast.makeText(activity.applicationContext, "Arkadaşlık isteği gönderildi", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
        } else if (title == SendFcmType.FRIEND_REQUEST_GAME.getTypeID().toString()) {
            mTitle = "Oyun isteği"
            val bodyData = body?.split(CHAR_SPLIT)
            val friendId = bodyData?.get(0).toString()
            val friendNickName = bodyData?.get(1).toString()
            val gameId = bodyData?.get(2).toString()
            mContentText = friendNickName
            firebaseOperatation.AddGameRequestWithFriend(friendId, friendNickName, gameId, null)
        }
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 1
        val channelId = "channel-01"
        val channelName = "Channel Name"
        val importance = NotificationManager.IMPORTANCE_HIGH
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                channelId, channelName, importance
            )
            notificationManager.createNotificationChannel(mChannel)
        }
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val mBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_delete)
            .setContentTitle(mTitle)
            .setSound(defaultSoundUri)
            .setContentText(mContentText)
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addNextIntent(intent)
        val resultPendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        mBuilder.setContentIntent(resultPendingIntent)
        notificationManager.notify(notificationId, mBuilder.build())
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0 /* request code */,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val pattern = longArrayOf(500, 500, 500, 500, 500)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_menu_mylocation)
            .setContentTitle("Word River")
            .setContentText(remoteMessage.notification!!.body)
            .setAutoCancel(true)
            .setVibrate(pattern)
            .setLights(Color.BLUE, 1, 1)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent) as NotificationCompat.Builder
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
        //val alarmNotificationRepository = AlarmNotificationRepository(this.application)
        //alarmNotificationRepository.insert(alarmNotification);
    }
}