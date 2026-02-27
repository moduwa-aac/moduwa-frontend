import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aac.data.remote.api.RetrofitInstance
import com.example.aac.data.remote.dto.TtsPreviewRequest
import com.example.aac.data.remote.dto.TtsSettingUpdateRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class VoiceSettingViewModel : ViewModel() {

    // 🟢 클래스 멤버 변수로 선언해야 중간에 날아가지 않습니다!
    private var mediaPlayer: MediaPlayer? = null
    private var isPreviewLoading = false

    fun playPreviewTts(context: Context, voiceKey: String) {
        Log.d("TTS_PREVIEW", "1. playPreviewTts 호출됨 - voiceKey: $voiceKey")

        if (isPreviewLoading) {
            Log.d("TTS_PREVIEW", "⚠️ 이미 로딩 중이라 요청 무시됨")
            return
        }
        isPreviewLoading = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("TTS_PREVIEW", "2. API 요청 시작")
                val res = RetrofitInstance.api.requestTtsPreviewMp3(TtsPreviewRequest(voiceKey))

                if (!res.isSuccessful) {
                    Log.e("TTS_PREVIEW", "❌ API 에러 발생: HTTP ${res.code()} / ${res.errorBody()?.string()}")
                    return@launch
                }

                val body = res.body()
                if (body == null) {
                    Log.e("TTS_PREVIEW", "❌ API 응답 Body가 null입니다.")
                    return@launch
                }

                Log.d("TTS_PREVIEW", "3. 파일 쓰기 시작 (Content-Length: ${body.contentLength()} bytes)")
                val outFile = File(context.applicationContext.cacheDir, "preview_tts.mp3")

                body.use { responseBody ->
                    responseBody.byteStream().use { input ->
                        FileOutputStream(outFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                Log.d("TTS_PREVIEW", "4. 파일 쓰기 완료 - 실제 파일 크기: ${outFile.length()} bytes, 경로: ${outFile.absolutePath}")

                if (outFile.length() == 0L) {
                    Log.e("TTS_PREVIEW", "❌ 파일 크기가 0입니다. 저장이 제대로 안 됐습니다.")
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    Log.d("TTS_PREVIEW", "5. 메인 스레드 진입 - MediaPlayer 세팅 시작")
                    stopMediaPlayer()

                    mediaPlayer = MediaPlayer().apply {
                        // 🟢 오디오 속성 명시 (최신 안드로이드 권장)
                        setAudioAttributes(
                            android.media.AudioAttributes.Builder()
                                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                                .build()
                        )

                        Log.d("TTS_PREVIEW", "6. DataSource 설정 (FileDescriptor 사용)")
                        // 🟢 절대 경로 대신 FileDescriptor 사용 (권한/보안 문제 해결)
                        java.io.FileInputStream(outFile).use { fis ->
                            setDataSource(fis.fd)
                        }

                        setOnPreparedListener {
                            Log.d("TTS_PREVIEW", "7. 준비 완료! ▶️ 재생 시작 (길이: ${it.duration}ms)")
                            start()
                        }

                        setOnCompletionListener {
                            Log.d("TTS_PREVIEW", "8. 재생 완료 ⏹️")
                            stopMediaPlayer()
                        }

                        setOnErrorListener { _, what, extra ->
                            Log.e("TTS_PREVIEW", "❌ MediaPlayer 에러 발생! what: $what, extra: $extra")
                            stopMediaPlayer()
                            true
                        }

                        Log.d("TTS_PREVIEW", " - prepareAsync 호출")
                        prepareAsync()
                    }
                }
            } catch (e: Exception) {
                Log.e("TTS_PREVIEW", "❌ 예외(Exception) 발생", e)
            } finally {
                isPreviewLoading = false
                Log.d("TTS_PREVIEW", "9. 로딩 상태 해제 완료")
            }
        }
    }

    // 재생 멈춤 및 메모리 해제
    private fun stopMediaPlayer() {
        runCatching { mediaPlayer?.stop() }
        runCatching { mediaPlayer?.release() }
        mediaPlayer = null
    }

    override fun onCleared() {
        super.onCleared()
        stopMediaPlayer() // 뷰모델이 죽을 때 반드시 해제해줘야 앱이 안 터집니다.
    }

    fun saveVoiceSetting(voiceKey: String, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = RetrofitInstance.api.updateTtsSetting(TtsSettingUpdateRequest(voiceKey))
                withContext(Dispatchers.Main) {
                    if (res.success) {
                        Log.d("TTS_SETTING", "✅ 보이스키 저장 성공: $voiceKey")
                        onSuccess() // 저장 성공 시 화면 닫기 실행
                    } else {
                        Log.e("TTS_SETTING", "❌ 저장 실패: ${res.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("TTS_SETTING", "❌ 저장 네트워크 에러", e)
            }
        }
    }
}