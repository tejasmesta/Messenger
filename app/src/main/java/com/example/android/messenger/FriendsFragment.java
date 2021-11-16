package com.example.android.messenger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class FriendsFragment extends Fragment {

    private View FriendsView;
    private RecyclerView friendsList;
    private DatabaseReference friendsReference, usersReference;
    private FirebaseUser currentUser;
    private String name;

    public FriendsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FriendsView =  inflater.inflate(R.layout.fragment_friends, container, false);

        friendsList = FriendsView.findViewById(R.id.friends_list);

        friendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        friendsReference = FirebaseDatabase.getInstance("https://messenger-dc6ce-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Friends").child(currentUser.getUid());

        usersReference = FirebaseDatabase.getInstance("https://messenger-dc6ce-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Users");

        return FriendsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Users>().setQuery(friendsReference,Users.class).build();

        final FirebaseRecyclerAdapter<Users,FriendsViewHolder> adapter = new FirebaseRecyclerAdapter<Users, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FriendsViewHolder holder, int position, @NonNull Users model) {
                String userid = getRef(position).getKey();

                friendsReference.child(userid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String date = snapshot.child("date").getValue().toString();
                        holder.since.setText("Friends since "+date);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                usersReference.child(userid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        name = snapshot.child("name").getValue().toString();
                        String thumb_image = snapshot.child("thumb_image").getValue().toString();

                        holder.name.setText(name);
                        if(!thumb_image.equals("default"))
                        {
                            Picasso.with(getContext()).load(thumb_image).into(holder.thumb_image);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CharSequence options[] = new CharSequence[]{"Open Profile","Send Message"};

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("WANT TO?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which==0)
                                {
                                    Intent intent = new Intent(getContext(),UserProfileActivity.class);
                                    intent.putExtra("user_id",userid);
                                    startActivity(intent);
                                }
                                if(which==1)
                                {
                                    Intent intent = new Intent(getContext(),ChatsActivity.class);
                                    intent.putExtra("user_name",name);
                                    intent.putExtra("user_id",userid);
                                    startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                });
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_item,parent,false);
                FriendsViewHolder friendsViewHolder = new FriendsViewHolder(view);
                return friendsViewHolder;
            }
        };

        friendsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder
    {
        TextView name;
        TextView since;
        CircleImageView thumb_image;
        CircleImageView isOnline;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id._name);
            since = itemView.findViewById(R.id.friends_since);
            thumb_image = itemView.findViewById(R.id._image);
            isOnline = itemView.findViewById(R.id.isOnline);
        }
    }
}