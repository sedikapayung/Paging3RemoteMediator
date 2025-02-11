package com.dicoding.picodiploma.loginwithanimation.data.dataRemoteMediator

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dicoding.picodiploma.loginwithanimation.data.api.apiService
import com.dicoding.picodiploma.loginwithanimation.data.database.RemoteKeys
import com.dicoding.picodiploma.loginwithanimation.data.database.StoryDatabase
import com.dicoding.picodiploma.loginwithanimation.data.response.ListStoryItem
@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val database: StoryDatabase,
    private val apiService: apiService,
    val token: String,

) : RemoteMediator<Int, ListStoryItem>() {

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ListStoryItem>
    ): MediatorResult {
        Log.d("LoadDebug", "Load called with loadType: $loadType")

        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                Log.d("LoadDebug", "Refresh: remoteKeys: $remoteKeys")
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                Log.d("LoadDebug", "Prepend: remoteKeys: $remoteKeys")
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                Log.d("LoadDebug", "Append: remoteKeys: $remoteKeys")
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            Log.d("LoadDebug", "Fetching stories from API for page: $page")
            val response = apiService.getStories(
                "Bearer $token",
                page,
                state.config.pageSize
            )

            Log.d("LoadDebug", "Fetched ${response.listStory.size} stories from API")
            val responseData = response.listStory
            val endOfPaginationReached = responseData.isEmpty()

            database.withTransaction {
                Log.d("TransactionDebug", "Starting transaction")
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeysDao().deleteRemoteKeys()
                    database.StoryDao().deleteAll()
                    Log.d("TransactionDebug", "Cleared previous data")
                }

                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = responseData.map {
                    RemoteKeys(id = it.id, prevKey = prevKey, nextKey = nextKey)
                }

                Log.d("TransactionDebug", "Prepared remote keys: $keys")
                database.remoteKeysDao().insertAll(keys)
                Log.d("TransactionDebug", "Inserted remote keys")
                database.StoryDao().insertStory(responseData)
                Log.d("TransactionDebug", "Inserted stories")

            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: Exception) {
            Log.e("LoadError", "Error occurred", exception)
            return MediatorResult.Error(exception)
        }
    }
    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            database.remoteKeysDao().getRemoteKeysId(data.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            database.remoteKeysDao().getRemoteKeysId(data.id)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { repoId ->
                database.remoteKeysDao().getRemoteKeysId(repoId)
            }
        }
    }
}