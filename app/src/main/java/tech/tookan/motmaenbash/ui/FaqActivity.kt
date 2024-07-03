package tech.tookan.motmaenbash.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import tech.tookan.motmaenbash.databinding.ActivityFaqBinding

class FaqActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFaqBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaqBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

    }
}