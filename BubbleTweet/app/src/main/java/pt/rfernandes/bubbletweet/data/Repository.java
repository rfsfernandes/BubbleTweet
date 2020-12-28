package pt.rfernandes.bubbletweet.data;

import android.app.Application;

import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import pt.rfernandes.bubbletweet.data.local.AppDatabase;
import pt.rfernandes.bubbletweet.data.local.SharedPreferencesManager;
import pt.rfernandes.bubbletweet.data.remote.DataSource;
import pt.rfernandes.bubbletweet.data.remote.TemplateService;
import pt.rfernandes.bubbletweet.model.CustomUser;

public class Repository {

  private static Repository INSTANCE;
  //TODO: implement service and DAO
  private Application application;
  private AppDatabase appDatabase;
  private TemplateService mTemplateService;

  private Repository(Application application) {
    this.mTemplateService = DataSource.getTemplateService();
    this.application = application;
//    appDatabase = AppDatabase.getInstance(application.getApplicationContext());
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

  public CustomUser getUserLoggedIn(Application application){
    CustomUser firebaseUser = SharedPreferencesManager.getInstance(application).getUserLoggedIn();

    return firebaseUser;
  }

  public void setUserLoggedIn(FirebaseUser firebaseUser, Application application){

    SharedPreferencesManager.getInstance(application).setUserLoggedIn(firebaseUser);

  }

}
