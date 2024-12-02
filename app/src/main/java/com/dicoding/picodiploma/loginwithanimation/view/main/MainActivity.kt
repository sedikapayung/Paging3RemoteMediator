package com.dicoding.picodiploma.loginwithanimation.view.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityMainBinding
import com.dicoding.picodiploma.loginwithanimation.maps.MapsActivity
import com.dicoding.picodiploma.loginwithanimation.view.ViewModelFactory
import com.dicoding.picodiploma.loginwithanimation.view.addstories.addstories
import com.dicoding.picodiploma.loginwithanimation.view.detailEvent.DetailActivity
import com.dicoding.picodiploma.loginwithanimation.view.welcome.WelcomeActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding
    private val storiesAdapter by lazy {
        storiesAdapter { story ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("title", story.name)
            intent.putExtra("description", story.description)
            intent.putExtra("photo", story.photoUrl)
            val optionsCompat: ActivityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this,
                    androidx.core.util.Pair(binding.recyclerView, "recyclerView"),
                )
            startActivity(intent, optionsCompat.toBundle())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        viewModel.getSession().observe(this) { user ->
            Log.d("MainActivity", "User isLogin: ${user.isLogin}")
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            } else {
                binding.progressBar.visibility = View.VISIBLE
                viewModel.getStories(this)
            }
        }


        binding.mapButton.setOnClickListener {
            val intent = Intent(this@MainActivity, MapsActivity::class.java)
            startActivity(intent)
        }


        binding.logoutButton.setOnClickListener {
            viewModel.logout()
        }


        setupView()
        setupRecyclerView()

        binding.recyclerView.adapter = storiesAdapter
        storiesAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        lifecycleScope.launch {
            viewModel.stories.observe(this@MainActivity) { pagingData ->
                binding.progressBar.visibility = View.GONE
                storiesAdapter.submitData(this@MainActivity.lifecycle,pagingData)
            }
        }


        binding.fab.setOnClickListener {
            val intent = Intent(this@MainActivity, addstories::class.java)
            startActivity(intent)
        }
    }


    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }
}
