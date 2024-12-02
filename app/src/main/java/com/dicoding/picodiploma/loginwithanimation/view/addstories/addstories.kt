package com.dicoding.picodiploma.loginwithanimation.view.addstories

import android.content.Intent
import android.net.Uri

import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View

import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels

import androidx.appcompat.app.AppCompatActivity

import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityAddstoriesBinding
import com.dicoding.picodiploma.loginwithanimation.view.ViewModelFactory

import com.dicoding.picodiploma.loginwithanimation.view.main.MainActivity
import java.io.File


class addstories : AppCompatActivity() {
    private val viewModel by viewModels<addStoriesViewModel> { ViewModelFactory.getInstance(this) }
    private lateinit var binding: ActivityAddstoriesBinding
    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddstoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.galery.setOnClickListener { startGallery() }
        binding.camera.setOnClickListener { startCamera() }
        setupAction()

        if (savedInstanceState != null) {
            currentImageUri = savedInstanceState.getParcelable("imageUri")
            showImage()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        currentImageUri?.let {
            outState.putParcelable("imageUri", it)
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        if (currentImageUri != null) {
            launcherIntentCamera.launch(currentImageUri!!)
        } else {
            Log.e("Camera", "Failed to create image URI")
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        } else {
            currentImageUri = null
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.imgPreaview.setImageURI(it)
        }
    }

    private fun setupAction() {
        binding.upload.setOnClickListener {
            val description = binding.edtTextDescription.text.toString()

            if (currentImageUri == null) {
                Toast.makeText(this, "Tidak ada gambar yang dipilih", Toast.LENGTH_SHORT).show()
                Log.e("Upload", "No image selected")
                return@setOnClickListener
            }

            if (description.isBlank()) {
                Toast.makeText(this, "Deskripsi tidak boleh kosong", Toast.LENGTH_SHORT).show()
                Log.e("Upload", "Description cannot be empty")
                return@setOnClickListener
            }

            val filePath = uriToFilePath(currentImageUri!!)
            if (filePath != null) {
                val file = File(filePath)
                binding.progressBar.visibility = View.VISIBLE

                viewModel.uploadStory(description, file, this)

                viewModel.uploadResult.observe(this) { result ->
                    binding.progressBar.visibility = View.GONE
                    if (result.isSuccess) {
                        Toast.makeText(this, "Story berhasil di upload", Toast.LENGTH_SHORT)
                            .show()
                        Log.d("Upload", "Story uploaded successfully")
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)

                        finish()
                    } else {
                        Toast.makeText(this, "Gagal mengupload story", Toast.LENGTH_SHORT).show()
                        Log.e("Upload", "Failed to upload story")
                    }
                }
            } else {
                Log.e("Upload", "Failed to convert Uri to File")
            }
        }
    }

    fun uriToFilePath(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val columnIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
            if (columnIndex != -1) {
                it.moveToFirst()
                return it.getString(columnIndex)
            }
        }
        return null
    }
}
