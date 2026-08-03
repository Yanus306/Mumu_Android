package kr.ac.anu.mumu.presentation.my

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.databinding.ActivityMyBinding
import kotlin.jvm.java

class MyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyBinding

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
    }
    private fun nextScreen(){
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
}