package com.example.anweshmishra.kotlinlinkedanimblockview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.linkedanimblockview.LinkedAnimBlockView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LinkedAnimBlockView.create(this)
    }
}
