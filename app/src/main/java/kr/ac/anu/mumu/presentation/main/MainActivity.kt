package kr.ac.anu.mumu.presentation.main

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_main) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        val analysisDestinations = setOf(
            R.id.analysisStartFragment,
            R.id.analysisCaptureFragment,
            R.id.analysisUploadFragment,
            R.id.analysisLoadingFragment,
            R.id.analysisResultFragment,
            R.id.analysisHistoryDetailFragment
        )

        binding.btnBack.setOnClickListener {
            navController.navigateUp()
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isAnalysisDestination = destination.id in analysisDestinations
            binding.btnBack.visibility = if (isAnalysisDestination) View.VISIBLE else View.GONE
            binding.ivLogo.visibility = if (isAnalysisDestination) View.GONE else View.VISIBLE
            binding.tvMumu.visibility = if (isAnalysisDestination) View.GONE else View.VISIBLE
            binding.tvAnalysisMumu.visibility = if (isAnalysisDestination) View.VISIBLE else View.GONE
            binding.bottomNav.visibility = if (isAnalysisDestination) View.GONE else View.VISIBLE
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNav) { view, windowInsets ->
            view.updatePadding(bottom = 0)
            windowInsets
        }
    }
}
