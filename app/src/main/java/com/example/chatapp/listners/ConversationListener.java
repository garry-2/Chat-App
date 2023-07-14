package com.example.chatapp.listners;

import com.example.chatapp.Models.ChatMessage;
import com.example.chatapp.Models.User;

public interface ConversationListener {

    void onConversationClicked(User user);
}
