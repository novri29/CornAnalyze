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
    private val pickImageGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            startCropActivity(it)
        }
    }

    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        binding.openCamera.setOnClickListener {
            openCameraActivity()
        }

        binding.galleryButton.setOnClickListener {
            handleGalleryButtonClicked()
        }

        (activity as? AppCompatActivity)?.supportActionBar?.hide()

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
                    // Hapus gambar lama jika ada
                    binding.previewImageView.setImageURI(null)
                    // Gambar baru
                    binding.previewImageView.setImageURI(resultUri)
                    binding.previewImageView.invalidate() // Memastikan tampilan di-refresh
                    binding.analyzeButton.visibility = View.VISIBLE
                }
            }
        } else {
            Toast.makeText(requireContext(), "Image cropping failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCropActivity(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(requireContext().cacheDir, "cropped_image.jpg"))
        val uCrop = UCrop.of(sourceUri, destinationUri)
        uCrop.withAspectRatio(1f, 1f) // Optional, set the aspect ratio
            .withMaxResultSize(1080, 1080) // Optional, set max size
        cropImageLauncher.launch(uCrop.getIntent(requireContext()))
    }

    private fun pickImageFromGallery() {
        pickImageGallery.launch("image/*")
    }

    private fun openCameraActivity() {
        val intent = Intent(requireContext(), CameraActivity::class.java)
        startActivity(intent)
    }

    private fun handleGalleryButtonClicked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionsIfNeeded(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
        } else {
            requestPermissionsIfNeeded(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
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
