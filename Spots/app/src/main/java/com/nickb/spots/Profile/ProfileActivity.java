package com.nickb.spots.Profile;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nickb.spots.R;
import com.nickb.spots.Utils.ViewPostFragment;
import com.nickb.spots.models.Spot;


public class ProfileActivity extends AppCompatActivity implements ProfileFragment.OnGridImageSelectedListener{
    private static final String TAG = "ProfileActivity";

    @Override
    public void onGridImageSelected(Spot spot, int activityNumber) {
        Log.d(TAG, "onGridImageSelected: image selected from gridview");

        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.spot), spot);
        args.putInt(getString(R.string.activity_number), activityNumber);
        fragment.setArguments(args); // attaches the bundle to the arguments

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment); // swap put the viewpager for view post fragment
        transaction.addToBackStack(getString(R.string.view_post_fragment)); // allowing for back tracking to this fragment
        transaction.commit();

    }

    private static final int ACTIVITY_NUM = 2;
    private static final int NUM_OF_GRID_COLUMNS = 3;

    private Context mContext = ProfileActivity.this;

    private ProgressBar mProgressBar;
    private ImageView mProfilePhoto;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG, "onCreate: Started.");


        init();
    }



    private void init() {
        Log.d(TAG, "init: inflating fragment: " + getString(R.string.profile_fragment));

        ProfileFragment fragment = new ProfileFragment();
        FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment); // swap out the container with the profile fragment
        transaction.addToBackStack(getString(R.string.profile_fragment)); // fragments dont keep track of their stack so you must manually do it
        transaction.commit();
    }



}
  