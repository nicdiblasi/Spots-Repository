package com.nickb.spots.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nickb.spots.Home.HomeActivity;
import com.nickb.spots.Profile.AccountSettingsActivity;
import com.nickb.spots.R;
import com.nickb.spots.Share.UploadActivity;
import com.nickb.spots.models.Location;
import com.nickb.spots.models.Spot;
import com.nickb.spots.models.User;
import com.nickb.spots.models.UserAccountSettings;
import com.nickb.spots.models.UserSettings;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import androidx.annotation.NonNull;

public class FirebaseMethods {
    private static final String TAG = "FirebaseMethods";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageReference;

    private double mPhotoUploadProgress = 0;
    private String userID;

    private Context mContext;

    public FirebaseMethods(Context context) {
        mContext = context;
        mAuth = FirebaseAuth.getInstance();

       mFirebaseDatabase = FirebaseDatabase.getInstance();
       myRef = mFirebaseDatabase.getReference();
       mStorageReference = FirebaseStorage.getInstance().getReference();


        if(mAuth.getCurrentUser() != null) {
            userID = mAuth.getUid();
        }

    }

    public void uploadNewPhoto(String photoType, final String title, final String description, int count, final String imgUrl, Bitmap bitmap, final Location location) {
        Log.d(TAG, "uploadNewPhoto: attempting to upload new photo");

        Log.d(TAG, "uploadNewPhoto: " + photoType + title + description + count + imgUrl + location);

        FileHelper fileHelper = new FileHelper();


        // case 1 new photo
        if(photoType.equals(mContext.getString(R.string.new_photo))) {
            Log.d(TAG, "uploadNewPhoto: uploading new photo");

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final StorageReference storageReference = mStorageReference.child(fileHelper.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo_" + (count + 1));

            if (bitmap == null) {
                // convert image url to bitmap
                bitmap = ImageManager.getBitmap(imgUrl);
            }

            // convert bitmap to byte array
            byte[] bytes = ImageManager.getBytesFromBitmap(bitmap, 100);


            final UploadTask uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Uri url = taskSnapshot.getDownloadUrl();
                    Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uri.isComplete());
                    Uri url = uri.getResult();

                    Toast.makeText(mContext, "Upload Success", Toast.LENGTH_LONG).show();

                    Log.d(TAG, "onSuccess: upload success, URL: " + url.toString());


                    // add url to photos node and user_photos node in database

                    addPhotoToDatabase(title, description, imgUrl, location);
                    // navigate to list view so users can see
                    //navigate to listview
                    Intent intent = new Intent( mContext, HomeActivity.class);
                    mContext.startActivity(intent);


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(mContext, "Photo upload failed", Toast.LENGTH_SHORT).show();

                    Log.d(TAG, "onProgress: Upload failed");

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if (progress - 15 > mPhotoUploadProgress) {
                        Toast.makeText(mContext, "Photo upload progress: " + String.format("%.05f",progress), Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }

                    Log.d(TAG, "onProgress: Upload progress: " + progress);
                }
            });
        }
        // case 2 new profile photo
        else if (photoType.equals(mContext.getString(R.string.profile_photo))) {
            Log.d(TAG, "uploadNewPhoto: uploading new profile photo");

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final StorageReference storageReference = mStorageReference.child(fileHelper.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo");

            if (bitmap == null) {
                // convert image url to bitmap
                bitmap = ImageManager.getBitmap(imgUrl);

                // else the photo was taken by camera and is already a bitmap
            }

            // convert bitmap to byte array
            byte[] bytes = ImageManager.getBytesFromBitmap(bitmap, 100);


            final UploadTask uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Uri url = taskSnapshot.getDownloadUrl();
                    Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uri.isComplete());
                    Uri url = uri.getResult();

                    Toast.makeText(mContext, "Upload Success, download URL ", Toast.LENGTH_LONG).show();

                    // insert into the user_account_settings
                    setProfilePhoto(url.toString());

                    // set the view pager to take directly to back to edit profile, the pager adapter has a method to get fragment number by the name
                    ((AccountSettingsActivity)mContext).setViewPager(
                            ((AccountSettingsActivity)mContext).pagerAdapter.getFragmentNumber(mContext.getString(R.string.edit_profile_fragment))
                    );


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(mContext, "Photo upload failed", Toast.LENGTH_SHORT).show();

                    Log.d(TAG, "onProgress: Upload failed");

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if (progress - 15 > mPhotoUploadProgress) {
                        Toast.makeText(mContext, "Photo upload progress: " + String.format("%.05f",progress), Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }

                    Log.d(TAG, "onProgress: Upload progress: " + progress);
                }
            });
        }
    }

    private void setProfilePhoto(String url) {
        Log.d(TAG, "setProfilePhoto: setting profile photo");

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(mContext.getString(R.string.profile_photo)).setValue(url);
    }

    // returns a date object of right now
    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("DD-MM-YYYY'T'HH:MM:SS'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("Australia/Melbourne"));

        return sdf.format(new Date());
    }

    private void addPhotoToDatabase(String title, String description, String imgUrl, Location location) {


        // retrieve a unique key for the photo
        String photoKey = myRef.child(mContext.getString(R.string.dbname_spots)).push().getKey();
        Spot spot = new Spot();
        spot.setTitle(title);
        spot.setDescription(description);
        spot.setImage_path(imgUrl);
        spot.setPhoto_id(photoKey);
        spot.setDate_created(getTimestamp());
        spot.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        spot.setLocation(location);

        Log.d(TAG, "addPhotoToDatabase: adding photo to database" + spot);

        // insert into database
        myRef.child(mContext.getString(R.string.dbname_user_spots)).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(photoKey).setValue(spot);
        myRef.child(mContext.getString(R.string.dbname_spots)).child(photoKey).setValue(spot);


    }




    public int getImageCount(DataSnapshot dataSnapshot) {
        int count = 0;
        for (DataSnapshot ds: dataSnapshot
                .child(mContext.getString(R.string.dbname_spots))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .getChildren()) {
            count++;
        }

        return count;
    }

    public void updateUsername(String username) {

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);

        myRef.child(mContext.getString(R.string.dbname_user))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);

    }

    public void updateHomeCity(String homeCity) {
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_home_city))
                .setValue(homeCity);
    }

    public void updateHomeTown(String homeTown) {
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_home_town))
                .setValue(homeTown);
    }

    public void updateDisplayName(String displayName) {
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_display_name))
                .setValue(displayName);
    }

    public void updateDescription(String description) {
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_description))
                .setValue(description);
    }


    /**
     * Register a new user to firebase
     * @param email
     * @param password
     * @param username
     */
    public void registerNewEmail(final String email, String password, final String username) {
        Log.d(TAG, "registerNewEmail: attempting to register new email");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(mContext, "Authentication success.", Toast.LENGTH_SHORT).show();

                            FirebaseUser user = mAuth.getCurrentUser();
                            userID = user.getUid();

                            // send verification email - commented out for now
                             sendVerificationEmail(user);


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Log.d(TAG, "onComplete: AuthState changed");

                            Toast.makeText(mContext, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }


    public void sendVerificationEmail(FirebaseUser user) {
        if(user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(!task.isSuccessful()) {
                                Toast.makeText(mContext, "Unable to send verification email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }


    /**
     * Adds information to the user_account_settings node
     * Adds information to the user node
     *
     * @param email
     * @param username
     * @param description
     * @param profile_photo
     */
    public void addNewUser(String email, String username, String description, String profile_photo) {
        // create user object
        User user = new User(email, StringManipulation.condenseUsername(username), userID);

        // create user account settings object
        UserAccountSettings userAccountSettings = new UserAccountSettings(
                description,
                username,
                0,
                0,
                "",
                "",
                StringManipulation.condenseUsername(username),
                profile_photo,
                userID);

        // find that node in the database and set it to the user
        myRef.child(mContext.getString(R.string.dbname_user))
                .child(userID)
                .setValue(user);

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .setValue(userAccountSettings);

    }


    /**
     * Returns the user and user_account_settings information from the database
     * @param dataSnapshot
     * @return
     */
    public UserSettings getUserSettings(DataSnapshot dataSnapshot) {
        Log.d(TAG, "getUserAccountSettings: retrieving user account settings from firebase");

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();

        for(DataSnapshot ds: dataSnapshot.getChildren()) {
            // retrieve UserAccountSettings
            if (ds.getKey().equals(mContext.getString(R.string.dbname_user_account_settings))) {
                    Log.d(TAG, "getUserSettings: dataSnapshot: " + ds);

                    try {
                        settings.setDisplay_name(
                                ds.child(userID)
                                        .getValue(UserAccountSettings.class)
                                        .getDisplay_name()
                        );

                        settings.setUsername(
                                ds.child(userID)
                                        .getValue(UserAccountSettings.class)
                                        .getUsername()
                        );

                        settings.setDescription(
                                ds.child(userID)
                                        .getValue(UserAccountSettings.class)
                                        .getDescription()
                        );

                        settings.setHome_city(
                                ds.child(userID)
                                        .getValue(UserAccountSettings.class)
                                        .getHome_city()
                        );

                        settings.setHome_town(
                                ds.child(userID)
                                        .getValue(UserAccountSettings.class)
                                        .getHome_town()
                        );

                        settings.setRating(
                                ds.child(userID)
                                        .getValue(UserAccountSettings.class)
                                        .getRating()
                        );

                        settings.setSpot_count(
                                ds.child(userID)
                                        .getValue(UserAccountSettings.class)
                                        .getSpot_count()
                        );

                        settings.setProfile_photo(
                                ds.child(userID)
                                        .getValue(UserAccountSettings.class)
                                        .getProfile_photo()
                        );
                        Log.d(TAG, "getUserSettings: retrieved user account settings information: " + settings.toString());
                    } catch (NullPointerException e) {
                        Log.e(TAG, "getUserSettings: Null pointer exception: " + e.getMessage() );
                    }
            }

            if (ds.getKey().equals(mContext.getString(R.string.dbname_user))) {
                Log.d(TAG, "getUserSettings: dataSnapshot: " + ds);

                try {
                    user.setUsername(
                            ds.child(userID)
                                    .getValue(User.class)
                                    .getUsername()
                    );

                    user.setEmail(
                            ds.child(userID)
                                    .getValue(User.class)
                                    .getEmail()
                    );

                    user.setUser_id(
                            ds.child(userID)
                                    .getValue(User.class)
                                    .getUser_id()
                    );
                    Log.d(TAG, "getUserSettings: retrieved user information: " + user.toString());
                } catch (NullPointerException e) {
                    Log.e(TAG, "getUserSettings: Null pointer exception: " + e.getMessage() );
                }

            }
        }

        return new UserSettings(user, settings);

    }

}
