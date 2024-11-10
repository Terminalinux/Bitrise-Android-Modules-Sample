package io.bitrise.sample.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.temporal.ChronoUnit

class MainActivity : AppCompatActivity() {

    private lateinit var healthConnectClient: HealthConnectClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация HealthConnectClient
        healthConnectClient = HealthConnectClient.getOrCreate(this)

        // Запрос разрешений
        requestPermissions()
    }

    private fun requestPermissions() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.all { it.value }) {
                // Все разрешения получены, можно продолжать
                readSteps()
            } else {
                // Разрешения не получены, показываем сообщение пользователю
                showPermissionDeniedMessage()
            }
        }

        val permissions = arrayOf(
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        requestPermissionLauncher.launch(permissions)
    }

    private fun readSteps() {
        val timeRangeFilter = TimeRangeFilter.between(
            Instant.now().minus(1, ChronoUnit.DAYS),
            Instant.now()
        )

        val request = ReadRecordsRequest(
            StepsRecord::class,
            timeRangeFilter
        )

        healthConnectClient.readRecords(request).thenAccept { response ->
            val steps = response.records.sumOf { it.count }
            // Добавляем шаги в Health Connect
            addSteps(steps)
        }
    }

    private fun addSteps(steps: Long) {
        val stepsRecord = StepsRecord(
            count = steps,
            startTime = Instant.now().minus(1, ChronoUnit.DAYS),
            endTime = Instant.now()
        )

        healthConnectClient.insertRecords(listOf(stepsRecord)).thenAccept {
            // Шаги добавлены успешно
        }
    }

    private fun showPermissionDeniedMessage() {
        // Показываем сообщение о том, что разрешения не получены
    }
}
