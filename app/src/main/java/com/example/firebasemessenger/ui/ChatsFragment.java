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
import com.example.firebasemessenger.adapters.ChatsAdapter;
import com.example.firebasemessenger.dialogs.CreateChatDialog;
import com.example.firebasemessenger.models.Chat;
import com.example.firebasemessenger.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ChatsFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference mUsersRef = db.collection("users");
    private CollectionReference mChatsRef = db.collection("chats");

    private ArrayList<Chat> chats = new ArrayList<>();

    private User currentUser;

    public ChatsFragment(User currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView rvChats = view.findViewById(R.id.rv_chats);
        FloatingActionButton fabChatAdd = view.findViewById(R.id.fab_chats_add);

        mUsersRef.document(currentUser.getKey())
                .collection("chats").get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot ds : queryDocumentSnapshots){
                        Chat chat = new Chat();

                        chat.setName((String) ds.get("name"));
                        chats.add(chat);
                    }

                    ChatsAdapter adapter = new ChatsAdapter(chats, currentUser);
                    rvChats.setAdapter(adapter);

                    fabChatAdd.setOnClickListener(view1 -> {
                        CreateChatDialog createChatDialog = new CreateChatDialog(adapter, currentUser);
                        createChatDialog.show(getParentFragmentManager(), "create_chat");
                    });
                });
    }
}