package com.example.mapasygeolocalizacion

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.mapasygeolocalizacion.ui.theme.MapasYGeolocalizacionTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    private val viewModel: MapsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        Configuration.getInstance().userAgentValue = packageName
        
        enableEdgeToEdge()
        setContent {
            MapasYGeolocalizacionTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
