package com.vikingelectronics.softphone.schedules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.vikingelectronics.softphone.accounts.RepositoryProvider
import com.vikingelectronics.softphone.schedules.data.Schedule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SchedulesViewModel @Inject constructor(
    repositoryProvider: RepositoryProvider
): ViewModel() {

    private val repository = repositoryProvider.schedulesRepository

    val isGloballySnoozed = MutableStateFlow(false)

    val schedules: Flow<PagingData<Schedule>> = Pager(
        config = PagingConfig(10),
        initialKey = null,
        pagingSourceFactory = { SchedulesPagingSource(repository) }
    ).flow.cachedIn(viewModelScope)


    fun saveSchedule(
        schedule: Schedule,
        onSuccess: (Unit) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            repository.addNew(schedule).onSuccess(onSuccess).onFailure(onError)
        }
    }

    fun updateSchedule(
        updatedSchedule: Schedule,
        onSuccess: (Unit) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            repository.updateSchedule(updatedSchedule).onSuccess(onSuccess).onFailure(onError)
        }
    }

    fun deleteSchedules(
        schedules: List<Schedule>,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            val errors = mutableListOf<Throwable>()
            repository.delete(schedules).onEach {
                it.exceptionOrNull()?.let { error -> errors.add(error) }
            }.onCompletion {
               if (errors.isEmpty()) onSuccess() else onError(errors[0])
            }.collect()
        }
    }

    fun globalSnoozeClicked(isChecked: Boolean) {
        isGloballySnoozed.tryEmit(isChecked)
    }

    fun scheduleEnabledChanged(schedule: Schedule, shouldBeEnabled: Boolean) {
        val updatedSchedule = schedule.copy(enabled = shouldBeEnabled)

        viewModelScope.launch {
            repository.updateSchedule(updatedSchedule)
        }
    }
}