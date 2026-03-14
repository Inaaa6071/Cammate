package com.example.cammate.presentation.viewer

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.cammate.MainActivity
import com.example.cammate.databinding.ActivityMakerCallBinding
import com.example.cammate.webRTC.Models.IceCandidateModel
import com.example.cammate.webRTC.Models.MessageModel
import com.example.cammate.webRTC.RTCClient
import com.example.cammate.webRTC.SocketRepository
import com.example.cammate.webRTC.utils.NewMessageInterface
import com.example.cammate.webRTC.utils.PeerConnectionObserver
import com.google.gson.Gson
import com.permissionx.guolindev.PermissionX
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MakerCallActivity : AppCompatActivity() , NewMessageInterface {
    lateinit var binding: ActivityMakerCallBinding
    private var roomName:String?=null
    private var userName:String?=null
    private var socketRepository: SocketRepository?=null
    private var rtcClient : RTCClient?=null
    private val gson = Gson()
    private var isCameraPause = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMakerCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lateinit var cameraLauncher: ActivityResultLauncher<Intent>
        // 이미지가 저장될 위치
        lateinit var filePath:String
        // 저장된 파일에 접근하기 위한 Uri
        lateinit var contentUri:Uri
        // 화면 꺼지지 않도록
        //window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // rtcClient?.endCall()

        //
//        binding.btnEndCall.setOnClickListener {
//            val intent = Intent(this, CameraActivity::class.java)
//            startActivity(intent)
//        }

//        filePath = getExternalFilesDir(null).toString()
//        val contract1 = ActivityResultContracts.StartActivityForResult()
//        cameraLauncher = registerForActivityResult(contract1){
//
//        }
//        binding.btnCamera.setOnClickListener {
////                val intent = Intent(this, CameraActivity::class.java)
////                startActivity(intent)
////                if (isCameraPause){
////                    isCameraPause = false
////                }else{
////                    isCameraPause = true
////                }
////                rtcClient?.toggleCamera(isCameraPause)
//
//            // 사진 촬영을 위한 Activity를 실행한다.
//            val newIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            // ACTION_IMAGE_CAPTURE : 카메라의 이름은 다 이걸로 명시
//            val now = System.currentTimeMillis()
//            val fileName = "/temp_${now}.jpg"
//            // 경로 + 파일 이름
//            val picPath = "${filePath}/${fileName}"
//
//            val file = File(picPath)
//
//            // 사진이 저장될 경로를 관리할 Uri 객체를 생성한다.
//            contentUri = FileProvider.getUriForFile(this,"com.cammate.provider",file)
//
//            newIntent.putExtra(MediaStore.EXTRA_OUTPUT,contentUri)
//            cameraLauncher.launch(newIntent)
//
//            cameraLauncher = registerForActivityResult(contract1){
//                if(it?.resultCode == RESULT_OK){
//                    // Uri를 이용해 이미지에 접근하여 Bitmap 객체를 생성한다.
//                    val bitmap = BitmapFactory.decodeFile(contentUri.path)
//                    // 이미지 파일이 계속 저장되므로 삭제해야한다.
//                }
//            }
//
////                PermissionX.init(this@MakerCallActivity)
////                    .permissions(
////                        Manifest.permission.READ_EXTERNAL_STORAGE,
////                        Manifest.permission.WRITE_EXTERNAL_STORAGE
////                    ).request{ allGranted, _ ,_ ->
////                        if (allGranted){
////
////                        } else {
////                            Toast.makeText(this@MakerCallActivity,"you should accept all permissions",Toast.LENGTH_LONG).show()
////                        }
////                    }
//
//
//        }

        userName = intent.getStringExtra("UserName")
        userName += "01"
        roomName = intent.getStringExtra("TargetName")
        roomName += "01"

        init()

    }
    private fun init(){
        socketRepository = SocketRepository(this)
        userName?.let { socketRepository?.initSocket(it) }
        rtcClient = RTCClient(application, userName!!,socketRepository!!, object : PeerConnectionObserver() {
            override fun onIceCandidate(p0: IceCandidate?) {
                super.onIceCandidate(p0)
                rtcClient?.addIceCandidate(p0)
                val candidate = hashMapOf(
                    "sdpMid" to p0?.sdpMid,
                    "sdpMLineIndex" to p0?.sdpMLineIndex,
                    "sdpCandidate" to p0?.sdp
                )

                socketRepository?.sendMessageToSocket(
                    MessageModel("ice_candidate",userName,roomName,candidate)
                )

            }

            override fun onAddStream(p0: MediaStream?) {
                super.onAddStream(p0)
                //p0?.videoTracks?.get(0)?.addSink(binding.remoteViewM)
                var tmp = p0?.videoTracks?.get(0)
                //Log.d(TAG, "onAddStream: $p0")
                Log.d("TAG", " MediaStream id : $p0 videoid : $tmp")

            }
        })

        binding.btnChatting.setOnClickListener {
            // 채팅 기능
        }
        binding.btnCamera.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        socketRepository?.sendMessageToSocket(MessageModel(
            "start_call",userName,roomName,null
        ))


    }

    override fun onNewMessage(message: MessageModel) {
        when(message.type){
            "call_response"->{ //전화 거는 사람
                if (message.data == "user is not online"){
                    //user is not reachable
                    runOnUiThread {
                        Toast.makeText(this,"user is not reachable", Toast.LENGTH_LONG).show()

                    }
                }else{
                    //we are ready for call, we started a call
                    runOnUiThread {
                        binding.apply {
                            remoteViewLoading.visibility = View.GONE
                            rtcClient?.initializeSurfaceView(remoteViewM)
                            rtcClient?.startLocalVideo(remoteViewM) //remoteView로 바꿔야
                            rtcClient?.startAddStream()
                            rtcClient?.call(roomName!!)
                        }


                    }

                }
            }
            "answer_received" ->{
                val session = SessionDescription(
                    SessionDescription.Type.ANSWER,
                    message.data.toString()
                )
                rtcClient?.onRemoteSessionReceived(session)
                runOnUiThread {
                    binding.remoteViewLoading.visibility = View.GONE
                }
                Log.d("answr_Received", "answer_received: session : $session")
            }
            "offer_received" ->{ // 전화 받는 사람
                runOnUiThread {
                        binding.apply {
                            //rtcClient?.initializeSurfaceView(localView)
                            rtcClient?.initializeSurfaceView(remoteViewM)
                            //rtcClient?.startLocalVideo(remoteView) //remote view
                        }
                        val session = SessionDescription(
                            SessionDescription.Type.OFFER,
                            message.data.toString()
                        )
                        rtcClient?.onRemoteSessionReceived(session)
                        rtcClient?.answer(message.name!!)
                        roomName = message.name!!
                        binding.remoteViewLoading.visibility = View.GONE
                }
                Log.d("offer", "offer_received")
            }

            "ice_candidate"->{
                try {
                    val receivingCandidate = gson.fromJson(gson.toJson(message.data),
                        IceCandidateModel::class.java)
                    rtcClient?.addIceCandidate(IceCandidate(receivingCandidate.sdpMid,
                        Math.toIntExact(receivingCandidate.sdpMLineIndex.toLong()),receivingCandidate.sdpCandidate))
                }catch (e:Exception){
                    e.printStackTrace()
                }
                Log.d("ice_candidate", "ice_candidate")
            }
        }
    }


//    fun ScreenshotButton(view: View?) {
//        PermissionX.init(this@MakerCallActivity)
//            .permissions(
//                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//            ).request{ allGranted, _ ,_ ->
//                if (allGranted){
//
//                } else {
//                    Toast.makeText(this@MakerCallActivity,"you should accept all permissions",
//                        Toast.LENGTH_LONG).show()
//                }
//            }
//        val rootView = window.decorView //전체화면 부분
//        val screenShot: File ?= ScreenShot(rootView)
//        if (screenShot != null) {
//            //갤러리에 추가합니다
//            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(screenShot)))
//        }
//        Toast.makeText(applicationContext, "갤러리에 저장되었습니다.", Toast.LENGTH_SHORT).show()
//    }
//
//    fun ScreenShot(view: View): File? {
//        view.setDrawingCacheEnabled(true)
//        val screenBitmap = view.drawingCache //비트맵으로 변환
//        val filename = "screenshot" + System.currentTimeMillis() + ".png"
//        val file =
//            File(Environment.getExternalStorageDirectory().toString() + "/Pictures", filename)
//        var os: FileOutputStream? = null
//        try {
//            os = FileOutputStream(file)
//            screenBitmap.compress(Bitmap.CompressFormat.PNG, 90, os) //비트맵 > PNG파일
//            os.close()
//        } catch (e: IOException) {
//            e.printStackTrace()
//            return null
//        }
//        view.setDrawingCacheEnabled(false)
//        return file
//    }

}