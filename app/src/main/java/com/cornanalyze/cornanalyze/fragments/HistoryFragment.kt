package com.cornanalyze.cornanalyze.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cornanalyze.cornanalyze.DetailActivity
import com.cornanalyze.cornanalyze.InformationActivity
import com.cornanalyze.cornanalyze.adapter.SavePredictionAdapter
import com.cornanalyze.cornanalyze.databinding.FragmentHistoryBinding
import com.cornanalyze.cornanalyze.save.AppDatabase
import com.cornanalyze.cornanalyze.save.PredictionSave
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class HistoryFragment : Fragment(), SavePredictionAdapter.OnDeleteClickListener {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var savePredictionAdapter: SavePredictionAdapter
    private var savepredictionList: MutableList<PredictionSave> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        savePredictionAdapter = SavePredictionAdapter(savepredictionList)
        savePredictionAdapter.setOnDeleteClickListener(this)

        savePredictionAdapter.setOnItemClickListener { prediction ->
            val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra("ID_PREDICTION", prediction.id)
            }
            startActivity(intent)
        }

        binding.rvHistory.adapter = savePredictionAdapter
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())

        return binding.root // Pastikan mengembalikan root view binding
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.hide()

        binding.btnInformation.setOnClickListener {
            openInformation()
        }

        loadPredictionFromDB()
    }

    private fun loadPredictionFromDB() {
        GlobalScope.launch(Dispatchers.Main) {
            val predictions = AppDatabase.getDatabase(requireContext()).predictionSaveDao().getALLPrediction()
            Log.d(TAG, "Number of predictions: ${predictions.size}")
            savepredictionList.clear()
            savepredictionList.addAll(predictions)
            savePredictionAdapter.notifyDataSetChanged()
        }
    }

    private fun openInformation() {
        val intent = Intent(requireContext(), InformationActivity::class.java)
        startActivity(intent)
    }

    override fun onDeleteClick(position: Int) {
        val prediction = savepredictionList[position]
        if (prediction.result.isNotEmpty()) {
            GlobalScope.launch(Dispatchers.IO) {
                AppDatabase.getDatabase(requireContext()).predictionSaveDao().deletePrediction(prediction)
            }
            savepredictionList.removeAt(position)
            savePredictionAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Hapus binding saat view dihancurkan
    }

    companion object {
        const val TAG = "historydata"
        private const val REQUEST_HISTORY_UPDATE = 1
    }
}
