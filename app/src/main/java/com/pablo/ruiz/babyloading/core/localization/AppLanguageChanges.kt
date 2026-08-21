package com.pablo.ruiz.babyloading.core.localization

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Reports effective app-language changes observed while this process is alive.
 */
interface AppLanguageChanges {
    val changes: Flow<AppLanguage>

    suspend fun refreshIfLanguageChanged(): Boolean
}

@Singleton
class AndroidAppLanguageChanges @Inject constructor(
    private val languageProvider: AppLanguageProvider,
) : AppLanguageChanges {
    private var previousLanguage = languageProvider.currentLanguage()
    private val mutableChanges = MutableSharedFlow<AppLanguage>(extraBufferCapacity = 1)

    override val changes: Flow<AppLanguage> = mutableChanges.asSharedFlow()

    override suspend fun refreshIfLanguageChanged(): Boolean {
        val currentLanguage = languageProvider.currentLanguage()
        if (currentLanguage == previousLanguage) return false

        previousLanguage = currentLanguage
        mutableChanges.emit(currentLanguage)
        return true
    }
}
