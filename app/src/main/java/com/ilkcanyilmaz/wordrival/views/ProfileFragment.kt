package com.ilkcanyilmaz.wordrival.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ilkcanyilmaz.wordrival.FirestoreOperation
import com.ilkcanyilmaz.wordrival.R
import com.ilkcanyilmaz.wordrival.databases.DatabaseManager
import com.ilkcanyilmaz.wordrival.models.User
import com.ilkcanyilmaz.wordrival.viewmodels.ProfileViewModel

class ProfileFragment : NavHostFragment() {
    lateinit var firestoreOperation: FirestoreOperation
    private var mAuth: FirebaseAuth
    private lateinit var user: User
    private lateinit var viewModel: ProfileViewModel
    private lateinit var db: DatabaseManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private val TAG = "DocSnippets"
    private lateinit var firestoreDb: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()


        //LoadFriendsList()

    }

    init {
        mAuth = FirebaseAuth.getInstance()
        firestoreDb = Firebase.firestore
        firestoreOperation = FirestoreOperation()
    }

    fun init() {
        db = context?.let { DatabaseManager.getDatabaseManager(it) }!!
        viewModel = ViewModelProviders.of(this).get(
            ProfileViewModel::class.java
        )
        user = db.userDao().getUser()
    }

}