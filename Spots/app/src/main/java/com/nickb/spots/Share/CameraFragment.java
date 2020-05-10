package com.nickb.spots.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.nickb.spots.Profile.AccountSettingsActivity;
import com.nickb.spots.R;
import com.nickb.spots.Utils.Permissions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class CameraFragment extends Fragment {
    private static final String TAG = "CameraFragment";

    private static final int CAMERA_FRAGMENT_NUM = 1;
    private static final int GALLERY_FRAGMENT_NUM = 2;
    private static final int CAMERA_REQUEST_CODE = 5;


    private Button btnCamera;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        Log.d(TAG, "onCreateView: started");

        btnCamera = view.findViewById(R.id.btnLaunchCamera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Launching camera");

                if (((ShareActivity)getActivity()).getCurrentTabNumber() == CAMERA_FRAGMENT_NUM) {
                    if (((ShareActivity)getActivity()).checkPermission(Permissions.CAMERA_PERMISSION)) {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        // use on acitivty for result because we want to store the image captured

                        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                    } else {
                        Intent intent = new Intent(getActivity(), ShareActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // clear activity stack
                        startActivity(intent);
                    }
                }
            }
        });

        return view;

    }

    private boolean isRootTask() {
        if(((ShareActivity)getActivity()).getTask() == 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // returns the image as a bitmap
        Bitmap bitmap = (Bitmap)data.getExtras().get("data");
        if (requestCode == CAMERA_REQUEST_CODE) {
            Log.d(TAG, "onActivityResult: Received photo from camera");
            if(isRootTask()) {
                Intent intent = new Intent(getActivity(), UploadActivity.class);
                intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                startActivity(intent);

            } else {
                try {
                    Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                    intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                    intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment)); // identifies the fragment that we need to return to
                    startActivity(intent);
                    getActivity().finish();

                } catch (NullPointerException e) {
                    Log.d(TAG, "onActivityResult: NullPointerException: " + e.getMessage());
                }

            }
        }
    }
}
