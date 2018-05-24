package com.example.linkedanimblockview

/**
 * Created by anweshmishra on 24/05/18.
 */

import android.content.Context
import android.view.View
import android.view.MotionEvent
import android.graphics.*
import java.util.*

class LinkedAnimBlockView (ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

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

        var cb : ((Canvas, Paint) -> Unit)? = null
        fun addNeighbor(cbs : List<(Canvas, Paint) -> Unit>) {
            takeIf{cbs.size > 0}.apply {
                cb = cbs[0]
                takeIf { cbs.size > 1 }.apply {
                    next = LABNode(i +1)
                    next?.prev = this
                    next?.addNeighbor(cbs.subList(1, cbs.size))
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
            cb?.invoke(canvas, paint)
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
}