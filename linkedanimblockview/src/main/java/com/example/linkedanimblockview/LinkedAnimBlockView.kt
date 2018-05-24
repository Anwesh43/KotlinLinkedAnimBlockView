package com.example.linkedanimblockview

/**
 * Created by anweshmishra on 24/05/18.
 */

import android.content.Context
import android.view.View
import android.view.MotionEvent
import android.graphics.*

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
}