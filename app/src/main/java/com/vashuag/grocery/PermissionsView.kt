package com.vashuag.grocery

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsView(
    content: @Composable () -> Unit
) {
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )

    when {
        permissionState.allPermissionsGranted -> {
            content()
        }

        permissionState.shouldShowRationale -> {
            Column(
                modifier = Modifier.Companion
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.Companion.CenterHorizontally
            ) {
                Text(
                    text = "Camera and audio permissions are required for this feature to work properly.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.Companion.padding(bottom = 16.dp)
                )
                Button(
                    onClick = { permissionState.launchMultiplePermissionRequest() }
                ) {
                    Text(text = "Grant Permissions")
                }
            }
        }

        else -> {
            Column(
                modifier = Modifier.Companion
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.Companion.CenterHorizontally
            ) {
                Text(
                    text = "This app requires camera and audio permissions to function.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.Companion.padding(bottom = 16.dp)
                )
                Button(
                    onClick = { permissionState.launchMultiplePermissionRequest() }
                ) {
                    Text(text = "Request Permissions")
                }
            }
        }
    }
}