package com.nickb.spots.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nickb.spots.R;
import com.nickb.spots.Utils.BottomNavigationViewHelper;
import com.nickb.spots.Utils.FirebaseMethods;
import com.nickb.spots.Utils.GridImageAdapter;
import com.nickb.spots.Utils.UniversalImageLoader;
import com.nickb.spots.models.Like;
import com.nickb.spots.models.Spot;
import com.nickb.spots.models.UserAccountSettings;
import com.nickb.spots.models.UserSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;


public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final int ACTIVITY_NUM = 2;
    private static final int NUM_OF_GRID_COLUMNS = 3;
    private static final String mAppend = "file:/";

    public interface OnGridImageSelectedListener {
        void onGridImageSelected(Spot spot, int activityNumber);
    }

    OnGridImageSelectedListener mOnGridImageSelectedListener;





    // Fire base
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods firebaseMethods;


    // have the option to add username - stored in database
    private TextView mSpots, mDisplayName, mHomeTown, mHomeCity, mDescription;
    private RatingBar mRating;
    private GridView gridView;
    private ProgressBar mProgressBar;
    private ImageView mProfilePhoto, profileMenu;
    private BottomNavigationViewEx bottomNavigationViewEx;
    private Toolbar toolbar;

    private Context mContext;

    // Navigating to EditProfile Fragment 33

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        Log.d(TAG, "onCreateView: started");

        // setup widgets
        gridView = view.findViewById(R.id.gridView);
        mProgressBar = view.findViewById(R.id.profileProgressBar);
        mProfilePhoto = view.findViewById(R.id.profile_photo);
        toolbar = view.findViewById(R.id.profileToolbar);
        bottomNavigationViewEx = view.findViewById(R.id.bottomNavViewBar);
        mSpots = view.findViewById(R.id.tvSpots);
        mDisplayName = view.findViewById(R.id.display_name);
        mRating = view.findViewById(R.id.ratingBar);
        mHomeCity = view.findViewById(R.id.homeCity);
        mHomeTown = view.findViewById(R.id.homeTown);
        mDescription = view.findViewById(R.id.description);
        profileMenu = view.findViewById(R.id.profileMenu);

        // get context
        mContext = getActivity();

        firebaseMethods = new FirebaseMethods(mContext);

        // method calls
        setupBottomNavigationView();
        setupToolbar();
        setupFirebaseAuth();
        setupGridView();

        return view;
    }



    // when the fragment is attached to the activity set the grid image select listener
    @Override
    public void onAttach(@NonNull Context context) {
        try {
            mOnGridImageSelectedListener = (OnGridImageSelectedListener) getActivity();
        } catch (ClassCastException e) {
            Log.d(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
        super.onAttach(context);
    }

    //    private void tempGridSetup() {
//        ArrayList<String> imgURLS = new ArrayList<>();
//        imgURLS.add("https://iso.500px.com/wp-content/uploads/2016/04/stock-photo-150595123.jpg");
//        imgURLS.add("https://iso.500px.com/wp-content/uploads/2016/04/stock-photo-150595123.jpg");
//        imgURLS.add("https://iso.500px.com/wp-content/uploads/2016/04/stock-photo-150595123.jpg");
//        imgURLS.add("https://iso.500px.com/wp-content/uploads/2016/04/stock-photo-150595123.jpg");
//
//        setupImageGrid(imgURLS);
//
//    }

    private void setupGridView() {
        Log.d(TAG, "setupGridView: Setting up image grid");

        final ArrayList<Spot> spots = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(getString(R.string.dbname_user_spots)).child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        // loop through all spots and add them to the arraylist
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {

                    Spot spot = new Spot();
                    // cast the snapshot to a hashmap to store the likes
                    // firebase doesn't like lists within nodes, it reads them as a hashmap

                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();


                    spot.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                    spot.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                    spot.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                    spot.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());
                    spot.setDescription(objectMap.get(getString(R.string.field_description)).toString());

                    Log.d(TAG, "onDataChange: " + objectMap.get(getString(R.string.field_title)));
                    spot.setTitle(objectMap.get(getString(R.string.field_title)).toString());
                    spot.setDescription(objectMap.get(getString(R.string.field_description)).toString());
//                  spot.setTags(objectMap.get(getString(R.string.field_tags)).toString());

                    List<Like> likesList = new ArrayList<Like>();
                    for (DataSnapshot dSnapshot: dataSnapshot.child(getString(R.string.field_likes)).getChildren()) {
                        Like like = new Like();
                        like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                        likesList.add(like);
                    }
                    spot.setLikes(likesList);

                    spots.add(spot);

                    // old way to add spots (without the likes list)
                    // spots.add(singleSnapshot.getValue(Spot.class));
                }

                // setup image grid
                int gridWidth = getResources().getDisplayMetrics().widthPixels;
                int imageWidth = gridWidth/NUM_OF_GRID_COLUMNS;
                gridView.setColumnWidth(imageWidth);


                ArrayList<String> imgURLs = new ArrayList<>();
                for (int i = 0; i < spots.size(); i++) {
                    imgURLs.add(spots.get(i).getImage_path());
                }

                GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview, imgURLs, mAppend);
                gridView.setAdapter(adapter);


                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mOnGridImageSelectedListener.onGridImageSelected(spots.get(position), ACTIVITY_NUM);
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });

    }


    private void setProfileWidget(UserSettings userSettings) {
        Log.d(TAG, "setProfileWidget: Setting profile widgets with data recieved from firebase: " + userSettings.toString());

        UserAccountSettings settings = userSettings.getSettings();


        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");
        mDisplayName.setText(settings.getDisplay_name());
        mSpots.setText(String.valueOf(settings.getSpot_count()));
        mHomeCity.setText(settings.getHome_city());
        mHomeTown.setText(settings.getHome_town());
        mDescription.setText(settings.getDescription());
        mRating.setRating((int)settings.getRating());

        mProgressBar.setVisibility(View.GONE);

    }



    private void setupToolbar() {

        Log.d(TAG, "setupToolbar: setting up toolbar");

        ((ProfileActivity)getActivity()).setSupportActionBar(toolbar);

        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigating to account settings");

                Intent intent = new Intent(mContext, AccountSettingsActivity.class);
                startActivity(intent);

            }
        });
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView () {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");

        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, getActivity(), bottomNavigationViewEx);


        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    // ----------------- Firebase ------------------




    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: ");

        // Initialize Fire base Auth
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        // sets a state listener to see if user logs in or logs out
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
                }
            }
        };



        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Log.d(TAG, "onDataChange: database reference called");
                UserSettings userSettings = firebaseMethods.getUserSettings(dataSnapshot);
                setProfileWidget(userSettings);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

}
