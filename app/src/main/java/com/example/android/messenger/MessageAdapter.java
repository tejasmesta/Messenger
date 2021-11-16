package com.example.android.messenger;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageListViewHolder>{

    private List<Messages> MessageList;
    private FirebaseUser currentUser;


    public MessageAdapter(List<Messages> MessageList) {
        this.MessageList = MessageList;

    }

    public MessageListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);
        return new MessageListViewHolder(v);
    }

    public static class MessageListViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        CircleImageView profileImage;
        TextView time;
        ImageView messageImage;

        public MessageListViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message_text);
            profileImage = itemView.findViewById(R.id.message_display);
            time = itemView.findViewById(R.id.message_time);
            messageImage = itemView.findViewById(R.id.message_image_send);

        }
    }

    public void onBindViewHolder(@NonNull MessageListViewHolder holder, int position) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String currentUSER = currentUser.getUid();

        Messages m = MessageList.get(position);

        DatabaseReference mDatabase = FirebaseDatabase.getInstance("https://messenger-dc6ce-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Users").child(m.getFrom());

        String from = m.getFrom();
        String message_type = m.getType();

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String thumb_image = snapshot.child("thumb_image").getValue().toString();
                if(!thumb_image.equals("default"))
                {
                    Picasso.with(holder.profileImage.getContext()).load(thumb_image).into(holder.profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(message_type.equals("text")) {
            if(from.equals(currentUSER))
            {
                holder.messageText.setBackgroundResource(R.drawable.message_text_bg1);
                holder.messageText.setTextColor(Color.BLACK);

            }
            else {
                holder.messageText.setBackgroundResource(R.drawable.message_text_bg);
                holder.messageText.setTextColor(Color.BLACK);

            }
            holder.messageText.setVisibility(View.VISIBLE);
            holder.time.setVisibility(View.VISIBLE);
            holder.messageText.setText(m.getMessage());
            holder.time.setText(m.getTime());
            holder.messageImage.setVisibility(View.INVISIBLE);
        }
        else
        {
            holder.messageText.setVisibility(View.INVISIBLE);
            holder.time.setText(m.getTime());
            holder.messageImage.setVisibility(View.VISIBLE);
            Picasso.with(holder.messageImage.getContext()).load(m.getMessage()).placeholder(R.drawable.default_profile_pic).into(holder.messageImage);
        }
    }

    public int getItemCount() {
        return MessageList.size();
    }
}
