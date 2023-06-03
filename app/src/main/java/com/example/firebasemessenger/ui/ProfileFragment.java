package com.example.firebasemessenger.ui;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.firebasemessenger.R;
import com.example.firebasemessenger.SignInActivity;
import com.example.firebasemessenger.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference mUsersRef = db.collection("users");
    private static final int SELECT_PICTURE = 1;
    private User mCurrentuser;
    private StorageReference userPhotosStorageReference = FirebaseStorage.getInstance().getReference("user_photos/");

    private TextView tvEmail, tvNickname, tvFI;
    private ImageView image;

    ActivityResultLauncher<Intent> loadPhotoResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Uri selectedImageUri = result.getData().getData();
                        image.setImageURI(selectedImageUri);

                        String filename = mCurrentuser.getKey();
                        StorageReference fileReference = userPhotosStorageReference.child(filename);
                        fileReference.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> Log.e("", "image loaded"));
                    }
                }
            });

    public ProfileFragment(User user) {
        mCurrentuser = user;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        CardView imageHolder = view.findViewById(R.id.profileImageHolder);
        image = view.findViewById(R.id.profileImage);
        tvEmail = view.findViewById(R.id.tv_profile_email);
        tvNickname = view.findViewById(R.id.tv_profile_nickname);
        tvFI = view.findViewById(R.id.tv_profile_sur_and_name);
        FloatingActionButton fabEdit = view.findViewById(R.id.fab_edit_profile);
        FloatingActionButton fabExit = view.findViewById(R.id.fab_exit);

        tvEmail.setText(mCurrentuser.getEmail());
        tvNickname.setText(mCurrentuser.getNickname());
        tvFI.setText(mCurrentuser.getSurname() + " " + mCurrentuser.getName());

        userPhotosStorageReference.child(mCurrentuser.getKey()).getDownloadUrl()
                .addOnSuccessListener(uri -> Picasso.get().load(uri).into(image));

        imageHolder.setOnClickListener(view1 -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            loadPhotoResultLauncher.launch(intent);
        });

        fabEdit.setOnClickListener(view1 -> {
            ProfileMBS mbs = new ProfileMBS(mCurrentuser, this);
            mbs.show(getActivity().getSupportFragmentManager(), null);
        });

        fabExit.setOnClickListener(view1 -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();

            auth.signOut();
            getActivity().finish();
            Intent signInIntent = new Intent(getContext(), SignInActivity.class);
            startActivity(signInIntent);
        });
    }

    public void updateText(User curUser){
        tvNickname.setText(curUser.getNickname());
        tvFI.setText(curUser.getSurname() + " " + curUser.getName());
    }
}