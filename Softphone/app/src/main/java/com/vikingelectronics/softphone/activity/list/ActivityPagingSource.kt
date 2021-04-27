package com.vikingelectronics.softphone.activity.list

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.DocumentSnapshot
import com.vikingelectronics.softphone.activity.ActivityEntry
import com.vikingelectronics.softphone.networking.ActivityRepository

class ActivityPagingSource (
    private val activityRepository: ActivityRepository
): PagingSource<DocumentSnapshot, ActivityEntry>() {

    override fun getRefreshKey(state: PagingState<DocumentSnapshot, ActivityEntry>): DocumentSnapshot? = null

    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, ActivityEntry> {
        return try {
            activityRepository.fetchEntries(params.key)?.let {
                LoadResult.Page(
                    data = it.activities,
                    nextKey = it.lastEntry,
                    prevKey = params.key
                )
            } ?: LoadResult.Error(Throwable("Error fetching results"))

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}