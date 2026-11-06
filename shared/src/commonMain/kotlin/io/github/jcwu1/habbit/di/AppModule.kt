package io.github.jcwu1.habbit.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module
@Configuration
@ComponentScan("io.github.jcwu1.habbit.startup")
class AppModule
