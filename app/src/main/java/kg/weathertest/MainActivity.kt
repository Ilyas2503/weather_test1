package kg.weathertest

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.load
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kg.weathertest.api.ApiClient
import kg.weathertest.api.ApiInterface
import kg.weathertest.databinding.ActivityMainBinding
import kg.weathertest.extensions.convertFahrenheitToCelsius
import kg.weathertest.extensions.getCurrentDateTime
import kg.weathertest.extensions.toString
import kg.weathertest.models.WeatherResponse
import retrofit2.Call


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private var fusedLocationProvider: FusedLocationProviderClient? = null
    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = 30000
        fastestInterval = 10000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        maxWaitTime = 60000
    }

    private var temp = ""
    private var cityName = ""
    private var weatherIcon = ""
    private var weatherDesc = ""

    private var currentLocation: Location? = null

    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                currentLocation = locationList.last()
                getWeatherData(
                    currentLocation?.latitude.toString(),
                    currentLocation?.longitude.toString(),
                    Language.ENGLISH.shortName
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission()
        startLocationUpdates()
        setSupportActionBar(binding.toolbar)

        binding.update.setOnClickListener { view ->
            startLocationUpdates()
        }

    }

    private fun getWeatherData(lat: String, lon: String, lang: String) {
        val apiInterface: ApiInterface = ApiClient.getClient().create(ApiInterface::class.java)
        val call: Call<WeatherResponse> = apiInterface.getWeatherData(lat, lon, lang)
        call.enqueue(object : retrofit2.Callback<WeatherResponse> {
            override fun onResponse(
                call: Call<WeatherResponse?>?, response: retrofit2.Response<WeatherResponse>
            ) {
                temp = getString(
                    R.string.temperature_celcius,
                    response.body()?.main?.temp?.convertFahrenheitToCelsius()
                )
                cityName = response.body()?.name ?: ""
                weatherIcon =
                    "https://openweathermap.org/img/wn/${response.body()?.weather?.last()?.icon}@4x.png"
                weatherDesc = response.body()?.weather?.last()?.description ?: ""
                binding.cityValue.text = cityName
                binding.lastUpdatedValue.text = getLastUpdated()
                binding.temperatureValue.text = temp

                binding.weatherIcon.load(weatherIcon)
                binding.weatherDesc.text = weatherDesc
                updateAppWidget()

            }

            override fun onFailure(call: Call<WeatherResponse?>?, t: Throwable?) {}
        })
    }

    fun getLastUpdated(): String {
        val date = getCurrentDateTime()
        return date.toString("yyyy/MM/dd HH:mm")
    }

    private fun updateAppWidget() {
        val intent = Intent(this, WeatherWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
            ComponentName(
                applicationContext, WeatherWidgetProvider::class.java
            )
        )
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        intent.putExtra("temp", temp)
        intent.putExtra("city", cityName)
        intent.putExtra("lastUpdated", getLastUpdated())
        intent.putExtra("weatherIcon", weatherIcon)
        intent.putExtra("weatherDesc", weatherDesc)
        sendBroadcast(intent)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_language_en -> {
                switchLanguage(Language.ENGLISH)
                getWeatherData(
                    currentLocation?.latitude.toString(),
                    currentLocation?.longitude.toString(),
                    Language.ENGLISH.shortName
                )
                true
            }
            R.id.action_language_kg -> {
                switchLanguage(Language.KYRGYZ)
                getWeatherData(
                    currentLocation?.latitude.toString(),
                    currentLocation?.longitude.toString(),
                    Language.KYRGYZ.shortName
                )
                true
            }
            R.id.action_language_ru -> {
                switchLanguage(Language.RUSSIAN)
                getWeatherData(
                    currentLocation?.latitude.toString(),
                    currentLocation?.longitude.toString(),
                    Language.RUSSIAN.shortName
                )
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    }

    private fun switchLanguage(language: Language) {
        when (language) {
            Language.ENGLISH -> {
                binding.apply {
                    temperatureLabel.text = getString(R.string.temperature_en)
                    cityLabel.text = getString(R.string.city_en)
                    lastUpdatedLabel.text = getString(R.string.last_updated_en)
                }
            }
            Language.KYRGYZ -> {
                binding.apply {
                    temperatureLabel.text = getString(R.string.temperature_kg)
                    cityLabel.text = getString(R.string.city_kg)
                    lastUpdatedLabel.text = getString(R.string.last_updated_kg)

                }
            }
            Language.RUSSIAN -> {
                binding.apply {
                    temperatureLabel.text = getString(R.string.temperature_ru)
                    cityLabel.text = getString(R.string.city_ru)
                    lastUpdatedLabel.text = getString(R.string.last_updated_ru)

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProvider?.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
        }
    }

    private fun stopLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProvider?.removeLocationUpdates(locationCallback)
        }
    }


    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                AlertDialog.Builder(this).setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        requestLocationPermission()
                    }.create().show()
            } else {
                requestLocationPermission()
            }
        }
    }


    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            ), MY_PERMISSIONS_REQUEST_LOCATION
        )
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }
}


