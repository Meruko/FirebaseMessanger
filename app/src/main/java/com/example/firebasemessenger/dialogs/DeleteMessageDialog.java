package com.example.firebasemessenger.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.firebasemessenger.R;
import com.example.firebasemessenger.adapters.MessagesAdapter;
import com.example.firebasemessenger.models.ChatMessage;
import com.example.firebasemessenger.ui.MessagesFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

public class DeleteMessageDialog extends DialogFragment {
    private ChatMessage selectedMsg;
    private DocumentReference mChatRef;
    private MessagesAdapter messagesAdapter;

    public DeleteMessageDialog(ChatMessage selectedMsg, DocumentReference mChatRef,
                               MessagesAdapter messagesAdapter){
        this.selectedMsg = selectedMsg;
        this.mChatRef = mChatRef;
        this.messagesAdapter = messagesAdapter;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder =new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        View view= inflater.inflate(R.layout.dialog_delete_msg, null);

        CheckBox cxDeleteForAll = view.findViewById(R.id.cx_delete_msg_for_all);

        builder.setView(view)
                .setPositiveButton("Удалить", (dialog, which) -> {
                    if (cxDeleteForAll.isChecked()){
                        mChatRef.collection("messages").document(selectedMsg.getKey()).delete();
                    }
                    else{
                        selectedMsg.setDeletedForSender(true);
                        mChatRef.collection("messages").document(selectedMsg.getKey()).set(selectedMsg);
                    }

                    messagesAdapter.getMessages().sort((o1, o2) -> o1.getTime() > o2.getTime() ? 1 : o1.getTime() < o2.getTime() ? -1 : 0);
                    messagesAdapter.notifyDataSetChanged();
                })
                .setNegativeButton("Отмена", (dialog, which) -> DeleteMessageDialog.this.getDialog().cancel());

        return builder.create();
    }
}
