package com.dicoding.picodiploma.loginwithanimation.view.addstories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.response.AddNewStoryResponse
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.launch
import java.io.File

class addStoriesViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _uploadResult = MutableLiveData<Result<AddNewStoryResponse>>()
    val uploadResult: LiveData<Result<AddNewStoryResponse>> = _uploadResult

    fun uploadStory(description: String, imageFile: File, context: Context) {
        viewModelScope.launch {
            try {

                val compressedImageFile = Compressor.compress(context, imageFile) {
                    resolution(720, 1280)
                    quality(75)
                }
                if (compressedImageFile.length() > 1_000_000) {

                    _uploadResult.postValue(Result.failure(Exception("Image file is too large after compression")))
                    return@launch
                }

                val response = userRepository.uploadStory(description, compressedImageFile)
                _uploadResult.postValue(Result.success(response))
            } catch (e: Exception) {
                _uploadResult.postValue(Result.failure(e))
            }
        }
    }
}
