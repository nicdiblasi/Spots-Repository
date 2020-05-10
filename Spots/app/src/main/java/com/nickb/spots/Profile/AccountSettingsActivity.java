package com.nickb.spots.Profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nickb.spots.R;
import com.nickb.spots.Utils.BottomNavigationViewHelper;
import com.nickb.spots.Utils.FirebaseMethods;
import com.nickb.spots.Utils.SectionsStatePagerAdapter;

import java.util.ArrayList;

public class AccountSettingsActivity extends AppCompatActivity {

    private static final String TAG = "AccountSettingsActivity";
    private static final int ACTIVITY_NUM = 2;

    private Context mContext;

    public SectionsStatePagerAdapter pagerAdapter;
    private ViewPager mViewPager;
    private RelativeLayout mRelativeLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountsettings);
        mContext = AccountSettingsActivity.this;
        mViewPager = (ViewPager)findViewById(R.id.viewpager_container);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relLayout1);

        Log.d(TAG, "onCreate: Started");

        setupBottomNavigationView();
        setupSettingsList();
        setupFragments();
        getIncomingIntent();


        // setup the backarrow for navigating back to "profileActivity"
        ImageView backArrow = (ImageView) findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigating back to 'profileActivity'");

                // calls the ondDestroy() method for the activity
                finish();
            }
        });


    }

    private void getIncomingIntent() {
        Intent intent = getIntent();

        Log.d(TAG, "getIncomingIntent: get incoming intent");
        // if there is an img url attatched as an extra then it was chosen from the gallery/photo fragment

        if(intent.hasExtra(getString(R.string.selected_image)) ||intent.hasExtra(getString(R.string.selected_bitmap))) {

            if (intent.getStringExtra(getString(R.string.return_to_fragment)).equals(getString(R.string.edit_profile_fragment))) {
                // we are changing our profile photo, set new profile picture

                if (intent.hasExtra(getString(R.string.selected_image))) {
                    Log.d(TAG, "getIncomingIntent: Incoming img url: " + intent.getStringExtra(getString(R.string.selected_image)));
                    FirebaseMethods firebaseMethods = new FirebaseMethods(AccountSettingsActivity.this);
                    firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo), "", "", 0, intent.getStringExtra(getString(R.string.selected_image)), null, null);
                } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
                    // if the photo was taken by a camera it will return a bitmap and not a url

                    FirebaseMethods firebaseMethods = new FirebaseMethods(AccountSettingsActivity.this);
                    firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo), "", "", 0, null, (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap)), null);

                }
            }
        }
    }

    // responsible for navigating to the fragment
    public void setViewPager(int fragmentNumber) {
        mRelativeLayout.setVisibility(View.GONE);
        Log.d(TAG, "setViewPager: Navigating to fragment #:" + fragmentNumber);
        // set up the view pager
        mViewPager.setAdapter(pagerAdapter);

        //navigate to specified fragment
        mViewPager.setCurrentItem(fragmentNumber);
    }

    private void setupFragments() {
        pagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new EditProfileFragment(), getString(R.string.edit_profile_fragment)); // fragment 0
        pagerAdapter.addFragment(new SignOutFragment(), getString(R.string.sign_out_fragment)); // fragment 1


    }

    private void setupSettingsList() {
        Log.d(TAG, "setupSettingsList: initialising 'Account Settings' list.");

        ListView listView = (ListView) findViewById(R.id.lvAccountSettings);

        ArrayList<String> options = new ArrayList<>();

        // use strings xml instead of hard coding.. better code practice
        options.add(getString(R.string.edit_profile_fragment));
        options.add(getString(R.string.sign_out_fragment));

        // create the adapter to bind the array to a list view
        ArrayAdapter adapter = new ArrayAdapter(mContext, android.R.layout.simple_list_item_1, options);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemClick: Navigating to fragment #:" + i);
                setViewPager(i);
            }
        });
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

}



