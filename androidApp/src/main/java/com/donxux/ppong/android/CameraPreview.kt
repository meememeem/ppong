package com.donxux.ppong.android

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.donxux.ppong.android.utils.chooseOptimalSize
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * It's camera preview screen
 *
 * @param modifier The modifier to be applied to the layout.
 * @param aspectRatio The camera ratio. If you use an unsupported ratio by the camera, the preview will not be visible.
 * @param useFrontCamera Whether to show the front camera.
 **/
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    aspectRatio: Size,
    useFrontCamera: Boolean = false,
) {
    val context = LocalContext.current
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val cameraIdList = cameraManager.cameraIdList
    val surfaceView = AutoFitSurfaceView(context)
    val coroutineScope = rememberCoroutineScope()
    val cameraThread = remember { HandlerThread("CameraThread").apply { start() } }
    val cameraHandler = remember { Handler(cameraThread.looper) }
    val cameraDevice: MutableState<CameraDevice?> = remember {
        mutableStateOf(null)
    }
    val cameraCaptureSession: MutableState<CameraCaptureSession?> = remember {
        mutableStateOf(null)
    }
    val supportedCameraSizes = remember {
        getSupportedCameraSizes(context, useFrontCamera)
    }

    AndroidView(factory = {
        surfaceView
    }, modifier = modifier, update = {
        for (id in cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)

            if (facing != null &&
                ((useFrontCamera && facing == CameraCharacteristics.LENS_FACING_FRONT) ||
                        (useFrontCamera.not() && facing == CameraCharacteristics.LENS_FACING_BACK))
            ) {
                surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        if (supportedCameraSizes == null) return

                        val cameraSize = chooseOptimalSize(
                            supportedCameraSizes,
                            surfaceView.width,
                            surfaceView.height,
                            3840,
                            3840,
                            aspectRatio
                        )

                        surfaceView.setAspectRatio(cameraSize.height, cameraSize.width)

                        cameraDevice.value?.let {
                            it.close()
                            cameraDevice.value = null
                        }

                        cameraCaptureSession.value?.let {
                            it.close()
                            cameraCaptureSession.value = null
                        }

                        surfaceView.post {
                            coroutineScope.launch(Dispatchers.Main) {
                                initializeCamera(
                                    context,
                                    id,
                                    holder.surface,
                                    cameraHandler
                                ).run {
                                    cameraDevice.value = this.first
                                    cameraCaptureSession.value = this.second
                                }

                            }
                        }
                    }

                    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit

                    override fun surfaceDestroyed(holder: SurfaceHolder) = Unit
                })

                // If you don't break, the app may crash
                // because the app tries to open multiple cameras of the same facing.
                break
            }
        }
    })
}

private fun getSupportedCameraSizes(
    context: Context,
    useFrontCamera: Boolean,
): List<Size>? {
    val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    try {
        for (id in manager.cameraIdList) {
            val characteristics = manager.getCameraCharacteristics(id)
            val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (lensFacing != null &&
                ((useFrontCamera && lensFacing == CameraCharacteristics.LENS_FACING_FRONT)) ||
                (!useFrontCamera && lensFacing == CameraCharacteristics.LENS_FACING_BACK)
            ) {
                val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                return map?.getOutputSizes(ImageFormat.JPEG)?.toList()
            }
        }
    } catch (e: CameraAccessException) {
        e.printStackTrace()
    }
    return null
}

private suspend fun initializeCamera(
    context: Context,
    cameraId: String,
    surface: Surface,
    cameraHandler: Handler? = null,
): Pair<CameraDevice, CameraCaptureSession> {
    val camera = openCamera(context, cameraId, cameraHandler)
    val session = createCameraCaptureSession(camera, surface, cameraHandler)
    repeatCapture(session, surface, cameraHandler)
    return Pair(camera, session)
}

/**
 * Open camera
 *
 * @param cameraId Camera ID
 * @param handler (< SDK 28) The handler on which the callback should be invoked, or null to use the current thread's looper.
 *
 * @throws RuntimeException Failed open
 */
@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun openCamera(
    context: Context,
    cameraId: String,
    handler: Handler? = null,
): CameraDevice = suspendCancellableCoroutine { continuation ->
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        val exception = RuntimeException("Camera $cameraId error: not permission")
        if (continuation.isActive) continuation.resumeWithException(exception)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        cameraManager.openCamera(
            cameraId,
            Executors.newSingleThreadExecutor(),
            object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    continuation.resume(camera, null)
                }

                override fun onDisconnected(camera: CameraDevice) {
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    val exception = RuntimeException(
                        "Camera $cameraId error: ($error) ${
                            getMessageOpenCameraError(error)
                        }"
                    )
                    if (continuation.isActive) continuation.resumeWithException(exception)
                }
            })
    } else {
        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                continuation.resume(camera, null)
            }

            override fun onDisconnected(camera: CameraDevice) {
            }

            override fun onError(camera: CameraDevice, error: Int) {
                val exception = RuntimeException(
                    "Camera $cameraId error: ($error) ${
                        getMessageOpenCameraError(error)
                    }"
                )
                if (continuation.isActive) continuation.resumeWithException(exception)
            }

        }, handler)
    }
}

/**
 * Create camera capture session
 *
 * @param camera Camera Device
 * @param surface The surface to output camera capture
 * @param handler (< SDK 28) The handler on which the callback should be invoked, or null to use the current thread's looper.
 *
 * @throws RuntimeException Failed create
 */
private suspend fun createCameraCaptureSession(
    camera: CameraDevice,
    surface: Surface,
    handler: Handler? = null,
): CameraCaptureSession = suspendCoroutine { continuation ->
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        camera.createCaptureSession(
            SessionConfiguration(SessionConfiguration.SESSION_REGULAR,
                listOf(OutputConfiguration(surface)),
                Executors.newSingleThreadExecutor(),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        continuation.resume(session)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        val exception =
                            RuntimeException("Camera ${camera.id} session configuration failed")
                        continuation.resumeWithException(exception)
                    }
                })
        )
    } else {
        camera.createCaptureSession(
            listOf(surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    continuation.resume(session)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    val exception =
                        RuntimeException("Camera ${camera.id} session configuration failed")
                    continuation.resumeWithException(exception)
                }
            },
            handler
        )
    }
}

private fun repeatCapture(
    cameraSession: CameraCaptureSession,
    surface: Surface,
    handler: Handler? = null,
) {
    val repeatingRequest = cameraSession.device.createCaptureRequest(
        CameraDevice.TEMPLATE_PREVIEW
    )

    repeatingRequest.addTarget(surface)
    cameraSession.setRepeatingRequest(repeatingRequest.build(), object : CaptureCallback() {
    }, handler)
}

private fun getMessageOpenCameraError(error: Int) = when (error) {
    CameraDevice.StateCallback.ERROR_CAMERA_DEVICE -> "Fatal (device)"
    CameraDevice.StateCallback.ERROR_CAMERA_DISABLED -> "Device policy"
    CameraDevice.StateCallback.ERROR_CAMERA_IN_USE -> "Camera in use"
    CameraDevice.StateCallback.ERROR_CAMERA_SERVICE -> "Fatal (service)"
    CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
    else -> "Unknown"
}

data class CameraAspectRatio(val width: Int, val height: Int)
