package kr.ac.anu.mumu.presentation.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.databinding.ActivityLoginBinding
import kr.ac.anu.mumu.presentation.join.JoinActivity
import kr.ac.anu.mumu.presentation.main.MainActivity

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewSet()

        initInputListener()
        initObserver()
    }

    private fun viewSet() {
        val originText = binding.tvTitle.text.toString()
        val spannable = SpannableStringBuilder(originText)

        val targetWord = "Mumu"
        val start = originText.indexOf(targetWord)
        val end = start + targetWord.length

        if (start != -1) {
            val color = ContextCompat.getColor(this, R.color.mumu_300)

            spannable.setSpan(
                ForegroundColorSpan(color),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        binding.tvTitle.text = spannable
    }

    private fun initInputListener() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                val id = binding.etId.text.toString()
                val pw = binding.etPw.text.toString()

                binding.btnLogin.isEnabled = id.isNotEmpty() && pw.isNotEmpty()

                binding.tvError.visibility = View.GONE
            }
        }

        binding.etId.addTextChangedListener(textWatcher)
        binding.etPw.addTextChangedListener(textWatcher)

        // 버튼 클릭 시 로그인 요청
        binding.btnLogin.setOnClickListener {
            val id = binding.etId.text.toString()
            val pw = binding.etPw.text.toString()

            viewModel.login(id, pw)
        }
        binding.btnJoin.setOnClickListener {
            val intent = Intent(this, JoinActivity::class.java)
            startActivity(intent)
        }
    }

    // 로그인 결과 -> UI 업데이트
    private fun initObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is LoginUiState.Loading -> {
                            // 로딩 상태 처리
                            binding.btnLogin.isEnabled = false
                        }
                        is LoginUiState.Success -> {
                            // 로그인 성공 처리
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        is LoginUiState.Error -> {
                            // 로그인 실패 처리
                            binding.tvError.visibility = View.VISIBLE
                            Log.e("emumu : ", state.message)
                            binding.btnLogin.isEnabled = true
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
