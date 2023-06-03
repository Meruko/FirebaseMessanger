package com.example.firebasemessenger.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.firebasemessenger.R;
import com.example.firebasemessenger.models.User;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileMBS extends BottomSheetDialogFragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference mUsersRef = db.collection("users");
    private User mCurrentuser;
    private ProfileFragment profileFragment;

    public ProfileMBS(User user, ProfileFragment profileFragment){
        mCurrentuser = user;
        this.profileFragment = profileFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.mbs_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        EditText etNickname = view.findViewById(R.id.et_nickname);
        EditText etSurname = view.findViewById(R.id.et_surname);
        EditText etName = view.findViewById(R.id.et_name);
        Button btnUpdateProfile = view.findViewById(R.id.btn_update_profile);

        etNickname.setText(mCurrentuser.getNickname());

        btnUpdateProfile.setOnClickListener(view1 -> {
            String nickname = etNickname.getText().toString().trim();
            String surname = etSurname.getText().toString().trim();
            String name = etName.getText().toString().trim();

            if (nickname.isEmpty() || surname.isEmpty() || name.isEmpty())
                return;

            mCurrentuser.setNickname(nickname);
            mCurrentuser.setSurname(surname);
            mCurrentuser.setName(name);

            mUsersRef.document(mCurrentuser.getKey())
                    .set(mCurrentuser);

            profileFragment.updateText(mCurrentuser);
            dismiss();
        });
    }
}
