package io.github.jcwu1.habbit.storage

import eu.anifantakis.lib.ksafe.KSafe
import eu.anifantakis.lib.ksafe.awaitCacheReady

internal actual suspend fun awaitStorageReady(ksafe: KSafe) {
    ksafe.awaitCacheReady()
}
