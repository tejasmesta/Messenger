package com.example.android.messenger;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UsersListAdapter extends RecyclerView.Adapter<UsersListAdapter.UsersListViewHolder>
{
    Context context;

    ArrayList<Users> list;

    public UsersListAdapter(Context context, ArrayList<Users> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public UsersListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.users_item,parent,false);
        return new UsersListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersListViewHolder holder, int position) {
        Users user = list.get(position);
        holder.name.setText(user.getName());
        holder.status.setText(user.getStatus());
        if(!user.getThumb_image().equals("default"))
        {
            Picasso.with(context).load(user.getThumb_image()).into(holder.thumb_image);
        }
        String uID = user.getuID();
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,UserProfileActivity.class);
                intent.putExtra("user_id",uID);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class UsersListViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView status;
        ImageView thumb_image;

        public UsersListViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.user_name);
            status = itemView.findViewById(R.id.user_status);
            thumb_image = itemView.findViewById(R.id.user_image);

        }

    }
}
