package pt.rfernandes.bubbletweet.data;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import androidx.annotation.NonNull;
import pt.rfernandes.bubbletweet.R;
import pt.rfernandes.bubbletweet.custom.Constants;
import pt.rfernandes.bubbletweet.data.local.AppDatabase;
import pt.rfernandes.bubbletweet.data.local.DBCallBack;
import pt.rfernandes.bubbletweet.data.remote.DataSource;
import pt.rfernandes.bubbletweet.data.remote.RequestCallBack;
import pt.rfernandes.bubbletweet.data.remote.RequestService;
import pt.rfernandes.bubbletweet.model.CustomUser;
import pt.rfernandes.bubbletweet.model.TweetBody;
import pt.rfernandes.bubbletweet.model.TweetErrors;
import pt.rfernandes.bubbletweet.model.TweetResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Repository {

  private static Repository INSTANCE;
  //TODO: implement service and DAO
  private Application application;
  private AppDatabase appDatabase;
  private RequestService mRequestService;

  private Repository(Application application) {

    this.application = application;
    this.appDatabase = AppDatabase.getInstance(application.getApplicationContext());
  }

  public static Repository getInstance(@NonNull Application application) {
    if (INSTANCE == null) {
      synchronized (Repository.class) {
        if (INSTANCE == null) {
          INSTANCE = new Repository(application);
        }
      }
    }
    return INSTANCE;
  }

  public void getUserLoggedIn(DBCallBack<CustomUser> callBack) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        if (appDatabase.getTemplateDAO().getUser() != null && appDatabase.getTemplateDAO().getUser().size() > 0) {
          callBack.returnDB(appDatabase.getTemplateDAO().getUser().get(0));
        } else {
          callBack.returnDB(null);
        }

      }
    }).start();

  }

  public void setUserLoggedIn(CustomUser customUser) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        appDatabase.getTemplateDAO().insertUser(customUser);
      }
    }).start();

  }

  public void sendTweet(TweetBody tweetBody, RequestCallBack requestCallBack) {
    this.mRequestService = DataSource.getRequestService(tweetBody.getOauth_consumer_key(),
        Constants.SECRET,
        tweetBody.getOauth_token(), tweetBody.getSecret());

    Call<TweetResponse> call = mRequestService.sendTweet(tweetBody.getOauth_version(),
        tweetBody.getStatus());

    call.enqueue(new Callback<TweetResponse>() {
      @Override
      public void onResponse(Call<TweetResponse> call, Response<TweetResponse> response) {
        if (response.isSuccessful()) {
          requestCallBack.success();
        } else {

          StringBuilder errors = new StringBuilder();
          if (response.errorBody() != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<TweetResponse>() {}.getType();
            TweetResponse errorResponse = gson.fromJson(response.errorBody().charStream(),type);
            for (TweetErrors e :
                errorResponse.getErrors()) {
              errors.append(e.getMessage());
            }
          }

          requestCallBack.failure(errors.toString());

        }
      }

      @Override
      public void onFailure(Call<TweetResponse> call, Throwable t) {
        requestCallBack.failure(t.getLocalizedMessage());
      }
    });

  }

}
