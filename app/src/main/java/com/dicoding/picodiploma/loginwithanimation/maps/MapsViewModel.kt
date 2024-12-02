package com.dicoding.picodiploma.loginwithanimation.maps

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import com.dicoding.picodiploma.loginwithanimation.data.response.ListStoryItem
import kotlinx.coroutines.launch

class MapsViewModel(private val repository: UserRepository): ViewModel() {
    private val  _stories = MutableLiveData<List<ListStoryItem>>()
    val stories: LiveData<List<ListStoryItem>> get() = _stories
    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun getStoriesLocation() {
        viewModelScope.launch {
            try {

                val storiesList = repository.getStoriesLocation()
                _stories.postValue(storiesList)
            } catch (e: Exception) {

                Log.e("MainViewModel", "Error fetching stories: ${e.message}")
            }
        }
    }
}