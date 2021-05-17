package com.vikingelectronics.softphone.activity.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.vikingelectronics.softphone.accounts.UserProvider
import com.vikingelectronics.softphone.activity.ActivityEntry
import com.vikingelectronics.softphone.dagger.UserComponentEntryPoint
import com.vikingelectronics.softphone.networking.ActivityRepository
import com.vikingelectronics.softphone.networking.ActivityRepositoryImpl
import dagger.hilt.EntryPoints
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ActivityListViewModel @Inject constructor(
    private val userProvider: UserProvider
): ViewModel() {

    private val repository: ActivityRepository = userProvider.userComponentEntryPoint.activityRepository()

    val activityEntries: Flow<PagingData<ActivityEntry>> = Pager(
        config = PagingConfig(10),
        initialKey = null,
        pagingSourceFactory = { ActivityPagingSource(repository) }
    ).flow.cachedIn(viewModelScope)


//    fun generateActivityEntries() = viewModelScope.launch {
//        repository.generateEntries()
//    }
}