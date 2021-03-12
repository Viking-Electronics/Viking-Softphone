package com.vikingelectronics.softphone.activity.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vikingelectronics.softphone.activity.ActivityEntry
import com.vikingelectronics.softphone.networking.ActivityRepository
import com.vikingelectronics.softphone.networking.ActivityRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityListViewModel @Inject constructor(
    private val activityRepository: ActivityRepositoryImpl
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
            activityRepository.getAllEntries().collect {
                activityEntries += it
            }
        }
    }

    fun navigateToDetail(navController: NavController, activityEntry: ActivityEntry) {
        val directions = ActivityListFragmentDirections.actionActivityListFragmentToActivityDetailFragment(activityEntry.timestamp.toDate().toString(), activityEntry)
        navController.navigate(directions)
    }

    fun generateActivityEntries() = viewModelScope.launch {
        activityRepository.generateEntries()
    }
}