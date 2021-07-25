package com.ilkcanyilmaz.wordrival.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ilkcanyilmaz.wordrival.R
import com.ilkcanyilmaz.wordrival.models.User
import com.ilkcanyilmaz.wordrival.repositories.UserLocalDataSource
import com.ilkcanyilmaz.wordrival.viewmodels.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : NavHostFragment() {
    private var mAuth: FirebaseAuth
    private var user: User? = null
    private val viewModel: ProfileViewModel by viewModels()

    @Inject
    lateinit var userDataSource: UserLocalDataSource

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
        // Glide.with(requireContext()).load("https://platform-lookaside.fbsbx.com/platform/profilepic/?asid=10218274302510824&height=200&width=200&ext=1606046973&hash=AeRZbtRLCP2-pUXff-0").into(img_facebookProfilePhoto);
        userDataSource.getUser {
            user = it
        }
        //LoadFriendsList()
    }

    init {
        mAuth = FirebaseAuth.getInstance()
        firestoreDb = Firebase.firestore
    }


}
