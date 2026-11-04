package io.hammerhead.karooexttemplate.extension

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Typeface
import android.widget.RemoteViews
import io.hammerhead.karooexttemplate.R
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.UserProfile
import io.hammerhead.karooext.models.ViewConfig
import kotlin.math.ceil

class HeartRateGraphicDataType(extension: String) :
    DataTypeImpl(extension, "heart-rate-graphic") {

    private var heartRate: Double? = null
    private var averageHeartRate: Double? = null
    private var maximumHeartRate: Double? = null

    private var heartRateZones: List<UserProfile.Zone> = emptyList()

    private var viewContext: Context? = null
    private var viewEmitter: ViewEmitter? = null

    private val zoneColors = listOf(
        Color.rgb(33, 150, 243),
        Color.rgb(0, 200, 83),
        Color.rgb(255, 234, 0),
        Color.rgb(251, 100, 85),
        Color.rgb(255, 23, 68)
    )

    override fun startView(
        context: Context,
        config: ViewConfig,
        emitter: ViewEmitter
    ) {
        viewContext = context
        viewEmitter = emitter

        emitter.onNext(UpdateGraphicConfig(showHeader = false))

        updateView()
    }

    fun updateHeartRate(value: Double) {
        heartRate = value
        updateView()
    }

    fun updateHeartRateStats(average: Double?, maximum: Double?) {
        averageHeartRate = average
        maximumHeartRate = maximum
        updateView()
    }

    fun updateZones(zones: List<UserProfile.Zone>) {
        heartRateZones = zones
        updateView()
    }

    private fun updateView() {
        val context = viewContext ?: return
        val emitter = viewEmitter ?: return

        val views = RemoteViews(
            context.packageName,
            R.layout.heart_rate_graphic_view
        )

        // CURRENT HR
        views.setImageViewBitmap(
            R.id.hr_value,
            createTextBitmap(
                context = context,
                text = heartRate?.toInt()?.toString() ?: "--",
                textSizeSp = 122f,
                textColor = Color.WHITE,
                bold = false,
                useMonospace = true,
                tightReference = "0123456789"
            )
        )

        // AVG LABEL
        views.setImageViewBitmap(
            R.id.hr_average_label,
            createTextBitmap(
                context = context,
                text = "AVG",
                textSizeSp = 15f,
                textColor = Color.WHITE,
                bold = true,
                useMonospace = false,
                tightReference = null
            )
        )

        // AVG VALUE
        views.setImageViewBitmap(
            R.id.hr_average,
            createTextBitmap(
                context = context,
                text = averageHeartRate?.toInt()?.toString() ?: "--",
                textSizeSp = 36f,
                textColor = Color.WHITE,
                bold = false,
                useMonospace = true,
                tightReference = "0123456789"
            )
        )

        // MAX LABEL
        views.setImageViewBitmap(
            R.id.hr_maximum_label,
            createTextBitmap(
                context = context,
                text = "MAX",
                textSizeSp = 15f,
                textColor = Color.WHITE,
                bold = true,
                useMonospace = false,
                tightReference = null
            )
        )

        // MAX VALUE
        views.setImageViewBitmap(
            R.id.hr_maximum,
            createTextBitmap(
                context = context,
                text = maximumHeartRate?.toInt()?.toString() ?: "--",
                textSizeSp = 36f,
                textColor = Color.WHITE,
                bold = false,
                useMonospace = true,
                tightReference = "0123456789"
            )
        )

        val bitmap = createZoneBar(
            width = 600,
            height = 20,
            currentHr = heartRate,
            zones = heartRateZones
        )

        views.setImageViewBitmap(
            R.id.hr_zone_bar,
            bitmap
        )

        emitter.updateView(views)
    }


    private fun createTextBitmap(
        context: Context,
        text: String,
        textSizeSp: Float,
        textColor: Int,
        bold: Boolean = false,
        useMonospace: Boolean = true,
        tightReference: String? = null
    ): Bitmap {

        val density = context.resources.displayMetrics.density
        val scaledSize = textSizeSp * density

        val typeface: Typeface = if (useMonospace) {
            Typeface.create(
                "monospace",
                if (bold) Typeface.BOLD else Typeface.NORMAL
            )
        } else {
            Typeface.create(
                "sans-serif",
                if (bold) Typeface.BOLD else Typeface.NORMAL
            )
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = scaledSize
            this.typeface = typeface

            if (bold && useMonospace) {
                isFakeBoldText = true
            }

            textAlign = Paint.Align.LEFT
        }

        val fontMetrics = paint.fontMetrics
        val padding = 2f * density

        val top: Float
        val bottom: Float

        if (tightReference != null) {
            val referenceBounds = Rect()

            paint.getTextBounds(
                tightReference,
                0,
                tightReference.length,
                referenceBounds
            )

            top = referenceBounds.top.toFloat()
            bottom = referenceBounds.bottom.toFloat()
        } else {
            top = fontMetrics.ascent
            bottom = fontMetrics.descent
        }

        val bitmapWidth = ceil(
            paint.measureText(text) + padding * 2
        ).toInt().coerceAtLeast(1)

        val bitmapHeight = ceil(
            bottom - top + padding * 2
        ).toInt().coerceAtLeast(1)

        val bitmap = Bitmap.createBitmap(
            bitmapWidth,
            bitmapHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        val baseline = padding - top

        canvas.drawText(
            text,
            padding,
            baseline,
            paint
        )

        return bitmap
    }

    private fun createZoneBar(
        width: Int,
        height: Int,
        currentHr: Double?,
        zones: List<UserProfile.Zone>
    ): Bitmap {

        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        if (zones.isEmpty()) {
            return bitmap
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        // Determine the active zone based on the current heart rate.
        val activeZoneIndex = currentHr?.let { value ->
            val hr = value.toFloat()

            zones.indexOfFirst { zone ->
                hr >= zone.min && hr <= zone.max
            }.let { index ->
                when {
                    index >= 0 -> index
                    hr < zones.first().min -> 0
                    else -> zones.lastIndex
                }
            }
        } ?: -1

        // Width of the active zone.
        val inactiveWeight = 1f
        val activeWeight = 1.9f

        val totalWeight = zones.indices.sumOf { index ->
            if (index == activeZoneIndex) {
                activeWeight.toDouble()
            } else {
                inactiveWeight.toDouble()
            }
        }.toFloat()

        val gap = 5f
        val availableWidth = width.toFloat() - gap * (zones.size - 1)
        val unitWidth = availableWidth / totalWeight

        var left = 0f

        zones.forEachIndexed { index, zone ->

            val isActive = index == activeZoneIndex

            val weight = if (isActive) {
                activeWeight
            } else {
                inactiveWeight
            }

            val segmentWidth = unitWidth * weight
            val right = left + segmentWidth

            val zoneColor = zoneColors.getOrElse(index) {
                Color.GRAY
            }

            // Height of the colored zones.
            val barTop = if (isActive) 1.5f else 6f
            val barBottom = if (isActive) 16f else 13f

            // Fully filled zone without borders.
            paint.color = zoneColor

            canvas.drawRect(
                left,
                barTop,
                right,
                barBottom,
                paint
            )

            if (isActive) {

                val hr = currentHr!!.toFloat()

                val zoneRange = (zone.max - zone.min + 1)
                    .toFloat()
                    .coerceAtLeast(1f)

                val positionInZone = (
                        (hr - zone.min + 0.5f) / zoneRange
                        ).coerceIn(0f, 1f)

                val markerCenter = left + positionInZone * segmentWidth

                // Indicator height.
                val markerTop = 0f
                val markerBottom = 17.8f

                // White outer shape of the indicator.
                val outerMarker = Path().apply {
                    moveTo(markerCenter - 8.4f, markerTop)
                    lineTo(markerCenter + 8.4f, markerTop)
                    lineTo(markerCenter + 13.2f, markerBottom)
                    lineTo(markerCenter - 13.2f, markerBottom)
                    close()
                }

                paint.color = Color.WHITE
                canvas.drawPath(outerMarker, paint)

                // Black interior of the indicator.
                val innerMarker = Path().apply {
                    moveTo(markerCenter - 4.4f, barTop)
                    lineTo(markerCenter + 4.4f, barTop)
                    lineTo(markerCenter + 10.8f, barBottom)
                    lineTo(markerCenter - 10.8f, barBottom)
                    close()
                }

                // Keep the black section entirely within the colored rectangle.
                canvas.save()

                canvas.clipRect(
                    left,
                    barTop,
                    right,
                    barBottom
                )

                paint.color = Color.BLACK
                canvas.drawPath(innerMarker, paint)

                canvas.restore()
            }

            left = right + gap
        }

        return bitmap
    }
}