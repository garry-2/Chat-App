package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.Models.ChatMessage;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.example.chatapp.adapters.ChatAdapter;
import com.example.chatapp.utilities.Constants;
import com.example.chatapp.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ChatActivity extends BaseActivity {

    User receiverUser;
    TextView chatNameText,textAvailabilty;
    AppCompatImageView backImage;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private List<ChatMessage> chatMessages;
    private RecyclerView chatMessageRecyclerview;
    private EditText messaageEditText;
    private FrameLayout layoutSend;
    private ProgressBar progressBar;
    private String conversionid = null;
    private Boolean isReceiverAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatNameText = findViewById(R.id.chatNameText);
        backImage = findViewById(R.id.backImage);
        chatMessageRecyclerview = findViewById(R.id.chatRecyclerView);
        messaageEditText = findViewById(R.id.messageEditText);
        layoutSend = findViewById(R.id.layoutSend);
        progressBar = findViewById(R.id.progressBar);
        textAvailabilty = findViewById(R.id.textAvailabilty);

        Log.d("Chatactivity","ChatActivity Opened");

        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        layoutSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!messaageEditText.getText().toString().equals("")){
                    sendMessage();
                }
                else{
                    Toast.makeText(ChatActivity.this, "Type a message", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loadReceiverDetails();
        init();
        listenMessages();


    }

    private void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(getImageBitmap(receiverUser.image),chatMessages,preferenceManager.getString(Constants.KEY_USER_ID),getApplicationContext());
        chatMessageRecyclerview.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
        Log.d("Chatactivity","Init executed");
    }

    private void sendMessage(){
        HashMap<String,Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
        message.put(Constants.KEY_MESSAGE,messaageEditText.getText().toString());
        message.put(Constants.KEY_TIMESTAMP,new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);



        if(conversionid != null){
            updateConversion(messaageEditText.getText().toString());
        }
        else{
            HashMap<String,Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME,preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));

            conversion.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME,receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE,receiverUser.image);
            Log.d("Chatactivity","Last message : "+messaageEditText.getText().toString());
            conversion.put(Constants.KEY_LAST_MESSAGE,messaageEditText.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP,new Date());
            addConversion(conversion);

        }
        messaageEditText.setText(null);
        Log.d("Chatactivity","Message Sent");

    }

    //handling availabilty of user and online status
    private void listenAvailabilityOfReceiver(){
        database.collection(Constants.KEY_COLLECTION_USERS).document(
                receiverUser.id
        ).addSnapshotListener(ChatActivity.this,(value,error) -> {
            if(error != null){
                return;
            }
            if(value != null){
                if(value.getLong(Constants.KEY_AVAILABILITY) != null){
                    int availability = Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABILITY)).intValue();
                    isReceiverAvailable = availability == 1;
                    //receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);
                }
            }
            if(isReceiverAvailable){
                textAvailabilty.setVisibility(View.VISIBLE);
            }
            else{
                textAvailabilty.setVisibility(View.GONE);
            }

        });

    }

    private void listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverUser.id)
                .addSnapshotListener(eventListener);

        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);

        Log.d("Chatactivity","Messages Listened");
    }


    private final EventListener<QuerySnapshot> eventListener = (value,error)->{
        if(error != null){
            return ;
        }
        if(value != null){
           int count = chatMessages.size();
           //fetchinng all the messages
           for(DocumentChange documentChange : value.getDocumentChanges()){

               if(documentChange.getType() == DocumentChange.Type.ADDED){
                   ChatMessage chatMessage = new ChatMessage();
                   chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                   chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                   chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                   chatMessage.dateTime = getReadableDate(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                   chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                   chatMessages.add(chatMessage);
               }
           }
            //sort messages according to date
            Collections.sort(chatMessages,(obj1,obj2) -> obj1.dateObject.compareTo(obj2.dateObject));

            if(count == 0){
                chatAdapter.notifyDataSetChanged();
            }
            else{
                chatAdapter.notifyItemRangeInserted(chatMessages.size(),chatMessages.size());
                chatMessageRecyclerview.smoothScrollToPosition(chatMessages.size()-1);
            }
            chatMessageRecyclerview.setVisibility(View.VISIBLE);
        }
        progressBar.setVisibility(View.GONE);
        if(conversionid == null){
            checkForConversion();
        }
    };



    private Bitmap getImageBitmap(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
    private void loadReceiverDetails(){
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        chatNameText.setText(receiverUser.name);
    }

    private String getReadableDate(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void addConversion(HashMap<String,Object> conversion){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionid = documentReference.getId());
    }

    private void updateConversion(String message){
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionid);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE,message,
                Constants.KEY_TIMESTAMP,new Date()
        );
    }
    private void checkForConversion(){
        if(chatMessages.size() != 0){
            checkForConversionRemotely(preferenceManager.getString(Constants.KEY_USER_ID),receiverUser.id);
            checkForConversionRemotely(receiverUser.id,preferenceManager.getString(Constants.KEY_USER_ID));
        }
    }

    private void checkForConversionRemotely(String senderId,String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionid = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }


}