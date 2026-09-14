package kr.ac.anu.mumu.presentation.main

import java.time.LocalDate
import java.time.YearMonth

fun YearMonth.toCalendarCells(): List<LocalDate?> {
    val leadingDays = atDay(1).dayOfWeek.value - 1
    val cells = List<LocalDate?>(leadingDays) { null } + (1..lengthOfMonth()).map(::atDay)
    return cells + List((7 - cells.size % 7) % 7) { null }
}
