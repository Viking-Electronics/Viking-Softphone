package com.vikingelectronics.softphone.activity.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.vikingelectronics.softphone.accounts.RepositoryProvider
import com.vikingelectronics.shared.activity.ActivityEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ActivityListViewModel @Inject constructor(
    private val repositoryProvider: RepositoryProvider
): ViewModel() {

    val activityEntries: Flow<PagingData<ActivityEntry>> = Pager(
        config = PagingConfig(10),
        initialKey = null,
        pagingSourceFactory = { ActivityPagingSource(repositoryProvider.activityRepository) }
    ).flow.cachedIn(viewModelScope)


//    fun generateActivityEntries() = viewModelScope.launch {
//        repository.generateEntries()
//    }
}