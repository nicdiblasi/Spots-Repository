package com.nickb.spots.Utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nickb.spots.R;
import com.nickb.spots.models.Like;
import com.nickb.spots.models.Spot;
import com.nickb.spots.models.User;
import com.nickb.spots.models.UserAccountSettings;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainfeedListAdapter extends ArrayAdapter<Spot> {

    private static final String TAG = "MainfeedListAdapter";


    private LayoutInflater mLayoutInflater;
    private int mLayoutResource;
    private Context mContext;
    private DatabaseReference reference;
    private String currentUsername = "";


    public MainfeedListAdapter(@NonNull Context context, int resource, @NonNull List<Spot> objects) {
        super(context, resource, objects);

        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutResource = resource;
        reference = FirebaseDatabase.getInstance().getReference();
        mContext = context;

    }

    static class ViewHolder {

        private static boolean LIKED = false;

        CircleImageView mProfileImage;
        TextView mUsername, mTitle, mDescription, mLocation, btnLike;
        SquareImageView mImage;
        FirebaseMethods firebaseMethods;

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();
        StringBuilder users;

        Spot mSpot;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder();
            holder.firebaseMethods = new FirebaseMethods(mContext);

            holder.mUsername = convertView.findViewById(R.id.username);
            holder.mImage = convertView.findViewById(R.id.post_view);
            holder.mDescription = convertView.findViewById(R.id.description);
            holder.btnLike = convertView.findViewById(R.id.btnLike);
            holder.mProfileImage = convertView.findViewById(R.id.profile_photo);
            holder.mLocation = convertView.findViewById(R.id.location);
            holder.mTitle = convertView.findViewById(R.id.title);
            holder.users = new StringBuilder();

            setLikeToggle(holder);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        holder.mSpot = getItem(position);

        // get the current users username
        getCurrentUsername();


        // set the image
        final ImageLoader imageLoader = ImageLoader.getInstance();

        // TODO: store image with correct appending
        imageLoader.displayImage("file:/" + getItem(position).getImage_path(), holder.mImage);
        holder.mTitle.setText(getItem(position).getTitle());
        holder.mDescription.setText(getItem(position).getDescription());
        holder.mLocation.setText(getItem(position).getLocation().getFeature());


        // get the user object
        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    Log.d(TAG, "onDataChange: found user: " + singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    holder.mUsername.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(), holder.mProfileImage);
                    holder.settings = singleSnapshot.getValue(UserAccountSettings.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return convertView;

    }

    private void getCurrentUsername() {

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: Retrieved datasnapshot: " + dataSnapshot.toString());

                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {
                    currentUsername = singleSnapshot.getValue(User.class).getUsername();
                    Log.d(TAG, "onDataChange: current username: " + currentUsername);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setLikeToggle(final ViewHolder holder) {

        holder.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                Query query = reference
                        .child(mContext.getString(R.string.dbname_spots))
                        .child(holder.mSpot.getPhoto_id())
                        .child(mContext.getString(R.string.field_likes));
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: Retrieved datasnapshot: " + dataSnapshot.toString());

                        for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {
                            String keyID = singleSnapshot.getKey();

                            // case 1 the user already liked the photo
                            if(holder.LIKED && singleSnapshot.getValue(Like.class).getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                Log.d(TAG, "onDataChange: likes exist, unliking");

                                reference.child(mContext.getString(R.string.dbname_spots))
                                        .child(holder.mSpot.getPhoto_id())
                                        .child(mContext.getString(R.string.field_likes))
                                        .child(keyID)
                                        .removeValue();

                                reference.child(mContext.getString(R.string.dbname_user_spots))
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child(holder.mSpot.getPhoto_id())
                                        .child(mContext.getString(R.string.field_likes))
                                        .child(keyID)
                                        .removeValue();

                                holder.btnLike.setText("Like");
                                holder.LIKED = false;
                            } else if (!holder.LIKED) {
                                // case 2 user has not liked it
                                // add new like
                                Log.d(TAG, "onDataChange: likes exist, adding a like");

                                addNewLike(holder);
                                holder.btnLike.setText("Unlike");
                                holder.LIKED = true;
                                break;
                            }
                        }

                        if (!dataSnapshot.exists()) {
                            Log.d(TAG, "onDataChange: no likes exists, adding like");
                            // add new like
                            addNewLike(holder);
                            holder.btnLike.setText("Unlike");
                            holder.LIKED = true;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }



        });
    }


    private void addNewLike(final ViewHolder mHolder) {
        Log.d(TAG, "addNewLike: adding new like");

        String newLikeID = reference.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        reference.child(mContext.getString(R.string.dbname_spots))
                .child(mHolder.mSpot.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        reference.child(mContext.getString(R.string.dbname_user_spots))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mHolder.mSpot.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);
    }

}
