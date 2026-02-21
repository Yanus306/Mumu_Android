package kr.ac.anu.mumu.presentation.join

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.databinding.ActivityJoinBinding

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
            val currentDest = navController.currentDestination?.id

            if (currentDest == R.id.accountFragment) {
                viewModel.onNextClick()
            } else if (currentDest == R.id.nameFragment) {
                viewModel.onNameCheckClick()
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
                }

                viewModel.doneNavigation()
            }

        }
    }
}