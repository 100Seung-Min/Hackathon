package com.example.narin.Adapter

import android.content.Context
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.narin.Data.Chat
import com.example.narin.R
import java.util.*
import kotlin.collections.ArrayList

class ChatingAdapter(val itemlist: ArrayList<Chat>, val context: Context): RecyclerView.Adapter<ChatingAdapter.ViewHolder>() {

    lateinit var tts: TextToSpeech

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val userChat = view.findViewById<TextView>(R.id.user_chat)
        val aiChat = view.findViewById<TextView>(R.id.ai_chat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedview = LayoutInflater.from(parent.context).inflate(R.layout.item_talk_block, parent, false)
        return ViewHolder(inflatedview)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var content = ""
        var count = 1
        //음성기본설정
        tts = TextToSpeech(context, object : TextToSpeech.OnInitListener{
            override fun onInit(status: Int) {
                if(status != android.speech.tts.TextToSpeech.ERROR){
                    tts.setLanguage(Locale.KOREAN)
                }
            }
        })
        tts.setSpeechRate(0.8f)


        for(i in itemlist[position].content){
            content = content + i
            if(count % 17 == 0){
                content = content + "\n"
            }
            count++
        }
        if(itemlist[position].mode == 1){
            holder.userChat.visibility = View.VISIBLE
            holder.userChat.text = content
        }
        else if(itemlist[position].mode == 0){
            holder.aiChat.visibility = View.VISIBLE
            holder.aiChat.text = content
        }
        holder.aiChat.setOnClickListener {
            var say: String = ""
            for(i in holder.aiChat.text.toString()){
                if(i != '\n'){
                    say = say + i
                }
            }
            tts.speak(say, TextToSpeech.QUEUE_FLUSH, null, null)
        }
        holder.userChat.setOnClickListener {
            var say: String = ""
            for(i in holder.userChat.text.toString()){
                if(i != '\n'){
                    say = say + i
                }
            }
            tts.speak(say, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun getItemCount(): Int {
        return itemlist.size
    }
}