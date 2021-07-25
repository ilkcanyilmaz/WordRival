package com.ilkcanyilmaz.wordrival.views

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.ilkcanyilmaz.wordrival.adapters.GameEndWordsAdapter
import com.ilkcanyilmaz.wordrival.enums.AnswerType
import com.ilkcanyilmaz.wordrival.models.Game
import com.ilkcanyilmaz.wordrival.models.Question
import com.ilkcanyilmaz.wordrival.offlineRandomGame
import com.ilkcanyilmaz.wordrival.repositories.QuestionLocalDataSource
import com.ilkcanyilmaz.wordrival.viewmodels.OfflineGameViewModel
import dagger.hilt.android.AndroidEntryPoint
import douglasspgyn.com.github.circularcountdown.CircularCountdown
import douglasspgyn.com.github.circularcountdown.listener.CircularListener
import kotlinx.android.synthetic.main.activity_game.btn_answer1
import kotlinx.android.synthetic.main.activity_game.btn_answer2
import kotlinx.android.synthetic.main.activity_game.btn_answer3
import kotlinx.android.synthetic.main.activity_game.btn_answer4
import kotlinx.android.synthetic.main.activity_game.timer
import kotlinx.android.synthetic.main.activity_game.txt_word
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.customdialog_game_end.adView_gameEnd
import kotlinx.android.synthetic.main.customdialog_game_end.btn_mainPage
import kotlinx.android.synthetic.main.customdialog_offline_game_end.*
import kotlinx.android.synthetic.main.offline_game_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class OfflineGameFragment : Fragment(), View.OnClickListener {
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
    private lateinit var userName: String
    private lateinit var callback: OnBackPressedCallback
    private var remainingTime = 0
    private val viewModel: OfflineGameViewModel by viewModels()

    @Inject
    lateinit var questionDataSource: QuestionLocalDataSource

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
        return inflater.inflate(R.layout.offline_game_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            showExitGameAlert()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

            getQuestions=offlineRandomGame(questionDataSource.getQuestion())

            loadQuestion()
            buttonTouch()


    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.bottomBar!!.visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.bottomBar!!.visibility = View.VISIBLE
        viewModel.gameDetail.removeObservers(this)
        viewModel.questions.removeObservers(this)
        callback.remove()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
        timerAddSecond(60, true)


        Glide.with(requireContext()).load(mAuth?.currentUser?.photoUrl)
            .into(img_userProfilePhoto);
        //viewModel.getGameById(gameId)
        //viewModel.getQuestionByGameId(gameId)
        //observeGameDetail()
    }

    override fun onDestroy() {
        super.onDestroy()
        endGame()
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

    private fun timerAddSecond(endTime: Int, isStart: Boolean) {
        var mEndTime = endTime
        if (!isStart) {
            mEndTime = endTime + 1
        }
        timer.stop()
        timer.create(0, mEndTime, CircularCountdown.TYPE_SECOND)
            .listener(object : CircularListener {
                override fun onTick(progress: Int) {
                    remainingTime = endTime - progress

                    timer.setProgress(((remainingTime * (60 - remainingTime)) / 60).toLong())
                }

                override fun onFinish(newCycle: Boolean, cycleCount: Int) {
                    if (!isEndGame) {
                        endGame()
                    }
                }
            })
            .start()
    }

    private fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if ((v as Button).text == getQuestions[questionCounter].answerTrue) {
                    v.setBackgroundResource(R.drawable.shape_true_background)
                } else {
                    v.setBackgroundResource(R.drawable.shape_false_background)
                }
            }
            MotionEvent.ACTION_UP -> {
                v.setBackgroundResource(R.drawable.shape_shadow_button)
                choiceOfAnswerClick((v as Button).text.toString())
            }
        }
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    fun buttonTouch() {
        btn_answer1.setOnTouchListener { v, event ->
            onTouch(v, event)
            true
        }
        btn_answer2.setOnTouchListener { v, event ->
            onTouch(v, event)
            true
        }
        btn_answer3.setOnTouchListener { v, event ->
            onTouch(v, event)
            true
        }
        btn_answer4.setOnTouchListener { v, event ->
            onTouch(v, event)
            true
        }
    }

    private fun endGame() {
        isEndGame = true
        if (timer != null) {
            timer.stop()
        }
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.customdialog_offline_game_end)

        val window: Window? = dialog.window
        window?.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )

        dialog.btn_mainPage.setOnClickListener {
            startMainActivity()
        }
        val adapter = GameEndWordsAdapter(getQuestions, viewModel)
        dialog.txt_trueCount.text = trueAnswerCounter.toString()
        dialog.txt_falseCount.text = falseAnswerCounter.toString()
        dialog.rv_word.adapter = adapter
        MobileAds.initialize(requireContext())

        val adRequest = AdRequest.Builder().build()
        dialog.adView_gameEnd.loadAd(adRequest)
        dialog.show()
    }

    fun startMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
    }

    private fun init() {
        //viewModel = ViewModelProvider(this).get(OfflineGameViewModel::class.java)
        btn_answer1.setOnClickListener(this)
        btn_answer2.setOnClickListener(this)
        btn_answer3.setOnClickListener(this)
        btn_answer4.setOnClickListener(this)
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
            getQuestions[questionCounter].user1Answer = AnswerType.TRUE.value
            txt_score.text = trueAnswerCounter.toString()
            timerAddSecond(remainingTime + 1, false)
        } else {
            falseAnswerCounter++
            getQuestions[questionCounter].user1Answer = AnswerType.FALSE.value
        }
        questionCounter++
        if (questionCounter < getQuestions.size) {
            loadQuestion()
        } else {
            endGame()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_answer1 -> {
                //choiceOfAnswerClick(btn_answer1.text.toString())
            }
            R.id.btn_answer2 -> {
                //choiceOfAnswerClick(btn_answer2.text.toString())
            }
            R.id.btn_answer3 -> {
                //choiceOfAnswerClick(btn_answer3.text.toString())
            }
            R.id.btn_answer4 -> {
                //choiceOfAnswerClick(btn_answer4.text.toString())
            }
        }
    }
}