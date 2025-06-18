package com.cornanalyze.cornanalyze.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.cornanalyze.cornanalyze.R
import com.yalantis.ucrop.UCrop
import java.io.File

//Ucrop
object UtilsUCrop {
    fun startUCrop(
        context: Context,
        sourceUri: Uri,
        cropLauncher: ActivityResultLauncher<Intent>
    ) {
        val destinationUri = Uri.fromFile(File(context.cacheDir, "cropped_image.jpg"))

        val options = UCrop.Options().apply {
            setCompressionQuality(90)
            setFreeStyleCropEnabled(true) // Mengaktifkan free-style cropping
            setToolbarTitle("Crop Gambar")
            setToolbarColor(ContextCompat.getColor(context, R.color.white))
            setStatusBarColor(ContextCompat.getColor(context, R.color.white))
            setActiveControlsWidgetColor(ContextCompat.getColor(context, R.color.color1))
        }

        val uCropIntent = UCrop.of(sourceUri, destinationUri)
            .withOptions(options)
            .withMaxResultSize(1080, 1080)
            .getIntent(context)

        cropLauncher.launch(uCropIntent)
    }
}