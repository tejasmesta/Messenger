package com.example.android.messenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class
UserProfileActivity extends AppCompatActivity {

    private TextView displayName;
    private TextView displayStatus;
    private ImageView displayImage;
    private DatabaseReference databaseReference;
    private DatabaseReference friendRequestDatabase;
    private DatabaseReference friendsDatabase;
    private DatabaseReference notificationDatabase;
    private ProgressDialog progressDialog;
    private Button send_request, decline_request;
    private String current_state;
    private FirebaseUser currentUser;
    private String unfriendName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        displayName = findViewById(R.id.single_user_display_name);
        displayStatus = findViewById(R.id.single_user_display_status);
        displayImage = findViewById(R.id.single_user_display_image);
        send_request = findViewById(R.id.single_user_friend_request);
        decline_request = findViewById(R.id.single_user_decline_friend_request);

        current_state = "Not friends";

        decline_request.setVisibility(View.INVISIBLE);
        decline_request.setEnabled(false);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        progressDialog  = new ProgressDialog(this);
        progressDialog.setTitle("Loading User data");
        progressDialog.setMessage("Please wait while we load user data");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String user_id = getIntent().getStringExtra("user_id");

        databaseReference = FirebaseDatabase.getInstance("https://messenger-dc6ce-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Users").child(user_id);

        friendRequestDatabase =FirebaseDatabase.getInstance("https://messenger-dc6ce-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Friend Requests");

        friendsDatabase = FirebaseDatabase.getInstance("https://messenger-dc6ce-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Friends");

        notificationDatabase = FirebaseDatabase.getInstance("https://messenger-dc6ce-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("notifications");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue().toString();
                String image = snapshot.child("image").getValue().toString();
                String status = snapshot.child("status").getValue().toString();

                unfriendName = name;

                displayName.setText(name);
                displayStatus.setText(status);
                if(!image.equals("default"))
                {
                    Picasso.with(UserProfileActivity.this).load(image).into(displayImage);
                }

                friendRequestDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChild(user_id))
                        {
                            String requestType = snapshot.child(user_id).child("request type").getValue().toString();
                            if(requestType.equals("sent"))
                            {
                                current_state = "Request sent";
                                send_request.setText("CANCEL FRIEND REQUEST");
                                decline_request.setVisibility(View.INVISIBLE);
                                decline_request.setEnabled(false);
                            }
                            else if(requestType.equals("received"))
                            {
                                current_state = "Request received";
                                send_request.setText("ACCEPT REQUEST");
                                decline_request.setVisibility(View.VISIBLE);
                                decline_request.setEnabled(true);
                            }
                            progressDialog.dismiss();
                        }
                        else
                        {
                            friendsDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.hasChild(user_id))
                                    {
                                        current_state = "Friends";
                                        send_request.setText("UNFRIEND "+unfriendName);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    progressDialog.dismiss();
                                }

                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });

        progressDialog.dismiss();

        send_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_request.setEnabled(false);
                if(current_state.equals("Not friends"))
                {
                    friendRequestDatabase.child(currentUser.getUid()).child(user_id).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                friendRequestDatabase.child(user_id).child(currentUser.getUid()).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> atask) {
                                        if(atask.isSuccessful())
                                        {
                                            HashMap<String,String> notidata = new HashMap<>();
                                            notidata.put("from",currentUser.getUid());
                                            notidata.put("type","request");

                                            notificationDatabase.child(user_id).push().setValue(notidata).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    current_state = "Request sent";
                                                    send_request.setText("CANCEL FRIEND REQUEST");
                                                    decline_request.setVisibility(View.INVISIBLE);
                                                    decline_request.setEnabled(false);
                                                    Toast.makeText(UserProfileActivity.this, "Friend request sent", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(UserProfileActivity.this, "Error sending friend request", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                            else
                            {
                                Toast.makeText(UserProfileActivity.this, "Error sending friend request", Toast.LENGTH_SHORT).show();
                            }
                            send_request.setEnabled(true);
                        }
                    });
                }

                if(current_state.equals("Request sent"))
                {
                    friendRequestDatabase.child(currentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            friendRequestDatabase.child(user_id).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    send_request.setEnabled(true);
                                    current_state = "Not friends";
                                    send_request.setText("SEND FRIEND REQUEST");
                                    decline_request.setVisibility(View.INVISIBLE);
                                    decline_request.setEnabled(false);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(UserProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UserProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                if(current_state.equals("Request received"))
                {
                    String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    friendsDatabase.child(currentUser.getUid()).child(user_id).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            friendsDatabase.child(user_id).child(currentUser.getUid()).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    friendRequestDatabase.child(currentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            friendRequestDatabase.child(user_id).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    send_request.setEnabled(true);
                                                    current_state = "Friends";
                                                    send_request.setText("UNFRIEND "+unfriendName);
                                                    decline_request.setVisibility(View.INVISIBLE);
                                                    decline_request.setEnabled(true);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(UserProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(UserProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(UserProfileActivity.this, "Request Failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UserProfileActivity.this, "Request Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                if(current_state.equals("Friends"))
                {
                    friendsDatabase.child(currentUser.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                friendsDatabase.child(user_id).child(currentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> atask) {
                                        if(atask.isSuccessful())
                                        {
                                            send_request.setEnabled(true);
                                            current_state = "Not friends";
                                            send_request.setText("SEND FRIEND REQUEST");
                                            decline_request.setVisibility(View.INVISIBLE);
                                            decline_request.setEnabled(false);
                                        }
                                        else
                                        {
                                            Toast.makeText(UserProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                            else
                            {
                                Toast.makeText(UserProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        decline_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendRequestDatabase.child(currentUser.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            friendRequestDatabase.child(user_id).child(currentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> atask) {
                                    if(atask.isSuccessful())
                                    {
                                        decline_request.setVisibility(View.INVISIBLE);
                                        decline_request.setEnabled(false);
                                        current_state = "Not friends";
                                        send_request.setText("CANCEL FRIEND REQUEST");
                                    }
                                    else
                                    {
                                        Toast.makeText(UserProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(UserProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }
}