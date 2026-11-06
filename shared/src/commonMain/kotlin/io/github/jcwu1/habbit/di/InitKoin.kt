package io.github.jcwu1.habbit.di

import org.koin.core.annotation.KoinApplication
import org.koin.dsl.KoinAppDeclaration
import org.koin.plugin.module.dsl.startKoin

@KoinApplication
object HabbitKoinApp

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin<HabbitKoinApp> {
        config?.invoke(this)
    }
}
