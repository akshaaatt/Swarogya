package com.aemerse.svarogya.utils.view_animator

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Path
import android.graphics.PathMeasure
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.CycleInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.TextView
import androidx.annotation.IntRange
import com.aemerse.svarogya.utils.view_animator.ViewAnimator.RepeatMode
import java.util.*

open class AnimationBuilder(private val viewAnimator: ViewAnimator, private vararg val views: View) {
    private val animatorList: MutableList<Animator> = ArrayList()
    var isWaitForHeight = false
    private var nextValueWillBeDp = false
    var singleInterpolator: Interpolator? = null

    fun dp(): AnimationBuilder {
        nextValueWillBeDp = true
        return this
    }

    protected fun add(animator: Animator): AnimationBuilder {
        animatorList.add(animator)
        return this
    }

    protected fun toDp(px: Float): Float {
        return px / views[0].context.resources.displayMetrics.density
    }

    protected fun toPx(dp: Float): Float {
        return dp * views[0].context.resources.displayMetrics.density
    }

    protected fun getValues(vararg values: Float): FloatArray {
        if (!nextValueWillBeDp) {
            return values
        }
        val pxValues = FloatArray(values.size)
        for (i in values.indices) {
            pxValues[i] = toPx(values[i])
        }
        return pxValues
    }

    fun property(propertyName: String?, vararg values: Float): AnimationBuilder {
        for (view in views) {
            animatorList.add(ObjectAnimator.ofFloat(view, propertyName, *getValues(*values)))
        }
        return this
    }

    fun translationY(vararg y: Float): AnimationBuilder {
        return property("translationY", *y)
    }

    fun translationX(vararg x: Float): AnimationBuilder {
        return property("translationX", *x)
    }

    fun alpha(vararg alpha: Float): AnimationBuilder {
        return property("alpha", *alpha)
    }

    private fun scaleX(vararg scaleX: Float): AnimationBuilder {
        return property("scaleX", *scaleX)
    }

    private fun scaleY(vararg scaleY: Float): AnimationBuilder {
        return property("scaleY", *scaleY)
    }

    fun scale(vararg scale: Float): AnimationBuilder {
        scaleX(*scale)
        scaleY(*scale)
        return this
    }

    fun pivotX(pivotX: Float): AnimationBuilder {
        for (view in views) {
            view.pivotX = pivotX
        }
        return this
    }

    fun pivotY(pivotY: Float): AnimationBuilder {
        for (view in views) {
            view.pivotY = pivotY
        }
        return this
    }

    fun pivotX(vararg pivotX: Float): AnimationBuilder {
        ObjectAnimator.ofFloat(view, "pivotX", *getValues(*pivotX))
        return this
    }

    fun pivotY(vararg pivotY: Float): AnimationBuilder {
        ObjectAnimator.ofFloat(view, "pivotY", *getValues(*pivotY))
        return this
    }

    fun rotationX(vararg rotationX: Float): AnimationBuilder {
        return property("rotationX", *rotationX)
    }

    fun rotationY(vararg rotationY: Float): AnimationBuilder {
        return property("rotationY", *rotationY)
    }

    fun rotation(vararg rotation: Float): AnimationBuilder {
        return property("rotation", *rotation)
    }

    fun backgroundColor(vararg colors: Int): AnimationBuilder {
        for (view in views) {
            val objectAnimator =
                ObjectAnimator.ofInt(view, "backgroundColor", *colors)
            objectAnimator.setEvaluator(ArgbEvaluator())
            animatorList.add(objectAnimator)
        }
        return this
    }

    fun textColor(vararg colors: Int): AnimationBuilder {
        for (view in views) {
            if (view is TextView) {
                val objectAnimator =
                    ObjectAnimator.ofInt(view, "textColor", *colors)
                objectAnimator.setEvaluator(ArgbEvaluator())
                animatorList.add(objectAnimator)
            }
        }
        return this
    }

    fun custom(update: AnimationListener.Update<View>?, vararg values: Float): AnimationBuilder {
        for (view in views) {
            val valueAnimator = ValueAnimator.ofFloat(*getValues(*values))
            if (update != null) valueAnimator.addUpdateListener { animation ->
                update.update(
                    view,
                    (animation.animatedValue as Float)
                )
            }
            add(valueAnimator)
        }
        return this
    }

    fun height(vararg height: Float): AnimationBuilder {
        return custom(object :
            AnimationListener.Update<View> {
            override fun update(view: View, value: Float) {
                view.layoutParams.height = value.toInt()
                view.requestLayout()
            }
        }, *height)
    }

    fun width(vararg width: Float): AnimationBuilder {
        return custom(object :
            AnimationListener.Update<View> {
            override fun update(view: View, value: Float) {
                view.layoutParams.width = value.toInt()
                view.requestLayout()
            }
        }, *width)
    }

    fun waitForHeight(): AnimationBuilder {
        isWaitForHeight = true
        return this
    }

    fun createAnimators(): List<Animator> {
        return animatorList
    }

    fun andAnimate(vararg views: View): AnimationBuilder {
        return viewAnimator.addAnimationBuilder(*views)
    }

    fun thenAnimate(vararg views: View): AnimationBuilder {
        return viewAnimator.thenAnimate(*views)
    }

    fun duration(@IntRange(from = 1) duration: Long): AnimationBuilder {
        viewAnimator.duration(duration)
        return this
    }

    fun startDelay(@IntRange(from = 0) startDelay: Long): AnimationBuilder {
        viewAnimator.startDelay(startDelay)
        return this
    }

    fun repeatCount(@IntRange(from = -1) repeatCount: Int): AnimationBuilder {
        viewAnimator.repeatCount(repeatCount)
        return this
    }

    fun repeatMode(@RepeatMode repeatMode: Int): AnimationBuilder {
        viewAnimator.repeatMode(repeatMode)
        return this
    }

    fun onStart(startListener: AnimationListener.Start?): AnimationBuilder {
        viewAnimator.onStart(startListener)
        return this
    }

    fun onStop(stopListener: AnimationListener.Stop?): AnimationBuilder {
        viewAnimator.onStop(stopListener)
        return this
    }

    fun interpolator(interpolator: Interpolator?): AnimationBuilder {
        viewAnimator.interpolator(interpolator)
        return this
    }

    fun singleInterpolator(interpolator: Interpolator?): AnimationBuilder {
        singleInterpolator = interpolator
        return this
    }

    fun accelerate(): ViewAnimator {
        return viewAnimator.interpolator(AccelerateInterpolator())
    }

    fun decelerate(): ViewAnimator {
        return viewAnimator.interpolator(DecelerateInterpolator())
    }

    /**
     * Start.
     */
    fun start(): ViewAnimator {
        viewAnimator.start()
        return viewAnimator
    }

    val view: View
        get() = views[0]

    fun bounce(): AnimationBuilder {
        return translationY(0f, 0f, -30f, 0f, -15f, 0f, 0f)
    }

    fun bounceIn(): AnimationBuilder {
        alpha(0f, 1f, 1f, 1f)
        scaleX(0.3f, 1.05f, 0.9f, 1f)
        scaleY(0.3f, 1.05f, 0.9f, 1f)
        return this
    }

    fun bounceOut(): AnimationBuilder {
        scaleY(1f, 0.9f, 1.05f, 0.3f)
        scaleX(1f, 0.9f, 1.05f, 0.3f)
        alpha(1f, 1f, 1f, 0f)
        return this
    }

    fun fadeIn(): AnimationBuilder {
        return alpha(0f, 0.25f, 0.5f, 0.75f, 1f)
    }

    fun fadeOut(): AnimationBuilder {
        return alpha(1f, 0.75f, 0.5f, 0.25f, 0f)
    }

    fun flash(): AnimationBuilder {
        return alpha(1f, 0f, 1f, 0f, 1f)
    }

    fun flipHorizontal(): AnimationBuilder {
        return rotationX(90f, -15f, 15f, 0f)
    }

    fun flipVertical(): AnimationBuilder {
        return rotationY(90f, -15f, 15f, 0f)
    }

    fun pulse(): AnimationBuilder {
        scaleY(1f, 1.1f, 1f)
        scaleX(1f, 1.1f, 1f)
        return this
    }

    fun rollIn(): AnimationBuilder {
        for (view in views) {
            alpha(0f, 1f)
            translationX(
                -(view.width - view.paddingLeft - view.paddingRight).toFloat(),
                0f
            )
            rotation(-120f, 0f)
        }
        return this
    }

    fun rollOut(): AnimationBuilder {
        for (view in views) {
            alpha(1f, 0f)
            translationX(0f, view.width.toFloat())
            rotation(0f, 120f)
        }
        return this
    }

    fun rubber(): AnimationBuilder {
        scaleX(1f, 1.25f, 0.75f, 1.15f, 1f)
        scaleY(1f, 0.75f, 1.25f, 0.85f, 1f)
        return this
    }

    fun shake(): AnimationBuilder {
        translationX(0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
        interpolator(CycleInterpolator(5F))
        return this
    }

    fun standUp(): AnimationBuilder {
        for (view in views) {
            val x =
                ((view.width - view.paddingLeft - view.paddingRight) / 2
                        + view.paddingLeft).toFloat()
            val y = view.height - view.paddingBottom.toFloat()
            pivotX(x, x, x, x, x)
            pivotY(y, y, y, y, y)
            rotationX(55f, -30f, 15f, -15f, 0f)
        }
        return this
    }

    fun swing(): AnimationBuilder {
        return rotation(0f, 10f, -10f, 6f, -6f, 3f, -3f, 0f)
    }

    fun tada(): AnimationBuilder {
        scaleX(1f, 0.9f, 0.9f, 1.1f, 1.1f, 1.1f, 1.1f, 1.1f, 1.1f, 1f)
        scaleY(1f, 0.9f, 0.9f, 1.1f, 1.1f, 1.1f, 1.1f, 1.1f, 1.1f, 1f)
        rotation(0f, -3f, -3f, 3f, -3f, 3f, -3f, 3f, -3f, 0f)
        return this
    }

    fun wave(): AnimationBuilder {
        for (view in views) {
            val x =
                ((view.width - view.paddingLeft - view.paddingRight) / 2
                        + view.paddingLeft).toFloat()
            val y = view.height - view.paddingBottom.toFloat()
            rotation(12f, -12f, 3f, -3f, 0f)
            pivotX(x, x, x, x, x)
            pivotY(y, y, y, y, y)
        }
        return this
    }

    fun wobble(): AnimationBuilder {
        for (view in views) {
            val width = view.width.toFloat()
            val one = (width / 100.0).toFloat()
            translationX(0 * one, -25 * one, 20 * one, -15 * one, 10 * one, -5 * one, 0 * one, 0f)
            rotation(0f, -5f, 3f, -3f, 2f, -1f, 0f)
        }
        return this
    }

    fun zoomIn(): AnimationBuilder {
        scaleX(0.45f, 1f)
        scaleY(0.45f, 1f)
        alpha(0f, 1f)
        return this
    }

    fun zoomOut(): AnimationBuilder {
        scaleX(1f, 0.3f, 0f)
        scaleY(1f, 0.3f, 0f)
        alpha(1f, 0f, 0f)
        return this
    }

    fun fall(): AnimationBuilder {
        rotation(1080f, 720f, 360f, 0f)
        return this
    }

    fun newsPaper(): AnimationBuilder {
        alpha(0f, 1f)
        scaleX(0.1f, 0.5f, 1f)
        scaleY(0.1f, 0.5f, 1f)
        return this
    }

    fun slit(): AnimationBuilder {
        rotationY(90f, 88f, 88f, 45f, 0f)
        alpha(0f, 0.4f, 0.8f, 1f)
        scaleX(0f, 0.5f, 0.9f, 0.9f, 1f)
        scaleY(0f, 0.5f, 0.9f, 0.9f, 1f)
        return this
    }

    fun slideLeftIn(): AnimationBuilder {
        translationX(-300f, 0f)
        alpha(0f, 1f)
        return this
    }

    fun slideRightIn(): AnimationBuilder {
        translationX(300f, 0f)
        alpha(0f, 1f)
        return this
    }

    fun slideTopIn(): AnimationBuilder {
        translationY(-300f, 0f)
        alpha(0f, 1f)
        return this
    }

    fun slideBottomIn(): AnimationBuilder {
        translationY(300f, 0f)
        alpha(0f, 1f)
        return this
    }

    fun path(path: Path?): AnimationBuilder {
        if (path == null) {
            return this
        }
        val pathMeasure = PathMeasure(path, false)
        return custom(object : AnimationListener.Update<View> {
            override fun update(view: View, value: Float) {
                val currentPosition = FloatArray(2)
                pathMeasure.getPosTan(value, currentPosition, null)
                val x = currentPosition[0]
                val y = currentPosition[1]
                view.x = x
                view.y = y
                Log.d(null, "path: value=$value, x=$x, y=$y")
            }

        }, 0f, pathMeasure.length)
    }

}