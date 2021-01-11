package com.oo.cardemo.mock;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.oo.cardemo.R;
import com.oo.cardemo.detectors.IDetector;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DetectionBasedTracker implements IDetector {
    private CascadeClassifier mJavaDetector;

    private long delay = 333L;


    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);

    public DetectionBasedTracker(Context context) {
        init(context);
//        mNativeObj = nativeCreateObject(cascadeName, minFaceSize);
    }

    public void start() {
        nativeStart(mNativeObj);
    }

    public void stop() {
        nativeStop(mNativeObj);
    }

    public void setMinFaceSize(int size) {
        nativeSetFaceSize(mNativeObj, size);
    }

    public void detect(Mat imageGray, MatOfRect faces) {
        nativeDetect(mNativeObj, imageGray.getNativeObjAddr(), faces.getNativeObjAddr());
    }


    public void init(Context context) {

        // Load native library after(!) OpenCV initialization
        System.loadLibrary("detection_based_tracker");
        File mCascadeFile;
        try {
            // load cascade file from application resources
            InputStream is = context.getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            cascadeDir.delete();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void release() {
        nativeDestroyObject(mNativeObj);
        mNativeObj = 0;
        faces.release();
    }

    private long mNativeObj = 0;

    int mAbsoluteFaceSize = 0;
    private float mRelativeFaceSize = 0.2f;

    MatOfRect faces = new MatOfRect();

    private static native long nativeCreateObject(String cascadeName, int minFaceSize);

    private static native void nativeDestroyObject(long thiz);

    private static native void nativeStart(long thiz);

    private static native void nativeStop(long thiz);

    private static native void nativeSetFaceSize(long thiz, int size);

    private static native void nativeDetect(long thiz, long inputImage, long faces);

    long timeRecord = 0;

    @Override
    public void process(@Nullable Mat rgbaImage, @Nullable List<? extends Color> colors) {


        Mat grey = new Mat();
        Imgproc.cvtColor(rgbaImage, grey, Imgproc.COLOR_RGBA2GRAY);

        if (mAbsoluteFaceSize == 0) {
            int height = grey.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        Rect[] facesArray = faces.toArray();

        if (System.currentTimeMillis() - timeRecord > delay) {
            timeRecord = System.currentTimeMillis();
            mJavaDetector.detectMultiScale(grey, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

            if (callback != null) {
                callback.onDetected(faces);
            }
        }

        for (int i = 0; i < facesArray.length; i++) {
            Imgproc.rectangle(rgbaImage, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
        }
        return;
    }

    int[] position = new int[2];

    @NotNull
    @Override
    public List<MatOfPoint> getContours() {
        return null;
    }

    public ResultCallback callback;

    public void setCallback(ResultCallback callback) {
        this.callback = callback;
    }

    public interface ResultCallback {
        void onDetected(Mat face);
    }
}
