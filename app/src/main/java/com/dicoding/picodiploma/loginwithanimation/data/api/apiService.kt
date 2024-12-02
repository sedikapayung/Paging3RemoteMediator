package com.dicoding.picodiploma.loginwithanimation.data.api

import com.dicoding.picodiploma.loginwithanimation.data.response.AddNewStoryResponse
import com.dicoding.picodiploma.loginwithanimation.data.response.DetailStoriesResponse
import com.dicoding.picodiploma.loginwithanimation.data.response.GetAllStoriesResponse

import com.dicoding.picodiploma.loginwithanimation.data.response.LoginResponse
import com.dicoding.picodiploma.loginwithanimation.data.response.RegisterResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface apiService {

    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): RegisterResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @GET("stories")
    suspend fun getStories(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): GetAllStoriesResponse

    @GET("stories")
    suspend fun getStoriesWithLocation(
        @Header("Authorization") authorization: String,
        @Query("location") location: Int = 1
    ): GetAllStoriesResponse

    @GET("stories/{id}")
    suspend fun getDetailStories(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): DetailStoriesResponse

    @Multipart
    @POST("stories")
    suspend fun uploadStories(
        @Header("Authorization") authorization: String,
        @Part photo: MultipartBody.Part,
        @Part("description") description: RequestBody
    ): AddNewStoryResponse
}