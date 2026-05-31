package com.lilbro.picobotella.utils

import android.content.Context
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ModelCache {
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