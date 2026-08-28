package com.pablo.ruiz.babyloading.core.pregnancy.domain.model

sealed interface DueDateRelation {
    data class Upcoming(val days: Int) : DueDateRelation {
        init {
            require(days > 0) { "Upcoming due-date relation must be positive" }
        }
    }

    data object Today : DueDateRelation

    data class Elapsed(val days: Int) : DueDateRelation {
        init {
            require(days > 0) { "Elapsed due-date relation must be positive" }
        }
    }
}
