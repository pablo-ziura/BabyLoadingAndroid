package com.pablo.ruiz.babyloading.core.pregnancy.content.data

import com.pablo.ruiz.babyloading.core.pregnancy.content.domain.model.BabySize
import org.junit.Assert.assertEquals
import org.junit.Test

class PregnancyContentMapperTest {
    @Test
    fun everyJsonBabySizeMapsToExactlyOneDomainValue() {
        val mapper = PregnancyContentMapper()

        val mappedValues = BabySizeDto.entries.map(mapper::toDomain)

        assertEquals(BabySize.entries.size, mappedValues.size)
        assertEquals(BabySize.entries.toSet(), mappedValues.toSet())
    }
}
