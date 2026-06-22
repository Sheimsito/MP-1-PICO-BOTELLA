package com.lilbro.picobotella.ui.home

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.opengl.Matrix
import android.view.Choreographer
import android.view.SurfaceView
import android.view.animation.DecelerateInterpolator
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.filament.EntityManager
import com.google.android.filament.LightManager
import com.google.android.filament.View as FilamentView
import com.google.android.filament.utils.ModelViewer
import com.lilbro.picobotella.data.local.ModelCache
import java.nio.ByteBuffer
import javax.inject.Inject

class BottleRenderer @Inject constructor(
    private val modelCache: ModelCache
) : DefaultLifecycleObserver {

    private lateinit var modelViewer: ModelViewer
    private lateinit var choreographer: Choreographer
    private var defaultAngle = 0f
    private val transformMatrix = FloatArray(16)
    
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(nanos: Long) {
            modelViewer.render(nanos)
            choreographer.postFrameCallback(this)
        }
    }

    fun setupBottle(surfaceView: SurfaceView, context: Context) {
        choreographer = Choreographer.getInstance()
        modelViewer = ModelViewer(surfaceView)
        
        val engine = modelViewer.engine
        val scene = modelViewer.scene

        surfaceView.setZOrderOnTop(true)
        surfaceView.setBackgroundColor(Color.TRANSPARENT)
        surfaceView.holder.setFormat(PixelFormat.TRANSLUCENT)

        modelViewer.view.blendMode = FilamentView.BlendMode.TRANSLUCENT
        modelViewer.scene.skybox = null

        val options = modelViewer.renderer.clearOptions
        options.clear = true
        modelViewer.renderer.clearOptions = options

        // Lights
        val mainLight = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(1.0f, 1.0f, 1.0f).intensity(110_000f)
            .direction(0.5f, -1.0f, -1.0f).castShadows(true)
            .build(engine, mainLight)
        scene.addEntity(mainLight)

        val fillLight = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(1.0f, 0.95f, 0.85f).intensity(50_000f)
            .direction(-0.5f, 1.0f, 0.5f)
            .build(engine, fillLight)
        scene.addEntity(fillLight)

        try {
            val buf = modelCache.bottleBuffer ?: context.assets.open("models/bottle.glb").use { input ->
                val bytes = input.readBytes()
                ByteBuffer.allocateDirect(bytes.size).apply { put(bytes); flip() }
            }
            if (modelCache.bottleBuffer == null) modelCache.bottleBuffer = buf
            modelCache.bottleBuffer!!.rewind()
            modelViewer.loadModelGlb(modelCache.bottleBuffer!!)
            modelViewer.transformToUnitCube()
        } catch (e: Exception) { e.printStackTrace() }

        modelViewer.asset?.root?.let { root ->
            val tm = engine.transformManager
            tm.getTransform(tm.getInstance(root), transformMatrix)
        }
    }

    fun spinBottle(onAnimationEnd: () -> Unit) {
        val startAngle = defaultAngle
        val direction = if (Math.random() < 0.5) 1f else -1f
        val extraRotation = 1440f + (Math.random() * 360f).toFloat()
        val finalAngle = startAngle + (direction * extraRotation)

        ValueAnimator.ofFloat(startAngle, finalAngle).apply {
            duration = 5000
            interpolator = DecelerateInterpolator(1.5f)
            addUpdateListener { anim ->
                defaultAngle = anim.animatedValue as Float
                modelViewer.asset?.root?.let { root ->
                    val tm = modelViewer.engine.transformManager
                    val inst = tm.getInstance(root)
                    val rotationMatrix = FloatArray(16)
                    Matrix.setIdentityM(rotationMatrix, 0)
                    Matrix.rotateM(rotationMatrix, 0, defaultAngle, 0f, 0f, 1f)
                    val finalMatrix = FloatArray(16)
                    Matrix.multiplyMM(finalMatrix, 0, rotationMatrix, 0, transformMatrix, 0)
                    tm.setTransform(inst, finalMatrix)
                }
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    onAnimationEnd()
                }
            })
        }.start()
    }

    override fun onResume(owner: LifecycleOwner) {
        choreographer.postFrameCallback(frameCallback)
    }

    override fun onPause(owner: LifecycleOwner) {
        choreographer.removeFrameCallback(frameCallback)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        modelViewer.engine.destroy()
    }
}
