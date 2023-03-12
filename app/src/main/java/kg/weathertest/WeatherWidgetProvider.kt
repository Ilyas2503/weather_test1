package kg.weathertest

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews


class WeatherWidgetProvider : AppWidgetProvider() {
    var temp: String = ""
    var city: String = ""
    var lastUpdated: String = ""
    var weatherIcon: String = ""
    var weatherDesc: String = ""

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context,
                appWidgetManager,
                appWidgetId,
                temp,
                city,
                lastUpdated,
                weatherIcon,
                weatherDesc
            )
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val extras = intent?.extras

        extras?.let {
            temp = it.getString("temp") ?: ""
            city = it.getString("city") ?: ""
            lastUpdated = it.getString("lastUpdated") ?: ""
            weatherIcon = it.getString("weatherIcon") ?: ""
            weatherDesc = it.getString("weatherDesc") ?: ""
        }
        super.onReceive(context, intent)
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    temp: String,
    city: String,
    lastUpdated: String,
    weatherIcon: String,
    weatherDesc: String,
) {
    val views = RemoteViews(context.packageName, R.layout.weather_widget)

    if (temp.isNotEmpty()) {
        views.setTextViewText(R.id.placeholder, "")
    }
    views.setTextViewText(R.id.temperature_value, temp)
    views.setTextViewText(R.id.city_value, city)
    views.setTextViewText(R.id.last_updated_value, lastUpdated)
    views.setTextViewText(R.id.weather_desc, weatherDesc)


    appWidgetManager.updateAppWidget(appWidgetId, views)
}