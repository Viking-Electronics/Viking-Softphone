package com.vikingelectronics.softphone.schedules

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.DocumentSnapshot
import com.vikingelectronics.softphone.schedules.data.Schedule

class SchedulesPagingSource(
    private val repository: SchedulesRepository
): PagingSource<DocumentSnapshot, Schedule>() {
    override fun getRefreshKey(state: PagingState<DocumentSnapshot, Schedule>): DocumentSnapshot? = null

    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, Schedule> {
        return try {
            repository.fetchSchedules(params.key).let {
                LoadResult.Page(
                    data = it.entries,
                    nextKey = it.index,
                    prevKey = params.key
                )
            } ?: LoadResult.Error(Throwable("Error fetching results"))

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}