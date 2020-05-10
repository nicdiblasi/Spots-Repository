package com.nickb.spots.Utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
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
import com.nickb.spots.models.Like;
import com.nickb.spots.models.Spot;
import com.nickb.spots.models.UserAccountSettings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ViewPostFragment extends Fragment {

    // constants
    private static final String TAG = "ViewPostFragment";
    private static final String mAppend = "file:/";
    private static boolean LIKED = false;


    public ViewPostFragment() {
        super();
        setArguments(new Bundle()); // setup prevents null pointer error
    }

    // Fire base
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods firebaseMethods;


    // variables
    private Context mContext;
    private int mActivityNum = 0;
    private String username;
    private String profileURL;
    private UserAccountSettings mUserAccountSettings;

    // widgets
    private BottomNavigationViewEx bottomNavigationViewEx;
    private Spot mSpot;
    private ImageView mPhoto, mProfilePhoto, mBackarrow;
    private TextView mTitle, mLocation, mDescription, btnLike, mUsername;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);
        Log.d(TAG, "onCreateView: Started");

        init(view);

        try {
            mSpot = getSpotFromBundle();
            UniversalImageLoader.setImage(mSpot.getImage_path(), mPhoto, null, mAppend);
            mActivityNum = getActivityNumberFromBundle();

            setupBottomNavigationView();
            setupFirebaseAuth();
            getPhotoDetails();
//            setupWidgets();

        } catch (NullPointerException e) {
            Log.d(TAG, "onCreateView: NullPointerException: " + e.getMessage());
        }


        return view;
    }




    private void getPhotoDetails(){
        Log.d(TAG, "getPhotoDetails: retrieving photo details.");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(mSpot.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    mUserAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);
                    Log.d(TAG, "onDataChange: user_account_settings: " + mUserAccountSettings);
                }
                setupWidgets();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });
    }


    private void init(View view) {

        mContext = getActivity();
        mPhoto = view.findViewById(R.id.post_view);
        mTitle = view.findViewById(R.id.title);
        mLocation = view.findViewById(R.id.location);
        mDescription = view.findViewById(R.id.description);
        btnLike = view.findViewById(R.id.btnLike);
        mUsername = view.findViewById(R.id.username);
        mProfilePhoto = view.findViewById(R.id.profile_photo);
        bottomNavigationViewEx = view.findViewById(R.id.bottomNavViewBar);
        mBackarrow = view.findViewById(R.id.backArrow);

    }



    private void setupWidgets() {
         UniversalImageLoader.setImage(mUserAccountSettings.getProfile_photo(), mProfilePhoto,null, "");
         mUsername.setText(mUserAccountSettings.getUsername());
         setLikeToggle();
         mDescription.setText(mSpot.getDescription());
         mTitle.setText(mSpot.getTitle());
         mLocation.setText("TODO");
         mUsername.setText(mUserAccountSettings.getUsername());

         mBackarrow.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Log.d(TAG, "onClick: navigating back");
                 getActivity().getSupportFragmentManager().popBackStack();
             }
         });
    }

    private void setLikeToggle() {
        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                Query query = reference
                        .child(getString(R.string.dbname_spots))
                        .child(mSpot.getPhoto_id())
                        .child(getString(R.string.field_likes));
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: Retrieved datasnapshot: " + dataSnapshot.toString());

                        for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {
                            String keyID = singleSnapshot.getKey();

                            // case 1 the user already liked the photo
                            if(LIKED && singleSnapshot.getValue(Like.class).getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                Log.d(TAG, "onDataChange: likes exist, unliking");

                                myRef.child(getString(R.string.dbname_spots))
                                        .child(mSpot.getPhoto_id())
                                        .child(getString(R.string.field_likes))
                                        .child(keyID)
                                        .removeValue();

                                myRef.child(getString(R.string.dbname_user_spots))
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child(mSpot.getPhoto_id())
                                        .child(getString(R.string.field_likes))
                                        .child(keyID)
                                        .removeValue();

                                btnLike.setText("Like");
                                LIKED = false;
                            } else if (!LIKED) {
                                // case 2 user has not liked it
                                // add new like
                                Log.d(TAG, "onDataChange: likes exist, adding a like");

                                addNewLike();
                                btnLike.setText("Unlike");
                                LIKED = true;
                                break;
                            }
                        }

                        if (!dataSnapshot.exists()) {
                            Log.d(TAG, "onDataChange: no likes exists, adding like");
                            // add new like
                            addNewLike();
                            btnLike.setText("Unlike");
                            LIKED = true;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }



        });
    }


    private void addNewLike() {
        Log.d(TAG, "addNewLike: adding new like");

        String newLikeID = myRef.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        myRef.child(getString(R.string.dbname_spots))
                .child(mSpot.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        myRef.child(getString(R.string.dbname_user_spots))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mSpot.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);


    }


    /**
     * returns the spot from the bundle that was passed through to the fragment
     *
     * @return
     */
    private Spot getSpotFromBundle() {
        Log.d(TAG, "getSpotFromBundle: Getting arguments from bundle " + getArguments());
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.spot));
        } else {
            return null;
        }
    }

    /**
     * gets the activity number from bumble to help the bottom navigation view determine where this fragment was launched from
     *
     * @return
     */
    private int getActivityNumberFromBundle() {
        Log.d(TAG, "getSpotFromBundle: Getting arguments from bundle " + getArguments());
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            return bundle.getInt(getString(R.string.activity_number));
        } else {
            return 0;
        }
    }


    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");

        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(getActivity(), getActivity(), bottomNavigationViewEx);


        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(mActivityNum);
        menuItem.setChecked(true);
    }

    private void setProfileWidget() {

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

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }
}