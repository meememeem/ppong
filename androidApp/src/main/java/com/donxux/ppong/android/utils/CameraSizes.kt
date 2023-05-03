package com.donxux.ppong.android.utils

import android.annotation.SuppressLint
import android.util.Size
import androidx.camera.core.impl.utils.CompareSizesByArea
import java.util.*
import java.util.Collections.max

@SuppressLint("RestrictedApi")
fun chooseOptimalSize(
    choices: List<Size>,
    previewWidth: Int,
    previewHeight: Int,
    maxWidth: Int,
    maxHeight: Int,
    aspectRatio: Size
): Size {
    val bigEnough = mutableListOf<Size>()
    val notBigEnough = mutableListOf<Size>()

    for (option in choices) {
        if (option.width <= maxWidth &&
            option.height <= maxHeight &&
            option.height == option.width * aspectRatio.height / aspectRatio.width
        ) {
            if (option.width >= previewWidth &&
                option.height >= previewHeight
            ) {
                bigEnough.add(option)
            } else {
                notBigEnough.add(option)
            }
        }
    }

    return if (bigEnough.size > 0) max(bigEnough, CompareSizesByArea())
    else if (notBigEnough.size > 0) max(notBigEnough, CompareSizesByArea())
    else choices[0]
}

