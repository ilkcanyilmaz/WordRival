package com.ilkcanyilmaz.wordrival.views

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase
import com.ilkcanyilmaz.wordrival.R
import com.ilkcanyilmaz.wordrival.enums.UserType
import com.ilkcanyilmaz.wordrival.models.Game
import com.ilkcanyilmaz.wordrival.models.Question
import com.ilkcanyilmaz.wordrival.models.User
import com.ilkcanyilmaz.wordrival.repositories.UserLocalDataSource
import com.ilkcanyilmaz.wordrival.viewmodels.GameViewModel
import dagger.hilt.android.AndroidEntryPoint
import douglasspgyn.com.github.circularcountdown.CircularCountdown
import douglasspgyn.com.github.circularcountdown.listener.CircularListener
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.customdialog_game_end.*
import javax.inject.Inject

@AndroidEntryPoint
class GameFragment : Fragment(), View.OnClickListener {
    private var mAuth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore
    private var functions: FirebaseFunctions = FirebaseFunctions.getInstance()
    private lateinit var getQuestions: List<Question>
    private var questionCounter = 0
    private var trueAnswerCounter = 0
    private var falseAnswerCounter = 0
    private var userType = 0
    private var gameId: String = ""
    private lateinit var game: Game
    private var isEndGame = false
    private var user1PhotoUrl = ""
    private var user2PhotoUrl = ""
    private lateinit var userName: String
    private lateinit var user: User
    private lateinit var callback: OnBackPressedCallback
    private val viewModel: GameViewModel by viewModels()
    @Inject
    lateinit var userDataSource: UserLocalDataSource

    init {
        mAuth = FirebaseAuth.getInstance()
        firestore = Firebase.firestore
    }

    companion object {
        fun newInstance() = GameFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.game_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            showExitGameAlert()
        }
    }

    private fun showExitGameAlert() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Oyundan Çık")
        builder.setMessage("Emin misiniz?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("Evet") { _, _ ->
            parentFragmentManager.popBackStack()
        }
        builder.setNeutralButton("İptal") { _, _ ->
            Toast.makeText(
                requireContext(),
                "clicked cancel\n operation cancel",
                Toast.LENGTH_LONG
            ).show()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            gameId = GameFragmentArgs.fromBundle(it).gameId
            userType = GameFragmentArgs.fromBundle(it).userType
        }


    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.bottomBar!!.visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.bottomBar!!.visibility = View.VISIBLE
        viewModel.gameDetail.removeObservers(this)
        viewModel.user1.removeObservers(this)
        viewModel.user2.removeObservers(this)
        viewModel.questions.removeObservers(this)
        callback.remove()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
        timer.create(3, 60, CircularCountdown.TYPE_SECOND)
            .listener(object : CircularListener {
                override fun onTick(progress: Int) {

                }

                override fun onFinish(newCycle: Boolean, cycleCount: Int) {
                    if (!isEndGame) {
                        endGame(trueAnswerCounter)
                    }
                }
            })
            .start()
        viewModel.getGameById(gameId)
        viewModel.getQuestionByGameId(gameId)
        observeGameDetail()
        observeQuestion()
    }

    override fun onDestroy() {
        super.onDestroy()
        endGame(trueAnswerCounter)
    }

    private fun endGameResponse() {
        val data =
            if (userType == UserType.USER1.getTypeID()) {
                hashMapOf(
                    "gameId" to gameId,
                    "user1Id" to mAuth?.currentUser?.uid,
                    "user1Score" to trueAnswerCounter,
                    "user2Id" to "",
                    "user2Score" to 0
                )
            } else {
                hashMapOf(
                    "gameId" to gameId,
                    "user1Id" to "",
                    "user1Score" to 0,
                    "user2Id" to mAuth?.currentUser?.uid,
                    "user2Score" to trueAnswerCounter
                )
            }

        FirebaseFunctions.getInstance()
            .getHttpsCallable("EndGame")
            .call(data)
            .continueWith { task ->
                val result = task.result?.data as String
                if (result == "success") {
                    Toast.makeText(requireContext(), "Başarılı...", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun endGame(user1Score: Int) {
        isEndGame = true
        if (timer != null) {
            timer.stop()
        }
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.customdialog_game_end)

        dialog.avLoading.show()
        dialog.rv_users.visibility = View.GONE
        userDataSource.getUser {
            dialog.txt_user1Name.text = it?.userNickName
        }
        dialog.txt_user2Name.text = userName
        dialog.btn_mainPage.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }
        endGameResponse()
        viewModel.gameDetail.observe(this) { game ->
            game.let {
                if (game.user1Time > 0 && game.user2Time > 0) {
                    dialog.rv_users.visibility = View.VISIBLE
                    dialog.avLoading.hide()
                    if (userType == UserType.USER1.getTypeID()) {
                        dialog.txt_user1Score.text = game.user1Score.toString()
                        dialog.txt_user2Score.text = game.user2Score.toString()
                        dialog.txt_user1Time.text = game.user1Time.toString()
                        dialog.txt_user2Time.text = game.user2Time.toString()
                        Glide.with(requireContext()).load(user1PhotoUrl)
                            .into(dialog.img_user1ProfilePhoto);
                        Glide.with(requireContext()).load(user2PhotoUrl)
                            .into(dialog.img_user2ProfilePhoto);


                    } else if (userType == UserType.USER2.getTypeID()) {
                        dialog.txt_user1Score.text = game.user2Score.toString()
                        dialog.txt_user2Score.text = game.user1Score.toString()
                        dialog.txt_user1Time.text = game.user2Time.toString()
                        dialog.txt_user2Time.text = game.user1Time.toString()
                        Glide.with(requireContext()).load(user1PhotoUrl)
                            .into(dialog.img_user2ProfilePhoto);
                        Glide.with(requireContext()).load(user2PhotoUrl)
                            .into(dialog.img_user1ProfilePhoto);
                    }
                }
            }
        }
        MobileAds.initialize(requireContext())

        val adRequest = AdRequest.Builder().build()
        dialog.adView_gameEnd.loadAd(adRequest)
        dialog.show()
    }

    private fun init() {
        btn_answer1.setOnClickListener(this)
        btn_answer2.setOnClickListener(this)
        btn_answer3.setOnClickListener(this)
        btn_answer4.setOnClickListener(this)

    }

    private fun observeGameDetail() {
        viewModel.gameDetail.observe(viewLifecycleOwner) { game ->
            game.let {
                if (userType == UserType.USER1.getTypeID()) {
                    txt_user_score1.text = game.user1Score.toString()
                    txt_user_score2.text = game.user2Score.toString()
                } else {
                    txt_user_score2.text = game.user1Score.toString()
                    txt_user_score1.text = game.user2Score.toString()
                }
                viewModel.getUserById(game.user1Id, UserType.USER1.getTypeID())
                viewModel.getUserById(game.user2Id, UserType.USER2.getTypeID())
                observeUser1()
                observeUser2()
                this.game = game
            }
        }
    }

    private fun observeQuestion() {
        viewModel.questions.observe(viewLifecycleOwner) { questions ->
            questions.let {
                getQuestions = questions
                loadQuestion()
            }
        }
    }

    private fun observeUser1() {
        viewModel.user1.observe(viewLifecycleOwner) { user ->
            user.let {
                this.userName = user.userNickName
                if (userType == UserType.USER1.getTypeID()) {
                    txt_user_name1.text = user.userNickName
                    Glide.with(requireContext()).load(user.userPhoto).into(img_userphoto1);
                    user1PhotoUrl = user.userPhoto
                } else if (userType == UserType.USER2.getTypeID()) {
                    txt_user_name2.text = user.userNickName
                    Glide.with(requireContext()).load(user.userPhoto).into(img_userphoto2);
                    user2PhotoUrl = user.userPhoto
                }
            }
        }
    }

    private fun observeUser2() {
        viewModel.user2.observe(viewLifecycleOwner) { user ->
            user.let {
                if (userType == UserType.USER2.getTypeID()) {
                    txt_user_name1.text = user.userNickName
                    Glide.with(requireContext()).load(user.userPhoto).into(img_userphoto1);
                    user1PhotoUrl = user.userPhoto
                } else if (userType == UserType.USER1.getTypeID()) {
                    txt_user_name2.text = user.userNickName
                    Glide.with(requireContext()).load(user.userPhoto).into(img_userphoto2);
                    user2PhotoUrl = user.userPhoto
                }
            }
        }
    }

    private fun loadQuestion() {
        txt_word.text = getQuestions[questionCounter].word
        val allAnswers = arrayOf(
            getQuestions[questionCounter].answerFalse1,
            getQuestions[questionCounter].answerFalse2,
            getQuestions[questionCounter].answerFalse3,
            getQuestions[questionCounter].answerTrue
        )
        val randomList = (0..3).shuffled().take(4)

        btn_answer1.text = allAnswers[randomList[0]]
        btn_answer2.text = allAnswers[randomList[1]]
        btn_answer3.text = allAnswers[randomList[2]]
        btn_answer4.text = allAnswers[randomList[3]]
    }

    private fun choiceOfAnswerClick(answer: String) {
        if (answer == getQuestions[questionCounter].answerTrue) {
            trueAnswerCounter++
            txt_user_score1.text = trueAnswerCounter.toString()
            var userTypeName = ""
            userTypeName = if (userType == UserType.USER1.getTypeID()) {
                "user1Score"
            } else {
                "user2Score"

            }
            firestore.collection("Games").document(gameId)
                .update(userTypeName, trueAnswerCounter)
                .addOnSuccessListener {
                    Log.d("TAG", "DocumentSnapshot successfully updated!")

                }
        } else {
            falseAnswerCounter++
        }
        questionCounter++
        if (questionCounter < getQuestions.size) {
            loadQuestion()
        } else {
            endGame(trueAnswerCounter)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_answer1 -> {
                choiceOfAnswerClick(btn_answer1.text.toString())
            }
            R.id.btn_answer2 -> {
                choiceOfAnswerClick(btn_answer2.text.toString())
            }
            R.id.btn_answer3 -> {
                choiceOfAnswerClick(btn_answer3.text.toString())
            }
            R.id.btn_answer4 -> {
                choiceOfAnswerClick(btn_answer4.text.toString())
            }

        }
    }

}