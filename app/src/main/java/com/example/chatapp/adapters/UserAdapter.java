package com.example.chatapp.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.helper.widget.Layer;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.example.chatapp.listners.UserListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<User> users;
    Context context;
    private final UserListener userListener;



    public UserAdapter(List<User> users, Context context,UserListener userListener) {
        this.users = users;
        this.context = context;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_container,parent,false);

        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));


    }

    @Override
    public int getItemCount() {
        return users.size();
    }




    public class UserViewHolder extends RecyclerView.ViewHolder {

        TextView nameText,emailText;
        RoundedImageView userImage;
        LinearLayout userLayout;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            nameText = itemView.findViewById(R.id.ItemUsertextName);
            emailText =itemView.findViewById(R.id.ItemUserTextEmail);
            userImage = itemView.findViewById(R.id.ItemUserImage);
            userLayout = itemView.findViewById(R.id.userLayout);

        }

        private void setUserData(User user){

            //Log.d("UserAdapter","Name : "+user.name+" Email : "+user.email+" Image : "+user.image);
            nameText.setText(user.name);
            emailText.setText(user.email);
            userImage.setImageBitmap(getImage(user.image));
            userLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    userListener.onUserClicked(user);
                }
            });
        }

        private Bitmap getImage(String encodedImage){

            byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        }
    }
}
