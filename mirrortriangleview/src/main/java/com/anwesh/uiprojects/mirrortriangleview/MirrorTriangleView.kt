package com.anwesh.uiprojects.mirrortriangleview

/**
 * Created by anweshmishra on 15/09/18.
 */

import android.app.Activity
import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Path

val nodes : Int = 5

fun Canvas.drawMTNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val sc1 : Float = Math.min(0.5f, scale) * 2
    val sc2 : Float = Math.min(0.5f, Math.max(scale - 0.5f, 0f)) * 2
    val size : Float = gap/2
    paint.color = Color.parseColor("#4A148C")
    save()
    translate(gap + i * gap, h/2)
    for (j in 0..1) {
        val sf : Float = 1f - 2 * (j % 2)
        save()
        translate(0f, -(h/2) * sf * sc2)
        rotate(180f * j * sc1 + 180f * sc2)
        val path: Path = Path()
        path.moveTo(-size/2, 0f)
        path.lineTo(0f, -size)
        path.lineTo(size/2, 0f)
        path.lineTo(-size/2, 0f)
        drawPath(path, paint)
        restore()
    }
    restore()

}

class MirrorTriangleView (ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0F, var t : Int = 0) {
        val MAX_T : Int = 5

        fun update(cb : (Float) -> Unit) {
            if (t == 0 || t == MAX_T) {
                scale += 0.05f * dir
                if (scale > 0.49f && scale < 0.51f) {
                    scale = 0.5f
                    t++
                }
                if (Math.abs(scale - prevScale) > 1) {
                    scale = prevScale + dir
                    dir = 0f
                    prevScale = scale
                    t = 0
                    cb(prevScale)
                }
            } else {
                t++
            }

        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class MTNode(var i : Int, val state : State = State()) {

        private var next : MTNode? = null
        private var prev : MTNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = MTNode(i + 1)
                next?.prev = this
            }
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : MTNode {
            var curr : MTNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawMTNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }
    }

    data class MirrorTriangle(var i : Int) {
        private var root : MTNode = MTNode(0)
        private var curr : MTNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : MirrorTriangleView) {

        private val animator : Animator = Animator(view)
        private val mt : MirrorTriangle = MirrorTriangle(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            mt.draw(canvas, paint)
            animator.animate {
                mt.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            mt.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : MirrorTriangleView {
            val view : MirrorTriangleView = MirrorTriangleView(activity)
            activity.setContentView(view)
            return view
        }
    }
}