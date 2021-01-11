package com.oo.cardemo

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import com.oo.cardemo.base.BaseDetectActivity
import com.oo.cardemo.detectors.IDetector
import com.oo.cardemo.mock.DetectionBasedTracker
import org.opencv.core.Mat
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.io.FileOutputStream


class DemoActivity : BaseDetectActivity(),DetectionBasedTracker.ResultCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun getDetector(): IDetector {
//        return ColorBlobDetector()

        return DetectionBasedTracker(this).apply {
            setCallback(this@DemoActivity)
        }
    }

    override fun getCustomLayoutId(): Int {
       return R.layout.info_view
    }

    lateinit var tv:TextView
    override fun initCustomView(container: ViewGroup) {
        tv = container.findViewById(R.id.position_info)
    }

    override fun onDetected(pos: Mat?) {
        Log.i(TAG, "onDetected: ${pos}")
    }


}