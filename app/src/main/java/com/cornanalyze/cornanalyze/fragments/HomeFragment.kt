package com.cornanalyze.cornanalyze.fragments


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.cornanalyze.cornanalyze.MainActivity
import com.cornanalyze.cornanalyze.TutorialActivity
import com.cornanalyze.cornanalyze.databinding.FragmentHomeBinding
import com.cornanalyze.cornanalyze.leaf.BlightLeaf
import com.cornanalyze.cornanalyze.leaf.CommonrustLeaf
import com.cornanalyze.cornanalyze.leaf.GrayleafspotLeaf
import com.cornanalyze.cornanalyze.leaf.HealthLeaf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.hide()

        // Mendapatkan tanggal saat ini
        val currentDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(Date())

        binding.tvDate.text = currentDate
        val layouthealth: ImageButton = binding.btnHealthy
        layouthealth.setOnClickListener {
            val intent = Intent(requireContext(), HealthLeaf::class.java)
            startActivity(intent)
        }
        val layoutgrayleafspot: ImageButton = binding.btnGrayLeafSpot
        layoutgrayleafspot.setOnClickListener {
            val intent = Intent(requireContext(), GrayleafspotLeaf::class.java)
            startActivity(intent)
        }
        val layoutblight: ImageButton = binding.btnBlight
        layoutblight.setOnClickListener {
            val intent = Intent(requireContext(), BlightLeaf::class.java)
            startActivity(intent)
        }
        val layoutcommonrust: ImageButton = binding.btnCommonRust
        layoutcommonrust.setOnClickListener {
            val intent = Intent(requireContext(), CommonrustLeaf::class.java)
            startActivity(intent)
        }
        val layoutquestion: ImageButton = binding.btnQustion
        layoutquestion.setOnClickListener {
            val intent = Intent(requireContext(), TutorialActivity::class.java)
            startActivity(intent)
        }

        _binding?.btnToScan?.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.putExtra("navigateTo","ScanFragment")
            startActivity(intent)
        }

    }

}