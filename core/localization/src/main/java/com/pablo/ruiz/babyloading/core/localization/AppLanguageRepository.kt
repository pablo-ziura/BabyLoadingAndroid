package com.pablo.ruiz.babyloading.core.localization

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface AppLanguageRepository {
    val changes: Flow<AppLanguage>

    fun currentLanguage(): AppLanguage

    suspend fun refreshIfChanged(): Boolean
}

@Singleton
class DefaultAppLanguageRepository @Inject constructor(
    private val dataSource: AppLanguageDataSource,
    private val mapper: AppLanguageMapper,
) : AppLanguageRepository {
    private val refreshMutex = Mutex()
    private var previousLanguage = currentLanguage()
    private val mutableChanges = MutableSharedFlow<AppLanguage>(extraBufferCapacity = 1)

    override val changes: Flow<AppLanguage> = mutableChanges.asSharedFlow()

    override fun currentLanguage(): AppLanguage {
        return mapper.map(
            applicationLocales = dataSource.applicationLocales(),
            deviceLocales = dataSource.deviceLocales(),
        )
    }

    override suspend fun refreshIfChanged(): Boolean = refreshMutex.withLock {
        val currentLanguage = currentLanguage()
        if (currentLanguage == previousLanguage) return@withLock false

        previousLanguage = currentLanguage
        mutableChanges.emit(currentLanguage)
        true
    }
}
