package com.example.weatherapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Toast
import com.example.weatherapp.databinding.ActivityMainBinding
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        searchView()
        fetchWeatherData("Faisalabad")
    }

    private fun searchView() {
        val searchView = binding.searchView2
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query.isNullOrEmpty()) {
                    Toast.makeText(this@MainActivity, "City name cannot be empty.", Toast.LENGTH_SHORT).show()
                } else {
                    fetchWeatherData(query.trim())
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun fetchWeatherData(cityName: String) {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection.", Toast.LENGTH_LONG).show()
            return
        }

        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build()
            .create(APiInterface::class.java)

        val response = retrofit.getWeatherData(cityName, "f4292b8ac2b772f749b297de09296f61", "metric")
        response.enqueue(object : Callback<weatherApp> {
            override fun onResponse(call: Call<weatherApp>, response: Response<weatherApp>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    val temperature = responseBody.main.temp.toString()
                    val humidity = responseBody.main.humidity
                    val wind = responseBody.wind.speed
                    val sunRise = responseBody.sys.sunrise
                    val sunset = responseBody.sys.sunset
                    val sealevel = responseBody.main.pressure
                    val conditions = responseBody.weather.firstOrNull()?.main ?: "Unknown"
                    val maxTemp = responseBody.main.temp_max
                    val minTemp = responseBody.main.temp_min

                    binding.temp.text = "$temperature °C"
                    binding.weather.text = conditions
                    binding.conditiontext1.text = conditions
                    binding.maxtemp.text = "Max Temp: $maxTemp °C"
                    binding.minitemp.text = "Min Temp: $minTemp °C"
                    binding.humaditytext1.text = "$humidity %"
                    binding.windimgtext1.text = "$wind m/s"
                    binding.seatext1.text = "$sealevel hPa"
                    binding.sunsettext.text = formatTime(sunset.toLong())
                    binding.sunrisetext1.text = formatTime(sunRise.toLong())
                    binding.city.text = cityName
                } else {
                    Toast.makeText(this@MainActivity, "Please enter a valid city name.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<weatherApp>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun formatTime(timestamp: Long): String {
        val date = Date(timestamp * 1000)
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(date)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities != null &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
