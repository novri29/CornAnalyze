package com.cornanalyze.cornanalyze

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cornanalyze.cornanalyze.databinding.ActivityCameraBinding
import com.cornanalyze.cornanalyze.utils.UtilsUCrop
import com.yalantis.ucrop.UCrop
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CameraActivity : AppCompatActivity() {
    private lateinit var binding : ActivityCameraBinding

    private var imageCapture: ImageCapture? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestCameraPermission()
        }

        supportActionBar?.hide()
        setupView()

        binding.captureImage.setOnClickListener { takePhoto() }
        binding.switchCamera.setOnClickListener { switchCamera() }
    }

    // Putar kamera
    private fun switchCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        startCamera()
    }

    private fun takePhoto() {
        val imageCaptureInstance = imageCapture ?: return
        val photoFile = File(
            getExternalFilesDir(null),
            "${SimpleDateFormat(FILENAME, Locale.US).format(System.currentTimeMillis())}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCaptureInstance.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    Log.d("CameraActivity", "Photo saved at: $savedUri")

                    val sharedPreferences = getSharedPreferences("com.cornanalyze.cornanalyze.PREFERENCE_FILE_KEY", MODE_PRIVATE)
                    with(sharedPreferences.edit()) {
                        putString("capture_image_uri", savedUri.toString())
                        apply()
                    }
                    startUCrop(savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraActivity", "Failed to take photo", exception)
                }
            }
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            Log.d("CameraActivity", "Cropped image URI: $resultUri")

            // Return the result to ScanFragment
            val intent = Intent().apply {
                putExtra("CROPPED_IMAGE_URI", resultUri.toString())
            }
            setResult(RESULT_OK, intent)
            finish()
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Log.e("CameraActivity", "Crop error: $cropError")
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), REQ_CAMERA_PERMISSION)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            imageCapture = ImageCapture.Builder().build()

            preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraActivity", "Failed to bind camera", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun startUCrop(sourceUri: Uri) {
        UtilsUCrop.startUCrop(this, sourceUri, cropImageLauncher)
    }

    private val cropImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val resultUri = UCrop.getOutput(result.data!!)
                Log.d("CameraActivity", "Cropped image URI: $resultUri")
                setResult(RESULT_OK, Intent().apply {
                    putExtra("CROPPED_IMAGE_URI", resultUri.toString())
                })
                finish()
            } else if (result.resultCode == UCrop.RESULT_ERROR) {
                val cropError = UCrop.getError(result.data!!)
                Log.e("CameraActivity", "Crop error: $cropError")
                setResult(RESULT_CANCELED)
                finish()
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                // Permission denied, handle accordingly
                Log.e("CameraActivity", "Camera permission denied")
            }
        }
    }

    private fun setupView() {
        @Suppress("DERPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    companion object {
        private const val REQ_CAMERA_PERMISSION = 100
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}