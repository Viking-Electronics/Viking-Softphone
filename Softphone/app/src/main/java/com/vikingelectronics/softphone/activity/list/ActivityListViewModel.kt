package com.vikingelectronics.softphone.activity.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vikingelectronics.softphone.activity.ActivityEntry
import com.vikingelectronics.softphone.networking.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityListViewModel @Inject constructor(
    private val repository: ActivityRepository
): ViewModel() {


    var activityEntries: List<ActivityEntry> by mutableStateOf(listOf())
        private set

    val groupedEntries: Map<String, List<ActivityEntry>>
        get()  {
            if (activityEntries.isEmpty()) getActivityEntries()
            return activityEntries.groupBy { it.sourceName }
        }

    fun getActivityEntries() {
        viewModelScope.launch {
            repository.getAllEntries().collect {
                activityEntries += it
            }
        }
    }

    fun generateActivityEntries() = viewModelScope.launch {
//        activityRepository.generateEntries()
    }
}