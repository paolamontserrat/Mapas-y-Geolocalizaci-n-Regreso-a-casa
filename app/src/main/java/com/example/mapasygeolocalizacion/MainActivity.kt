package com.example.mapasygeolocalizacion

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.mapasygeolocalizacion.ui.theme.MapasYGeolocalizacionTheme
import org.osmdroid.config.Configuration

// Clase principal que inicia la aplicacion
class MainActivity : ComponentActivity() {
    // Conectamos el ViewModel que maneja la logica
    private val viewModel: MapsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuracion necesaria para que el mapa cargue correctamente
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        Configuration.getInstance().userAgentValue = packageName
        
        // Hace que la app use toda la pantalla
        enableEdgeToEdge()
        
        // Dibuja la interfaz de usuario
        setContent {
            MapasYGeolocalizacionTheme {
                // Llamamos a la pantalla principal
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
