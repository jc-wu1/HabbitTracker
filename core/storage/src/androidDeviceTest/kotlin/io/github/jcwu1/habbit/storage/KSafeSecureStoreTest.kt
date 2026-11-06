package io.github.jcwu1.habbit.storage

import androidx.test.platform.app.InstrumentationRegistry
import eu.anifantakis.lib.ksafe.KSafe
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.nullable
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class KSafeSecureStoreTest {
    @Test
    fun committedRecordSurvivesReopeningTheStore() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val fileName = "storage_test_" + System.nanoTime().toString().replace("-", "")
        val record = StoredRecord(1, "test-access-token", "test-refresh-token")
        val first = KSafe(context.applicationContext, fileName = fileName)
        try {
            KSafeSecureStore(first).put("session", record, StoredRecord.serializer())
            assertNotNull(first.getKeyInfo("session")?.protection, "The session must be encrypted")
        } finally {
            first.close()
        }
        val reopened = KSafe(context.applicationContext, fileName = fileName)
        try {
            val restored = KSafeSecureStore(reopened).get("session", null, StoredRecord.serializer().nullable)
            assertEquals(record, restored)
        } finally {
            reopened.clearAll()
            reopened.close()
        }
    }
}

@Serializable
private data class StoredRecord(val id: Int, val accessToken: String, val refreshToken: String)
