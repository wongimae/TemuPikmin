package com.example.temupikmin

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.temupikmin.service.LocationTrackingService
import com.example.temupikmin.data.AppDatabase
import com.example.temupikmin.ui.theme.TemuPikminTheme
import com.example.temupikmin.work.EXTRA_ENTRY_ID
import com.example.temupikmin.work.scheduleNextCheckIn
import kotlinx.coroutines.launch

import org.osmdroid.config.Configuration
//import org.osmdroid.library.BuildConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = "Temupikmin-MoodTracker/1.0"

        val pendingEntryId = intent.getLongExtra(EXTRA_ENTRY_ID, -1L)

        setContent {
            TemuPikminTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (pendingEntryId != -1L) {
                        MoodPickerScreen(entryId = pendingEntryId)
                    } else {
                        PermissionScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun MoodPickerScreen(entryId: Long) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var submitted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        if (submitted) {
            Text("record logged! ( ˶ˆᗜˆ˵ )", style = MaterialTheme.typography.headlineSmall)
        } else {
            Text("(づ •. •)? how are you feeling?", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("sad" to "😞 Sad", "neutral" to "😐 Neutral", "happy" to "😊 Happy")
                    .forEach { (mood, label) ->
                        Button(onClick = {
                            scope.launch {
                                val db = AppDatabase.getInstance(context.applicationContext)
                                val entry = db.moodEntryDao().getById(entryId)
                                if (entry != null) {
                                    db.moodEntryDao().update(entry.copy(mood = mood))
                                }
                                submitted = true
                            }
                        }) {
                            Text(label)
                        }
                    }
            }
        }
    }
}

@Composable
fun PermissionScreen() {
    var foregroundGranted by remember { mutableStateOf(false) }
    var backgroundGranted by remember { mutableStateOf(false) }
    var tracking by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current

    val foregroundLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        foregroundGranted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    val backgroundLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        backgroundGranted = granted
        if (granted) {
            scheduleNextCheckIn(context)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("GPS Mood Tracker (๑´>᎑<)~*", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        if (!foregroundGranted) {
            Button(onClick = {
                val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                }
                foregroundLauncher.launch(permissions.toTypedArray())
            }) {
                Text("Grant location & notification access")
            }
        } else if (!backgroundGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Button(onClick = {
                backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }) {
                Text("Allow background location")
            }
        } else {
            var showMap by remember { mutableStateOf(false) }

            if (showMap) {
                MapScreen(onBack = { showMap = false})
            } else {
                Text("ᕙ(  •̀ ᗜ •́  )ᕗ All set — 2-hour mood check-ins are scheduled.")
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    if (tracking) {
                        LocationTrackingService.stop(context)
                    } else {
                        LocationTrackingService.start(context)
                    }
                    tracking = !tracking
                }) {
                    Text(if (tracking) "Stop tracking (ᵕ—ᴗ—)" else "Start tracking my path ⸜(｡˃ ᵕ ˂ )⸝♡")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { showMap = true }) {
                    Text("View my path ヽ(°〇°)ﾉ")
                }
            }
        }
    }
}
