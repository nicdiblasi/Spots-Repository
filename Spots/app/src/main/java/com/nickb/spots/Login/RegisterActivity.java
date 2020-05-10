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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nickb.spots.Home.HomeActivity;
import com.nickb.spots.R;
import com.nickb.spots.Utils.FirebaseMethods;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    // firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;


    private FirebaseMethods firebaseMethods;
    private Context mContext;
    private EditText mEmail, mPassword, mUsername;
    private String email, password, username;
    private RelativeLayout mLoadingLayout;
    private Button btnRegister;
    private String append;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Log.d(TAG, "onCreate: Started");


        mEmail = findViewById(R.id.input_email);
        mPassword = findViewById(R.id.input_password);
        mUsername = findViewById(R.id.input_username);
        mContext = RegisterActivity.this;
        btnRegister = findViewById(R.id.button_register);
        append = "";

        firebaseMethods = new FirebaseMethods(mContext);


        mLoadingLayout = findViewById(R.id.RelLayoutLoading);
        mLoadingLayout.setVisibility(View.GONE);



        init();
        setupFirebaseAuth();
    }


    private boolean isStringNull(String string) {
        Log.d(TAG, "isStringNull: checking if string is null");

        return string.equals("");
    }



    // ----------------- Firebase ------------------



    private void init() {
        //initialise the register button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Attempting to log in");

                email = mEmail.getText().toString();
                password = mPassword.getText().toString();
                username = mUsername.getText().toString();

                if (isStringNull(email) || isStringNull(password) || isStringNull(username))
                {
                    Toast.makeText(mContext,"You must fill out all the fields", Toast.LENGTH_SHORT).show();
                }
                else {

                    mLoadingLayout.setVisibility(View.VISIBLE);
                    firebaseMethods.registerNewEmail(email, password, username);
                    mLoadingLayout.setVisibility(View.GONE);

                }

            }
        });
    }



    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: ");
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();


        // ses a state listener to see if user logs in or logs out
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (null != user) {
                    // user is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in: " + user.getUid());

                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mLoadingLayout.setVisibility(View.VISIBLE);


                            checkIfUsernameExists(username);

                            mLoadingLayout.setVisibility(View.GONE);

                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    // navigate back to login screen
                    finish();
                } else {
                    // user signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out: ");
                }
            }
        };
    }

    /**
     * Checks if username already exist in database
     * @param username
     */
    private void checkIfUsernameExists(final String username) {
        Log.d(TAG, "checkIfUsernameExists: Checking if " + username + "already exists in database");

        Query query = myRef
                .child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {
                    if (singleSnapshot.exists()) {
                        append = myRef.push().getKey().substring(3,10); // create random string
                        Log.d(TAG, "onDataChange: username already exists, appending random string to the end: " + append);
                    }
                }

                String mUsername = "";
                mUsername = username + append;

                // add new user to database
                firebaseMethods.addNewUser(email,mUsername, mUsername, "");

                // sign out the user because the must verify first
                mAuth.signOut();
                Toast.makeText(mContext, "Signup successful. Sending verification email", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthStateListener);
        // Check if user is signed in (non-null) and update UI accordingly.

    }
}
