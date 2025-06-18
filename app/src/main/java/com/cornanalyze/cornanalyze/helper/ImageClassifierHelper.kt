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

    private val interpreter8020: Interpreter
    private val interpreter7030: Interpreter
    init {
        interpreter8020 = Interpreter(loadModelFile(context, "CornLeafDisease8020V1.tflite"))
        interpreter7030 = Interpreter(loadModelFile(context, "CornLeafDisease7030V2.tflite"))
    }

    //Hasil prediksi
    data class PredictionResult(
        val label: String,
        val probability: Int,
        val description: String,
        val cause: String,
        val handling: String,
        val source: String
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

    fun predictImageWith8020(bitmap: Bitmap): PredictionResult {
        return predictImage(bitmap, interpreter8020)
    }

    fun predictImageWith7030(bitmap: Bitmap): PredictionResult {
        return predictImage(bitmap, interpreter7030)
    }

    fun predictImage(bitmap: Bitmap, interpreter: Interpreter): PredictionResult {
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
                "Penyakit hawar (blight) jagung merupakan penyakit yang banyak menyebar di Amerika, Asia, Afrika, dan juga Eropa, kerugian apabila tanaman jagung terkena penyakit hawar dapat mencapai 50%. Penyakit hawar jagung terindikasi berbentuk bercak kecil yang berbentuk oval, yang kemudian bercak tersebut menjadi memanjang seperti ellips serta tumbuh menjadi nekrotik."
            } else if (label == "Common Rust") {
                "Penyakit karat daun (common rust) yang diakibatkan oleh Pucciania sorgi terindikasi dini berbentuk bintik-bintik merah dengan mengeluarkan serbuk semacam tepung bercorak coklat kekuningan. Dengan adanya penyakit ini, tumbuhan jagung tidak bsia melakukan fotosintesis dengan sempurna yang berakibat pertumbuhan menjadi lamat dan dapat meyebabkan tumbuhan mati."
            } else if (label == "Gray Leaf Spot") {
                "Penyakit bercak daun (gray leaf spot) merupakan penyakit yang memiliki gejala awal berupa daun abu-abu yang terlihat seperti lesi bulat kecil dengan memiliki lingkaran kuning yang berada disekitar daun. Lesi tersebut dapat berubah menjadi coklat sebelum sporulasi jamur dimulai. Penyakit ini pada tahap awalnya sangat sulit untuk diidentifikasi karena bercak menyerupai penyakit karat daun pada umumnya."
            } else if (label == "Healthy") {
                "Daun jagung yang sehat adalah daun yang memiliki kondisi optimal dan berada dalam keadaan baik, tanpa adanya tanda-tanda kerusakan seperti bercak, perubahan warna, atau kelainan lain pada permukaannya. \n\nDaun ini mencerminkan kesehatan tanaman secara keseluruhan, karena tidak menujukkan gejala gangguan yang dapat disebabkan oleh penyakit, hama, atau faktor lingkungan yang merugikan. \n\nDaun jagung merupakan jenis daun sempurna yang memiliki bentuk memanjang. Salah satu ciri khasnya adalah kehadiran ligula yang terletak di antara pelepah daun dan helai daun. Tulang daun jagung tersusun sejajar dengan ibu tulang daun, menciptakan struktur yang teratur. Permukaan daun ini bervariasi, ada yang terasa licin saat disentuh, tetapi ada juga yang memiliki lapisan berambut halus. \n\nStomata pada daun jagung memiliki bentuk unik yang menyerupai halter, sebuah karakteristik khas dari tanaman yang termasuk dalam famili Poaceae. Setiap stomata dikelilingi oleh sel-sel epidermis berbentuk seperti kipas. Struktur ini memiliki fungsi penting dalam membantu tanaman merespons defisit air yang terjadi pada sel-sel daun, sehingga memungkinkan daun jagung untuk beradaptasi dengan kondisi lingkungan yang kering atau kurang air.\n\nBerikut adalah ciri-ciri daun jagung sehat : \n\n1. Jumlah daun : Jumlah daun jagung sesuai dengan jumlah buku pada batangnya, umumnya berkisar 10-18 helai daun. \n\n2. Pola daun : Pola daun jagung berupa garis lurus sepanjang daun, garis-gairs tersebut memiliki jarak tertentu."
            } else {
                "Tidak ada deskripsi tersedia."
            }

            val cause = if (label == "Blight") {
                "Hawar (blight) pada jagung dapat disebabkan oleh berbagai jamur dan bakter, yaitu \n1. Colletotrichum graminicola : Jamur penyebab hawar daun antraknosa pada jagung ini bertahan di sisa tanaman selama musim dingin dan menghasilkan spora untuk menginfeksi tanaman di musim berikutnya. \n2. Exserohilum turcicum : Jamur penyebab hawar daun jagung utara ini tumbuh subur di cuaca basah, lembab, dan dingin, terutama di akhir musim tanam. \n3. Faktor lainnya yaitu kondisi lembap dan ringan, suhu antara 18-27 derajat celcius, dan udara yang berembun. \n4. Sisa tanaman yang terkontaminasi : jamur dapat bertahan lama pada sisa-sisa tanaman yang tidak dibersihkan pada lahan. \n5. Kerapatan tanaman : penanaman terlalu rapat dapat menghambat sirkulasi udara, yang mengakibatkan meningkatkan kelembapan, dan mempercepat penyebaran spora jamur pada tanaman. \n6. Tanaman rentan : varietas jagung yang tidak tahan terhadap hawar dapat lebih mudah terinfeksi, hal tersebut dapat diperparah dengan tidak mendapatkannya nutrisi yang cukup pada tanaman yang akan mengalami stres lingkungn."
            } else if (label == "Common Rust") {
                "1. Spora : Spora jamur dapat dipindahkan oleh angin dan hujan ke tanaman lain yang sehat. \n2. Kelembaban : Kelembaban yang tinggi mendukung perkemabngan penyakit ini. \n3. Suhu : Suhu dingin antara 15-20 derajat celcius mendukung perkembangan penyakit ini. \n4. Genetik : Setiap varietas jagung memiliki sifat ketahanan yang berbeda terhadap penyakit ini. "
            } else if (label == "Gray Leaf Spot") {
                "1. Jamur: Penyakit ini disebabkan oleh jamur Cercospora zeae-maydis yang dapat bertahan dalam sisa-sisa tanaman di tanah. \n2. Suhu: Suhu hangat yang mendukung perkembangan penyakit adalah sekitar 27°C (80°F). \n3. Kelembaban: Kelembaban tinggi yang mendukung perkembangan penyakit adalah sekitar 90% atau lebih tinggi selama 12 jam atau lebih \n4. Pengolahan tanah: Sistem penanaman dengan pengolahan tanah yang dikurangi atau tanpa olah tanah dapat meningkatkan risiko wabah penyakit ini. \n5. Penanaman jagung terus-menerus: Penanaman jagung secara terus-menerus juga dapat meningkatkan risiko wabah penyakit ini. \n6. Hibrida yang rentan: Hibrida yang rentan lebih mudah terserang penyakit ini. \n7. Tanggal penanaman: Penanaman yang terlambat dapat meningkatkan risiko wabah penyakit ini. \n8. Riwayat penyakit parah: Riwayat lapangan penyakit parah dapat meningkatkan risiko wabah penyakit ini. \n9. Irigasi: Irigasi dapat meningkatkan risiko wabah penyakit ini."
            } else if (label == "Healthy") {
                "-"
            } else {
                "Tidak ada penyebab ditemukan."
            }

            val handling = if (label == "Blight") {
                "1. Menanam varietas yang memiliki ketahanan pada penyakit hawar (Blight) seperti Bisma, Pioneer, Semar, dan lain-lain. \n2. Menanam jagung pada awal hingga akhir musim kemarau secara bersama-sama atau serempak. \n3. Gunakan funisida sistemik. \n4. Rotasi tanaman dengan tanaman yang bukan sereal (misalnya dengan tanaman kacang-kacangan) untuk memutus rantai makanan dari jamur. \n5. Lakukan sanitasi dan eradikasi (upaya pembasmian) lingkungan seperti rumput-rumputan karena dapat menjadi inang penyakit untuk masa tanam berikutnya."
            } else if (label == "Common Rust") {
                "1. Menanam varietas tahan karat daun jagung yang sehat. \n2. Mengatur jarak tanam untuk menjaga suhu dan kelembaban tanaman. \n3. Menanam di awal musim kemarau. \n4. Menggunakan pestisida kimiawi seperti zineb, oksilorida tembaga, Fermat, dan dithane."
            } else if (label == "Gray Leaf Spot") {
                "1. Menanam varietas yang tahan terhadap penyakit. \n2. Memusnahkan seluruh bagian tanaman yang terinfeksi, termasuk akarnya. \n3. Menyemprotkan fungisida pada tahap awal. \n4. Melakukan rotasi tanaman jangka panjang dengan tanaman bukan inang. \n5. Memperluas ruang di antara tanaman untuk menjaga peredaran udara yang baik. \n6. Menanam di awal musim kemarau.."
            } else if (label == "Healthy") {
                "-"
            } else {
                "Tidak ada langkah penanganan spesifik."
            }

            val source = if (label == "Blight") {
                "1. Jurnal : D. Iswantoro and D. Handayani UN, “Klasifikasi Penyakit Tanaman Jagung Menggunakan Metode Convolutional Neural Network (CNN),” J. Ilm. Univ. Batanghari Jambi, vol. 22, no. 2, p. 900, 2022, doi: 10.33087/jiubj.v22i2.2065. \n2. Website Edu : https://cals.cornell.edu/field-crops/corn/diseases-corn/anthracnose-leaf-blight \n3. Website : https://www.kompas.com/homey/read/2022/08/04/085600276/penyakit-hawar-daun-jagung--gejala-siklus-dan-cara-mengatasi \n4. Website : https://academy.bertani.co/perpustakaan/teknik-pengendalian-penyakit-hawar-daun-helminthosporium-turcicum-pada-tanaman-jagung"
            } else if (label == "Common Rust") {
                "1. Jurnal : D. Iswantoro and D. Handayani UN, “Klasifikasi Penyakit Tanaman Jagung Menggunakan Metode Convolutional Neural Network (CNN),” J. Ilm. Univ. Batanghari Jambi, vol. 22, no. 2, p. 900, 2022, doi: 10.33087/jiubj.v22i2.2065. \n2. Website Pemerintah : https://bbpopt.tanamanpangan.pertanian.go.id/artikel/penyakit-karat-daun-tanaman-jagung. \n3. Jurnal : Interaksi Faktor Iklim dan Varietas terhadap Laju Perkembangan Penyakit Karat Daun (Puccinia polysora Undrew) pada Jagung (Zea mays L.) oleh Reymas M.R. Ruimassa, Rosdiana Sari, Eko Agus Martanto. \n4. Website : https://plantix.net/id/library/plant-diseases/100082/common-rust-of-maize/. "
            } else if (label == "Gray Leaf Spot") {
                "1. Jurnal : A. Sapitri, J. Raharjo, and S. Rizal, “Identifikasi Penyakit Jagung Dengan Menerapkan Metode Gray Level Co-Occurrence Matrix (GLCM) Dan Support Vector Machine (SVM) Melalui Citra Daun Identification Of Corn Diseases By Applying Gray Level Co-Occurrence Matrix (GLCM) And Support Vector Machine ,” e-Proceeding Eng., vol. 8, no. 6, pp. 2963–2971, 2022. \n2. Jurnal : Identifikasi Jenis Penyakit Daun Jagung Menggunakan Deep Learning PreTrained Model oleh Muhammad Imron Rosadi dan Moch.Lutfi. \n3. Website : https://cropprotectionnetwork.org/encyclopedia/gray-leaf-spot-of-corn. \n4. Website : https://www.pioneer.com/us/agronomy/gray_leaf_spot_cropfocus.html \n5. Website : https://plantix.net/id/library/plant-diseases/100107/grey-leaf-spot-of-maize/ \n6. Website : https://extensionpubs.unl.edu/publication/g1902/na/html/view \n7. Facebook : Direktorat Jenderal Tanaman Pangan - Kementan RI. \n8. Skripsi : Potensi Antagonis Jamur Dari Endofit Daun Jagung Terhadap Helminthosporium turcicum oleh Faizah, Akhamat Riza. \n9. Website Kompas : Penyakit Hawar Daun Jagung : Gejala, Siklus, dan Cara Menagtasi"
            } else if (label == "Healthy") {
                "1. Skripsi : Reza Mawarni,Resty Wulaningrum,Risa Helilintar \"Implementasi Metode CNN Pada Klasifikasi Penyakit Jagung\". \n2. Skripsi : Alima Maolidea Suri \"Uji Efektivitas Ekstrak Daun Jeruk Nipis (Citrus aurantifolia) Sebagai Bioherbisida Untuk Pengencalian Gulma Teki (Cyperus rotundus) Pada Tanaman Jagung"
            } else {
                "Tidak ada sumber penanganan"
            }

            return PredictionResult(label, probability, description, cause, handling, source)
        }

        return PredictionResult("Unknown", 0, "Tidak ada informasi tersedia.", "Tidak ada penyebab ditemukan.","Tidak ada langkah penanganan.", "Tidak ada sumber informasi.")
    }

    // Menutup interpreter ketika tidak digunakan
    fun close() {
        interpreter8020.close()
        interpreter7030.close()
    }
}