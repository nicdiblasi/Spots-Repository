package com.nickb.spots.Profile;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nickb.spots.R;
import com.nickb.spots.Share.GalleryFragment;
import com.nickb.spots.Share.ShareActivity;
import com.nickb.spots.Utils.FirebaseMethods;
import com.nickb.spots.Utils.StringManipulation;
import com.nickb.spots.Utils.UniversalImageLoader;
import com.nickb.spots.models.User;
import com.nickb.spots.models.UserAccountSettings;
import com.nickb.spots.models.UserSettings;

public class EditProfileFragment extends Fragment {

    private static final String TAG = "EditProfileFragment";


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private ImageView  mBackArrow, mProfilePhoto, mSubmit;
    private EditText mDisplayName, mHomeCity, mHomeTown, mUsername, mDescription;
    private TextView mChangePhoto;
    private FirebaseMethods firebaseMethods;
    private String userID;
    private UserSettings mUserSettings;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_editprofile, container, false);

        mProfilePhoto = view.findViewById(R.id.profile_photo);
        mBackArrow = view.findViewById(R.id.backArrow);
        mDisplayName = view.findViewById(R.id.display_name);
        mHomeCity = view.findViewById(R.id.homeCity);
        mHomeTown = view.findViewById(R.id.homeTown);
        mDescription = view.findViewById(R.id.description);
        mChangePhoto = view.findViewById(R.id.changeProfilePhoto);
        mUsername = view.findViewById(R.id.username);
        mSubmit = view.findViewById(R.id.saveChanges);
        firebaseMethods = new FirebaseMethods(getActivity());

        setupBackarrow();

        setupFirebaseAuth();

        mChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: changing profile photo");

                Intent intent = new Intent(getActivity(), ShareActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // add the flag so we the gallery knows where we are coming from
                getActivity().startActivity(intent);
                getActivity().finish();


            }
        });


        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to save changes");
                saveProfileSettings();
            }
        });
        return view;
    }


    private void setupBackarrow() {
        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigating back to account settings");

                getActivity().finish();
            }
        });
    }

    /**
     *  retrieve the data in the widgets and save them to fire base
     */
    private void saveProfileSettings() {
        // To change firebase email (Part 37)

        final String displayName = mDisplayName.getText().toString();
        final String username = mUsername.getText().toString();
        final String homeCity = mHomeCity.getText().toString();
        final String homeTown = mHomeTown.getText().toString();
        final String description = mDescription.getText().toString();


        // only listens to database once
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // if display name was changed
                if (!mUserSettings.getSettings().getDisplay_name().equals(displayName)) {
                    firebaseMethods.updateDisplayName(displayName);
                }
                // if username was changed
                if (!mUserSettings.getSettings().getUsername().equals(username)) {
                    checkIfUsernameExists(username);
                }

                // if home city was changed
                if (!mUserSettings.getSettings().getHome_city().equals(homeCity)) {
                    firebaseMethods.updateHomeCity(homeCity);
                }

                // if home town was changed
                if (!mUserSettings.getSettings().getHome_town().equals(homeTown)) {
                    firebaseMethods.updateHomeTown(homeTown);
                }

                // if description was changed
                if (!mUserSettings.getSettings().getDescription().equals(description)) {
                    firebaseMethods.updateDescription(description);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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

                // if the query returned null and that username is not in the database - then update the username
                if (!dataSnapshot.exists()) {
                    // add the username
                    firebaseMethods.updateUsername(username);
                    Toast.makeText(getActivity(), "Saved username", Toast.LENGTH_SHORT).show();
                }

                // search through the database and find the username
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {
                    if (singleSnapshot.exists()) {
                        Toast.makeText(getActivity(), "That username already exists", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void setProfileWidget(UserSettings userSettings) {
        Log.d(TAG, "setProfileWidget: Setting profile widgets with data recieved from firebase: " + userSettings.toString());

        UserAccountSettings settings = userSettings.getSettings();

        mUserSettings = userSettings;
        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");
        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mHomeCity.setText(settings.getHome_city());
        mHomeTown.setText(settings.getHome_town());
        mDescription.setText(settings.getDescription());
        mUsername.setText(settings.getUsername());

    }


    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: ");

        // Initialize Fire base Auth
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        userID = mAuth.getCurrentUser().getUid();
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
