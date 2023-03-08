package kg.weathertest.extensions

import java.text.DecimalFormat

fun Double.convertFahrenheitToCelsius(): String {
    val df = DecimalFormat("#.#")
    return df.format((this - 32) * 5 / 9)
}
