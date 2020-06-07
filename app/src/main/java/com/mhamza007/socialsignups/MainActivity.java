package com.mhamza007.socialsignups;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.mhamza007.socialsignups.Instagram.InstagramApp;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.Arrays;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    //fb
    private static final String EMAIL = "email";
    CallbackManager callbackManager;
    Button fbBtn;
    LoginButton loginButton;

    //google
    GoogleSignInClient mGoogleSignInClient;
    private static final String DEFAULT_SIGN_IN = "1";
    private static final int RC_SIGN_IN = 1;

    //titter
    TwitterLoginButton twitterLoginButton;

    //insta
    Button insta_connect;
    InstagramApp mApp;
    private static final String CLIENT_ID = "33df059c82f0495295d27a054689076f";
    private static final String CLIENT_SECRET = "e8b997139db44485bfa46d5a6dcd6511";
    private static final String CALLBACKK_URL = "https://ebmacs.com/";

//    private HashMap<String, String> userInfoHashmap = new HashMap<String, String>();
    private String name;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == InstagramApp.WHAT_FINALIZE){
                name = mApp.getUserName();
            } else if (msg.what == InstagramApp.WHAT_FINALIZE) {
                Toast.makeText(MainActivity.this, "Check your network.", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Twitter.initialize(this);
        setContentView(R.layout.activity_main);

        //fb start

        fbInitUI();
        FacebookSdk.sdkInitialize(getApplicationContext());
        loginButton.setReadPermissions(Arrays.asList(EMAIL));

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //send user to other activity giving success

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Logged in with Facebook")
                        .setMessage("Logged in as : "+ EMAIL)
                        .setIcon(R.drawable.ic_facebook)
                        .setNeutralButton("Log out", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog1 = alertDialog.create();
                alertDialog1.show();
            }

            @Override
            public void onCancel() {
                //fb login cancelled
                Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                //fb error
                Toast.makeText(MainActivity.this, "Error : "+ error, Toast.LENGTH_LONG).show();

            }
        });

        //fb end


        //google start

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        //google end


        //twitter start

        twitterLoginButton = findViewById(R.id.twitter_button);
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // Do something with result, which provides a TwitterSession for making API calls
                TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                TwitterAuthToken authToken = session.getAuthToken();
                String token = authToken.token;
                String secret = authToken.secret;

                twitterLogin(session);
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(MainActivity.this, "Twitter Login Failed " + exception, Toast.LENGTH_LONG).show();
            }
        });

        //twitter end



        //insta

        mApp = new InstagramApp(this, CLIENT_ID, CLIENT_SECRET, CALLBACKK_URL);
        mApp.setListener(new InstagramApp.OAuthAuthenticationListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Instagram login Success", Toast.LENGTH_LONG).show();
                mApp.fetchUserName();
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(MainActivity.this, "Instagram login failed", Toast.LENGTH_LONG).show();
            }
        });

        setWidgetReference();
        bindEventHandlers();

        if (mApp.hasAccessToken()) {
            Toast.makeText(MainActivity.this, "Logged in from Instagram", Toast.LENGTH_LONG).show();
            mApp.fetchUserName();
        }
    } // end onCreate()

    private void setWidgetReference() {
        insta_connect = findViewById(R.id.insta_connect);
    }

    private void bindEventHandlers() {
        insta_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectUser();
            }
        });
    }

    public void connectUser(){
        if (mApp.hasAccessToken()){
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Disconnect?")
                    .setCancelable(false)
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mApp.resetAccessToken();
                        }
                    })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            mApp.authorize();
        }
    }


    //end insta


    private void twitterLogin(TwitterSession session) {
        String username = session.getUserName();
        Intent intent = new Intent(MainActivity.this, SignedIn.class);
        intent.putExtra("username", username);
        startActivity(intent);
        Toast.makeText(MainActivity.this, "Twitter Login Success", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

    }


    private void fbInitUI() {
        callbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList(EMAIL));
    }

    public void fbClick(View view){
        fbBtn = findViewById(R.id.fb_btn);
        if (view == fbBtn){
            loginButton.performClick();

            facebookLogin();
        }
    }

    private void facebookLogin() {

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                //send user to other activity giving success

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Logged in with Facebook")
                        .setMessage("Logged in as : "+ EMAIL)
                        .setIcon(R.drawable.ic_facebook)
                        .setNeutralButton("Log out", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog1 = alertDialog.create();
                alertDialog1.show();
            }

            @Override
            public void onCancel() {
                //fb login cancelled
                Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                //fb error
                Toast.makeText(MainActivity.this, "Error : " + error, Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //fb
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        //google
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

        //twitter
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            Toast.makeText(MainActivity.this, "Google Signin Successful", Toast.LENGTH_LONG).show();
//            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Google Signin", "signInResult:failed code=" + e.getStatusCode());
//            updateUI(null);
        }
    }
}
