package kr.ac.anu.mumu.presentation.join

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.databinding.ActivityJoinBinding

@AndroidEntryPoint
class JoinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJoinBinding
    private val viewModel: JoinViewModel by viewModels()
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nameFragment -> viewModel.setStep(3)
                R.id.phoneFragment -> viewModel.setStep(4)
                R.id.addressFragment -> viewModel.setStep(5)
                R.id.TermsFragment -> viewModel.setStep(6)
                R.id.FinishFragment -> {
                    viewModel.setStep(7)
                    binding.btnNext.isEnabled = true
                }
            }
        }

        viewModel.isButtonEnabled.observe(this) { isEnabled ->
            binding.btnNext.isEnabled = isEnabled
        }

        // 뒤로 가기
        binding.ibBack.setOnClickListener {
            val currentDest = navController.currentDestination?.id

            if (currentDest == R.id.accountFragment) {
                finish()
            } else {
                navController.popBackStack()
            }
        }

        // 다음 버튼
        binding.btnNext.setOnClickListener {
            viewModel.onNextClick()
        }

        viewModel.accountStep.observe(this) { currentStep ->
            if (currentStep == 7) {
                binding.btnNext.text = "완료"
            } else {
                binding.btnNext.text = "다음"
            }
        }

        viewModel.finishJoinFlow.observe(this) { isFinished ->
            if (isFinished) {
                finish()
            }
        }

        // 페이지 이동 신호 감지
        viewModel.moveToNextPage.observe(this) { move ->
            if (move) {
                val currentDest = navController.currentDestination?.id

                if (currentDest == R.id.accountFragment) {
                    navController.navigate(R.id.action_accountFragment_to_nameFragment)
                } else if (currentDest == R.id.nameFragment) {
                    navController.navigate(R.id.action_nameFragment_to_phoneFragment)
                } else if (currentDest == R.id.phoneFragment) {
                    navController.navigate(R.id.action_phoneFragment_to_addressFragment)
                } else if (currentDest == R.id.addressFragment) {
                    navController.navigate(R.id.action_addressFragment_to_TermsFragment)
                } else if (currentDest == R.id.TermsFragment) {
                    navController.navigate(R.id.action_TermsFragment_to_FinishFragment)
                }

                viewModel.doneNavigation()
            }

        }
    }
}