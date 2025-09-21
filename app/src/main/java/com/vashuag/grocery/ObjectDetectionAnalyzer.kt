package com.vashuag.grocery

import android.os.SystemClock
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions

class ObjectDetectionAnalyzer {
    private val options =
        ObjectDetectorOptions.Builder().setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects().enableClassification().build()
    private val detector: ObjectDetector = ObjectDetection.getClient(options)

    @OptIn(ExperimentalGetImage::class)
    fun analyze(imageProxy: ImageProxy, overlay: GraphicOverlay) {
        val frameStartMs = SystemClock.elapsedRealtime()
        val mediaImage =
            InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
        val task = detector.process(mediaImage)
        task.addOnSuccessListener { results ->
            overlay.clear()
            for (result in results) {
                overlay.add(ObjectGraphic(overlay, result))
            }
            overlay.postInvalidate()
        }.addOnSuccessListener {
            imageProxy.close()
        }
    }
}

