package com.example.firebasemessenger.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebasemessenger.R;
import com.example.firebasemessenger.adapters.InviteFriendsAdapter;
import com.example.firebasemessenger.models.Chat;
import com.example.firebasemessenger.models.Invite;
import com.example.firebasemessenger.models.StringModel;
import com.example.firebasemessenger.models.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class InviteToChatDialog extends DialogFragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference mUsersRef = db.collection("users");
    private CollectionReference mFriendsRef;
    private DocumentReference mChatRef;

    private InviteFriendsAdapter adapter;

    private User currentUser;
    private String chatName;
    private ArrayList<User> friends;

    public InviteToChatDialog(ArrayList<User> friends, User currentUser, DocumentReference mChatRef, String chatName) {
        this.friends = friends;
        this.currentUser = currentUser;
        this.mChatRef = mChatRef;
        this.chatName = chatName;

        mFriendsRef = db.collection("users").document(currentUser.getKey()).collection("friends");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder =new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        View view= inflater.inflate(R.layout.dialog_invite_to_chat, null);

        RecyclerView rvInvite = view.findViewById(R.id.rv_invite);
        adapter = new InviteFriendsAdapter(friends);
        rvInvite.setAdapter(adapter);

        builder.setView(view)
                .setPositiveButton("Пригласить", ((dialog, which) -> {
                    for (Invite invite : adapter.getInvites()){
                        if (invite.isInvited()){
                            StringModel friendKey = new StringModel(invite.getFriend().getKey());

                            mChatRef.collection("members")
                                    .document(friendKey.getKey()).set(friendKey);

                            Chat chat = new Chat(chatName);
                            mUsersRef.document(friendKey.getKey())
                                    .collection("chats").document(chat.getName()).set(chat);
                        }
                    }
                }))
                .setNegativeButton("Отмена", (dialog, which) -> InviteToChatDialog.this.getDialog().cancel());

        return builder.create();
    }


}
