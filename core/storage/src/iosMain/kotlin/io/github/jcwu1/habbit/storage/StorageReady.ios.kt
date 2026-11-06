package io.github.jcwu1.habbit.storage

import eu.anifantakis.lib.ksafe.KSafe

internal actual suspend fun awaitStorageReady(ksafe: KSafe) = Unit
