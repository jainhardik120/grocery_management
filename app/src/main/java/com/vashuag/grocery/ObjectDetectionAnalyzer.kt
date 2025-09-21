package com.vashuag.grocery

import android.app.ActivityManager
import android.os.SystemClock
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.lang.Math.max
import java.lang.Math.min
import java.util.Timer
import java.util.TimerTask

class ObjectDetectionAnalyzer {
    private val options =
        ObjectDetectorOptions.Builder().setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects().enableClassification().build()
    private val detector: ObjectDetector = ObjectDetection.getClient(options)
    private var numRuns = 0
    private var totalFrameMs = 0L
    private var maxFrameMs = 0L
    private var minFrameMs = Long.MAX_VALUE
    private var totalDetectorMs = 0L
    private var maxDetectorMs = 0L
    private var minDetectorMs = Long.MAX_VALUE

    // Frame count that have been processed so far in an one second interval to calculate FPS.
    private var frameProcessedInOneSecondInterval = 0
    private var framesPerSecond = 0

    private val fpsTimer = Timer()

    init {
        fpsTimer.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    framesPerSecond = frameProcessedInOneSecondInterval
                    frameProcessedInOneSecondInterval = 0
                }
            },
            0,
            1000
        )
    }


    @OptIn(ExperimentalGetImage::class)
    fun processImageProxy(imageProxy: ImageProxy, overlay: GraphicOverlay) {
        val frameStartMs = SystemClock.elapsedRealtime()
        val mediaImage = imageProxy.image!!

        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val detectorStartMs = SystemClock.elapsedRealtime()
        val task = detector.process(inputImage)
        task.addOnSuccessListener { results ->
            val endMs = SystemClock.elapsedRealtime()
            val currentFrameLatencyMs = endMs - frameStartMs
            val currentDetectorLatencyMs = endMs - detectorStartMs
            if (numRuns >= 500) {
                resetLatencyStats()
            }
            numRuns++
            frameProcessedInOneSecondInterval++
            totalFrameMs += currentFrameLatencyMs
            maxFrameMs = max(currentFrameLatencyMs, maxFrameMs)
            minFrameMs = min(currentFrameLatencyMs, minFrameMs)
            totalDetectorMs += currentDetectorLatencyMs
            maxDetectorMs = max(currentDetectorLatencyMs, maxDetectorMs)
            minDetectorMs = min(currentDetectorLatencyMs, minDetectorMs)
            overlay.clear()
            for (result in results) {
                overlay.add(ObjectGraphic(overlay, result))
            }
            overlay.add(
                InferenceInfoGraphic(
                    overlay,
                    currentFrameLatencyMs,
                    currentDetectorLatencyMs,
                    framesPerSecond
                )
            )
            overlay.postInvalidate()
            imageProxy.close()
        }.addOnFailureListener { exception ->
            Log.e("ObjectDetectionAnalyzer", "Object detection failed", exception)
            imageProxy.close()
        }
    }
    private fun resetLatencyStats() {
        numRuns = 0
        totalFrameMs = 0
        maxFrameMs = 0
        minFrameMs = Long.MAX_VALUE
        totalDetectorMs = 0
        maxDetectorMs = 0
        minDetectorMs = Long.MAX_VALUE
    }

}
