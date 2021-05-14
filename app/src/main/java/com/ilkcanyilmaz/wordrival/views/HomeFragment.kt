package com.ilkcanyilmaz.wordrival.views

import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.aakira.expandablelayout.ExpandableLayoutListenerAdapter
import com.github.aakira.expandablelayout.Utils
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase
import com.ilkcanyilmaz.wordrival.FirestoreOperation
import com.ilkcanyilmaz.wordrival.MyFirebaseInstanceIDService
import com.ilkcanyilmaz.wordrival.R
import com.ilkcanyilmaz.wordrival.adapters.FriendListAdapter
import com.ilkcanyilmaz.wordrival.databases.DatabaseManager
import com.ilkcanyilmaz.wordrival.enums.*
import com.ilkcanyilmaz.wordrival.models.Friend
import com.ilkcanyilmaz.wordrival.models.User
import com.ilkcanyilmaz.wordrival.utils.SendFCM
import com.ilkcanyilmaz.wordrival.utils.pxToDp
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
    var gameDisplayType: GameDisplay = GameDisplay.ONLINE
    private var level: Level = Level.EASY

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
        MobileAds.initialize(requireContext())
        updateUIGameDisplay()
        btn_online.setOnClickListener(this)
        btn_offline.setOnClickListener(this)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        db = context?.let { DatabaseManager.getDatabaseManager(it) }!!
        user = db.userDao().getUser()
        btn_play.setOnClickListener(this)
        btn_gameReject.setOnClickListener(this)
        btn_gameAccept.setOnClickListener(this)
        setLevelSpinner()
        expandedLayout()
        updateUIGameRequest()
        viewModel.getFriendsFirestore(firestore, mAuth.currentUser?.uid.toString())
        observeFriends()
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

    private fun setLevelSpinner() {
        val values: Array<String> = arrayOf("Kolay", "Orta", "Zor")
        txt_level.setOnClickListener {
            when (level) {
                Level.EASY -> {
                    level=Level.NORMAL
                    txt_level.text=values[1]
                }
                Level.NORMAL -> {
                    level=Level.HARD
                    txt_level.text=values[2]
                }
                Level.HARD -> {
                    level=Level.EASY
                    txt_level.text=values[0]
                }
            }
        }

    }

    private fun expandedLayout() {
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
            showDialogAddFriend()
        }
    }

    fun createRotateAnimator(target: View?, from: Float, to: Float): ObjectAnimator? {
        val animator = ObjectAnimator.ofFloat(target, "rotation", from, to)
        animator.duration = 300
        animator.interpolator = Utils.createInterpolator(Utils.LINEAR_INTERPOLATOR)
        return animator
    }

    private fun updateUIGameRequest() {
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
                    cv_gameRequest.visibility = View.VISIBLE
                    cv_game.visibility = View.GONE
                    requestGameId = snapshot.data!!["gameId"].toString()
                } else {
                    cv_gameRequest.visibility = View.GONE
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

    private fun observeRandomGameId(dialog: Dialog) {
        viewModel.randomGameId.observe(this, {
            it.let {
                if (it.isNotEmpty()) {
                    val docRef =
                        Firebase.firestore.collection("Games").document(it)
                    docRef.addSnapshotListener(requireActivity()) { snapshot, e ->
                        if (e != null) {
                            Log.w("TAG", "Listen failed.", e)
                            return@addSnapshotListener
                        }

                        if (snapshot != null && snapshot.exists()) {
                            val gameId = it
                            if (gameId.isNotEmpty()) {
                                dialog.dismiss()
                                val action =
                                    HomeFragmentDirections.actionHomeFragmentToGameFragment(
                                        gameId,
                                        UserType.USER1.getTypeID()
                                    )
                                if (findNavController().currentDestination!!.id == R.id.homeFragment) {
                                    findNavController().navigate(action)
                                }
                            }
                        }
                    }
                }
            }
        })

    }

    private fun startOfflineGame() {
        val action = HomeFragmentDirections.actionHomeFragmentToOfflineGameFragment()
        findNavController().navigate(action)
    }

    private fun startRandomGameActivity() {
        val game = hashMapOf(
            "userScore" to user.userScore,
            "userStatus" to 0,
            "gameId" to ""
        )
        val data = HashMap<String, Any>()
        data["user1Id"] = mAuth.uid.toString()
        data["user1Score"] = user.userScore
        var isAddPool = false
        val poolRef: DocumentReference = firestore.collection("Pool").document(mAuth.uid.toString())
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.progressdialog_game_connect)
        dialog.show()
        val btnCancel = dialog.findViewById(R.id.btn_cancel) as Button
        btnCancel.setOnClickListener {
            /* if (gameId.length > 0) {
                 Firebase.firestore.collection("Games").document(gameId).delete()
             }*/
            if (isAddPool) {
                poolRef.delete()
            }
            dialog.dismiss()
        }
        viewModel.getRandomGameConnect(data)

        poolRef
            .set(game)
            .addOnSuccessListener {
                isAddPool = true
                poolRef.addSnapshotListener(requireActivity()) { snapshot, e ->
                    if (e != null) {
                        Log.w("TAG", "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val gameId = snapshot["gameId"].toString()
                        if (gameId.isNotEmpty()) {
                            poolRef.delete()
                            val action = HomeFragmentDirections.actionHomeFragmentToGameFragment(
                                gameId,
                                UserType.USER2.getTypeID()
                            )
                            findNavController().navigate(action)
                            dialog.dismiss()
                            /*val intent = Intent(
                                context,
                                GameActivity::class.java
                            )
                            intent.putExtra(
                                "userType",
                                UserType.USER2.getTypeID()
                            )
                            intent.putExtra("gameId", gameId)
                            context?.startActivity(intent)*/
                        }
                    }
                }

            }.addOnFailureListener { e ->
                Log.w(TAG, "Error writing document", e)
            }

        observeRandomGameId(dialog)
    }


    private fun observeFriends() {
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

    private fun observeSearchFriend(dialog: Dialog) {
        viewModel.friendByMail.observe(viewLifecycleOwner, Observer { friends ->
            friends.let {
                if (friends.userId.isNotEmpty()) {
                    dialog.cv_friend.visibility = View.VISIBLE
                    dialog.txt_userName.text = friends.userNickName
                    Glide.with(this).load(friends.userPhoto)
                        .into(dialog.img_profilePhoto)
                    dialog.btn_addFriendRequest.setOnClickListener {
                        val title = SendFcmType.FRIEND_REQUEST.getTypeID().toString()
                        val body =
                            MyFirebaseInstanceIDService.getToken(activity?.applicationContext!!) + CHAR_SPLIT + mAuth.currentUser?.uid.toString() + CHAR_SPLIT + user.userNickName + CHAR_SPLIT + user.userPhoto
                        SendFCM(
                            title,
                            body,
                            friends.userToken,
                            friends,
                            requireActivity()
                        )
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

    fun showDialogAddFriend() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.customdialog_add_friend)
        dialog.btn_friendSearch.setOnClickListener {
            viewModel.getFriendsFristoreByMail(
                firestore,
                dialog.edt_name.text.toString()
            )
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

    override fun onDestroy() {
        super.onDestroy()

    }


    private fun updateUIGameDisplay() {
        when (gameDisplayType) {

            GameDisplay.ONLINE -> {

                val drawableOnline = btn_online.background as GradientDrawable
                drawableOnline.setStroke(
                    3,
                    ResourcesCompat.getColor(resources, R.color.colorAccent, null)
                )
                drawableOnline.setColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.colorAccent,
                        null
                    )
                )
                drawableOnline.cornerRadii =
                    floatArrayOf(pxToDp(8f), pxToDp(8f), 0f, 0f, 0f, 0f, pxToDp(8f), pxToDp(8f))
                btn_online.background = drawableOnline

                val drawableOffline = btn_offline.background as GradientDrawable
                drawableOffline.setStroke(
                    3,
                    ResourcesCompat.getColor(resources, R.color.colorAccent, null)
                )
                drawableOffline.cornerRadii =
                    floatArrayOf(0f, 0f, pxToDp(8f), pxToDp(8f), pxToDp(8f), pxToDp(8f), 0f, 0f)
                drawableOffline.setColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.color_white,
                        null
                    )
                )
                btn_offline.background = drawableOffline


                txt_headerOnline.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.color_tab_disable,
                        null
                    )
                )
                txt_headerOffline.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.colorAccent,
                        null
                    )
                )

                ll_online.visibility = View.VISIBLE
                ll_offline.visibility = View.GONE


            }

            GameDisplay.OFFLINE -> {
                val drawableOnline = btn_online.background as GradientDrawable
                drawableOnline.setStroke(
                    3,
                    ResourcesCompat.getColor(resources, R.color.colorAccent, null)
                )
                drawableOnline.cornerRadii =
                    floatArrayOf(pxToDp(8f), pxToDp(8f), 0f, 0f, 0f, 0f, pxToDp(8f), pxToDp(8f))
                drawableOnline.setColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.color_white,
                        null
                    )
                )

                val drawableOffline = btn_offline.background as GradientDrawable
                drawableOnline.setStroke(
                    3,
                    ResourcesCompat.getColor(resources, R.color.colorAccent, null)
                )
                drawableOffline.setColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.colorAccent,
                        null
                    )
                )
                drawableOffline.cornerRadii =
                    floatArrayOf(0f, 0f, pxToDp(8f), pxToDp(8f), pxToDp(8f), pxToDp(8f), 0f, 0f)
                btn_offline.background = drawableOffline

                txt_headerOnline.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.colorAccent,
                        null
                    )
                )
                txt_headerOffline.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.color_tab_disable,
                        null
                    )
                )

                ll_online.visibility = View.GONE
                ll_offline.visibility = View.VISIBLE

            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_online -> {
                gameDisplayType = GameDisplay.ONLINE
                updateUIGameDisplay()
            }
            R.id.btn_offline -> {
                gameDisplayType = GameDisplay.OFFLINE
                updateUIGameDisplay()
            }

            R.id.btn_play -> {
                if (gameDisplayType == GameDisplay.ONLINE) {
                    startRandomGameActivity()
                } else if (gameDisplayType == GameDisplay.OFFLINE) {
                    startOfflineGame()
                }
            }
            R.id.btn_gameReject -> {
                firestore.collection("Users")
                    .document(mAuth.currentUser?.uid.toString())
                    .collection("GameRequest").document("request")
                    .delete()
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!")
                    }
                    .addOnFailureListener { e ->
                        Log.w(
                            TAG,
                            "Error deleting document",
                            e
                        )
                    }
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
                val refGameDocument =
                    firestore.collection("Games").document(requestGameId)
                firestore.collection("Users")
                    .document(mAuth.currentUser?.uid.toString())
                    .collection("GameRequest").document("request")
                    .delete()
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!")
                    }
                    .addOnFailureListener { e ->
                        Log.w(
                            TAG,
                            "Error deleting document",
                            e
                        )
                    }
                Log.d(TAG, "DocumentSnapshot successfully updated!")
                refGameDocument.get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            refGameDocument.update(
                                "user2Status",
                                GameReadyStatus.READY.getTypeID()
                            )
                                .addOnSuccessListener {
                                    val intent =
                                        Intent(context, GameActivity::class.java)
                                    intent.putExtra(
                                        "userType",
                                        UserType.USER2.getTypeID()
                                    )
                                    intent.putExtra("gameId", requestGameId)
                                    context?.startActivity(intent)
                                }
                        } else {
                            firestore.collection("Users")
                                .document(mAuth.currentUser?.uid.toString())
                                .collection("GameRequest").document("request")
                                .delete()
                        }
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

    override fun onPlayButtonClick(friend: Friend) {
        FriendGameConnect(this, friend, GameRequestType.SENDER).execute(null as Void?)
    }

    private inner class FriendGameConnect(
        context: HomeFragment,
        val friend: Friend?,
        val type: GameRequestType
    ) :
        AsyncTask<Void, Void, String>() {
        private val activityReference: WeakReference<HomeFragment> =
            WeakReference(context)
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
                            val docRef =
                                Firebase.firestore.collection("Games").document(gameId)
                            docRef.addSnapshotListener(requireActivity()) { snapshot, e ->
                                if (e != null) {
                                    Log.w("TAG", "Listen failed.", e)
                                    return@addSnapshotListener
                                }

                                if (snapshot != null && snapshot.exists()) {
                                    Log.d("TAG", "Current data: ${snapshot.data}")
                                    if (!dialog.isShowing) {
                                        viewModel.UpdateUserStatus(
                                            gameId,
                                            "user1Score",
                                            GameReadyStatus.REJECTED.getTypeID()
                                        )
                                    } else {
                                        if (snapshot["user2Status"].toString()
                                                .toInt() == GameReadyStatus.READY.value
                                        ) {
                                            dialog.dismiss()
                                            val intent = Intent(
                                                context,
                                                GameActivity::class.java
                                            )
                                            intent.putExtra(
                                                "userType",
                                                UserType.USER1.getTypeID()
                                            )
                                            intent.putExtra("gameId", gameId)
                                            context?.startActivity(intent)
                                        } else if (snapshot["user2Status"].toString()
                                                .toInt() == GameReadyStatus.REJECTED.value
                                        ) {
                                            dialog.dismiss()
                                            Toast.makeText(
                                                requireContext(),
                                                "Oyun isteği reddedildi.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        if (snapshot["user1Status"].toString()
                                                .toInt() != GameReadyStatus.READY.value
                                        ) {
                                            dialog.dismiss()
                                            Log.d("firestore", "User1Status = Ready")
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
