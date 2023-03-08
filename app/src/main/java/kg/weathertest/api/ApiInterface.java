package kg.weathertest.api;

import kg.weathertest.models.WeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("weather?appid=6fe4959bfb3d9c45412422f18a7cadd5&units=imperial")
    Call<WeatherResponse> getWeatherData(@Query("lat") String lat, @Query("lon") String lon,
                                         @Query("lang") String lang);
}