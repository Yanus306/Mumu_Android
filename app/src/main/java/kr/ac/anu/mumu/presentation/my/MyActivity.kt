package kr.ac.anu.mumu.presentation.my

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.databinding.ActivityMyBinding
import kr.ac.anu.mumu.domain.model.MyInformation
import kr.ac.anu.mumu.presentation.login.LoginUiState
import kotlin.jvm.java

@AndroidEntryPoint
class MyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyBinding

    private val viewModel: MyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewModel.getUserProfile(1)

        observerViewModel()
        nextScreen()
    }

    private fun nextScreen() {
        binding.layoutModifyInformation.setOnClickListener {
            val intent = Intent(this, ModifyInformationActivity::class.java)
            startActivity(intent)
        }
        binding.layoutAnimalAdminister.setOnClickListener {
            val intent = Intent(this, AnimalAdministerActivity::class.java)
            startActivity(intent)
        }
        binding.tvMyInformationAdminister1.setOnClickListener {
            val intent = Intent(this, MyInformationAdministerActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observerViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is MyUiState.Success -> {
                            binding.tvName.text = state.myInfo.name
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
