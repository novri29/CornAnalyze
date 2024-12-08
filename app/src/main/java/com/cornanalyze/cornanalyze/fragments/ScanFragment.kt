package com.cornanalyze.cornanalyze.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cornanalyze.cornanalyze.CameraActivity
import com.cornanalyze.cornanalyze.R
import com.cornanalyze.cornanalyze.ResultsActivity
import com.cornanalyze.cornanalyze.databinding.FragmentScanBinding
import com.cornanalyze.cornanalyze.helper.ImageClassifierHelper
import com.cornanalyze.cornanalyze.utils.UtilsUCrop
import com.yalantis.ucrop.UCrop

@Suppress("UNREACHABLE_CODE")
class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!
    private lateinit var tfliteModel: ImageClassifierHelper
    private lateinit var imageView: ImageView
    private lateinit var analyzeButton: Button
    private lateinit var predictionText: TextView
    private lateinit var hasilText: TextView // Tambahkan referensi untuk TextView baru

    // Pilih gambar dari galeri
    private val pickImageGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            startCropActivity(it)
        }
    }

    // Req permission dari camera dan galeri
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>
    // permission camera
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

        // Inisialisasi elemen UI
        val imageView = view?.findViewById<ImageView>(R.id.imageView)
        val analyzeButton = view?.findViewById<Button>(R.id.analyzeButton)

        // Load TensorFlow Lite model
        tfliteModel = ImageClassifierHelper(requireContext())

        // Atur klik listener
        analyzeButton?.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            imagePickerLauncher.launch(intent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.hide()

        // Inisialisasi tfliteModel
        tfliteModel = ImageClassifierHelper(requireContext())

        binding.openCamera.setOnClickListener {
            openCameraWithPermission()
        }

        binding.galleryButton.setOnClickListener {
            handleGalleryButtonClicked()
        }

        binding.analyzeButton.setOnClickListener {
            analyzeImage()
        }

        binding.previewImageView.post {
            val width = binding.previewImageView.width
            val layoutParams = binding.previewImageView.layoutParams
            layoutParams.height = width
            binding.previewImageView.layoutParams = layoutParams
        }
    }

    private fun analyzeImage() {
        val drawable = binding.previewImageView.drawable
        if (drawable == null) {
            Toast.makeText(requireContext(), "Tidak ada gambar untuk dianalisis!", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmap = (drawable as BitmapDrawable).bitmap
        val prediction = tfliteModel.predictImage(bitmap)

        // Pisahkan hasil prediksi dan saran
        val predictionLines = prediction.split("\n\nPenanganan:\n")
        val hasilPrediksi = predictionLines.getOrNull(0) ?: "Hasil tidak diketahui"
        val saranPenanganan = predictionLines.getOrNull(1) ?: "Saran tidak tersedia"

        // Pastikan imageUri sudah ada
        val imageUri = Uri.parse(binding.previewImageView.tag as? String ?: "")

        // Pindah ke ResultsActivity
        val intent = Intent(requireContext(), ResultsActivity::class.java).apply {
            putExtra("EXTRA_GAMBAR_URI", imageUri.toString()) // Mengirim URI gambar
            putExtra("EXTRA_HASIL_PREDIKSI", hasilPrediksi)
            putExtra("EXTRA_SARAN", saranPenanganan)
        }
        startActivity(intent)
    }

    private val cropImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            result.data?.let { data ->
                val resultUri = UCrop.getOutput(data)
                resultUri?.let { uri ->
                    // Clear previous image
                    binding.previewImageView.setImageURI(null)
                    // Set the new image
                    binding.previewImageView.setImageURI(uri)
                    binding.previewImageView.tag = uri.toString() // Simpan URI untuk digunakan nanti
                    binding.previewImageView.invalidate() // Refresh the view
                    binding.analyzeButton.visibility = View.VISIBLE // Aktifkan tombol Analisis
                }
            }
        } else {
            Toast.makeText(requireContext(), "Image cropping failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCropActivity(sourceUri: Uri) {
        // Ensure the tag is set with the original URI
        binding.previewImageView.tag = sourceUri.toString()
        UtilsUCrop.startUCrop(requireContext(), sourceUri, cropImageLauncher)
    }

    private val cameraActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val croppedImageUri = result.data?.getStringExtra("CROPPED_IMAGE_URI")
            croppedImageUri?.let {
                val uri = Uri.parse(it)
                binding.previewImageView.setImageURI(uri)
                binding.previewImageView.tag = uri.toString() // Simpan URI untuk digunakan nanti
                binding.previewImageView.invalidate() // Refresh view
                binding.analyzeButton.visibility = View.VISIBLE // Aktifkan tombol Analisis
            }
        } else {
            Toast.makeText(requireContext(), "Failed to capture or crop image", Toast.LENGTH_SHORT).show()
        }
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
        cameraActivityLauncher.launch(intent)
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

    private val imagePickerLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { imageUri ->
                    // Menyimpan URI gambar
                    val inputStream = requireContext().contentResolver.openInputStream(imageUri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.previewImageView.setImageBitmap(bitmap)
                    binding.previewImageView.tag = imageUri.toString() // Simpan URI untuk digunakan nanti

                    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true)
                    Log.d("ScanFragment", "Resized bitmap dimensions: ${resizedBitmap.width}x${resizedBitmap.height}")

                    binding.analyzeButton.visibility = View.VISIBLE // Aktifkan tombol Analisis
                }
            }
        }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
