package com.lilbro.picobotella.data.local

import android.content.Context
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


// Implementation of ModelCache class to handle model loading.
@Singleton
class ModelCache @Inject constructor() {
    var bottleBuffer: ByteBuffer? = null

    suspend fun preloadModel(context: Context) {
        if (bottleBuffer != null) return
        withContext(Dispatchers.IO) {
            try {
                context.assets.open("models/bottle.glb").use { input ->
                    val bytes = input.readBytes()
                    // Direct buffer for better performance
                    val buffer = ByteBuffer.allocateDirect(bytes.size)
                    buffer.order(ByteOrder.nativeOrder())
                    buffer.put(bytes)
                    buffer.flip()
                    bottleBuffer = buffer
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
