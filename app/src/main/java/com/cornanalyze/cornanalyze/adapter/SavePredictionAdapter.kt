package com.cornanalyze.cornanalyze.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cornanalyze.cornanalyze.R
import com.cornanalyze.cornanalyze.save.PredictionSave

class SavePredictionAdapter(private val predictionList: List<PredictionSave>) :
        RecyclerView.Adapter<SavePredictionAdapter.ViewHolder>() {

    private var onDeleteClickListener: OnDeleteClickListener? = null
    private var onItemClickListener: ((PredictionSave) -> Unit)? = null

    interface OnDeleteClickListener {
        fun onDeleteClick(position: Int)
    }

    fun setOnItemClickListener(listener: (PredictionSave) -> Unit) {
        onItemClickListener = listener
    } // Item klik untuk melihat detail prediksi.

    fun setOnDeleteClickListener(listener: OnDeleteClickListener) {
        onDeleteClickListener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = predictionList[position]
        holder.bind(currentItem)
        holder.itemView.visibility = if (currentItem.result.isNotEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }


    override fun getItemCount() = predictionList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.iv_history)
        private val resultTextView: TextView = itemView.findViewById(R.id.tv_result)
        private val createdAtTextView: TextView = itemView.findViewById(R.id.tv_createdAt)
        private val deleteButton: ImageView = itemView.findViewById(R.id.iv_trash)

        fun bind(prediction: PredictionSave) {
            Glide.with(itemView.context)
                .load(prediction.image)
                .into(imageView) // Pastikan ini ditambahkan untuk memuat gambar.

            resultTextView.text = prediction.result // Tetapkan hasil prediksi.
            createdAtTextView.text = prediction.date // Tetapkan tanggal dan waktu.
            deleteButton.setOnClickListener {
                AlertDialog.Builder(itemView.context).apply {
                    setTitle("Konfirmasi Hapus")
                    setMessage("Apakah Anda yakin ingin menghapus item ini?")
                    setPositiveButton("Ya") { dialog, _ ->
                        onDeleteClickListener?.onDeleteClick(adapterPosition) // Menghapus apabila menekan "YA"
                        dialog.dismiss()
                    }
                    setNegativeButton("Tidak") { dialog, _ ->
                        dialog.dismiss() // Tutup dialog jika pengguna memilih "Tidak".
                    }
                    create()
                    show()
                }
            }

            itemView.setOnClickListener {
                onItemClickListener?.invoke(prediction)
            }
        }

    }
}