package com.example.narin

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.narin.Adapter.ChatingAdapter
import com.example.narin.Data.Chat
import com.example.narin.Data.Message
import com.example.narin.Data.PostChatData
import com.example.narin.Retrofit.RetrofitClient
import com.example.narin.databinding.ActivityTalkNarinBinding
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Response
import java.util.*
import java.util.jar.Manifest
import kotlin.collections.ArrayList

class TalkNarin : AppCompatActivity() {

    val context = this

    val binding by lazy { ActivityTalkNarinBinding.inflate(layoutInflater) }
    var pressTime: Long = 0
    var content: String = ""
    var history: ArrayList<String?> = ArrayList()
    var itemlist: ArrayList<Chat> = ArrayList()
    lateinit var adapter: ChatingAdapter
    lateinit var pref: SharedPreferences
    lateinit var edit: SharedPreferences.Editor
    var count = 1

    lateinit var STTintent:Intent
    lateinit var recognizer: SpeechRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //기존 입력데이터 가져오기
        pref = getSharedPreferences("CHATLOG", MODE_PRIVATE)
        edit = pref.edit()

        for(i in 1..10){
            println("갖고오기")
            history.add(pref.getString("${i}", ""))
        }
        // 툴바 기본설정
        setSupportActionBar(binding.talkNaringToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)


        //음성인식 기본 설정
        STTintent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        STTintent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        STTintent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR")


        //음성인식 시작
        binding.micBtn.setOnClickListener {
            recognizer = SpeechRecognizer.createSpeechRecognizer(context)

            recognizer.setRecognitionListener(object : RecognitionListener{
                override fun onReadyForSpeech(params: Bundle?) {
                    binding.micBtn.setImageResource(R.drawable.ic_baseline_mic_blue_24)
                }

                override fun onBeginningOfSpeech() {
                }

                override fun onRmsChanged(rmsdB: Float) {

                }

                override fun onBufferReceived(buffer: ByteArray?) {
                    println("여기4")
                }

                override fun onEndOfSpeech() {
                    binding.micBtn.setImageResource(R.drawable.ic_baseline_mic_grey_24)
                }

                override fun onError(error: Int) {
                    binding.micBtn.setImageResource(R.drawable.ic_baseline_mic_grey_24)
                    var message = ""
                    when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> {
                            message = "오디오 에러"
                        }
                        SpeechRecognizer.ERROR_CLIENT -> {
                            message = "클라이언트 에러"
                        }
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                            message = "퍼미션 없음"
                        }
                        SpeechRecognizer.ERROR_NETWORK -> {
                            message = "네트워크 에러"
                        }
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                            message = "네트웍 타임아웃"
                        }
                        SpeechRecognizer.ERROR_NO_MATCH -> {
                            message = "찾을 수 없음"
                        }
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                            message = "RECOGNIZER가 바쁨"
                        }
                        SpeechRecognizer.ERROR_SERVER -> {
                            message = "서버가 이상함"
                        }
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                            message = "말하는 시간초과"
                        }
                        else -> {
                            message = "알 수 없는 오류임"
                        }
                    }
                    println("여기 ${message}")
                }

                override fun onResults(results: Bundle?) {
                    val matches = results!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    for(i in matches!!){
                        binding.userChatTxt.setText(i)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    println("여기8")
                }

                override fun onEvent(eventType: Int, params: Bundle?) {
                    println("여기9")
                }

            })
            recognizer.startListening(STTintent)
        }

        //recyclerview 기본 설정
        adapter = ChatingAdapter(itemlist, this)
        binding.chatRecyclerView.adapter = adapter
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)

        binding.sendBtn.setOnClickListener {
            val userText = binding.userChatTxt
            content = userText.text.toString()
            if(content != ""){
                itemlist.add(Chat(content, 1))
                userText.setText("")
                changeList()
                callAi()
            }
        }


        //텍스트 입력시 버튼 색깔 바꾸기
        binding.userChatTxt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val content = s.toString()
                if(content != ""){
                    binding.sendBtn.setTextColor(Color.parseColor("#3182F7"))
                }
                else{
                    binding.sendBtn.setTextColor(Color.parseColor("#B4B4B4"))
                }
            }

        })


    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                startActivity(intent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    // 뒤로가기 두번 시 종료
    override fun onBackPressed() {
        val currentTime = System.currentTimeMillis()
        val intervalTime = currentTime - pressTime
        if (intervalTime < 2000) {
            super.onBackPressed()
            finishAffinity()
        } else {
            pressTime = currentTime
            Toast.makeText(this, "한 번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
    }

    //ai 채팅 가져오기
    private fun callAi(){
        RetrofitClient.api.postChat(PostChatData(history, content)).enqueue(object : retrofit2.Callback<Message>{
            override fun onResponse(call: Call<Message>, response: Response<Message>) {
                itemlist.add(Chat(response.body()!!.message, 0))
                changeList()
            }

            override fun onFailure(call: Call<Message>, t: Throwable) {
                println("this error ${t}")
            }
        })
    }


    //기록 남기는 부분
    private fun addHistory(){
        edit.putString("${count}", itemlist[itemlist.size-1].content)
        edit.commit()
        count++
        if(count == 10){
            count = 1
        }
    }


    //리스트가 바뀌었을 때 갱신하는 부분
    private fun changeList(){
        adapter.notifyItemInserted(itemlist.size - 1)
        addHistory()
        binding.chatRecyclerView.smoothScrollToPosition(itemlist.size)
    }
}