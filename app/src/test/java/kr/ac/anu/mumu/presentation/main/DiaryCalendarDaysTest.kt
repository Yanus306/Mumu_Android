package kr.ac.anu.mumu.presentation.main

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class DiaryCalendarDaysTest {
    @Test fun september2026StartsOnTuesdayAndFillsWeeks() {
        val cells = YearMonth.of(2026, 9).toCalendarCells()
        assertNull(cells[0])
        assertEquals(LocalDate.of(2026, 9, 1), cells[1])
        assertEquals(35, cells.size)
    }

    @Test fun leapFebruaryIncludes29th() {
        val cells = YearMonth.of(2024, 2).toCalendarCells()
        assertEquals(LocalDate.of(2024, 2, 29), cells.filterNotNull().last())
        assertEquals(35, cells.size)
    }
}
