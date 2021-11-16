package com.example.android.messenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class ChatsActivity extends AppCompatActivity {

    private String ChatUser;
    private Toolbar toolbar;
    private DatabaseReference usersReference;
    private String name;
    private TextView displayName;
    private TextView lastSeen;
    private CircleImageView displayImage;
    private DatabaseReference chatsDatabase;
    private DatabaseReference messageDatabase;
    private FirebaseAuth mAuth;
    private String CurrentUser;
    private ImageButton sendButton;
    private ImageButton chatAddButton;
    private EditText chatMessage;
    private RecyclerView messagesList;
    private final List<Messages> messageAdapterList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private static final int GALLERY_PIC = 1;
    private StorageReference mImageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        ChatUser = getIntent().getStringExtra("user_id");

        usersReference = FirebaseDatabase.getInstance("https://messenger-dc6ce-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Users").child(ChatUser);

        mAuth = FirebaseAuth.getInstance();
        CurrentUser = mAuth.getCurrentUser().getUid();

        name = getIntent().getStringExtra("user_name");

        toolbar = findViewById(R.id.chats_app_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        sendButton = findViewById(R.id.chat_send_chat);
        chatAddButton = findViewById(R.id.chat_send_image);
        chatMessage = findViewById(R.id.chat_send_message);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_layout,null);

        actionBar.setCustomView(action_bar_view);

        displayImage = findViewById(R.id.custom_bar_image);
        displayName = findViewById(R.id.chat_display_name);
        lastSeen = findViewById(R.id.chat_last_seen);

        mImageStorage = FirebaseStorage.getInstance().getReference();

        messagesList = findViewById(R.id.messages_list);

        messageAdapter = new MessageAdapter(messageAdapterList);

        linearLayoutManager = new LinearLayoutManager(this);

        messagesList.setHasFixedSize(true);
        messagesList.setLayoutManager(linearLayoutManager);

        messagesList.setAdapter(messageAdapter);

        displayName.setText(name);

        messageDatabase = FirebaseDatabase.getInstance("https://messenger-dc6ce-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("messages");
        
        loadMessages();

        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String thumb_image = snapshot.child("thumb_image").getValue().toString();
                if(!thumb_image.equals("default"))
                {
                    Picasso.with(ChatsActivity.this).load(thumb_image).into(displayImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        chatsDatabase = FirebaseDatabase.getInstance("https://messenger-dc6ce-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Chat");

        HashMap<String,String> chatMap = new HashMap<>();

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-4:00"));
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("HH:mm");
        date.setTimeZone(TimeZone.getTimeZone("GMT-4:00"));
        String localTime = date.format(currentLocalTime);

        chatMap.put("Seen","false");
        chatMap.put("timestamp",localTime);

        chatsDatabase.child(CurrentUser).child(ChatUser).setValue(chatMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    chatsDatabase.child(ChatUser).child(CurrentUser).setValue(chatMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> atask) {
                            if(atask.isSuccessful())
                            {

                            }
                            else
                            {

                            }
                        }
                    });
                }
                else
                {

                }
            }
        });



        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();

            }
        });

        chatAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PIC);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PIC && resultCode == RESULT_OK) {
            Uri imageURI = data.getData();

            CropImage.activity(imageURI)
                    .start(ChatsActivity.this);
        }

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK) {

                DatabaseReference user_message_push = messageDatabase.child(CurrentUser).child(ChatUser).push();

                String push_id = user_message_push.getKey();

                StorageReference filepath = mImageStorage.child("message_images").child(push_id + ".jpg");

                Uri resultUri = result.getUri();

                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-4:00"));
                Date currentLocalTime = cal.getTime();
                DateFormat date = new SimpleDateFormat("KK:mm");
                date.setTimeZone(TimeZone.getTimeZone("GMT-4:00"));
                String localTime = date.format(currentLocalTime);

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            filepath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> atask) {

                                    String imageDownload = atask.getResult().toString();

                                    HashMap<String, String> messageMap = new HashMap<>();

                                    messageMap.put("message", imageDownload);
                                    messageMap.put("seen", "false");
                                    messageMap.put("time", localTime);
                                    messageMap.put("type", "image");
                                    messageMap.put("from", CurrentUser);

                                    messageDatabase.child(CurrentUser).child(ChatUser).child(push_id).setValue(messageMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> btask) {
                                            if (btask.isSuccessful()) {
                                                messageDatabase.child(ChatUser).child(CurrentUser).child(push_id).setValue(messageMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> ctask) {
                                                        if (ctask.isSuccessful()) {
                                                            chatMessage.setText("");
                                                        } else {

                                                        }
                                                    }
                                                });
                                            } else {

                                            }
                                        }
                                    });

                                }
                            });
                        }
                    }
                });
            }
        }


    }

    private void loadMessages()
    {
        messageDatabase.child(CurrentUser).child(ChatUser).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages messages = snapshot.getValue(Messages.class);

                messageAdapterList.add(messages);

                messageAdapter.notifyDataSetChanged();

                messagesList.scrollToPosition(messageAdapterList.size()-1);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendMessage()
    {
        String message = chatMessage.getText().toString();

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-4:00"));
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("KK:mm");
        date.setTimeZone(TimeZone.getTimeZone("GMT-4:00"));
        String localTime = date.format(currentLocalTime);

        if(!TextUtils.isEmpty(message))
        {
            HashMap<String,String> messageMap = new HashMap<>();

            DatabaseReference user_message_push = messageDatabase.child(CurrentUser).child(ChatUser).push();

            String push_id  = user_message_push.getKey();

            messageMap.put("message",message);
            messageMap.put("seen","false");
            messageMap.put("time",localTime);
            messageMap.put("type","text");
            messageMap.put("from",CurrentUser);

            messageDatabase.child(CurrentUser).child(ChatUser).child(push_id).setValue(messageMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        messageDatabase.child(ChatUser).child(CurrentUser).child(push_id).setValue(messageMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> atask) {
                                if(atask.isSuccessful())
                                {
                                    chatMessage.setText("");
                                }
                                else
                                {

                                }
                            }
                        });
                    }
                    else
                    {

                    }
                }
            });
        }
    }
}