package com.example.firebasemessenger;

import android.os.Bundle;

import com.example.firebasemessenger.models.User;
import com.example.firebasemessenger.ui.FriendsFragment;
import com.example.firebasemessenger.ui.ChatsFragment;
import com.example.firebasemessenger.ui.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference mUsersRef = db.collection("users");
    private User mCurrentUser = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCurrentUser.setEmail(mAuth.getCurrentUser().getEmail());
        mCurrentUser.setKey(mAuth.getCurrentUser().getUid());

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setEnabled(false);
        bottomNav.getMenu().getItem(0).setChecked(true);

        mUsersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                QuerySnapshot snapshot = task.getResult();
                DocumentSnapshot userDoc = snapshot.getDocuments()
                        .stream().filter(ds -> ds.get("key").equals(mCurrentUser.getKey()))
                        .collect(Collectors.toList()).get(0);

                mUsersRef.document(userDoc.getId()).get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()){
                        DocumentSnapshot result = task1.getResult();
                        String nickname = (String) result.get("nickname");
                        String surname = (String) result.get("surname");
                        String name = (String) result.get("name");
                        mCurrentUser.setNickname(nickname);
                        mCurrentUser.setSurname(surname);
                        mCurrentUser.setName(name);

                        getSupportFragmentManager().beginTransaction().replace(R.id.frame, new ProfileFragment(mCurrentUser)).commit();

                        bottomNav.setEnabled(true);
                    }
                });
            }
        });

        bottomNav.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment =null;
            if (item.getItemId() == R.id.menu_profile)
                fragment = new ProfileFragment(mCurrentUser);
            else if (item.getItemId() == R.id.menu_messages)
                fragment = new ChatsFragment(mCurrentUser);
            else if (item.getItemId() == R.id.menu_friends)
                fragment = new FriendsFragment(mCurrentUser);

            if (fragment != null){
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.frame, fragment).commit();
            }
            item.setChecked(true);
            return false;
        });
    }
}