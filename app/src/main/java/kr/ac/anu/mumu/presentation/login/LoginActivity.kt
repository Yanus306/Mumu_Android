package kr.ac.anu.mumu.presentation.login

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.databinding.ActivityLoginBinding

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

        binding.btnLogin.setOnClickListener {
            val id = binding.etId.text.toString()
            val pw = binding.etPw.text.toString()
            viewModel.do_login(id, pw)
        }

        binding.btnJoin.setOnClickListener {
            //TODO Move 회원가입

        }
    }

    private fun viewSet() {
        val originText = binding.tvTitle.text.toString()
        val spannable = SpannableStringBuilder(originText)

        val targetWord = "Mumu"
        val start = originText.indexOf(targetWord)
        val end = start + targetWord.length

        if (start != -1) {
            val color = ContextCompat.getColor(this, R.color.mumumint_300)

            spannable.setSpan(
                ForegroundColorSpan(color),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        binding.tvTitle.text = spannable
    }
}