package com.ilkcanyilmaz.wordrival.views

import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.github.aakira.expandablelayout.ExpandableLayoutListenerAdapter
import com.github.aakira.expandablelayout.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase
import com.ilkcanyilmaz.wordrival.FirestoreOperation
import com.ilkcanyilmaz.wordrival.MyFirebaseInstanceIDService
import com.ilkcanyilmaz.wordrival.R
import com.ilkcanyilmaz.wordrival.adapters.FriendListAdapter
import com.ilkcanyilmaz.wordrival.databases.DatabaseManager
import com.ilkcanyilmaz.wordrival.enums.GameReadyStatus
import com.ilkcanyilmaz.wordrival.enums.GameRequestType
import com.ilkcanyilmaz.wordrival.enums.GameType
import com.ilkcanyilmaz.wordrival.enums.SendFcmType
import com.ilkcanyilmaz.wordrival.models.Friend
import com.ilkcanyilmaz.wordrival.models.User
import com.ilkcanyilmaz.wordrival.utils.SendFCM
import com.ilkcanyilmaz.wordrival.viewmodels.HomeViewModel
import kotlinx.android.synthetic.main.customdialog_add_friend.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.lang.ref.WeakReference

class HomeFragment : Fragment(), FriendListAdapter.ItemListener,
    FriendListAdapter.PlayButtonListener, View.OnClickListener {
    private val TAG = "GoogleActivity"
    private var mAuth: FirebaseAuth
    private var firestore: FirebaseFirestore
    lateinit var firestoreOperation: FirestoreOperation
    private val BASE_URL = "https://fcm.googleapis.com/fcm/"
    private lateinit var viewModel: HomeViewModel
    val CHAR_SPLIT = "$!"
    private lateinit var user: User
    private lateinit var db: DatabaseManager
    private var requestGameId = ""
    var functions: FirebaseFunctions
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    init {
        mAuth = FirebaseAuth.getInstance()
        firestore = Firebase.firestore
        functions = FirebaseFunctions.getInstance()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(
            HomeViewModel::class.java
        )
        db = context?.let { DatabaseManager.getDatabaseManager(it) }!!
        user = db.userDao().getUser()
        btn_play.setOnClickListener(this)
        btn_gameReject.setOnClickListener(this)
        btn_gameAccept.setOnClickListener(this)
        ExpandedLayout()
        UpdateUI()
        viewModel.getFriendsFirestore(firestore, mAuth.currentUser?.uid.toString())
        observeLiveData()
        /*btn_addFriend.setOnClickListener(View.OnClickListener {
            showOkeyDialog(activity)
        })*/
        /* btn_play.setOnClickListener(View.OnClickListener {
             activity?.let {
                 showConnectDialog(activity)
                 SendGameRequest()
                 GameConnectControl()
                 //SendGameRequest()
                 //ConnectGame()
             }
         })*/
    }

    fun ExpandedLayout() {
        ll_friends.setOnClickListener {
            expandableLayout_friends.toggle()
        }
        expandableLayout_friends.setListener(object : ExpandableLayoutListenerAdapter() {
            override fun onPreOpen() {
                fl_addFriend.visibility = View.VISIBLE
            }

            override fun onPreClose() {
                fl_addFriend.visibility = View.GONE
            }
        })
        fl_addFriend.setOnClickListener {
            ShowDialogAddFriend()
        }
    }

    fun createRotateAnimator(target: View?, from: Float, to: Float): ObjectAnimator? {
        val animator = ObjectAnimator.ofFloat(target, "rotation", from, to)
        animator.duration = 300
        animator.interpolator = Utils.createInterpolator(Utils.LINEAR_INTERPOLATOR)
        return animator
    }

    fun UpdateUI() {
        val docRef =
            firestore.collection("Users").document(mAuth.uid.toString()).collection("GameRequest")
                .document("request")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                if (snapshot.data != null) {
                    cv_gameRequst.visibility = View.VISIBLE
                    cv_game.visibility = View.GONE
                    requestGameId = snapshot.data!!["gameId"].toString()
                } else {
                    cv_gameRequst.visibility = View.GONE
                    cv_game.visibility = View.VISIBLE
                }
            }
        }
    }

    fun showConnectDialog(activity: Activity?) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.customdialog_game_connecting)

        dialog.show()
    }

    fun startGameActivity() {
        val intent = Intent(context, GameActivity::class.java)
        context?.startActivity(intent)
    }


    fun observeLiveData() {
        viewModel.friends.observe(viewLifecycleOwner, Observer { friends ->
            friends.let {
                val adapter =
                    FriendListAdapter(friends, requireContext())
                rv_friends.adapter = adapter

                adapter.itemListener = this
                adapter.playButtonListener = this
            }
        })
    }

    fun observeSearchFriend(dialog: Dialog) {
        viewModel.friendByMail.observe(viewLifecycleOwner, Observer { friends ->
            friends.let {
                if (friends.userId.length > 0) {
                    dialog.cv_friend.visibility = View.VISIBLE
                    dialog.txt_userName.text = friends.userNickName
                    Glide.with(this).load(friends.userPhoto).into(dialog.img_profilePhoto)
                    dialog.btn_addFriendRequest.setOnClickListener {
                        val title = SendFcmType.FRIEND_REQUEST.getTypeID().toString()
                        val body =
                            MyFirebaseInstanceIDService.getToken(activity?.applicationContext!!) + CHAR_SPLIT + mAuth.currentUser?.uid.toString() + CHAR_SPLIT + user.userNickName + CHAR_SPLIT + user.userPhoto
                        SendFCM(title, body, friends.userToken, friends, requireActivity())
                    }
                } else {
                    dialog.cv_friend.visibility = View.GONE
                    dialog.txt_warning.text =
                        StringBuilder().append(dialog.edt_name.text).append(" ")
                            .append("kullanıcısı bulunamadı").toString()
                }
            }
        })
    }

    fun ShowDialogAddFriend() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.customdialog_add_friend)
        dialog.btn_friendSearch.setOnClickListener {
            viewModel.getFriendsFristoreByMail(firestore, dialog.edt_name.text.toString())
            observeSearchFriend(dialog)
        }
        dialog.show()
    }

    override fun onFriendRequestResponse(friend: Friend, isFriend: Int) {
        val title = SendFcmType.FRIEND_REQUEST_RESPONSE.getTypeID().toString()
        val body = mAuth.currentUser?.uid + CHAR_SPLIT + db.userDao()
            .getUser().userNickName + CHAR_SPLIT + isFriend.toString()
        val friendUser = User()
        friendUser.userId = friend.friendId
        SendFCM(title, body, friend.friendToken, friendUser, requireActivity())
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_play -> {
                startGameActivity()
            }
            R.id.btn_gameReject -> {
                firestore.collection("Users").document(mAuth.currentUser?.uid.toString())
                    .collection("GameRequest").document("request")
                    .delete()
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!")
                    }
                    .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
                firestore.collection("Games").document(requestGameId)
                    .update("user2Status", GameReadyStatus.REJECTED.getTypeID())
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot successfully updated!")

                    }
                /*  firestore.collection("Games").document(requestGameId).update("user2Status", GameReadyStatus.REJECTED.getTypeID())
                      .addOnSuccessListener {
                          Log.d(TAG, "DocumentSnapshot successfully updated!")
                      }*/
            }
            R.id.btn_gameAccept -> {
                //FriendGameConnect(this, null, GameRequestType.SENDER).execute(null as Void?)
                val refGameDocument = firestore.collection("Games").document(requestGameId)


                Log.d(TAG, "DocumentSnapshot successfully updated!")
                refGameDocument.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.data != null) {
                        refGameDocument.update("user2Status", GameReadyStatus.READY.getTypeID())
                            .addOnSuccessListener {
                                val intent = Intent(context, GameActivity::class.java)
                                intent.putExtra("gameType", GameType.FRIEND)
                                intent.putExtra("gameId", requestGameId)
                                context?.startActivity(intent)
                            }

                    } else {
                        firestore.collection("Users")
                            .document(mAuth.currentUser?.uid.toString())
                            .collection("GameRequest").document("request")
                            .delete()
                        Toast.makeText(requireContext(), "Oyun isteği iptal edildi.", Toast.LENGTH_SHORT).show()
                    }


                    /*firestore.collection("Games").document(requestGameId)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e)
                            return@addSnapshotListener
                        }
                        var a = snapshot?.get("Questions")

                    }*/
                }
            }
        }
    }

    override fun onPlayButtonClick(friend: Friend) {
        FriendGameConnect(this, friend, GameRequestType.SENDER).execute(null as Void?)
    }

    inner private class FriendGameConnect(
        context: HomeFragment,
        val friend: Friend?,
        val type: GameRequestType
    ) :
        AsyncTask<Void, Void, String>() {
        private val activityReference: WeakReference<HomeFragment> = WeakReference(context)
        val dialog = Dialog(activityReference.get()!!.requireContext())
        var gameId = ""
        override fun onPreExecute() {
            super.onPreExecute()
            val activity = activityReference.get()
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.progressdialog_game_connect)
            val btnCancel = dialog.findViewById(R.id.btn_cancel) as Button
            btnCancel.setOnClickListener {
                if (gameId.length > 0) {
                    Firebase.firestore.collection("Games").document(gameId).delete()
                }
                dialog.dismiss()
            }
            /*  val body = dialog.findViewById(R.id.body) as TextView
          body.text = title
          val yesBtn = dialog.findViewById(R.id.yesBtn) as Button
          val noBtn = dialog.findViewById(R.id.noBtn) as TextView
          yesBtn.setOnClickListener {
              dialog.dismiss()
          }
          noBtn.setOnClickListener { dialog.dismiss() }*/
            dialog.show()
        }

        override fun doInBackground(vararg params: Void?): String {
            if (type == GameRequestType.SENDER) {
                val data = hashMapOf(
                    "user1Id" to activityReference.get()?.mAuth?.currentUser?.uid,
                    "user2Id" to friend?.friendId,
                    "user2Token" to friend?.friendToken
                )
                FirebaseFunctions.getInstance()
                    .getHttpsCallable("GameWithFriend")
                    .call(data)
                    .continueWith { task ->
                        // This continuation runs on either success or failure, but if the task
                        // has failed then result will throw an Exception which will be
                        // propagated down.
                        val result = task.result?.data as String
                        val isSuccess = result.split(CHAR_SPLIT).get(0)
                        gameId = result.split(CHAR_SPLIT).get(1)
                        if (isSuccess == "success") {
                            val docRef = Firebase.firestore.collection("Games").document(gameId)
                            docRef.addSnapshotListener { snapshot, e ->
                                if (e != null) {
                                    Log.w("TAG", "Listen failed.", e)
                                    return@addSnapshotListener
                                }

                                if (snapshot != null && snapshot.exists()) {
                                    Log.d("TAG", "Current data: ${snapshot.data}")
                                    if (!dialog.isShowing) {
                                        docRef.delete()
                                    } else {
                                        if (snapshot["user2Status"].toString()
                                                .toInt() == GameReadyStatus.READY.value
                                        ) {
                                            val intent = Intent(context, GameActivity::class.java)
                                            intent.putExtra("gameType", GameType.FRIEND)
                                            intent.putExtra("gameId", gameId)
                                            context?.startActivity(intent)
                                        } else if (snapshot["user2Status"].toString()
                                                .toInt() == GameReadyStatus.REJECTED.value
                                        ) {
                                            docRef.delete()
                                        }
                                        if (snapshot["user1Status"].toString()
                                                .toInt() != GameReadyStatus.READY.value
                                        ) {
                                            docRef.delete()
                                        }
                                    }
                                } else {
                                    Log.d("TAG", "Current data: null")
                                }
                            }
                        }
                    }
            } else if (type == GameRequestType.RECEIVING) {

            }
            return ""
        }

    }
}
