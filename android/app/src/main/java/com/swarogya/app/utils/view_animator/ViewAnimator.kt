package com.swarogya.app.utils.view_animator

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.Interpolator
import androidx.annotation.IntDef
import androidx.annotation.IntRange
import java.util.*

class ViewAnimator {
    private val animationList: MutableList<AnimationBuilder> = ArrayList()
    private var duration = DEFAULT_DURATION
    private var startDelay: Long = 0
    private var interpolator: Interpolator? = null
    private var repeatCount = 0
    private var repeatMode = RESTART
    private var animatorSet: AnimatorSet? = null
    private var waitForThisViewHeight: View? = null
    private var startListener: AnimationListener.Start? = null
    private var stopListener: AnimationListener.Stop? = null
    private var prev: ViewAnimator? = null
    private var next: ViewAnimator? = null
    private fun createAnimatorSet(): AnimatorSet {
        val animators: MutableList<Animator> = ArrayList()
        for (animationBuilder in animationList) {
            val animatorList = animationBuilder.createAnimators()
            if (animationBuilder.singleInterpolator != null) {
                for (animator in animatorList) {
                    animator.interpolator = animationBuilder.singleInterpolator
                }
            }
            animators.addAll(animatorList)
        }
        for (animationBuilder in animationList) {
            if (animationBuilder.isWaitForHeight) {
                waitForThisViewHeight = animationBuilder.view
                break
            }
        }
        for (animator in animators) {
            if (animator is ValueAnimator) {
                animator.repeatCount = repeatCount
                animator.repeatMode = repeatMode
            }
        }
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(animators)
        animatorSet.duration = duration
        animatorSet.startDelay = startDelay
        if (interpolator != null) animatorSet.interpolator = interpolator
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                if (startListener != null) startListener!!.onStart()
            }

            override fun onAnimationEnd(animation: Animator) {
                if (stopListener != null) stopListener!!.onStop()
                if (next != null) {
                    next!!.prev = null
                    next!!.start()
                }
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        return animatorSet
    }

    fun thenAnimate(vararg views: View): AnimationBuilder {
        val nextViewAnimator = ViewAnimator()
        next = nextViewAnimator
        nextViewAnimator.prev = this
        return nextViewAnimator.addAnimationBuilder(*views)
    }

    fun addAnimationBuilder(vararg views: View): AnimationBuilder {
        val animationBuilder = AnimationBuilder(this, *views)
        animationList.add(animationBuilder)
        return animationBuilder
    }

    fun repeatCount(@IntRange(from = -1) repeatCount: Int): ViewAnimator {
        this.repeatCount = repeatCount
        return this
    }

    fun start() {
        if (prev != null) {
            prev!!.start()
        } else {
            animatorSet = createAnimatorSet()
            if (waitForThisViewHeight != null) {
                waitForThisViewHeight!!.viewTreeObserver
                    .addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                        override fun onPreDraw(): Boolean {
                            animatorSet!!.start()
                            waitForThisViewHeight!!.viewTreeObserver
                                .removeOnPreDrawListener(this)
                            return false
                        }
                    })
            } else {
                animatorSet!!.start()
            }
        }
    }

    fun cancel() {
        if (animatorSet != null) {
            animatorSet!!.cancel()
        }
        if (next != null) {
            next!!.cancel()
            next = null
        }
    }

    fun duration(@IntRange(from = 1) duration: Long): ViewAnimator {
        this.duration = duration
        return this
    }

    fun startDelay(@IntRange(from = 0) startDelay: Long): ViewAnimator {
        this.startDelay = startDelay
        return this
    }

    @IntDef(value = [RESTART, REVERSE])
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class RepeatMode { // use this instead enum, I heard that the enumeration will take a lot of memory
    }

    fun repeatMode(@RepeatMode repeatMode: Int): ViewAnimator {
        this.repeatMode = repeatMode
        return this
    }

    fun onStart(startListener: AnimationListener.Start?): ViewAnimator {
        this.startListener = startListener
        return this
    }

    fun onStop(stopListener: AnimationListener.Stop?): ViewAnimator {
        this.stopListener = stopListener
        return this
    }

    fun interpolator(interpolator: Interpolator?): ViewAnimator {
        this.interpolator = interpolator
        return this
    }

    companion object {
        const val RESTART = ValueAnimator.RESTART
        const val REVERSE = ValueAnimator.REVERSE
        const val INFINITE = ValueAnimator.INFINITE
        private const val DEFAULT_DURATION: Long = 3000
        fun animate(vararg view: View): AnimationBuilder {
            val viewAnimator = ViewAnimator()
            return viewAnimator.addAnimationBuilder(*view)
        }
    }
}