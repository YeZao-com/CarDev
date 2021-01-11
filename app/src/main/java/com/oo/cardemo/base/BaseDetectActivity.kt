package com.oo.cardemo.base

import android.os.Bundle
import android.util.Log
import android.view.*
import com.oo.cardemo.detectors.IDetector
import com.oo.cardemo.R
import com.oo.cardemo.renders.IFrameRender
import org.opencv.android.*
import org.opencv.core.CvType
import org.opencv.core.Mat
import java.util.*


/**
 * 所有扫描页面的基类 activity
 * 包含 touch  以及预览 的基本功能
 * 流出  detector 接口方法
 * */
abstract class BaseDetectActivity : CameraActivity(),CameraBridgeViewBase.CvCameraViewListener2 {
    val TAG:String by lazy{
        javaClass.simpleName
    }
    // 预览帧 缓存数据
    protected lateinit var mRgba: Mat
    //预览 view
    protected lateinit var mOpenCvCameraView :CameraBridgeViewBase


    private val touchListener :View.OnTouchListener = object :View.OnTouchListener{
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            return false;
        }

    }

    private val mLoaderCallback:BaseLoaderCallback = object :BaseLoaderCallback(this){
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")
                    mOpenCvCameraView.enableView()
                    mOpenCvCameraView.setOnTouchListener(touchListener)
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }

        override fun onPackageInstall(operation: Int, callback: InstallCallbackInterface?) {
            super.onPackageInstall(operation, callback)
        }
    }


    //识别功能 接口类  具体实现由子 view 传输的 detector 来处理
    private lateinit var mDetector: IDetector
    //图像渲染 接口类
    private lateinit var mFrameRender:IFrameRender

    private lateinit var container:ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_base_detect)
        mOpenCvCameraView = findViewById(R.id.java_camera_view)
        container = findViewById(R.id.custom_container)
        container.visibility = View.GONE
        if(getCustomLayoutId() > 0){
            layoutInflater.inflate(getCustomLayoutId(),container,true)
            initCustomView(container)
            container.visibility = View.VISIBLE
        }

        mOpenCvCameraView.visibility = View.VISIBLE
        mOpenCvCameraView.setCvCameraViewListener(this@BaseDetectActivity)
    }

    abstract fun getDetector(): IDetector
    abstract fun getCustomLayoutId():Int
    protected abstract fun initCustomView(container:ViewGroup)


    override fun onCameraViewStarted(width: Int, height: Int) {
        mDetector=getDetector()

        mRgba = Mat(height, width, CvType.CV_8UC4)
    }

    override fun onCameraViewStopped() {
        mRgba.release()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        mRgba = inputFrame.rgba()
        mDetector.process(mRgba)
        return mRgba
    }

    override fun getCameraViewList(): MutableList<out CameraBridgeViewBase> {
        return Collections.singletonList(mOpenCvCameraView)
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    override fun onPause() {
        super.onPause()
        mOpenCvCameraView.disableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        mOpenCvCameraView.disableView()
    }
}