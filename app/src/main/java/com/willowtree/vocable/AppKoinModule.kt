package com.willowtree.vocable

import com.willowtree.vocable.utils.VocableSharedPreferences
import org.koin.dsl.module

object AppKoinModule {

    fun getModule() = module {
        single { VocableSharedPreferences() }
    }
}