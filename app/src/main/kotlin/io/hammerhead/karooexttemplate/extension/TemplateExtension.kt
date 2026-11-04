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

        Log.d(TAG, "HR Extension: zagon razširitve")

        val service = KarooSystemService(this)
        karooSystemService = service

        service.connect { connected ->
            if (connected) {
                Log.d(TAG, "HR Extension: povezava uspešna")

                // Spremljanje stanja aktivnosti.
                rideStateConsumerId = service.addConsumer<RideState> { state ->
                    when (state) {
                        is RideState.Idle -> {
                            Log.d(TAG, "HR Extension: aktivnost ni aktivna")
                            resetHeartRateStats()
                        }

                        is RideState.Recording -> {
                            Log.d(TAG, "HR Extension: aktivnost se snema")
                        }

                        is RideState.Paused -> {
                            Log.d(TAG, "HR Extension: aktivnost je na pavzi")
                        }
                    }
                }

                // Spremljanje trenutnega srčnega utripa.
                consumerId = service.addConsumer<OnStreamState>(
                    OnStreamState.StartStreaming(DataType.Type.HEART_RATE)
                ) { event ->
                    when (val state = event.state) {
                        is StreamState.Streaming -> {
                            val hr = state.dataPoint.values[
                                DataType.Field.HEART_RATE
                            ]

                            if (hr != null) {
                                Log.d(TAG, "HR Extension: prejet HR = $hr")
                                heartRateGraphicDataType.updateHeartRate(hr)
                            }
                        }

                        StreamState.Searching -> {
                            Log.d(TAG, "HR Extension: iskanje HR")
                        }

                        StreamState.NotAvailable -> {
                            Log.d(TAG, "HR Extension: HR ni na voljo")
                        }

                        StreamState.Idle -> {
                            Log.d(TAG, "HR Extension: tok HR je nedejaven")
                        }
                    }
                }

                // AVG HR neposredno iz Karoo SDK.
                averageConsumerId = service.addConsumer<OnStreamState>(
                    OnStreamState.StartStreaming(DataType.Type.AVERAGE_HR)
                ) { event ->
                    when (val state = event.state) {
                        is StreamState.Streaming -> {
                            averageHeartRate = state.dataPoint.values[
                                DataType.Field.AVG_HR
                            ]

                            Log.d(
                                TAG,
                                "HR Extension: Karoo AVG HR = $averageHeartRate"
                            )

                            updateHeartRateStats()
                        }

                        StreamState.Searching -> {
                            Log.d(TAG, "HR Extension: iskanje AVG HR")
                        }

                        StreamState.NotAvailable -> {
                            Log.d(TAG, "HR Extension: AVG HR ni na voljo")
                        }

                        StreamState.Idle -> {
                            Log.d(TAG, "HR Extension: tok AVG HR je nedejaven")
                        }
                    }
                }

                // MAX HR neposredno iz Karoo SDK.
                maximumConsumerId = service.addConsumer<OnStreamState>(
                    OnStreamState.StartStreaming(DataType.Type.MAX_HR)
                ) { event ->
                    when (val state = event.state) {
                        is StreamState.Streaming -> {
                            maximumHeartRate = state.dataPoint.values[
                                DataType.Field.MAX_HR
                            ]

                            Log.d(
                                TAG,
                                "HR Extension: Karoo MAX HR = $maximumHeartRate"
                            )

                            updateHeartRateStats()
                        }

                        StreamState.Searching -> {
                            Log.d(TAG, "HR Extension: iskanje MAX HR")
                        }

                        StreamState.NotAvailable -> {
                            Log.d(TAG, "HR Extension: MAX HR ni na voljo")
                        }

                        StreamState.Idle -> {
                            Log.d(TAG, "HR Extension: tok MAX HR je nedejaven")
                        }
                    }
                }

                Log.d(TAG, "HR Extension: naročnini AVG HR in MAX HR registrirani")

                // Spremljanje uporabniškega profila in HR-con.
                profileConsumerId = service.addConsumer<UserProfile> { profile ->
                    Log.d(TAG, "HR Extension: prejet uporabniški profil")
                    Log.d(TAG, "HR Extension: max HR = ${profile.maxHr}")
                    Log.d(TAG, "HR Extension: resting HR = ${profile.restingHr}")

                    profile.heartRateZones.forEachIndexed { index, zone ->
                        Log.d(
                            TAG,
                            "HR Extension: Z${index + 1} = ${zone.min}–${zone.max} BPM"
                        )
                    }

                    heartRateGraphicDataType.updateZones(
                        profile.heartRateZones
                    )
                }

                Log.d(TAG, "HR Extension: naročnina profila registrirana")

            } else {
                Log.e(TAG, "HR Extension: povezava ni uspela")
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

        Log.d(TAG, "HR Extension: statistika HR ponastavljena")
    }

    override fun onDestroy() {
        Log.d(TAG, "HR Extension: zaustavitev")

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
        private const val TAG = "KarooHR"
    }
}