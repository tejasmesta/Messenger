package com.example.android.messenger;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class RequestsFragment extends Fragment {

    private View requestsView;
    private RecyclerView requestList;
    private DatabaseReference requestsReference, usersReference;
    private FirebaseUser currentUser;
    private String name;

    public RequestsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        requestsView =  inflater.inflate(R.layout.fragment_requests, container, false);

        requestList = requestsView.findViewById(R.id.requests_list);

        requestList.setLayoutManager(new LinearLayoutManager(getContext()));

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        requestsReference = FirebaseDatabase.getInstance("https://messenger-dc6ce-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Friend Requests").child(currentUser.getUid());

        usersReference = FirebaseDatabase.getInstance("https://messenger-dc6ce-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Users");

        return requestsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Requests>().setQuery(requestsReference,Requests.class).build();

        final FirebaseRecyclerAdapter<Requests,requestsViewHolder> adapter = new FirebaseRecyclerAdapter<Requests, requestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull requestsViewHolder holder, int position, @NonNull Requests model) {
                String user_id = getRef(position).getKey();

                requestsReference.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child("request_type").getValue().toString().equals("received"))
                        {
                            usersReference.child(user_id).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot aSnapshot) {
                                    holder.name.setText(aSnapshot.child("name").getValue().toString());
                                    String image = aSnapshot.child("thumb_image").getValue().toString();
                                    if(!image.toString().equals("default"))
                                    {
                                        Picasso.with(holder.image.getContext()).load(image).into(holder.image);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public requestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.requests_fragment_single,parent,false);
                requestsViewHolder requests = new requestsViewHolder(view);
                return requests;
            }
        };

        requestList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class requestsViewHolder extends RecyclerView.ViewHolder
    {
        TextView name;
        CircleImageView image;

        public requestsViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.request_name);
            image = itemView.findViewById(R.id.request_image);
        }
    }
}