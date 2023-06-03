package com.example.firebasemessenger.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebasemessenger.MainActivity;
import com.example.firebasemessenger.R;
import com.example.firebasemessenger.models.Chat;
import com.example.firebasemessenger.models.User;
import com.example.firebasemessenger.ui.MessagesFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder>{
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference mChatsRef = db.collection("chats");
    private ArrayList<Chat> chats;
    private User currentUser;

    public ChatsAdapter(ArrayList<Chat> chats, User currentUser) {
        this.chats = chats;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.card_chat, parent, false);
        return new ChatsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatsViewHolder holder, int position) {
        holder.bind(chats.get(position));
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public ArrayList<Chat> getChats() {
        return chats;
    }

    public void setChats(ArrayList<Chat> chats) {
        this.chats = chats;
    }

    public class ChatsViewHolder extends RecyclerView.ViewHolder{
        private String chatName;

        private TextView tvChatName;
        private FloatingActionButton fabChatDelete;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            tvChatName = itemView.findViewById(R.id.tv_chats_name);
            fabChatDelete = itemView.findViewById(R.id.fab_chat_delete);

            itemView.setOnClickListener(view -> {
                MessagesFragment fragment = new MessagesFragment(chatName, currentUser);
                ((MainActivity)itemView.getContext())
                        .getSupportFragmentManager().beginTransaction().replace(R.id.frame, fragment).commit();
            });
        }

        public void bind(Chat chat){
            chatName = chat.getName();

            tvChatName.setText(chatName);

            fabChatDelete.setOnClickListener(view -> {
                mChatsRef.document(chatName).collection("members")
                        .document(currentUser.getKey()).delete();

                mChatsRef.document(chatName).collection("members").get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty())
                        mChatsRef.document(chatName).delete();
                });

                chats.remove(chat);
                notifyItemRemoved(chats.size()-1);
            });
        }
    }
}
