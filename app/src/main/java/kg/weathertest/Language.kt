package kg.weathertest

enum class Language(val shortName: String) {
    ENGLISH("en"),
    RUSSIAN("ru"),
    KYRGYZ("ru")
    // ru shortname was given to KYRGYZ enum 'cause there's not available kyrgyz
    // language in a list of supported languages in openweathermap.org
    // See https://openweathermap.org/current#multi

}