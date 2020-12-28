package pt.rfernandes.bubbletweet.model.viewmodels;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import pt.rfernandes.bubbletweet.data.Repository;


public class MainActivityViewModel extends AndroidViewModel {
  private final Application mApplication;
  private final Repository mRepository;

  public MutableLiveData<FirebaseUser> authLiveData = new MutableLiveData<>();
  public MutableLiveData<FirebaseUser> mFirebaseUserMutableLiveData = new MutableLiveData<>();
  private FirebaseAuth mFirebaseAuth;

  /**
   * This méthod creates a new instance of the ViewModel.
   *
   * @param application Use getApplicationContext
   */

  public MainActivityViewModel(@NonNull Application application) {
    super(application);
    this.mApplication = application;
    this.mRepository = Repository.getInstance(application);

    mFirebaseUserMutableLiveData.postValue(mRepository.getUserLoggedIn(application));

  }

  private void setLoggedInUser(FirebaseUser firebaseUser){
    this.mRepository.setUserLoggedIn(firebaseUser, mApplication);
    authLiveData.postValue(firebaseUser);
  }

  public void authTwitter(Activity activity) {
    mFirebaseAuth = FirebaseAuth.getInstance();

    Task<AuthResult> pendingResultTask = mFirebaseAuth.getPendingAuthResult();
    if (pendingResultTask != null) {
      // There's something already here! Finish the sign-in for your user.
      pendingResultTask
          .addOnSuccessListener(
              new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {

                  setLoggedInUser(authResult.getUser());
                }
              })
          .addOnFailureListener(
              new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                  // Handle failure.

                }
              });
    } else {
      OAuthProvider.Builder provider = OAuthProvider.newBuilder("twitter.com");

      mFirebaseAuth.startActivityForSignInWithProvider(activity, provider.build())
          .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
              setLoggedInUser(authResult.getUser());
            }
          })
          .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
          });
    }


  }


}