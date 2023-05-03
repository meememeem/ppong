package com.donxux.ppong.android

import android.Manifest
import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable.Orientation
import android.os.Build
import android.util.Size
import androidx.annotation.RequiresApi
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    navController: NavHostController,
    cameraViewModel: CameraViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
        )
    )
    var orientation by remember { mutableStateOf(Configuration.ORIENTATION_PORTRAIT) }
    val configuration = LocalConfiguration.current

    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
        snapshotFlow {
            configuration.orientation
        }.collect {
            orientation = it
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        PermissionsRequired(multiplePermissionsState = permissionState,
            permissionsNotGrantedContent = {
                CameraErrorView("카메라 권한이 필요합니다. 권한을 활성화 해주세요.")
            },
            permissionsNotAvailableContent = {
                CameraErrorView("카메라 권한이 필요합니다. 권한을 활성화 해주세요.")
            }
        ) {
            CameraPreview(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(9f / 16f),
                aspectRatio = Size(16, 9),
                useFrontCamera = true
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(64.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = {
                cameraViewModel.nextCameraRatio()
            }) {
                Icon(
                    Icons.Default.Crop169,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            Canvas(modifier = Modifier.size(64.dp), onDraw = {
                drawCircle(Color.White, style = Stroke(10f))
                drawCircle(Color.White, radius = 28.dp.toPx())
            })
            IconButton(onClick = { cameraViewModel.toggleCameraSelector() }) {
                Icon(Icons.Default.Cameraswitch, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Stable
@Composable
private fun CameraErrorView(message: String = "에러가 발생하였습니다.") {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.CameraAlt,
            modifier = Modifier.size(48.dp),
            contentDescription = null,
            tint = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, color = Color.White)
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun CameraScreenPreview() {
    CameraScreen(rememberNavController())
}