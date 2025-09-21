package com.vashuag.grocery

import androidx.camera.view.PreviewView

sealed class CameraUiState {
    object Loading : CameraUiState()
    data class Ready(val previewView: PreviewView, val graphicOverlay: GraphicOverlay) : CameraUiState()
    data class Error(val message: String) : CameraUiState()
}