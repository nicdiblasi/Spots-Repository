package com.nickb.spots.Home;

import android.content.Context;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nickb.spots.Login.LoginActivity;
import com.nickb.spots.R;
import com.nickb.spots.Utils.BottomNavigationViewHelper;
import com.nickb.spots.Utils.SectionsPagerAdapter;
import com.nickb.spots.Utils.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;



// use forward reverse geolocation

public class HomeActivity extends AppCompatActivity {


    private static final int ACTIVITY_NUM = 0;
    private static final String TAG = "HomeActivity";
    private Context mContext = HomeActivity.this;



    // firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    private ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        Log.d(TAG, "onCreate: Starting.");

        mViewPager = findViewById(R.id.viewpager_container);

        setupFirebaseAuth();
        initImageLoader();
        setupBottomNavigationView();
        setupViewPager();
    }





    // method that initialises the image loader, assigning the context and using the helper method
    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }


    /**
     * Responsible for adding the fragments into the top navbar allowing for the toggle between
     * mapview and listview
     */
    private void setupViewPager() {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ListViewFragment());
        adapter.addFragment(new SearchFragment());
        adapter.addFragment(new MapViewFragment());
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


        tabLayout.getTabAt(0).setIcon(R.drawable.ic_list);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_search_white);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_map);


    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView () {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");

        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext,this, bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);

    }


    // ----------------- Firebase ------------------


    private void checkCurrentUser(FirebaseUser user) {
        Log.d(TAG, "checkCurrentUser: checking if user is logged in");
        if( user == null ) {
            // not logged in navigate to login
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
        }
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
                checkCurrentUser(user);
                if (null != user) {
                    // user is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in: " + user.getUid());
                    //onSignedInInitialize(user);
                } else {
                    // user signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out: ");

                    //onSignedOutCleanup();
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
        checkCurrentUser(currentUser);
    }



}
