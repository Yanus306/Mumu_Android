package kr.ac.anu.mumu.data.model

fun List<PetDto>.selectedPetId(preferredId: Long?): Long? =
    firstOrNull { it.petId == preferredId }?.petId ?: firstOrNull()?.petId
