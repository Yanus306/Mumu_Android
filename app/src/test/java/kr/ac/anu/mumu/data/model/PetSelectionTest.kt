package kr.ac.anu.mumu.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PetSelectionTest {
    private val pets = listOf(PetDto(1, "첫째"), PetDto(2, "둘째"))

    @Test fun selectedPetIsUsedForAnalysisAndHistory() {
        assertEquals(2L, pets.selectedPetId(2L))
    }

    @Test fun removedPetFallsBackToFirstAvailable() {
        assertEquals(1L, pets.selectedPetId(3L))
    }

    @Test fun emptyPetListHasNoSelection() {
        assertNull(emptyList<PetDto>().selectedPetId(2L))
    }
}
