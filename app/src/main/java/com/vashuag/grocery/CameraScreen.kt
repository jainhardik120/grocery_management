package com.vashuag.grocery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun CameraScreen(
    viewModel: MainViewModel = hiltViewModel<MainViewModel>()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        viewModel.initializeCamera(lifecycleOwner)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is CameraUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.Companion.align(Alignment.Companion.Center)
                )
            }

            is CameraUiState.Ready -> {
                AndroidView(
                    factory = { (uiState as CameraUiState.Ready).previewView },
                    modifier = Modifier.fillMaxSize()
                )
                AndroidView(
                    factory = { (uiState as CameraUiState.Ready).graphicOverlay },
                    modifier = Modifier.fillMaxSize()
                )
            }

            is CameraUiState.Error -> {
                Text(
                    text = "Error: ${(uiState as CameraUiState.Error).message}",
                    modifier = Modifier.Companion
                        .align(Alignment.Companion.Center)
                        .padding(16.dp)
                )
            }
        }
    }
}