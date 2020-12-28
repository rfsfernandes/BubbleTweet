package pt.rfernandes.bubbletweet.data.remote;

import pt.rfernandes.bubbletweet.model.TweetResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface RequestService {
  /**
   * User, for example:
   *
   * @POST("/api/v1/login") Call<ResponseObject> login(@Body User user, @Header("Language") String language);
   * <p>
   * To login using a given url.
   */
  @FormUrlEncoded
  @POST("statuses/update.json")
  Call<TweetResponse> sendTweet(
                                @Field("oauth_version") String oauth_version,
                                @Field("status") String status);

}