package com.ilkcanyilmaz.wordrival.views

import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.aakira.expandablelayout.ExpandableLayoutListenerAdapter
import com.github.aakira.expandablelayout.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ilkcanyilmaz.wordrival.FirestoreOperation
import com.ilkcanyilmaz.wordrival.MyFirebaseInstanceIDService
import com.ilkcanyilmaz.wordrival.R
import com.ilkcanyilmaz.wordrival.adapters.FriendListAdapter
import com.ilkcanyilmaz.wordrival.databases.DatabaseManager
import com.ilkcanyilmaz.wordrival.enums.SendFcmType
import com.ilkcanyilmaz.wordrival.interfaces.FcmInterface
import com.ilkcanyilmaz.wordrival.models.FCMModel
import com.ilkcanyilmaz.wordrival.models.Friend
import com.ilkcanyilmaz.wordrival.models.Game
import com.ilkcanyilmaz.wordrival.models.User
import com.ilkcanyilmaz.wordrival.utils.SendFCM
import com.ilkcanyilmaz.wordrival.viewmodels.HomeViewModel
import kotlinx.android.synthetic.main.customdialog_add_friend.*
import kotlinx.android.synthetic.main.fragment_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeFragment : Fragment(), FriendListAdapter.ItemListener {
    private val TAG = "GoogleActivity"
    private var mAuth: FirebaseAuth
    private var firestore: FirebaseFirestore
    lateinit var firestoreOperation: FirestoreOperation
    private val BASE_URL = "https://fcm.googleapis.com/fcm/"
    private lateinit var viewModel: HomeViewModel
    val CHAR_SPLIT = "$!"
    private lateinit var user: User
    private lateinit var db: DatabaseManager

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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(
            HomeViewModel::class.java
        )
        db = context?.let { DatabaseManager.getDatabaseManager(it) }!!
        user = db.userDao().getUser()

        ExpandedLayout()
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
        fl_addFriend.setOnClickListener{
            Toast.makeText(context, "test", Toast.LENGTH_SHORT).show()
        }
    }

    fun createRotateAnimator(target: View?, from: Float, to: Float): ObjectAnimator? {
        val animator = ObjectAnimator.ofFloat(target, "rotation", from, to)
        animator.duration = 300
        animator.interpolator = Utils.createInterpolator(Utils.LINEAR_INTERPOLATOR)
        return animator
    }

    fun GameConnectControl() {
        firestore.collection("Games").document("HjiA3E2OdnW9zJb13ZI6")
            //.whereEqualTo("capital", true)

            .addSnapshotListener() { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }
                var a = snapshot?.get("Questions")
                val game = Game(
                    snapshot?.id.toString(),
                    snapshot?.data?.get("isDone") as Boolean,
                    snapshot.data!!["user1Id"].toString(),
                    snapshot.data!!["user2Id"].toString(),
                    snapshot.data!!["isUser1Ready"] as Boolean,
                    snapshot.data!!["isUser2Ready"] as Boolean,

                    )

                if (game.isUser1Ready) {
                    startGameActivity()
                }
            }
    }

    /*var game = Game(
        document.id,
        document.data["isDone"].toString(),
        document.data["user1Id"].toString(),
        document.data["user2Id"].toString(),
        document.data["isUser1Ready"],
        document.data["isUser2Ready"]
    )
    if (document.data["isUser1Ready"] == true) {
        startGameActivity()
    }*/
    fun SendGameRequest() {
        var retrofit = Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        var header = HashMap<String, String>()
        header.put("Content-Type", "application/json")
        header.put(
            "Authorization",
            "key=AAAAILyNFNY:APA91bHA9UnC4nto9r0VL7UzEG7KtyfdSyv3vwLAyDKRmHijYbgu0aDjm5RA95HEDvKep-HpGpdu9Ej8Ta8GxIKOtu4a-lRaR4vaSB9Qa9qJlmZJRgcEGZTEVrdjGUncS0Um644qy26B"
        )
        var data = FCMModel.Data("GameRequest", "0")
        var notify: FCMModel = FCMModel(
            "dvRybNtfR36RTJ1LvuHI7g:APA91bF73L4HrI4t-QC9xQRoASeCpZx1WoFvlLugcTMT9K4ASmoPubAXL_hkTogqDQCvdhx-BXx_Z6ckZ4Pn1SENOtgfRmS5sAXqTQMN320bltmdwTlRqYT2HpsHC0DWzPnliCCA3iZn",
            data
        )
        var myInterface = retrofit.create(FcmInterface::class.java)
        var request = myInterface.SendFriendRequest(header, notify)
        request.enqueue(object : Callback<Response<FCMModel>> {
            override fun onResponse(
                call: Call<Response<FCMModel>>,
                response: Response<Response<FCMModel>>
            ) {
                Log.d("Retrofit", "Success: $response")
            }

            override fun onFailure(calal: Call<Response<FCMModel>>, t: Throwable) {
                Log.e("Retrofit", "Error: " + t?.message)
            }
        })
    }

    fun AddNewGame() {

        val auth = mAuth!!.currentUser
        val user = hashMapOf(
            "name" to auth?.displayName,
        )

        firestore.collection("Pool").document("users")
            .set(user)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }

    fun showConnectDialog(activity: Activity?) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.customdialog_game_connecting)

        dialog.show()
    }

    fun ConnectGame() {
        firestore.collection("Pool")
            //.whereEqualTo("capital", true)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    startGameActivity()
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
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
                rv_friends.setAdapter(adapter)

                adapter.itemListener = this
            }
        })
    }

    fun observeSearchFriend(dialog: Dialog) {
        viewModel.friendByMail.observe(viewLifecycleOwner, Observer { friends ->
            friends.let {
                dialog.ll_friend.visibility = View.VISIBLE
                dialog.txt_userName.text = friends.userNickName
                dialog.btn_addFriendRequest.setOnClickListener {
                    val title = SendFcmType.FRIEND_REQUEST.getTypeID().toString()
                    val body =
                        MyFirebaseInstanceIDService.getToken(activity?.applicationContext!!) + CHAR_SPLIT + mAuth.currentUser?.uid.toString() + CHAR_SPLIT + user.userNickName + CHAR_SPLIT + user.userPhoto
                    SendFCM(title, body, friends.userToken, friends)
                }
            }
        })
    }

    fun showOkeyDialog(activity: Activity?) {
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
        SendFCM(title, body, friend.friendToken, friendUser)
    }


}