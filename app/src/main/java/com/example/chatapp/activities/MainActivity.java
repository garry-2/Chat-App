package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.Models.ChatMessage;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.example.chatapp.adapters.RecentConversationsAdapter;
import com.example.chatapp.listners.ConversationListener;
import com.example.chatapp.utilities.Constants;
import com.example.chatapp.utilities.PreferenceManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversationListener{

    PreferenceManager preferenceManager;
    TextView nameTextView;
    RoundedImageView imageProfile;
    RoundedImageView signOutImage;
    FloatingActionButton addFloatingButton;
    RecyclerView recentRecyclerView;
    List<ChatMessage> conversations;

    RecentConversationsAdapter conversationsAdapter;
    FirebaseFirestore database;

    ProgressBar progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferenceManager = new PreferenceManager(getApplicationContext());

        nameTextView = findViewById(R.id.nameText);
        imageProfile = findViewById(R.id.imageProfile);
        signOutImage = findViewById(R.id.signOutImageView);
        addFloatingButton = findViewById(R.id.addFloatingButton);
        recentRecyclerView = findViewById(R.id.userRecycleView);
        progressBar = findViewById(R.id.mainProgressBar);
        Log.d("MainActivity","Inside Main Activity");

        init();
        loadUserDetails();
        getToken();
        listenConversations();

        signOutImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });

        addFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("MainAcitivity","Calling userAcitivity");
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                startActivity(intent);
            }
        });



    }

    private void init(){
        conversations = new ArrayList<>();
        conversationsAdapter = new RecentConversationsAdapter(conversations,getApplicationContext(), this);
        recentRecyclerView.setAdapter(conversationsAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
    }

    private void loadUserDetails(){
        nameTextView.setText(preferenceManager.getString(Constants.KEY_NAME));
        String imageString = preferenceManager.getString(Constants.KEY_IMAGE);
        byte[] bytes = Base64.decode(imageString,Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        imageProfile.setImageBitmap(bitmap);
    }

    private void getToken(){
        FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
        firebaseMessaging.getToken().addOnSuccessListener(this::updateToken);

    }

    private void updateToken(String token){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN,token)
                .addOnFailureListener(e->showToast("unable to update token !"));
    }

    private void signOut(){

        showToast("Signing Out");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));

        HashMap<String,Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());

        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    Intent intent = new Intent(getApplicationContext(),SignInActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e->showToast("Unable to Sign In"));
    }


    private void listenConversations(){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);

        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);

        Log.d("MainActivity","Messages Listened");
    }


    private final EventListener<QuerySnapshot> eventListener = (value, error)->{
        if(error != null){
            return ;
        }
        if(value != null){

            //fetchinng all the messages
            for(DocumentChange documentChange : value.getDocumentChanges()){

                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage= new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;

                    if(preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)){
                        chatMessage.conversationImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversationName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversationId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    }
                    else{
                        chatMessage.conversationImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversationName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversationId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);

                    //Log.d("MainActivity",chatMessage.senderId+" "+chatMessage.receiverId+" "+chatMessage.dateObject);
                    conversations.add(chatMessage);

                    Log.d("MainActivity","conversations added !");


                }else if(documentChange.getType() == DocumentChange.Type.MODIFIED){

                    for(int i = 0;i<conversations.size();i++){
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);

                        if(conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)){
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversations,(obj1,obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversationsAdapter.notifyDataSetChanged();
            recentRecyclerView.smoothScrollToPosition(0);
            recentRecyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

        }

    };

    @Override
    public void onConversationClicked(User user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}