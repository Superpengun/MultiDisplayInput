package com.sinovoice.ai

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import com.sinovoice.ai.nlu.NLUEventListener
import com.sinovoice.ai.nlu.NLUManager
import com.sinovoice.ai.nlu.NluResult
import com.sinovoice.ai.services.AnswerResult
import com.sinovoice.ai.tts.TTSConstants
import com.sinovoice.ai.utils.GsonUtils
import com.sinovoice.sdk.HciError
import com.sinovoice.sdk.tts.SynthResult
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class AIRecorderTest {
    private val TAG = "AIRecordTest"

    @Before
    fun init() {
        initLogger()
    }

    //语音识别接口
    @Test
    fun aiRecorderAsrTest() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val countDownLatch = CountDownLatch(1)
        var sdkInit = false
        AIRecorder.get().init(appContext, object : AIRecorder.AIEventListener {
            override fun onEvent(event: AIEvent) {
                when (event.eventType) {
                    AIEventType.SDK_INIT -> {
                        sdkInit = true
                        //ai 课堂使用对话 用非持续模式，会自动判断句子结尾
                        AIRecorder.get().startRecognize(continueRecognize = true)
                    }
                    AIEventType.SDK_INIT_FAIL -> {
                    }
                    AIEventType.RECOGNIZE_RESULT -> {
                        Logger.t(TAG).d("onEvent: " + event.result)
                        //识别结果在此回调中 实时给出
                        //AIRecorder.get().getServiceAnswer("什么是血管紧张素")
                    }
                    //识别开始
                    AIEventType.RECOGNIZE_START -> {

                    }
                    //识别结束
                    AIEventType.RECOGNIZE_STOP -> {

                    }
                    //识别的音频数据
                    AIEventType.RECORDER_RECOGNIZE_VOICE_DATA -> {

                    }
                    else -> Unit
                }
            }
        })
        countDownLatch.await(20, TimeUnit.SECONDS)
        AIRecorder.get().stopRecognize(false)
        AIRecorder.get().release()
    }

    //单独使用问答接口
    @Test
    fun aiRecorderServiceTest() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val countDownLatch = CountDownLatch(1)
        AIRecorder.get().init(appContext, object : AIRecorder.AIEventListener {
            override fun onEvent(event: AIEvent) {
                when (event.eventType) {
                    AIEventType.SERVICE_RESULT -> {
                        val result = GsonUtils.fromJson(event.result, AnswerResult::class.java)
                        countDownLatch.countDown()
                    }
                    AIEventType.SERVICE_ERROR -> {
                        countDownLatch.countDown()
                    }
                    else -> Unit
                }
            }
        })
        AIRecorder.get().getServiceAnswer("什么是血管紧张素？")
        countDownLatch.await(20, TimeUnit.SECONDS)
    }


    @Test
    fun aiRecorderTTSTest() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        AIRecorder.get().init(appContext, object : AIRecorder.AIEventListener {
            override fun onEvent(event: AIEvent) {
            }
        })
        AIRecorder.get().playTTS(
            text = "北京天气为阴,气温0到7摄氏度,北京天气为阴,气温0到7摄氏度,北京天气为阴,气温0到7摄氏度,北京天气为阴,气温0到7摄氏度,北京天气为阴,气温0到7摄氏度,",
            TTSConstants.TTS_ROU_MEI_QIAN, id = 0
        )
        SystemClock.sleep(10000000)
//        AIRecorder.get().playTTS(text = "明天天气怎么样", TTSConstants.TTS_ROU_MEI_JUAN,id = 0)
    }

    @Test
    fun aiRecorderTTSDataTest() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val countDownLatch = CountDownLatch(1)
        var synthResult: SynthResult? = null
        AIRecorder.get().init(appContext, object : AIRecorder.AIEventListener {
            override fun onEvent(event: AIEvent) {
                when (event.eventType) {
                    AIEventType.TTS_RESULT -> {
                        synthResult = GsonUtils.fromJson(event.result, SynthResult::class.java)
                        assert(synthResult!!.code == HciError.OK)
                        countDownLatch.countDown()
                    }
                    else -> Unit
                }
            }
        })
        AIRecorder.get().getTTSData(
            text = "古时候有一个人,他想偷一个富人家的铃铛,但是他一碰铃铛铃铛就不叮叮当当的响这" +
                    "样的声音会被别人发现的.于是他就想到一个好办法,把自己的耳朵捂住.这样" +
                    "自己就听不到铃铛响了,就可以放心的偷铃铛了,但是他的这种做法是愚蠢的,因为" +
                    "别人还是会听到铃铛响,只是他自己捂住了自己的耳朵听不见而已.最后他被别人发现了.",
            TTSConstants.TTS_ROU_MEI_QIAN
        )
        countDownLatch.await()
        playBuffer(synthResult?.audio!!)
    }

    fun playBuffer(buffer: ByteArray) {
        val bufferSize = AudioTrack.getMinBufferSize(
            16000,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val mAudioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            16000,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
            AudioTrack.MODE_STREAM
        )
        mAudioTrack.play()
        mAudioTrack.write(buffer, 0, buffer.size)
    }


    @Test
    fun aiRecorderNLUTest() {
        val startSessionResult = NLUManager.get().startNLUSession()
        Logger.t(TAG).d("aiRecorderNLUTest: $startSessionResult")
        check(startSessionResult)
        NLUManager.get().processNLU("是的", object : NLUEventListener {
            override fun onNLUResult(result: NluResult) {
                Logger.t(TAG).d("onNLUResult: $result")
                check(result.cmd!!.isNotEmpty())
            }
        })
        SystemClock.sleep(101000)
    }


    private fun initLogger() {
        val formatStrategy: FormatStrategy = PrettyFormatStrategy.newBuilder()
            .methodCount(0)
            .showThreadInfo(false)
            .tag("VoiceAssistant")
            .build()
        Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))
    }

}