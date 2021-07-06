package com.vikingelectronics.softphone.captures.list

import android.net.Uri
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.vikingelectronics.shared.captures.Capture
import com.vikingelectronics.shared.captures.CapturesRepository

class CapturePagingSource(
    private val repository: CapturesRepository,
    private val localUris: List<Uri>
): PagingSource<String, Capture>() {

    override fun getRefreshKey(state: PagingState<String, Capture>): String? = null

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Capture> {
        return try {
            repository.getExternalCaptures(params.loadSize, localUris, params.key)?.let {
                LoadResult.Page(
                    data = it.captures,
                    nextKey = it.pageToken,
                    prevKey = params.key
                )
            } ?: LoadResult.Error(Throwable("Error fetching captures"))
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}