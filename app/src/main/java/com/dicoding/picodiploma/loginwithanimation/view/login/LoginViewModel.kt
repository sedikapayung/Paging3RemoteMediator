package com.dicoding.picodiploma.loginwithanimation.view.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<UserModel>>()
    val loginResult: LiveData<Result<UserModel>> = _loginResult

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            repository.saveSession(user)
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {

                val response = repository.login(email, password)


                response.loginResult?.let { loginResult ->

                    val user = UserModel(
                        email = email,
                        token = loginResult.token ?: ""

                    )

                    _loginResult.postValue(Result.success(user))
                } ?: run {

                    _loginResult.postValue(Result.failure(Exception("Login gagal. Data tidak lengkap.")))
                }
            } catch (e: Exception) {

                _loginResult.postValue(Result.failure(e))
            }
        }
    }
}
