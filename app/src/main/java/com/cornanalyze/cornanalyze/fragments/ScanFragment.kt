package com.cornanalyze.cornanalyze.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cornanalyze.cornanalyze.CameraActivity
import com.cornanalyze.cornanalyze.R
import com.cornanalyze.cornanalyze.databinding.FragmentScanBinding
import com.yalantis.ucrop.UCrop
import java.io.File

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences

    // For selecting image from gallery
    private val pickImageGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            startCropActivity(it)
        }
    }

    // Request permissions for camera and storage
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>

    // For camera permission
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request multiple permissions (storage)
        requestPermissionsLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                if (isGranted) {
                    if (permissionName == Manifest.permission.READ_EXTERNAL_STORAGE ||
                        permissionName == Manifest.permission.READ_MEDIA_IMAGES) {
                        pickImageFromGallery()
                    }
                } else {
                    Toast.makeText(requireContext(), "Permission denied: $permissionName", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Request camera permission
        requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCameraActivity()
            } else {
                Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.hide()

        binding.openCamera.setOnClickListener {
            openCameraWithPermission()
        }

        binding.galleryButton.setOnClickListener {
            handleGalleryButtonClicked()
        }

        binding.previewImageView.post {
            val width = binding.previewImageView.width
            val layoutParams = binding.previewImageView.layoutParams
            layoutParams.height = width
            binding.previewImageView.layoutParams = layoutParams
        }
    }

    private val cropImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            result.data?.let { data ->
                val resultUri = UCrop.getOutput(data)
                resultUri?.let {
                    // Clear previous image
                    binding.previewImageView.setImageURI(null)
                    // Set the new image
                    binding.previewImageView.setImageURI(resultUri)
                    binding.previewImageView.invalidate() // Refresh the view
                    binding.analyzeButton.visibility = View.VISIBLE
                }
            }
        } else {
            Toast.makeText(requireContext(), "Image cropping failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCropActivity(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(requireContext().cacheDir, "cropped_image.jpg"))

        // Create UCrop options with freestyle cropping enabled
        val options = UCrop.Options().apply {
            setCompressionQuality(90) // Optional: Set compression quality for the cropped image
            setFreeStyleCropEnabled(true) // Enables free-style cropping without a fixed aspect ratio
        }

        // Create the UCrop intent with the options and start it
        val uCrop = UCrop.of(sourceUri, destinationUri)
            .withOptions(options) // Apply the custom options
        cropImageLauncher.launch(uCrop.getIntent(requireContext()))
    }

    private fun pickImageFromGallery() {
        pickImageGallery.launch("image/*")
    }

    private fun openCameraWithPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // If permission granted, open the camera activity
            openCameraActivity()
        } else {
            // Request camera permission if not granted
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCameraActivity() {
        val intent = Intent(requireContext(), CameraActivity::class.java)
        startActivity(intent)
    }

    private fun handleGalleryButtonClicked() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                // Untuk versi Android 14 (API 34 dan 15 (API 35), langsung akses galeri tanpa perlu minta izin
                pickImageGallery.launch("image/*")
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Untuk versi Android 13 (API 33) keatas)
                requestPermissionsIfNeeded(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
            }
            else -> {
                // Untuk versi dibawah Android 13
                requestPermissionsIfNeeded(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
            }
        }
    }

    private fun requestPermissionsIfNeeded(permissions: Array<String>) {
        val permissionsToRequest = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest)
        } else {
            pickImageFromGallery()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
