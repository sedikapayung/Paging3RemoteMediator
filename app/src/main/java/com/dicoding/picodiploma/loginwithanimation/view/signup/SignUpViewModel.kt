package com.dicoding.picodiploma.loginwithanimation.view.signup

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.api.ApiConfig
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import com.dicoding.picodiploma.loginwithanimation.data.response.RegisterResponse
import kotlinx.coroutines.launch

class SignUpViewModel(app: UserRepository): ViewModel() {
    private val _RegisResult = MutableLiveData<RegisterResponse?>()
    val RegisResult: LiveData<RegisterResponse?> get() = _RegisResult

    fun Register(name: String, email: String, password: String){
        viewModelScope.launch {try {
            val apiService = ApiConfig.getApiService("")
            val response = apiService.register(name, email, password)
            Log.d("RegisterDebug", "Response: $response")
            _RegisResult.value = response
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("RegisterDebug", "Error: ${e.localizedMessage}")
            _RegisResult.value = RegisterResponse(true, "Email Sudah terdaftar")
            Log.e("RegisterDebug", "Error: ${e.localizedMessage}")
        }
        }

    }

    fun SaveSession(userModel: UserModel){

    }
}