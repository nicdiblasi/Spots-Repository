package com.nickb.spots.Share;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nickb.spots.R;
import com.nickb.spots.Utils.BottomNavigationViewHelper;
import com.nickb.spots.Utils.Permissions;
import com.nickb.spots.Utils.SectionsPagerAdapter;

public class ShareActivity extends AppCompatActivity {
    private static final String TAG = "ShareActivity";

    // constants
    private static final int ACTIVITY_NUM = 1;
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;

    private ViewPager mViewPager;

    private Context mContext = ShareActivity.this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        Log.d(TAG, "onCreate: Started.");

        // if all permissions are allowed
        if (checkPermissionArray(Permissions.PERMISSIONS)) {
            setupViewPager();
        } else {
            // else verify them
            verifyPermissions(Permissions.PERMISSIONS);
        }
        setupBottomNavigationView ();
    }

    public int getTask() {
        // returns zero if intent is from home activity or a number if from edit profile
        return getIntent().getFlags();
    }

    private void setupViewPager() {
        // create the adapter that holds the fragments
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new GalleryFragment());
        adapter.addFragment(new CameraFragment());

        // attach the adapter to the viewpager
        mViewPager = findViewById(R.id.viewpager_container);
        mViewPager.setAdapter(adapter);

        // attach the viewpager to the tab layout
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // set the tabs
        tabLayout.getTabAt(0).setText(getString(R.string.gallery));
        tabLayout.getTabAt(1).setText(getString(R.string.camera));


    }


    /**
     * return the current tab number
     * 0 - gallery
     * 1 - photo
     * @return
     */
    public int getCurrentTabNumber() {
        return mViewPager.getCurrentItem();
    }

    // verifies the permissions
    private void verifyPermissions(String[] permissions) {
        Log.d(TAG, "verifyPermissions: verifying permissions");

        ActivityCompat.requestPermissions(ShareActivity.this, permissions, VERIFY_PERMISSIONS_REQUEST);
    }


    // checks array of permissions
    public boolean checkPermissionArray(String[] permissions) {
        Log.d(TAG, "checkPermissionArray: checking permisions");

        for (int i = 0; i < permissions.length; i++) {
            String check = permissions[i];
            if(!checkPermission(check)) {
                return false;
            }
        }
        return true;
    }

    // checks a single permission
    public boolean checkPermission(String permission) {
        Log.d(TAG, "checkPermission: checking permission: " + permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(mContext, permission);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkPermission: permission not granted for: " + permission);
            return false;
        } else {
            return true;
        }
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
