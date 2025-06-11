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
        // Start observing the habits from the repository as soon as the ViewModel is created.
        repository.getAllHabits().onEach { habits ->
            _state.update {
                // We need to map from HabitEntity to our domain Habit model
                // For now, we'll create a simple mapping.
                // A proper mapper class would be better in a larger project.
                val domainHabits = habits.map {
                    Habit(
                        id = it.id, name = it.name, completedDates = it.completedDates
                    )
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
            }
    }
}
