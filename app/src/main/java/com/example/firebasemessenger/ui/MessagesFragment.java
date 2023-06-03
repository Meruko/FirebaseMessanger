package com.example.firebasemessenger.ui;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.NOTIFICATION_SERVICE;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebasemessenger.R;
import com.example.firebasemessenger.adapters.MessagesAdapter;
import com.example.firebasemessenger.dialogs.InviteToChatDialog;
import com.example.firebasemessenger.models.ChatMessage;
import com.example.firebasemessenger.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

public class MessagesFragment extends Fragment {
    private static final int MICROPHONE_PERMISSION_CODE = 200;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference mUsersRef = db.collection("users");
    private DocumentReference mChatRef;
    private CollectionReference mMembersRef, mMessagesRef;
    private StorageReference imagesStorageReference, filesStorageReference, audiosStorgeReference;
    private User currentUser;
    private String chatName;
    private ArrayList<ChatMessage> messages = new ArrayList<>();
    private ArrayList<User> friends = new ArrayList<>();
    private MessagesAdapter adapter;
    private RecyclerView rvMessages;
    private EditText etMessageText;
    private MediaRecorder mediaRecorder = new MediaRecorder();
    private boolean isRecording = false;

    private ActivityResultLauncher<Intent> loadPhotoResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Uri selectedImageUri = result.getData().getData();

                        String fileExt = getFileExtension(selectedImageUri);
                        String filename = currentUser.getKey() + (new Date().getTime()) + "." + fileExt;

                        if (fileExt.equals("jpg") || fileExt.equals("jpeg") || fileExt.equals("jpeg") || fileExt.equals("svg") ||
                                fileExt.equals("png") || fileExt.equals("gif") || fileExt.equals("webp")){
                            StorageReference fileReference = imagesStorageReference.child(filename);
                            fileReference.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> Log.e("", "image loaded"));

                            sendMsg("images/" + filename);
                        }
                        else{
                            StorageReference fileReference = filesStorageReference.child(filename);
                            fileReference.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> Log.e("", "file loaded"));

                            sendMsg("files/" + filename);
                        }
                    }
                }
            });

    public MessagesFragment() {
    }

    public MessagesFragment(String chatName, User currentUser){
        this.chatName = chatName;
        this.currentUser = currentUser;

        mChatRef = db.collection("chats").document(chatName);
        mMembersRef = db.collection("chats").document(chatName).collection("members");
        mMessagesRef = db.collection("chats").document(chatName).collection("messages");
        imagesStorageReference = FirebaseStorage.getInstance().getReference(chatName + "/images/");
        filesStorageReference = FirebaseStorage.getInstance().getReference(chatName + "/files/");
        audiosStorgeReference = FirebaseStorage.getInstance().getReference(chatName + "/audios/");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ImageButton btnBack = view.findViewById(R.id.img_btn_messages_back);
        TextView tvChatName = view.findViewById(R.id.tv_messages_chat_name);
        Button btnInviteToChat = view.findViewById(R.id.btn_invite_to_chat);
        rvMessages = view.findViewById(R.id.rv_messages);
        adapter = new MessagesAdapter(messages, currentUser, mChatRef, chatName);
        rvMessages.setAdapter(adapter);
        etMessageText = view.findViewById(R.id.et_message_text);
        FloatingActionButton fabAttachFile = view.findViewById(R.id.fab_attach_file);
        FloatingActionButton fabSendMessage = view.findViewById(R.id.fab_send_message);
        FloatingActionButton fabVoiceMessage = view.findViewById(R.id.fab_voice_message);

        btnInviteToChat.setEnabled(false);

        mUsersRef.document(currentUser.getKey()).collection("friends").get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot ds : queryDocumentSnapshots) {
                        User friend = new User();

                        friend.setKey((String) ds.get("key"));

                        mUsersRef.document(friend.getKey()).get().addOnSuccessListener(documentSnapshot -> {
                            friend.setEmail((String) documentSnapshot.get("email"));
                            friend.setNickname((String) documentSnapshot.get("nickname"));
                            friend.setSurname((String) documentSnapshot.get("surname"));
                            friend.setName((String) documentSnapshot.get("name"));

                            friends.add(friend);
                        });
                    }

                    btnInviteToChat.setEnabled(true);
                });

        tvChatName.setText(chatName);

        btnBack.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction().replace(R.id.frame, new ChatsFragment(currentUser)).commit();
        });

        btnInviteToChat.setOnClickListener(v -> {
            mMembersRef.get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                for (DocumentSnapshot ds : queryDocumentSnapshots1){
                    String key = (String) ds.get("key");

                    if (friends.removeIf(f -> f.getKey().equals(key)));
                }

                InviteToChatDialog dialog = new InviteToChatDialog(friends, currentUser, mChatRef, chatName);
                dialog.show(getParentFragmentManager(), "invite_to_chat");
            });
        });

        fabAttachFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("file/*");
            loadPhotoResultLauncher.launch(intent);
        });

        fabSendMessage.setOnClickListener(v -> {
            String msgText = etMessageText.getText().toString().trim();

            sendMsg(msgText);
        });

        fabVoiceMessage.setOnClickListener(v -> {
            if (isRecording){
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;

                isRecording = false;

                String fileExt = "mp3";
                String filename = currentUser.getKey() + (new Date().getTime()) + "." + fileExt;

                StorageReference fileReference = audiosStorgeReference.child(filename);
                fileReference.putFile(Uri.fromFile(new File(getRecordingFilePath()))).addOnSuccessListener(taskSnapshot -> Log.e("", "image loaded"));

                sendMsg("audios/" + filename);

                Toast.makeText(getContext(), "Запись закончилась", Toast.LENGTH_SHORT).show();
            }
            else{
                if(isMicrophonePresent()){
                    getMicrophonePermission();
                }

                try {
                    mediaRecorder = new MediaRecorder();

                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mediaRecorder.setOutputFile(getRecordingFilePath());
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    mediaRecorder.prepare();
                    mediaRecorder.start();

                    isRecording = true;

                    Toast.makeText(getContext(), "Запись началась", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        getMessages();
    }

    private void sendMsg(String msgText){
        if (msgText.isEmpty()){
            Toast.makeText(getContext(), "Сообщение пусто!", Toast.LENGTH_SHORT).show();
            return;
        }

        ChatMessage msg = new ChatMessage(msgText, currentUser.getNickname(), currentUser.getKey());

        mMessagesRef.add(msg).addOnSuccessListener(documentReference -> {
            msg.setKey(documentReference.getId());
            mMessagesRef.document(documentReference.getId()).set(msg);

            etMessageText.setText("");

            getMessages();
        });
    }

    private void getMessages(){
        mMessagesRef.addSnapshotListener((value, error) -> {
            if (error != null){
                Log.e("msg_error", error.getMessage());
                return;
            }

            messages.clear();
            rvMessages.removeAllViews();

            for (DocumentSnapshot ds : value){
                ChatMessage msg = new ChatMessage();

                String senderNick = (String) ds.get("senderNick");
                String senderKey = (String) ds.get("senderKey");
                String key = (String) ds.get("key");
                String text = (String) ds.get("text");
                long time = (long) ds.get("time");
                boolean deletedForSender = (boolean) ds.get("deletedForSender");

                msg.setSenderNick(senderNick);
                msg.setSenderKey(senderKey);
                msg.setKey(key);
                msg.setText(text);
                msg.setTime(time);
                msg.setDeletedForSender(deletedForSender);

                if (msg.isDeletedForSender() && currentUser.getKey().equals(msg.getSenderKey())){

                }
                else{
                    messages.add(msg);
                }
            }

            //Сортировка сообщений по дате написания
            messages.sort((o1, o2) -> o1.getTime() > o2.getTime() ? 1 : o1.getTime() < o2.getTime() ? -1 : 0);

            adapter.notifyDataSetChanged();
        });
    }

    private String getFileExtension(Uri uri){
        ContentResolver resolver = getActivity().getContentResolver();
        MimeTypeMap typeMap = MimeTypeMap.getSingleton();
        return  typeMap.getExtensionFromMimeType(resolver.getType(uri));
    }

    private boolean isMicrophonePresent(){
        return getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
    }

    private void getMicrophonePermission(){
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) ==
         PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(getActivity(), new String[]
                    {Manifest.permission.RECORD_AUDIO}, MICROPHONE_PERMISSION_CODE);
        }
    }

    private String getRecordingFilePath(){
        ContextWrapper contextWrapper = new ContextWrapper(getContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(musicDirectory, "testRecordingFile" + ".mp3");
        return  file.getPath();
    }
}