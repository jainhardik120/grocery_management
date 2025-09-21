package com.vashuag.grocery

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Loading)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var preview: Preview? = null
    private var previewView: PreviewView? = null
    private var graphicOverlay: GraphicOverlay? = null

    fun initializeCamera(lifecycleOwner: LifecycleOwner) {
        if (_uiState.value is CameraUiState.Ready) return

        viewModelScope.launch {
            try {
                val provider = ProcessCameraProvider.Companion.getInstance(context).get()
                cameraProvider = provider

                setupCameraUseCases(lifecycleOwner)

            } catch (exception: Exception) {
                _uiState.value = CameraUiState.Error(
                    exception.message ?: "Unknown camera error"
                )
            }
        }
    }

    private fun setupCameraUseCases(lifecycleOwner: LifecycleOwner) {
        val provider = cameraProvider ?: return

        try {
            // Create PreviewView
            val previewView = PreviewView(context)
            this.previewView = previewView

            // Create Preview use case
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            this.preview = preview
            val graphicOverlay = GraphicOverlay(
                context, null
            )
            this.graphicOverlay = graphicOverlay

            // Create ImageAnalysis use case
            val imageAnalyzer = createImageAnalysisUseCase()
            this.imageAnalyzer = imageAnalyzer

            // Create camera selector
            val cameraSelector =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            // Unbind all use cases before binding new ones
            provider.unbindAll()

            // Bind use cases to lifecycle
            provider.bindToLifecycle(
                lifecycleOwner, cameraSelector, preview, imageAnalyzer
            )

            _uiState.value = CameraUiState.Ready(previewView, graphicOverlay)

        } catch (exception: Exception) {
            _uiState.value = CameraUiState.Error(
                "Failed to bind camera use cases: ${exception.message}"
            )
        }
    }

    private fun createImageAnalysisUseCase(): ImageAnalysis {
        val executor = ContextCompat.getMainExecutor(context)

        return ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
            .also { analysis ->
                analysis.setAnalyzer(executor) { imageProxy ->
                    ObjectDetectionAnalyzer().analyze(imageProxy, graphicOverlay!!)
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        imageAnalyzer?.clearAnalyzer()
        cameraProvider?.unbindAll()

        imageAnalyzer = null
        preview = null
        previewView = null
        cameraProvider = null
    }
}