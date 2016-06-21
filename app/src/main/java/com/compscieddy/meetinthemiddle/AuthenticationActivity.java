package com.compscieddy.meetinthemiddle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.compscieddy.eddie_utils.Etils;
import com.compscieddy.eddie_utils.Lawg;
import com.compscieddy.meetinthemiddle.model.User;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by ambar on 6/15/16.
 */
public class AuthenticationActivity extends Activity {

  private static final Lawg lawg = Lawg.newInstance(AuthenticationActivity.class.getSimpleName());
  private static final int RC_SIGN_IN = 100;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //Checking if the user is already logged in
    FirebaseAuth auth = FirebaseAuth.getInstance();
    if (auth.getCurrentUser() != null) {
      Intent intent = new Intent(this, HomeActivity.class);
      startActivity(intent);
      finish();
    }

    //User isn't logged in, so check if he has working internet
    if (isOnline()) {
      startActivityForResult(
          AuthUI.getInstance().createSignInIntentBuilder()
              .setLogo(R.mipmap.ic_launcher)
              .setProviders(AuthUI.EMAIL_PROVIDER,
                  AuthUI.FACEBOOK_PROVIDER,
                  AuthUI.GOOGLE_PROVIDER)
              .build(),
          RC_SIGN_IN);
      finish();
    } else {
      // TODO Display DialogFragment saying net is not available
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == RC_SIGN_IN) {
      GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
      if (resultCode == RESULT_OK) {
        // User creation is occurring! Tada! Welcome to being trapped to the most addictive map app ever
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        User.createUser(database, firebaseUser);

        startActivity(new Intent(this, HomeActivity.class));
        finish();
      } else {
        // user is not signed in. Maybe just wait for the user to press
        // "sign in" again, or show a message
        lawg.e("FAIL 1");
        lawg.e(" requestCode: " + requestCode + " resultCode: " + resultCode);
        lawg.e(" result: " + result);
        Etils.showToast(AuthenticationActivity.this, "Failed to Sign-In");
      }
    } else {
      lawg.e("FAIL 2");
      lawg.e(" requestCode: " + requestCode + " resultCode: " + resultCode);
    }
  }

  private boolean isOnline() {
    ConnectivityManager connMgr = (ConnectivityManager)
        getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    return (networkInfo != null && networkInfo.isConnected());
  }

  private boolean isFirstRun(){
    final String PREF_VERSION_CODE_KEY = "version_code";
    final int DOESNT_EXIST = -1;

    // Get current version code
    int currentVersionCode = BuildConfig.VERSION_CODE;

    // Get saved version code
    SharedPreferences prefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

    // Check for first run or upgrade
    if (currentVersionCode == savedVersionCode) {

      // This is just a normal run
      return false;

    } else if (savedVersionCode == DOESNT_EXIST) {

      // This is a new install (or the user cleared the shared preferences)
      return true;

    } else if (currentVersionCode > savedVersionCode) {

      //This is an upgrade
       return false;
    }

    // Update the shared preferences with the current version code
    prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).commit();
    return false;

  }

}
