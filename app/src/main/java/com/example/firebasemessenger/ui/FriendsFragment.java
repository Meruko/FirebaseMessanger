package com.example.firebasemessenger.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.firebasemessenger.R;
import com.example.firebasemessenger.adapters.FriendsAdapter;
import com.example.firebasemessenger.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FriendsFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference mUsersRef = db.collection("users");

    private User currentUser;

    public FriendsFragment(User currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SearchView svFriends = view.findViewById(R.id.sv_friends);
        RecyclerView rvFriends = view.findViewById(R.id.rv_friends);

        ArrayList<User> friends = new ArrayList<>();

        mUsersRef.document(currentUser.getKey())
                .collection("friends")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        QuerySnapshot result = task.getResult();

                        for (DocumentSnapshot ds : result){
                            User user = new User();

                            String key, email, nickname;
                            nickname = (String) ds.get("nickname");
                            key = (String) ds.get("key");
                            email = (String) ds.get("email");

                            user.setKey(key);
                            user.setEmail(email);
                            user.setNickname(nickname);

                            friends.add(user);
                        }

                        FriendsAdapter adapter = new FriendsAdapter(friends, currentUser, false);
                        rvFriends.setAdapter(adapter);
                    }
                });

        svFriends.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                ArrayList<User> users = new ArrayList<>();

                if (s.length() > 0){
                    Pattern searchPattern = Pattern.compile(".*"+s.toLowerCase()+".*");
                    mUsersRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            QuerySnapshot result = task.getResult();

                            for (DocumentSnapshot ds : result){
                                User user = new User();

                                String key, email, nickname;
                                nickname = (String) ds.get("nickname");
                                key = (String) ds.get("key");

                                if (searchPattern.matcher(nickname).matches() && !key.equals(currentUser.getKey())) {
                                    email = (String) ds.get("email");

                                    user.setKey(key);
                                    user.setEmail(email);
                                    user.setNickname(nickname);

                                    users.add(user);
                                }
                            }

                            FriendsAdapter adapter = new FriendsAdapter(users, currentUser, true);
                            rvFriends.setAdapter(adapter);
                        }
                    }).addOnFailureListener(
                            e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
                }
                else if  (s.length() == 0){
                    mUsersRef.document(currentUser.getKey())
                            .collection("friends")
                            .get().addOnCompleteListener(task -> {
                                if (task.isSuccessful()){
                                    QuerySnapshot result = task.getResult();

                                    for (DocumentSnapshot ds : result){
                                        User user = new User();

                                        String key, email, nickname;
                                        nickname = (String) ds.get("nickname");
                                        key = (String) ds.get("key");
                                        email = (String) ds.get("email");

                                        user.setKey(key);
                                        user.setEmail(email);
                                        user.setNickname(nickname);

                                        users.add(user);
                                    }

                                    FriendsAdapter adapter = new FriendsAdapter(users, currentUser, false);
                                    rvFriends.setAdapter(adapter);
                                }
                            });
                }
                return false;
            }
        });
    }
}