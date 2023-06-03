package com.example.firebasemessenger.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebasemessenger.R;
import com.example.firebasemessenger.models.User;
import com.example.firebasemessenger.models.StringModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>{
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference mUsersRef = db.collection("users");
    private StorageReference userPhotosStorageReference = FirebaseStorage.getInstance().getReference("user_photos/");


    private ArrayList<User> users;
    private User currentUser;
    private boolean isSearching;

    public FriendsAdapter(ArrayList<User> users, User currentUser, boolean isSearching) {
        this.users = users;
        this.currentUser = currentUser;
        this.isSearching = isSearching;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.card_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class FriendViewHolder extends RecyclerView.ViewHolder{
        private ImageView userImage;
        private TextView userNickname;
        private FloatingActionButton fabAdd, fabRemove;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.iv_friends_user_image);
            userNickname = itemView.findViewById(R.id.tv_friends_nickname);
            fabAdd = itemView.findViewById(R.id.fab_friends_add);
            fabRemove = itemView.findViewById(R.id.fab_friends_remove);
        }

        public void bind(User user){
            mUsersRef.document(currentUser.getKey())
                    .collection("friends").document(user.getKey()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (isSearching && documentSnapshot.exists()){
                                users.remove(user);
                                notifyItemRemoved(getAdapterPosition());
                            }
                            else if (isSearching && !documentSnapshot.exists()){
                                fabAdd.setVisibility(View.VISIBLE);
                                fabAdd.setEnabled(true);
                                fabRemove.setVisibility(View.INVISIBLE);
                                fabRemove.setEnabled(false);
                            } else if (!isSearching) {
                                fabAdd.setVisibility(View.INVISIBLE);
                                fabAdd.setEnabled(false);
                                fabRemove.setVisibility(View.VISIBLE);
                                fabRemove.setEnabled(true);

                                mUsersRef.document(user.getKey()).get().addOnSuccessListener(documentSnapshot1 -> userNickname.setText((String) documentSnapshot1.get("nickname")));
                            }
                        }
                    });

            userPhotosStorageReference.child(user.getKey()).getDownloadUrl()
                    .addOnSuccessListener(uri -> Picasso.get().load(uri).into(userImage));

            fabAdd.setOnClickListener(v -> {
                StringModel userKey = new StringModel(user.getKey());
                mUsersRef.document(currentUser.getKey())
                        .collection("friends").document(user.getKey()).set(userKey);

                userKey = new StringModel(currentUser.getKey());
                mUsersRef.document(user.getKey())
                        .collection("friends").document(currentUser.getKey()).set(userKey);

                users.remove(user);
                notifyItemRemoved(getAdapterPosition());
            });

            fabRemove.setOnClickListener(v -> {
                mUsersRef.document(currentUser.getKey())
                        .collection("friends").document(user.getKey()).delete();
                mUsersRef.document(user.getKey())
                        .collection("friends").document(currentUser.getKey()).delete();

                users.remove(user);
                notifyItemRemoved(getAdapterPosition());
            });
        }
    }
}
