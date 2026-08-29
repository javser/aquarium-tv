package ru.javser.aquarium

import android.service.dreams.DreamService

class AquariumDreamService : DreamService() {

    private var view: AquariumView? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isFullscreen = true
        setInteractive(false)
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        val v = AquariumView(this)
        view = v
        setContentView(v)
        v.start()
    }

    override fun onDreamingStopped() {
        view?.stop()
        view = null
        super.onDreamingStopped()
    }
}
