package com.example.chatapp.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.Telephony;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.Models.ChatMessage;
import com.example.chatapp.R;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


    private final Bitmap bitmapReceivedImage;
    private final List<ChatMessage> chatMessages;
    private final String senderId;
    private final Context context;


    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED =2;

    public ChatAdapter(Bitmap bitmapReceivedImage, List<ChatMessage> chatMessages, String senderId, Context context) {
        this.bitmapReceivedImage = bitmapReceivedImage;
        this.chatMessages = chatMessages;
        this.senderId = senderId;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == VIEW_TYPE_SENT){
            return new SentMessageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_container_sent_message,parent,false));
        }
        else{
            return new ReceivedMessageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_container_received_message,parent,false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if(getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder)holder).setData(chatMessages.get(position));
        }
        else{
            ((ReceivedMessageViewHolder)holder).setData(chatMessages.get(position),bitmapReceivedImage);
        }

    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }
        else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{

        TextView TextMessage,TextDateTime;
        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            TextMessage  = itemView.findViewById(R.id.textMessage);
            TextDateTime = itemView.findViewById(R.id.textDateTime);
        }

        public void setData(ChatMessage chatMessage){
            TextMessage.setText(chatMessage.message);
            TextDateTime.setText(chatMessage.dateTime);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{

        TextView TextMessage,TextDateTime;
        RoundedImageView receiverImage;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            TextMessage  = itemView.findViewById(R.id.receivedTextMessage);
            TextDateTime = itemView.findViewById(R.id.receivedTextDateTime);
            receiverImage = itemView.findViewById(R.id.receivedchatImage);
        }

        public void setData(ChatMessage chatMessage,Bitmap receivedImage){
            TextMessage.setText(chatMessage.message);
            TextDateTime.setText(chatMessage.dateTime);
            receiverImage.setImageBitmap(receivedImage);
        }
    }


}
