package com.ilkcanyilmaz.wordrival.adapters

import android.graphics.drawable.AnimatedVectorDrawable
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.ilkcanyilmaz.wordrival.R
import com.ilkcanyilmaz.wordrival.databinding.ItemGameEndWordBinding
import com.ilkcanyilmaz.wordrival.enums.AnswerType
import com.ilkcanyilmaz.wordrival.models.Question
import com.ilkcanyilmaz.wordrival.models.Word
import java.util.*
import kotlin.collections.HashMap

class GameEndWordsAdapter(getWords: List<Question>) :
    RecyclerView.Adapter<GameEndWordsAdapter.ViewHolder>() {
    var getWords: List<Question>
    var itemListener: ItemListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val productRowBinding: ItemGameEndWordBinding =
            ItemGameEndWordBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(productRowBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(getWords[position], position)
    }

    override fun getItemCount(): Int {
        return getWords.size
    }

    interface ItemListener {
        fun onFriendRequestResponse(word: Word)
    }

    inner class ViewHolder(private var productRowBinding: ItemGameEndWordBinding) :
        RecyclerView.ViewHolder(productRowBinding.root) {
        lateinit var t1: TextToSpeech
        var tickToCross: AnimatedVectorDrawable? = null
        var crossToTick: AnimatedVectorDrawable? = null
        var tick = true

        init {
            /* productRowBinding.activeButton.setOnClickListener(View.OnClickListener {
                 Log.d(
                     TAG,
                     "onClick: " + userList[adapterPosition]
                 )
             })*/

        }

        fun setData(data: Question?, position: Int) {
            t1 = TextToSpeech(
                productRowBinding.root.context
            ) { status ->
                if (status != TextToSpeech.ERROR) {
                    t1.language = Locale.UK
                }
            }
            productRowBinding.wave.alpha = 0f
            if (data?.user1Answer == AnswerType.TRUE.getTypeID()) {
                productRowBinding.cvItemGameEnd.setCardBackgroundColor(
                    ResourcesCompat.getColor(
                        productRowBinding.root.resources,
                        R.color.color_true,
                        null
                    )
                )
            } else {
                productRowBinding.cvItemGameEnd.setCardBackgroundColor(
                    ResourcesCompat.getColor(
                        productRowBinding.root.resources,
                        R.color.color_false,
                        null
                    )
                )
            }
            productRowBinding.btnAddFavorite.setOnClickListener{
                if(tick){
                    productRowBinding.btnAddFavorite.speed=1f
                    productRowBinding.btnAddFavorite.playAnimation()
                    tick=false
                }else{
                    productRowBinding.btnAddFavorite.speed=-1f
                    productRowBinding.btnAddFavorite.playAnimation()
                    tick=true

                }
            }
            productRowBinding.imgSound.setOnClickListener {
                val speechListener = object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        productRowBinding.imgSound.alpha = 0f
                        productRowBinding.wave.alpha = 1f
                    }

                    override fun onDone(utteranceId: String?) {
                        productRowBinding.imgSound.alpha = 1f
                        productRowBinding.wave.alpha = 0f
                    }

                    override fun onError(utteranceId: String?) {
                        TODO("Not yet implemented")
                    }

                }

                t1.setOnUtteranceProgressListener(speechListener)
                val mostRecentUtteranceID = (Random().nextInt() % 9999999).toString() + ""
                val params: HashMap<String, String> = HashMap()
                params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, mostRecentUtteranceID)
                t1.speak(data?.word, TextToSpeech.QUEUE_FLUSH, params)

            }
            productRowBinding.txtWord1.text = data?.word
            productRowBinding.txtWord2.text = data?.answerTrue

            tickToCross =
                getDrawable(
                    productRowBinding.root.context, R.drawable.avd_tick_to_cross
                ) as AnimatedVectorDrawable?

            crossToTick =
                getDrawable(
                    productRowBinding.root.context, R.drawable.avd_cross_to_tick
                ) as AnimatedVectorDrawable?
        }

       /* fun animate(view: View?) {
            val drawable: AnimatedVectorDrawable? = if (tick) tickToCross else crossToTick
            productRowBinding.btnAddFavorite.setImageDrawable(drawable)
            drawable?.start()
            tick = !tick
        }*/
    }


    companion object {
        private const val TAG = "UserRecyclerAdapter"
    }

    init {
        this.getWords = getWords
    }
}