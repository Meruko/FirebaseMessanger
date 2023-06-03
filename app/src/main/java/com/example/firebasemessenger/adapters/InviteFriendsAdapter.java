package com.example.firebasemessenger.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebasemessenger.R;
import com.example.firebasemessenger.models.Invite;
import com.example.firebasemessenger.models.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class InviteFriendsAdapter extends RecyclerView.Adapter<InviteFriendsAdapter.InviteFriendViewHolder>{
    private StorageReference userPhotosStorageReference = FirebaseStorage.getInstance().getReference("user_photos/");
    private ArrayList<User> friends;
    private ArrayList<Invite> invites = new ArrayList<>();

    public InviteFriendsAdapter(ArrayList<User> friends) {
        this.friends = friends;
    }

    @NonNull
    @Override
    public InviteFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_invite, parent, false);
        return new InviteFriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InviteFriendViewHolder holder, int position) {
        holder.bind(friends.get(position));
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public class InviteFriendViewHolder extends RecyclerView.ViewHolder{
        private ImageView friendImage;
        private CheckBox cxIncludeFriendToInvite;
        private TextView tvFriendInfo;

        public InviteFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            friendImage = itemView.findViewById(R.id.invite_profile_image);
            cxIncludeFriendToInvite = itemView.findViewById(R.id.cx_include_friend_to_invite);
            tvFriendInfo = itemView.findViewById(R.id.tv_invite_friend_info);
        }

        public void bind(User friend){
            userPhotosStorageReference.child(friend.getKey()).getDownloadUrl()
                    .addOnSuccessListener(uri -> Picasso.get().load(uri).into(friendImage));

            tvFriendInfo.setText(friend.getNickname() + " (" + friend.getSurname() + " " + friend.getName() + ")");

            Invite invite = new Invite(false, friend);
            invites.add(invite);

            cxIncludeFriendToInvite.setOnCheckedChangeListener((buttonView, isChecked) -> invite.setInvited(isChecked));
        }
    }

    public ArrayList<Invite> getInvites() {
        return invites;
    }

    public void setInvites(ArrayList<Invite> invites) {
        this.invites = invites;
    }
}
