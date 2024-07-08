package com.google.mediapipe.examples.poselandmarker
import android.util.Log
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
interface KneeAngleListener {
    fun onKneeAngleBelowThreshold(count: Int)
}
class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var kneeAngleListener: KneeAngleListener? = null
    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()
    private var textPaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    private var isSquatting = false
    private var squatCount = 0
    private var squatThreshold = 200 // 假设膝盖角度小于90度认为是一个有效深蹲
    private var test: Double = 0.0
    init {
        initPaints()
    }

    fun clear() {
        results = null
        pointPaint.reset()
        linePaint.reset()
        textPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color =
            ContextCompat.getColor(context!!, R.color.mp_color_primary)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL

        textPaint.color = Color.RED
        textPaint.textSize = 32f
        textPaint.style = Paint.Style.FILL
    }

    private fun calculateAngle(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float): Double {
        val angle = Math.toDegrees(
            (atan2(y3 - y2, x3 - x2) - atan2(y1 - y2, x1 - x2)).toDouble()
        )
        return if (angle < 0) angle + 360 else angle
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.let { poseLandmarkerResult ->
            for (landmark in poseLandmarkerResult.landmarks()) {
                var hipX = 0f
                var hipY = 0f
                var kneeX = 0f
                var kneeY = 0f
                var ankleX = 0f
                var ankleY = 0f

                for ((index, normalizedLandmark) in landmark.withIndex()) {
                    val x = normalizedLandmark.x() * imageWidth * scaleFactor
                    val y = normalizedLandmark.y() * imageHeight * scaleFactor

                    // 繪製標記點
                    canvas.drawPoint(x, y, pointPaint)

                    // 繪製標記點數據
                    //canvas.drawText("(${"%.2f".format(normalizedLandmark.x())}, " +
                            //"${"%.2f".format(normalizedLandmark.y())}, ${"%.2f".format(normalizedLandmark.z())})", x, y, textPaint)

                    when (index) {
                        // 假設 23 是臀部，25 是膝蓋，27 是腳踝的索引值
                        23 -> {
                            hipX = x
                            hipY = y
                        }
                        25 -> {
                            kneeX = x
                            kneeY = y
                        }
                        27 -> {
                            ankleX = x
                            ankleY = y
                        }
                    }
                }

                // 計算膝蓋的角度
                val kneeAngle = calculateAngle(hipX, hipY, kneeX, kneeY, ankleX, ankleY)
                // 繪製膝蓋角度
                canvas.drawText("Knee Angle: ${"%.2f".format(kneeAngle)}", kneeX, kneeY - 20, textPaint)
                // 深蹲檢測邏輯
                if (kneeAngle < squatThreshold && !isSquatting) {
                    isSquatting = true
                } else if (kneeAngle >= squatThreshold && isSquatting) {
                    isSquatting = false
                    squatCount++
                    Log.d("OverlayView", "Squat count updated: $squatCount")
                    kneeAngleListener?.onKneeAngleBelowThreshold(squatCount)
                    // 通知MainActivity

                }

                // 繪製深蹲計數
                canvas.drawText("繪製深蹲計數: $squatCount", hipX,hipY, textPaint)

                PoseLandmarker.POSE_LANDMARKS.forEach {
                    canvas.drawLine(
                        poseLandmarkerResult.landmarks().get(0).get(it!!.start()).x() * imageWidth * scaleFactor,
                        poseLandmarkerResult.landmarks().get(0).get(it.start()).y() * imageHeight * scaleFactor,
                        poseLandmarkerResult.landmarks().get(0).get(it.end()).x() * imageWidth * scaleFactor,
                        poseLandmarkerResult.landmarks().get(0).get(it.end()).y() * imageHeight * scaleFactor,
                        linePaint)
                }

            }
        }
    }

    fun setResults(
        poseLandmarkerResults: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = poseLandmarkerResults

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                // PreviewView is in FILL_START mode. So we need to scale up the
                // landmarks to match with the size that the captured images will be
                // displayed.
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }
        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 12F
    }
    fun setKneeAngleListener(listener: KneeAngleListener) {
        kneeAngleListener = listener
    }


}
