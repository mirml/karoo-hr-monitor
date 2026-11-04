package io.hammerhead.karooexttemplate.extension

import android.util.Log
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.KarooExtension
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.OnStreamState
import io.hammerhead.karooext.models.RideState
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UserProfile

class TemplateExtension : KarooExtension("karoo-hr-monitor", "1.0") {

    private val heartRateGraphicDataType =
        HeartRateGraphicDataType("karoo-hr-monitor")

    override val types = listOf(
        heartRateGraphicDataType
    )

    private var karooSystemService: KarooSystemService? = null

    private var consumerId: String? = null
    private var averageConsumerId: String? = null
    private var maximumConsumerId: String? = null
    private var profileConsumerId: String? = null
    private var rideStateConsumerId: String? = null

    private var averageHeartRate: Double? = null
    private var maximumHeartRate: Double? = null

    override fun onCreate() {
        super.onCreate()

        Log.d(LOG_TAG, "HR Extension: starting extension")

        val service = KarooSystemService(this)
        karooSystemService = service

        service.connect { connected ->
            if (connected) {
                Log.d(LOG_TAG, "HR Extension: connection successful")

                // Monitor activity state.
                rideStateConsumerId = service.addConsumer<RideState> { state ->
                    when (state) {
                        is RideState.Idle -> {
                            Log.d(LOG_TAG, "HR Extension: activity is idle")
                            resetHeartRateStats()
                        }

                        is RideState.Recording -> {
                            Log.d(LOG_TAG, "HR Extension: activity recording")
                        }

                        is RideState.Paused -> {
                            Log.d(LOG_TAG, "HR Extension: activity paused")
                        }
                    }
                }

                // Monitor current heart rate.
                consumerId = service.addConsumer<OnStreamState>(
                    OnStreamState.StartStreaming(DataType.Type.HEART_RATE)
                ) { event ->
                    when (val state = event.state) {
                        is StreamState.Streaming -> {
                            val hr = state.dataPoint.values[
                                DataType.Field.HEART_RATE
                            ]

                            if (hr != null) {
                                heartRateGraphicDataType.updateHeartRate(hr)
                            }
                        }

                        StreamState.Searching -> {
                            Log.d(LOG_TAG, "HR Extension: searching for HR")
                        }

                        StreamState.NotAvailable -> {
                            Log.d(LOG_TAG, "HR Extension: HR unavailable")
                        }

                        StreamState.Idle -> {
                            Log.d(LOG_TAG, "HR Extension: HR stream idle")
                        }
                    }
                }

                // Get average heart rate directly from the Karoo SDK.
                averageConsumerId = service.addConsumer<OnStreamState>(
                    OnStreamState.StartStreaming(DataType.Type.AVERAGE_HR)
                ) { event ->
                    when (val state = event.state) {
                        is StreamState.Streaming -> {
                            averageHeartRate = state.dataPoint.values[
                                DataType.Field.AVG_HR
                            ]

                            updateHeartRateStats()
                        }

                        StreamState.Searching -> {
                            Log.d(LOG_TAG, "HR Extension: searching for average HR")
                        }

                        StreamState.NotAvailable -> {
                            Log.d(LOG_TAG, "HR Extension: average HR unavailable")
                        }

                        StreamState.Idle -> {
                            Log.d(LOG_TAG, "HR Extension: average HR stream idle")
                        }
                    }
                }

                // Get maximum heart rate directly from the Karoo SDK.
                maximumConsumerId = service.addConsumer<OnStreamState>(
                    OnStreamState.StartStreaming(DataType.Type.MAX_HR)
                ) { event ->
                    when (val state = event.state) {
                        is StreamState.Streaming -> {
                            maximumHeartRate = state.dataPoint.values[
                                DataType.Field.MAX_HR
                            ]

                            updateHeartRateStats()
                        }

                        StreamState.Searching -> {
                            Log.d(LOG_TAG, "HR Extension: searching for maximum HR")
                        }

                        StreamState.NotAvailable -> {
                            Log.d(LOG_TAG, "HR Extension: maximum HR unavailable")
                        }

                        StreamState.Idle -> {
                            Log.d(LOG_TAG, "HR Extension: maximum HR stream idle")
                        }
                    }
                }

                Log.d(
                    LOG_TAG,
                    "HR Extension: average and maximum HR subscriptions registered"
                )

                // Monitor user profile and heart rate zones.
                profileConsumerId = service.addConsumer<UserProfile> { profile ->
                    Log.d(LOG_TAG, "HR Extension: user profile received")

                    heartRateGraphicDataType.updateZones(
                        profile.heartRateZones
                    )
                }

                Log.d(LOG_TAG, "HR Extension: profile subscription registered")

            } else {
                Log.e(LOG_TAG, "HR Extension: connection failed")
            }
        }
    }

    private fun updateHeartRateStats() {
        heartRateGraphicDataType.updateHeartRateStats(
            average = averageHeartRate,
            maximum = maximumHeartRate
        )
    }

    private fun resetHeartRateStats() {
        averageHeartRate = null
        maximumHeartRate = null

        heartRateGraphicDataType.updateHeartRateStats(
            average = null,
            maximum = null
        )

        Log.d(LOG_TAG, "HR Extension: HR statistics reset")
    }

    override fun onDestroy() {
        Log.d(LOG_TAG, "HR Extension: stopping extension")

        consumerId?.let {
            karooSystemService?.removeConsumer(it)
        }

        averageConsumerId?.let {
            karooSystemService?.removeConsumer(it)
        }

        maximumConsumerId?.let {
            karooSystemService?.removeConsumer(it)
        }

        profileConsumerId?.let {
            karooSystemService?.removeConsumer(it)
        }

        rideStateConsumerId?.let {
            karooSystemService?.removeConsumer(it)
        }

        karooSystemService?.disconnect()
        karooSystemService = null

        super.onDestroy()
    }

    companion object {
        private const val LOG_TAG = "KarooHR"
    }
}
