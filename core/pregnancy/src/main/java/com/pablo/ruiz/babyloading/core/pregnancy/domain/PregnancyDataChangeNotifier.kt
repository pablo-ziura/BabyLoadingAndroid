package com.pablo.ruiz.babyloading.core.pregnancy.domain

fun interface PregnancyDataChangeNotifier {
    suspend fun onPregnancyDataChanged()
}
