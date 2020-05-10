package com.nickb.spots.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.nickb.spots.Home.HomeActivity;
import com.nickb.spots.R;
import com.nickb.spots.Utils.FirebaseMethods;
import com.nickb.spots.Utils.UniversalImageLoader;
import com.nickb.spots.models.Location;
import com.nickb.spots.models.UserSettings;
import com.nostra13.universalimageloader.utils.L;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class UploadActivity extends AppCompatActivity {

    // constants
    private static final String TAG = "UploadActivity";
    private static final String mAppend = "file:/";

    // variables
    private int imageCount = 0;
    private String imgUrl;
    private Intent intent;
    private Bitmap bitmap;
    private Location location;



    // Fire base
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;


    // widgets
    private ImageView btnUpload, mBackarrow;
    private EditText mTitle, mDescription, mLocation;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        btnUpload = findViewById(R.id.btnUpload);
        mBackarrow = findViewById(R.id.backArrow);
        mDescription = findViewById(R.id.description);
        mTitle = findViewById(R.id.title);
        mLocation = findViewById(R.id.location);


        mFirebaseMethods = new FirebaseMethods(UploadActivity.this);

        Log.d(TAG, "onCreate: got selected image: " + getIntent().getStringExtra(getString(R.string.selected_image)));

        mBackarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the activity");
                finish();
            }
        });

        init();

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: uploading selected image");
                // upload image to database
                String description = mDescription.getText().toString();
                String title = mTitle.getText().toString();

                if (intent.hasExtra(getString(R.string.selected_image))) {
                    // intent came from gallery
                    imgUrl = intent.getStringExtra(getString(R.string.selected_image));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), title, description , imageCount, imgUrl, null, location);

                } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
                    // intent came from camera
                    bitmap = intent.getParcelableExtra(getString(R.string.selected_bitmap));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), title, description , imageCount, null, bitmap, location);
                }

            }
        });


        setupFirebaseAuth();
        setImage();
    }


    private void init() {
        Log.d(TAG, "init: initialising");

        // override the enter button to cause it to search
        mLocation.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == event.ACTION_DOWN
                        || event.getAction() == event.KEYCODE_ENTER) {

                    // execute method for searching
                    geoLocate();

                }

                return false;
            }
        });
    }

    private void geoLocate() {
        Log.d(TAG, "geoLocate: geolocating");

        String search = mLocation.getText().toString();

        Geocoder geocoder = new Geocoder(UploadActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(search, 1);

        } catch (IOException e) {
            Log.d(TAG, "geoLocate: IOException: " + e.getMessage());

        }

        if (list.size() > 0 ){
            Address address = list.get(0);
            Log.d(TAG, "geoLocate: found location: " + address.toString());

            location = new Location();
            location.setLatitude(address.getLatitude());
            location.setLongitude(address.getLongitude());
            location.setFeature(address.getFeatureName());

//            Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * get the image url coming from the extra inside the intent and set it to the preview
     *
     */
    private void setImage() {
        intent = getIntent();
        ImageView image = findViewById(R.id.selectedImage);

        if (intent.hasExtra(getString(R.string.selected_image))) {
            // intent came from gallery
            imgUrl = intent.getStringExtra(getString(R.string.selected_image));
            Log.d(TAG, "setImage: got new image url: " + imgUrl );
            UniversalImageLoader.setImage(imgUrl, image, null, mAppend);
        } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
            // intent came from camera
            bitmap = intent.getParcelableExtra(getString(R.string.selected_bitmap));
            Log.d(TAG, "setImage: got new bitmap");
            image.setImageBitmap(bitmap);
        }

    }

    // ----------------- Firebase ------------------

    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: ");

        // Initialize Fire base Auth
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        Log.d(TAG, "setupFirebaseAuth: image count: " + imageCount);
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

                imageCount = mFirebaseMethods.getImageCount(dataSnapshot);
                Log.d(TAG, "setupFirebaseAuth: image count: " + imageCount);


            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

}
