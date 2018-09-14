package com.anwesh.uiprojects.linkedmirrortriangleview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.mirrortriangleview.MirrorTriangleView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MirrorTriangleView.create(this)
    }
}
