package com.oo.cardemo.detectors

import android.graphics.Color
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
/**
 *  识别处理
 *
 * */
interface IDetector {
    fun process(rgbaImage: Mat?,colors:List<Color>?=null)
    fun getContours():List<MatOfPoint>
}