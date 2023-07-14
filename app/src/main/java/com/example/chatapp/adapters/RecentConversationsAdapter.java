package com.example.chatapp.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.Models.ChatMessage;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.example.chatapp.listners.ConversationListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversionViweHolder> {


    private final List<ChatMessage> chatMessages;
    private final Context context;
    private final ConversationListener conversationListener;

    public RecentConversationsAdapter(List<ChatMessage> chatMessages, Context context, ConversationListener conversationListener) {
        this.chatMessages = chatMessages;
        this.context = context;
        this.conversationListener = conversationListener;
    }

    @NonNull
    @Override
    public RecentConversationsAdapter.ConversionViweHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecentConversationsAdapter.ConversionViweHolder(LayoutInflater.from(context).inflate(R.layout.item_user_recent_conversation,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecentConversationsAdapter.ConversionViweHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public class ConversionViweHolder extends RecyclerView.ViewHolder {

        TextView nameText,recentMessageText;
        RoundedImageView imageView;

        LinearLayout recentLayout;
        public ConversionViweHolder(@NonNull View itemView) {
            super(itemView);

            nameText = itemView.findViewById(R.id.recentItemUsertextName);
            recentMessageText = itemView.findViewById(R.id.recentItemtextMessage);
            imageView = itemView.findViewById(R.id.recentItemUserImage);
            recentLayout = itemView.findViewById(R.id.recentConversationLayout);
        }

        public void setData(ChatMessage chatMessage){

            //Log.d("RecentConversationAdapter",chatMessage.conversationName+chatMessage.message);
            nameText.setText(chatMessage.conversationName);
            recentMessageText.setText(chatMessage.message);
            imageView.setImageBitmap(getRecentImageBitmap(chatMessage.conversationImage));
            recentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    User user = new User();
                    user.name = chatMessage.conversationName;
                    user.id = chatMessage.conversationId;
                    user.image = chatMessage.conversationImage;
                    conversationListener.onConversationClicked(user);
                }
            });
        }
    }

    private Bitmap getRecentImageBitmap(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }


}
