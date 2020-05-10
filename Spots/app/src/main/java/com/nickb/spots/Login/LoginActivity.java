package com.nickb.spots.Login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nickb.spots.Home.HomeActivity;
import com.nickb.spots.R;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private Context mContext;
    private EditText mEmail, mPassword;


    private RelativeLayout mLoadingLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate: Started");

        mEmail = findViewById(R.id.input_email);
        mPassword = findViewById(R.id.input_password);
        mContext = LoginActivity.this;


        mLoadingLayout = findViewById(R.id.RelLayoutLoading);
        mLoadingLayout.setVisibility(View.GONE);


        setupFirebaseAuth();
        init();
    }


    private boolean isStringNull(String string) {
        Log.d(TAG, "isStringNull: checking if string is null");

        if(string == "")
        {
            return true;
        } else {
            return false;
        }
    }

    // ----------------- Firebase ------------------
    private void init() {
        //initialise the login button
        Button btnLogin = findViewById(R.id.button_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Attempting to log in");

                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                if (isStringNull(email) && isStringNull(password))
                {
                    Toast.makeText(mContext,"You must fill out all the fields", Toast.LENGTH_SHORT).show();
                }
                else {
                    mLoadingLayout.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithEmail:success");

                                        FirebaseUser user = mAuth.getCurrentUser();
                                        try {
                                            if(user.isEmailVerified()) {
                                                Log.d(TAG, "onComplete: successs, email is verified");
                                                Intent intent = new Intent(mContext, HomeActivity.class);
                                                startActivity(intent);
                                            } else {
                                                Toast.makeText(mContext, "Email is not verified \n check your inbox", Toast.LENGTH_SHORT).show();
                                                mLoadingLayout.setVisibility(View.GONE);

                                            }

                                        } catch (NullPointerException e) {
                                            Log.e(TAG, "onComplete: NullPointerException" + e.getMessage());
                                        }


                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                        mLoadingLayout.setVisibility(View.GONE);

                                        Toast.makeText(mContext, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    }

                                    // ...
                                }
                            });
                }

            }
        });

        // setup sign up btn to navigate to register activity
        TextView mSignUp = findViewById(R.id.button_register);
        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to the register screen");
                Intent intent = new Intent(mContext, RegisterActivity.class);
                startActivity(intent);
            }
        });


        // navigate to the home screen if the user is logged in
        if(mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(mContext, HomeActivity.class);
            startActivity(intent);

            // call finish because there is no need for the login screen
            finish();

        }
    }



    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: ");
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // sets a state listener to see if user logs in or logs out
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (null != user) {
                    // user is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in: " + user.getUid());
                } else {
                    // user signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out: ");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthStateListener);
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }
}
