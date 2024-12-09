package com.cornanalyze.cornanalyze.helper

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ImageClassifierHelper(context: Context) {
    private val interpreter: Interpreter
    init {
        interpreter = Interpreter(loadModelFile(context, "CornLeafDisease7030V2.tflite"))
    }

    data class PredictionResult(
        val label: String,
        val probability: Int,
        val description: String,
        val handling: String
    )

    @Throws(IOException::class)
    private fun loadModelFile(context: Context, modelPath: String): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun predictImage(bitmap: Bitmap): PredictionResult {
        // Mengubah ukuran gambar ke 256x256
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true)

        // Konversi bitmap menjadi TensorImage
        val tensorImage = TensorImage.fromBitmap(resizedBitmap)

        // Normalisasi gambar (nilai piksel 0.0 - 1.0)
        val normalizedArray = tensorImage.tensorBuffer.floatArray.map { it / 255.0f }.toFloatArray()

        // Membuat TensorBuffer input dengan ukuran sesuai model
        val inputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 256, 256, 3), org.tensorflow.lite.DataType.FLOAT32)
        inputBuffer.loadArray(normalizedArray)

        // Memeriksa ukuran output tensor
        val outputTensor = interpreter.getOutputTensor(0)
        val outputShape = outputTensor.shape()  // Bentuk/ukuran output tensor
        val outputSize = outputTensor.numBytes() // Ukuran byte output
        Log.d("TFLiteModel", "Output Tensor Shape: ${outputShape.contentToString()}, Output Size: $outputSize bytes")

        // Membuat TensorBuffer untuk output
        val outputBuffer = TensorBuffer.createFixedSize(outputShape, org.tensorflow.lite.DataType.FLOAT32)

        // Menjalankan inferensi
        interpreter.run(inputBuffer.buffer.rewind(), outputBuffer.buffer.rewind())

        // Mendapatkan hasil prediksi
        val predictions = outputBuffer.floatArray

        // Menentukan label berdasarkan prediksi tertinggi
        val labels = listOf("Blight", "Common Rust", "Gray Leaf Spot", "Healthy")
        val maxIndex = predictions.indices.maxByOrNull { predictions[it] } ?: -1

        if (maxIndex != -1) {
            val label = labels[maxIndex]
            val probability = (predictions[maxIndex] * 100).toInt()

            val description = if (label == "Blight") {
                "Penyakit ini disebabkan oleh bakteri atau jamur yang menyebabkan bercak cokelat berbentuk lonjong pada daun, terutama pada kondisi lembap."
            } else if (label == "Common Rust") {
                "Penyakit ini disebabkan oleh jamur Puccinia sorghi yang menyebabkan pustula cokelat kemerahan pada daun."
            } else if (label == "Gray Leaf Spot") {
                "Penyakit ini disebabkan oleh jamur Cercospora zeae-maydis yang menyebabkan bercak persegi panjang berwarna abu-abu hingga cokelat pada daun."
            } else if (label == "Healthy") {
                "Tanaman dalam kondisi sehat tanpa tanda-tanda penyakit atau kerusakan. Tanaman dapat berproduksi optimal."
            } else {
                "Tidak ada deskripsi tersedia."
            }

            val handling = if (label == "Blight") {
                "1. Gunakan fungisida berbasis tembaga.\n2. Pastikan drainase lahan baik untuk mengurangi kelembapan.\n3. Hindari irigasi di malam hari."
            } else if (label == "Common Rust") {
                "1. Terapkan fungisida berbasis mankozeb atau klorotalonil.\n2. Pilih varietas jagung yang tahan karat.\n3. Hindari penanaman terlalu padat."
            } else if (label == "Gray Leaf Spot") {
                "1. Gunakan fungisida strobilurin atau triazole.\n2. Lakukan rotasi tanaman untuk mengurangi inokulum.\n3. Hindari sisa tanaman tertinggal di lahan."
            } else if (label == "Healthy") {
                "Tanaman sehat. Lanjutkan perawatan rutin dan pastikan kondisi optimal untuk pertumbuhan."
            } else {
                "Tidak ada langkah penanganan spesifik."
            }

            return PredictionResult(label, probability, description, handling)
        }

        return PredictionResult("Unknown", 0, "Tidak ada informasi tersedia.", "Tidak ada langkah penanganan.")
    }


    // Menutup interpreter ketika tidak digunakan
    fun close() {
        interpreter.close()
    }
}