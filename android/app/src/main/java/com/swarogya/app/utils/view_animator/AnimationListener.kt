package com.swarogya.app.utils.view_animator

import android.view.View

class AnimationListener private constructor() {
    interface Start {
        fun onStart()
    }

    interface Stop {
        fun onStop()
    }

    interface Update<V : View?> {
        fun update(view: V, value: Float)
    }
}