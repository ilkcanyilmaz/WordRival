package com.ilkcanyilmaz.wordrival.views

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ilkcanyilmaz.wordrival.R
import com.ilkcanyilmaz.wordrival.adapters.WordListAdapter
import com.ilkcanyilmaz.wordrival.enums.WordStatus
import com.ilkcanyilmaz.wordrival.models.Question
import com.ilkcanyilmaz.wordrival.models.WordModel
import com.ilkcanyilmaz.wordrival.utils.pxToDp
import com.ilkcanyilmaz.wordrival.viewmodels.OfflineGameViewModel
import com.ilkcanyilmaz.wordrival.viewmodels.WordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.ll_online
import kotlinx.android.synthetic.main.fragment_home.txt_headerOffline
import kotlinx.android.synthetic.main.fragment_home.txt_headerOnline
import kotlinx.android.synthetic.main.fragment_word.*
@AndroidEntryPoint
class WordFragment : Fragment() {
    private val viewModel: WordViewModel by viewModels()
    var wordDisplayType: WordStatus = WordStatus.STUDIED


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_word, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUIGameDisplay()

        btn_studiedWord.setOnClickListener {
            wordDisplayType = WordStatus.STUDIED
            updateUIGameDisplay()
        }
        btn_learnedWord.setOnClickListener {
            wordDisplayType = WordStatus.LEARNED
            updateUIGameDisplay()
        }

        loadWords()
    }

    private fun updateUIGameDisplay() {
        when (wordDisplayType) {

            WordStatus.STUDIED -> {

                val drawableOnline = btn_studiedWord.background as GradientDrawable
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
                btn_studiedWord.background = drawableOnline

                val drawableOffline = btn_learnedWord.background as GradientDrawable
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
                btn_learnedWord.background = drawableOffline


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
            }
            WordStatus.LEARNED -> {
               /* val drawableOnline = btn_online.background as GradientDrawable
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
*/
            }
        }
    }

    private fun loadWords() {
        val data: ArrayList<Question> = ArrayList()
        data.add(Question("", "Orange", "Turuncu", "Sarı", "Yeşil", "Mavi", 1, 0, 0))
        data.add(Question("", "Blue", "Mavi", "Sarı", "Yeşil", "Kırmızı", 1, 0, 0))
       viewModel.wordToQuestionById()
        viewModel.getWords.observe(viewLifecycleOwner, {
            val adapter = WordListAdapter(it, requireContext())
            rv_learningWord.adapter = adapter
        })

    }

    /*fun wordToQuestionById(words:List<WordModel>):List<Question>{
        var question:List<Question>
        words.forEach{ word ->
        word.questionId
        }
        return question
    }*/
}