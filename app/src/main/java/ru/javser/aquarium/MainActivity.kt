package ru.javser.aquarium

import android.app.Activity
import android.os.Bundle

class MainActivity : Activity() {
    private var view: AquariumView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val v = AquariumView(this)
        view = v
        setContentView(v)
    }

    override fun onResume() { super.onResume(); view?.start() }
    override fun onPause()  { super.onPause();  view?.stop() }
}
