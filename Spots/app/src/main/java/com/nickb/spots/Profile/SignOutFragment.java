package com.nickb.spots.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nickb.spots.Login.LoginActivity;
import com.nickb.spots.R;

public class SignOutFragment extends Fragment {

    private static final String TAG = "SignOutFragment";

    private Context mContext;

    private Button btnSignout;
    private RelativeLayout mLoadingLayout;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_signout, container, false);

        mContext = getActivity();
        btnSignout = view.findViewById(R.id.btnConfirmSignout);
        mLoadingLayout = view.findViewById(R.id.RelLayoutLoading);
        mLoadingLayout.setVisibility(View.GONE);
        setupFirebaseAuth();

        btnSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to sign out");
                mLoadingLayout.setVisibility(View.VISIBLE);

                mAuth.signOut();
                getActivity().finish();
            }
        });
        return view;
    }


    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: ");
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // ses a state listener to see if user logs in or logs out
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (null != user) {
                    // user is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in: " + user.getUid());
                    //onSignedInInitialize(user);
                } else {
                    // user signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out: ");
                    Log.d(TAG, "onAuthStateChanged: navigating to login screen");
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK); // clear activity stack so the user cant press back and get into the app
                    startActivity(intent);
                }
            }
        };

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    
}
