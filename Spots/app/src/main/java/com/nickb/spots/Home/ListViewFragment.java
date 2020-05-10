package com.nickb.spots.Home;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nickb.spots.R;
import com.nickb.spots.Utils.MainfeedListAdapter;
import com.nickb.spots.models.Like;
import com.nickb.spots.models.Location;
import com.nickb.spots.models.Spot;
import com.nickb.spots.models.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Cleaning things up and Tab Preparation (Part 5) - [Build an Instagram Clone]

public class ListViewFragment extends Fragment {
    private static final String TAG = "ListViewFragment";


    private ArrayList<Spot> mSpots;
    private ListView mListView;
    private MainfeedListAdapter mAdapter;
    private ArrayList<String> mUsers;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_view, container, false);

        mListView = view.findViewById(R.id.listView);
        mSpots = new ArrayList<>();
        mUsers = new ArrayList<>();


        getUsers();


        return view;

    }


    private void getUsers() {
        Log.d(TAG, "getUsers: getting the user ids to display");


        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: Retrieved datasnapshot: " + dataSnapshot.toString());

                // get all the user ids in the database
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {

                    Log.d(TAG, "onDataChange: Found user " + singleSnapshot.child(getString(R.string.field_user_id)).getValue());

                    mUsers.add(singleSnapshot.child(getString(R.string.field_user_id)).getValue().toString());
                }

                // users assigned, get the spots
                getSpots();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void getSpots() {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();


        for (int i = 0; i < mUsers.size(); i++) {
            final int count = i;

            // loop through db and find all users
            Query query = reference
                    .child(getString(R.string.dbname_user_spots))
                    .child(mUsers.get(i))
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(mUsers.get(i));


            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange: Retrieved datasnapshot: " + dataSnapshot.toString());

                    // get all the user ids in the database
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                        // add spot to array of spots for every user
                        Spot spot = new Spot();
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();


                        Log.d(TAG, "onDataChange: title: " + objectMap.get(getString(R.string.field_title)).toString());
                        spot.setTitle(objectMap.get(getString(R.string.field_title)).toString());
                        spot.setDescription(objectMap.get(getString(R.string.field_description)).toString());
                        spot.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());
                        spot.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                        spot.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        spot.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());
                        spot.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());


                        Map<String, Object> locationMap = (HashMap<String, Object>) objectMap.get(getString(R.string.field_location));

                        double latitude = (double) locationMap.get(getString(R.string.field_latitude));
                        double longitude = (double) locationMap.get(getString(R.string.field_longitude));
                        String feature = (String) locationMap.get(getString(R.string.field_feature));

                        Location location = new Location(latitude, longitude, feature);
                        Log.d(TAG, "onDataChange: location: " + location);

                        spot.setLocation(location);

                        List<Like> likesList = new ArrayList<>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child(getString(R.string.field_likes)).getChildren()) {
                            Like like = new Like();
                            like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                            likesList.add(like);
                        }
                        spot.setLikes(likesList);


                        mSpots.add(spot);
                    }


                    if (count >= mUsers.size() - 1) {
                        // on the last spot, display all the photos
                        displaySpots();

                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void displaySpots() {
        if(mSpots != null) {
            Collections.sort(mSpots, new Comparator<Spot>() {
                @Override
                public int compare(Spot o1, Spot o2) {
                    return o1.getDate_created().compareTo(o2.getDate_created());
                }
            });
        }

        mAdapter = new MainfeedListAdapter(getActivity(), R.layout.layout_mainfeed_listitem, mSpots);
        mListView.setAdapter(mAdapter);
    }


}
