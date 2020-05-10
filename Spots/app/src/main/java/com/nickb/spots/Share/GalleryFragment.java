package com.nickb.spots.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nickb.spots.Profile.AccountSettingsActivity;
import com.nickb.spots.R;
import com.nickb.spots.Utils.FileHelper;
import com.nickb.spots.Utils.GridImageAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFragment";

    private static boolean IMAGE_SELECTED = false;
    private static final int NUM_GRID_COLUMNS = 3;
    private static final String mAppend = "file:/";


    private ImageView mConfirm, mGalleryImageView;
    private GridView gridView;
    private ProgressBar mProgressBar;
    private String mSelectedImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        Log.d(TAG, "onCreateView: started");
        mConfirm = view.findViewById(R.id.tvConfirm);
        mGalleryImageView = view.findViewById(R.id.galleryImageView);
        mProgressBar = view.findViewById(R.id.progressBar);
        gridView = view.findViewById(R.id.galleryGridView);

        mProgressBar.setVisibility(View.GONE);
        mConfirm.setVisibility(View.GONE);
        Toast.makeText(getActivity(), "Tap image to confirm", Toast.LENGTH_LONG).show();

        setupConfirmButton();
        setupGridView();
        return view;

    }

    private void setupConfirmButton() {
        Log.d(TAG, "setupConfirmClick: setting up confirm button");

        mGalleryImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IMAGE_SELECTED) {
                    Log.d(TAG, "onClick: deselecting image");
                    mGalleryImageView.setImageAlpha(255);
                    mConfirm.setVisibility(View.GONE);
                    IMAGE_SELECTED = false;
                } else {
                    Log.d(TAG, "onClick: selecting image");

                    mGalleryImageView.setImageAlpha(100);
                    mConfirm.setVisibility(View.VISIBLE);
                    IMAGE_SELECTED = true;
                }

            }
        });
        
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(isRootTask()) {
                // is root task checks if we got to this activity by anything other than edit profile, edit profile has a flag set with it
                Log.d(TAG, "onClick: confirming image \n navigating to upload");
                Intent intent = new Intent(getActivity(), UploadActivity.class);
                intent.putExtra(getString(R.string.selected_image), mSelectedImage);
                startActivity(intent);
            } else {
                // here we know we came from edit profile so along with putting the imgurl as an extra we also want to attach the fragment we return to
                Log.d(TAG, "onClick: confirming image navigating to accountSettingsActivity");
                Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                intent.putExtra(getString(R.string.selected_image), mSelectedImage);
                intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment)); // identifies the fragment that we need to return to
                Log.d(TAG, "onClick: putting intent extras: img URL" + mSelectedImage + "return fragment: "+ getString(R.string.edit_profile_fragment));

                startActivity(intent);
                getActivity().finish();
            }
            }
        });
    }

    private void setupGridView() {
        String directory = Environment.getExternalStorageDirectory().getPath() + "/DCIM/camera";

        // String directory = Environment.getExternalStorageDirectory().getParent() + "/pictures";
        Log.d(TAG, "setupGridView: file path: " + directory);


        final ArrayList<String> imgURLs = FileHelper.getFilePaths(directory);

        // set the grid column width so each image is the same size
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth/NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview, imgURLs, mAppend);
        gridView.setAdapter(adapter);


        // set image view to first image
        try {
            setImage(imgURLs.get(0), mGalleryImageView, mAppend);
            mSelectedImage = imgURLs.get(0);
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.d(TAG, "setupGridView: ArrayIndexOutOfBoundsException: " + e.getMessage());

        }


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected item: " + imgURLs.get(position));

                setImage(imgURLs.get(position), mGalleryImageView, mAppend);
                mSelectedImage = imgURLs.get(position);

            }
        });

    }

    private boolean isRootTask() {
        if(((ShareActivity)getActivity()).getTask() == 0) {
            return true;
        } else {
            return false;
        }
    }

    private void setImage(String imgURL, ImageView image, String append) {
        Log.d(TAG, "setImage: setting image");

        ImageLoader imageLoader = ImageLoader.getInstance();


        imageLoader.displayImage(append + imgURL, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mProgressBar.setVisibility(View.GONE);

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                mProgressBar.setVisibility(View.GONE);

            }
        });

    }
}
