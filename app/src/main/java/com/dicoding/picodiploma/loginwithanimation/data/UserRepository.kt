package com.dicoding.picodiploma.loginwithanimation.data

import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dicoding.picodiploma.loginwithanimation.data.api.ApiConfig
import com.dicoding.picodiploma.loginwithanimation.data.response.RegisterResponse
import com.dicoding.picodiploma.loginwithanimation.data.api.apiService
import com.dicoding.picodiploma.loginwithanimation.data.dataRemoteMediator.StoryRemoteMediator
import com.dicoding.picodiploma.loginwithanimation.data.database.StoryDatabase
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.response.AddNewStoryResponse
import com.dicoding.picodiploma.loginwithanimation.data.response.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.data.response.LoginResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class UserRepository private constructor(
    private val storyDatabase: StoryDatabase,
    private val apiService: apiService,
    private val userPreference: UserPreference
) {

    suspend fun register(name: String, email: String, password: String): RegisterResponse {
        return apiService.register(name, email, password)
    }

    suspend fun login(email: String, password: String): LoginResponse {
        val response = apiService.login(email, password)
        response.loginResult?.let { loginResult ->
            saveSession(
                UserModel(
                    email = email,
                    token = loginResult.token ?: "",
                    isLogin = true,

                )
            )
        } ?: throw IllegalStateException("Login result is null")
        return response
    }

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }



    suspend fun getStoriesLocation(): List<ListStoryItem> {
        val token = userPreference.getSession().firstOrNull()?.token
            ?: throw IllegalStateException("User not logged in")

        val apiService = ApiConfig.getApiService(token)

        val response = apiService.getStoriesWithLocation(
            "Bearer $token"
        )
        if (response.error == false) {
            return response.listStory ?: emptyList()
        } else {
            throw IllegalStateException("Failed to fetch stories: ${response.message}")
        }
    }

    suspend fun uploadStory(description: String, imageFile: File): AddNewStoryResponse {
        val user = userPreference.getSession().firstOrNull()
            ?: throw IllegalStateException("User not logged in")

        val token = user.token
        val descriptionPart = description.toRequestBody("text/plain".toMediaType())
        val filePart = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            .let { MultipartBody.Part.createFormData("photo", imageFile.name, it) }

        val apiService = ApiConfig.getApiService(token)

        val response = apiService.uploadStories(
            "Bearer $token",
            filePart,
            descriptionPart
        )
        if (response.error == false) {
            return response
        } else {
            throw IllegalStateException("Failed to upload story: ${response.message}")
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    suspend fun getStories(): LiveData<PagingData<ListStoryItem>> {
        val token = userPreference.getSession().firstOrNull()?.token ?: ""
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                initialLoadSize = 10
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService, token),
            pagingSourceFactory = { storyDatabase.StoryDao().getAllStory() }
        ).liveData
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            apiService: apiService,
            userPreference: UserPreference,
            storyDatabase: StoryDatabase
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(storyDatabase, apiService, userPreference)
            }.also { instance = it }
    }
}

