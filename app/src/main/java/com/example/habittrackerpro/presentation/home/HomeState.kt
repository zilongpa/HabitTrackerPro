package com.example.habittrackerpro.presentation.home
import com.example.habittrackerpro.domain.model.Habit
import java.time.ZonedDateTime

/**
 * Represents all the necessary information to render the Home screen.
 *
 * @property selectedDate The selected date being displayed.
 * @property habits The list of habits to be displayed on the screen.
 */
data class HomeState(
    // Renamed from currentDate for clarity, represents the currently selected date.
    val selectedDate: ZonedDateTime = ZonedDateTime.now(),
    // A list of dates to be displayed in the date selector.
    val visibleDates: List<ZonedDateTime> = emptyList(),
    val habits: List<Habit> = emptyList(),
    // Property to hold the habit that is about to be deleted.
    // If null, the dialog is hidden. If not null, the dialog is shown.
    val habitToDelete: Habit? = null
)
