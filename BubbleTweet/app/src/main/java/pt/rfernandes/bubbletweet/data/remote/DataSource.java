package pt.rfernandes.bubbletweet.data.remote;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import se.akerfeldt.okhttp.signpost.OkHttpOAuthConsumer;
import se.akerfeldt.okhttp.signpost.SigningInterceptor;


public class DataSource {

  private static final String BASE_URL = "https://api.twitter.com/1.1/";

  private static RequestService sRequestService;

  public static RequestService getRequestService(String consumer_key, String consumer_secret,
                                                 String access_token, String token_secret) {

    OkHttpOAuthConsumer consumer = new OkHttpOAuthConsumer(consumer_key, consumer_secret);
    consumer.setTokenWithSecret(access_token, token_secret);

    OkHttpClient client = new OkHttpClient.Builder()
        .addInterceptor(new SigningInterceptor(consumer))
        .build();

    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build();

    if (sRequestService == null) {
      sRequestService = retrofit.create(RequestService.class);
    }
    return sRequestService;
  }

}