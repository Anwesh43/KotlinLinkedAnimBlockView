package com.example.linkedanimblockview

/**
 * Created by anweshmishra on 24/05/18.
 */

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.View
import android.view.MotionEvent
import android.graphics.*
import kotlin.collections.ArrayList

fun PointF.getMin() : Float {
    return Math.min(x, y)
}
val getDimension : (Canvas) -> PointF = {canvas -> PointF(canvas.width.toFloat(), canvas.height.toFloat()) }

val getSize : (Canvas) -> Float = {canvas -> getDimension(canvas).getMin()/3}

class LinkedAnimBlockView (ctx : Context) : View(ctx) {

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

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(stopcb : (Float) -> Unit) {
            scale += 0.1f * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                stopcb(scale)

            }
        }

        fun startUpdating(startcb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                startcb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(updatecb : () -> Unit) {
            if (animated) {
                updatecb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch (ex : Exception) {

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

    data class LABNode(var i : Int, val state : State = State()) {

        var next : LABNode? = null

        var prev : LABNode? = null

        var cb : ((Canvas, Paint, Float) -> Unit)? = null
        fun addNeighbor(cbs : ArrayList<(Canvas, Paint, Float) -> Unit>) {
            if(cbs.isNotEmpty()) {
                cb = cbs[0]
                cbs.removeAt(0)
                if (cbs.isNotEmpty()) {
                    next = LABNode(i +1)
                    next?.prev = this
                    next?.addNeighbor(cbs)
                }
            }
        }

        fun update(stopcb : (Float) -> Unit) {
            state.update(stopcb)
        }


        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }

        fun draw(canvas : Canvas, paint : Paint) {
            prev?.draw(canvas, paint)
            canvas.save()
            canvas.translate(getDimension(canvas).x/2, getDimension(canvas).y/2)
            cb?.invoke(canvas, paint, state.scale)
            canvas.restore()
        }

        fun getNext(dir : Int, cb : () -> Unit) : LABNode {
            var curr : LABNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LinkedAnimBlock(var i : Int) {

        private var curr : LABNode = LABNode(0)

        private var dir : Int = 1
        init {
            curr.addNeighbor(createDrawFunctions())
        }

        fun createDrawFunctions() : ArrayList<(Canvas, Paint, Float) -> Unit> {
            val drawCbs : ArrayList<(Canvas, Paint, Float) -> Unit> = ArrayList()
            drawCbs.add {canvas, paint, scale ->
                paint.color = Color.parseColor("#9E9E9E")
                val size : Float = getSize(canvas) * scale
                canvas.save()
                canvas.drawRect(-size, -size, size, size, paint)
                canvas.restore()
            }

            drawCbs.add { canvas, paint, scale ->
                paint.color = Color.parseColor("#00695C")
                val size : Float = getSize(canvas)
                val w : Float = size/3
                val h : Float = size * 0.8f
                canvas.save()
                canvas.translate(-size/2, 0f)
                canvas.drawRect(RectF(-w * scale, -h, w * scale, h), paint)
                canvas.restore()
            }

            val addCircleToCbs : (Int) -> Unit = { i ->
                drawCbs.add { canvas, paint, scale ->
                    paint.color = Color.parseColor("#00695C")
                    val size : Float = getSize(canvas)
                    val r : Float = size / 3
                    val y : Float = -size / 2
                    canvas.save()
                    canvas.translate(size/2, y + size * i)
                    canvas.drawArc(RectF(-r, -r, r, r), 0f, 360f * scale, true, paint)
                    canvas.restore()
                }
            }

            for (i in 0..1) {
                addCircleToCbs(i)
            }

            return drawCbs
        }

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(stopcb : (Float) -> Unit) {
            curr.update({scale ->
                curr = this.curr.getNext(dir) {
                    this.dir *= -1
                }
                stopcb(scale)
            })
        }

        fun startUpdating(startcb : () -> Unit) {
            curr.startUpdating(startcb)
        }
    }

    data class Renderer (var view : LinkedAnimBlockView) {

        private val linkedAnimBlock : LinkedAnimBlock = LinkedAnimBlock(0)

        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            linkedAnimBlock.draw(canvas, paint)
            animator.animate {
                linkedAnimBlock.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            linkedAnimBlock.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : LinkedAnimBlockView {
            val view : LinkedAnimBlockView = LinkedAnimBlockView(activity)
            activity.setContentView(view)
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            return view
        }
    }
}