package com.example.firebasemessenger.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.firebasemessenger.R;
import com.example.firebasemessenger.adapters.ChatsAdapter;
import com.example.firebasemessenger.models.Chat;
import com.example.firebasemessenger.models.User;
import com.example.firebasemessenger.models.StringModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class CreateChatDialog extends DialogFragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference mUsersRef = db.collection("users");
    private CollectionReference mChatsRef = db.collection("chats");

    private ChatsAdapter chatsAdapter;
    private User currentUser;

    public CreateChatDialog(ChatsAdapter chatsAdapter, User currentUser) {
        this.chatsAdapter = chatsAdapter;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder =new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        View view= inflater.inflate(R.layout.dialog_add_chat, null);

        builder.setView(view)
                .setPositiveButton("Создать", (dialog, which) -> {
                    EditText etChatName = view.findViewById(R.id.et_create_chat_name);
                    String chatName = etChatName.getText().toString().trim();

                    if (chatName.length() == 0){
                        Toast.makeText(getContext(), "Неверное имя чата", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mChatsRef.document(chatName).get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()){
                            Toast.makeText(getContext(), "Чат с таким названием уже есть", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Chat newChat = new Chat(chatName);
                            chatsAdapter.getChats().add(newChat);
                            chatsAdapter.notifyItemInserted(chatsAdapter.getItemCount());

                            StringModel userKey = new StringModel(currentUser.getKey());

                            mChatsRef.document(chatName).collection("members")
                                    .document(currentUser.getKey()).set(userKey);

                            mUsersRef.document(currentUser.getKey())
                                    .collection("chats").document(chatName).set(newChat);
                        }
                    });
                })
                .setNegativeButton("Отмена", (dialog, which) -> CreateChatDialog.this.getDialog().cancel());

        return builder.create();
    }
}
