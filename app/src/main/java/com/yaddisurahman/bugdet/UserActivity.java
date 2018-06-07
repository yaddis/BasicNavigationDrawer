package com.yaddisurahman.bugdet;

import android.app.ActionBar;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class UserActivity extends AppCompatActivity {

  SignInButton signInButton;
  TextView userInfo;
  Button signOutButton;
  GoogleSignInClient mGoogleSignInClient;
  FirebaseAuth firebaseAuth;
  private static final int RC_SIGN_IN = 9001;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user);
    getSupportActionBar().setTitle("Login");
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(getString(R.string.default_web_client_id))
        .requestEmail()
        .build();

    mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    firebaseAuth = FirebaseAuth.getInstance();

    signInButton = findViewById(R.id.user_google_sign_in);
    userInfo = findViewById(R.id.user_userinfo);
    signInButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
      }
    });

    signOutButton = findViewById(R.id.user_logout);
    signOutButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        signOut();
      }
    });

  }

  private void signOut() {
    firebaseAuth.signOut();

    // Google sign out
    mGoogleSignInClient.signOut().addOnCompleteListener(this,
        new OnCompleteListener<Void>() {
          @Override
          public void onComplete(@NonNull Task<Void> task) {
            updateUI(null);
          }
        });

  }

  @Override
  protected void onStart() {
    super.onStart();
    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
    updateUI(currentUser);
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
    if (requestCode == RC_SIGN_IN) {
      Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
      try {
        // Google Sign In was successful, authenticate with Firebase
        GoogleSignInAccount account = task.getResult(ApiException.class);
        firebaseAuthWithGoogle(account);
      } catch (ApiException e) {
        // Google Sign In failed, update UI appropriately
        Log.e("Google Firebase", "Google sign in failed", e);
        // [START_EXCLUDE]
        updateUI(null);
        // [END_EXCLUDE]
      }
    }
  }

  private void updateUI(FirebaseUser currentUser) {
    if (currentUser != null) {

      userInfo.setText(getString(R.string.google_status_fmt, currentUser.getEmail()));

      findViewById(R.id.user_google_sign_in).setVisibility(View.INVISIBLE);
      findViewById(R.id.user_logout).setVisibility(View.VISIBLE);
    } else {
      userInfo.setText(null);

      findViewById(R.id.user_google_sign_in).setVisibility(View.VISIBLE);
      findViewById(R.id.user_logout).setVisibility(View.INVISIBLE);
    }
  }

  private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
    Log.d("google", credential.toString());
    firebaseAuth.signInWithCredential(credential)
        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
          @Override
          public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful()) {
              // Sign in success, update UI with the signed-in user's information
              Log.d("Google Firebase", "signInWithCredential:success");
              FirebaseUser user = firebaseAuth.getCurrentUser();
              updateUI(user);
            } else {
              // If sign in fails, display a message to the user.
              Log.w("Google Firebase", "signInWithCredential:failure", task.getException());
              updateUI(null);
            }

          }
        });
  }
}
