package com.example.firebasemessenger.adapters;

import android.media.MediaPlayer;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebasemessenger.MainActivity;
import com.example.firebasemessenger.R;
import com.example.firebasemessenger.dialogs.DeleteMessageDialog;
import com.example.firebasemessenger.models.ChatMessage;
import com.example.firebasemessenger.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>{
    private StorageReference userPhotosStorageReference = FirebaseStorage.getInstance().getReference("user_photos/");
    private StorageReference chatStorageReference;
    private ArrayList<ChatMessage> messages;
    private User currentUser;
    private DocumentReference mChatRef;
    private String chatName;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private File directory = new File("/storage/emulated/0/Download/");

    public MessagesAdapter(ArrayList<ChatMessage> messages, User currentUser, DocumentReference mChatRef, String chatName) {
        this.messages = messages;
        this.currentUser = currentUser;
        this.mChatRef = mChatRef;

        chatStorageReference = FirebaseStorage.getInstance().getReference(chatName + "/");
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        private ImageButton imgBtnDeleteMsg, imgBtnDownload, imgBtnPlayAudio;
        private ImageView senderPic, msgImage;
        private TextView sender, text, time;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            imgBtnDeleteMsg = itemView.findViewById(R.id.message_img_btn_delete_msg);
            imgBtnDownload = itemView.findViewById(R.id.message_img_btn_download);
            imgBtnPlayAudio = itemView.findViewById(R.id.message_img_btn_play_audio);
            senderPic = itemView.findViewById(R.id.message_profile_image);
            sender = itemView.findViewById(R.id.message_user);
            text = itemView.findViewById(R.id.message_text);
            time = itemView.findViewById(R.id.message_time);
            msgImage = itemView.findViewById(R.id.message_image);
        }

        public void bind(ChatMessage msg){
            userPhotosStorageReference.child(msg.getSenderKey()).getDownloadUrl()
                    .addOnSuccessListener(uri -> Picasso.get().load(uri).into(senderPic));

            sender.setText(msg.getSenderNick());
            time.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                    msg.getTime()));

            if (msg.getText().contains("images/")){
                msgImage.setEnabled(true);
                msgImage.setVisibility(View.VISIBLE);
                String ext = msg.getText().split(Pattern.quote("."))[1];
                String name = msg.getText().split("/")[1];
                name = name.replace("."+ext, "");
                text.setText(name + "." + ext);
                text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                chatStorageReference.child(msg.getText()).getDownloadUrl()
                        .addOnSuccessListener(uri -> Picasso.get().load(uri).into(msgImage));

                String finalName = name;
                msgImage.setOnClickListener(v -> {
                    StorageReference imgRef = chatStorageReference.child(msg.getText());

                    try {
                        File file = new File("/storage/emulated/0/Download/"+finalName+"."+ext);
                        if (file.exists()){
                            Toast.makeText(text.getContext(), "Файл уже скачан", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        File localFile = new File("/storage/emulated/0/Download/"+finalName+"."+ext);

                        imgRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                            Log.e("download_img", localFile.getAbsolutePath());
                            Toast.makeText(text.getContext(), localFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Log.e("download_img", e.getMessage());
                        });
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            else if (msg.getText().contains("files/")){
                msgImage.setEnabled(false);
                msgImage.setVisibility(View.INVISIBLE);
                imgBtnDownload.setEnabled(true);
                imgBtnDownload.setVisibility(View.VISIBLE);
                String ext = msg.getText().split(Pattern.quote("."))[1];
                String name = msg.getText().split("/")[1];
                name = name.replace("."+ext, "");
                text.setText(name + "." + ext);
                text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                String finalName = name;
                imgBtnDownload.setOnClickListener(v -> {
                    StorageReference imgRef = chatStorageReference.child(msg.getText());

                    try {
                        File file = new File("/storage/emulated/0/Download/"+finalName+"."+ext);
                        if (file.exists()){
                            Toast.makeText(text.getContext(), "Файл уже скачан", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        File localFile = new File("/storage/emulated/0/Download/"+finalName+"."+ext);

                        imgRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                            Log.e("download_file", localFile.getAbsolutePath());
                            Toast.makeText(text.getContext(), localFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Log.e("download_file", e.getMessage());
                        });
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            else if (msg.getText().contains("audios/")) {
                msgImage.setEnabled(false);
                msgImage.setVisibility(View.INVISIBLE);
                text.setEnabled(false);
                msgImage.setVisibility(View.INVISIBLE);
                imgBtnPlayAudio.setEnabled(true);
                imgBtnPlayAudio.setVisibility(View.VISIBLE);
                String ext = msg.getText().split(Pattern.quote("."))[1];
                String name = msg.getText().split("/")[1];
                name = name.replace("."+ext, "");

                String finalName = name;
                imgBtnPlayAudio.setOnClickListener(v -> {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()){
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                        return;
                    }

                    StorageReference imgRef = chatStorageReference.child(msg.getText());

                    mediaPlayer = new MediaPlayer();
                    try{
                        File file = new File("/storage/emulated/0/Download/"+finalName+"."+ext);
                        if (file.exists()){
                            try{
                                mediaPlayer.setDataSource(file.getAbsolutePath());
                                mediaPlayer.prepare();
                                mediaPlayer.start();
                            }
                            catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return;
                        }

                        File localFile = new File("/storage/emulated/0/Download/"+finalName+"."+ext);
                        imgRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                            Log.e("download_file", localFile.getAbsolutePath());
                            Toast.makeText(text.getContext(), localFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();

                            try {
                                mediaPlayer.setDataSource(localFile.getAbsolutePath());
                                mediaPlayer.prepare();
                                mediaPlayer.start();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }).addOnFailureListener(e -> {
                            Log.e("download_file", e.getMessage());
                        });
                    }
                    catch (Exception e){
                        throw new RuntimeException(e);
                    }

                });
            }
            else{
                msgImage.setEnabled(false);
                msgImage.setVisibility(View.INVISIBLE);
                imgBtnDownload.setEnabled(false);
                imgBtnDownload.setVisibility(View.INVISIBLE);
                text.setText(msg.getText());
            }

            if (!currentUser.getKey().equals(msg.getSenderKey())){
                imgBtnDeleteMsg.setVisibility(View.INVISIBLE);
                imgBtnDeleteMsg.setEnabled(false);
            }

            imgBtnDeleteMsg.setOnClickListener(v -> {
                DeleteMessageDialog dialog = new DeleteMessageDialog(msg, mChatRef, MessagesAdapter.this);
                dialog.show(((MainActivity)sender.getContext()).getSupportFragmentManager(), "delete_msg");
            });
        }
    }

    public ArrayList<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<ChatMessage> messages) {
        this.messages = messages;
    }
}
