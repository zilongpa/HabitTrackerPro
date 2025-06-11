package com.example.habittrackerpro.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittrackerpro.data.local.entity.HabitEntity
import com.example.habittrackerpro.domain.model.Habit
import com.example.habittrackerpro.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import javax.inject.Inject

/**
 * The ViewModel for the Home screen. It holds the UI state and handles business logic.
 *
 * @param repository The repository for accessing habit data. Hilt provides this.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        repository.getAllHabits().onEach { entityList ->
            _state.update {
                // When mapping from Entity to Domain model, include completedDates
                val domainHabits = entityList.map { entity ->
                    Habit(id = entity.id,
                        name = entity.name,
                        completedDates = entity.completedDates.map { timestamp ->
                            // Assuming timestamps are stored as Long (epoch seconds)
                            ZonedDateTime.ofInstant(
                                java.time.Instant.ofEpochSecond(timestamp),
                                java.time.ZoneId.systemDefault()
                            )
                        })
                }
                it.copy(
                    habits = domainHabits
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnAddHabitClick -> {
                // Logic to navigate will be handled by the UI layer
            }

            is HomeEvent.OnHabitClick -> {
                // Logic to mark a habit as completed will be added later
            }

            is HomeEvent.OnHabitLongClick -> {
                // Logic for deleting a habit will be added later
            }

            // Handle the new completion event
            is HomeEvent.OnCompletedClick -> {
                viewModelScope.launch {
                    val habit = state.value.habits.first { it.id == event.habit.id }
                    val updatedDates = habit.completedDates.toMutableList()
                    val today = ZonedDateTime.now().toLocalDate()

                    if (event.isCompleted) {
                        // Add today's date if it doesn't exist
                        if (updatedDates.none { it.toLocalDate() == today }) {
                            updatedDates.add(ZonedDateTime.now())
                        }
                    } else {
                        // Remove today's date
                        updatedDates.removeAll { it.toLocalDate() == today }
                    }

                    // We need the full entity to update it in the database.
                    // In a real app, you might fetch it first or have it cached.
                    // For simplicity, we create a new one with updated dates.
                    val habitToUpdate = HabitEntity(
                        id = habit.id,
                        name = habit.name,
                        completedDates = updatedDates.map { it.toEpochSecond() },
                        // Keep other fields as they were, we might need a fetch for that
                        frequency = emptyList(),
                        reminder = 0L,
                        startDate = 0L
                    )
                    repository.updateHabit(habitToUpdate)
                }
            }

            // Handle deletion confirmation
            is HomeEvent.OnDeleteHabitConfirm -> {
                viewModelScope.launch {
                    state.value.habitToDelete?.let { habit ->
                        val entityToDelete = HabitEntity(
                            id = habit.id,
                            name = habit.name,
                            completedDates = habit.completedDates.map { it.toEpochSecond() },
                            frequency = emptyList(), // These fields need to be present for the entity
                            reminder = 0L,
                            startDate = 0L
                        )
                        repository.deleteHabit(entityToDelete)
                    }
                    // Hide the dialog after deletion
                    _state.update { it.copy(habitToDelete = null) }
                }
            }

            // Handle deletion cancellation
            is HomeEvent.OnDeleteHabitCancel -> {
                _state.update { it.copy(habitToDelete = null) }
            }
        }
    }
}
