package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.admin.PreferentialNetworkServiceConfig;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.example.chatapp.adapters.UserAdapter;
import com.example.chatapp.listners.UserListener;
import com.example.chatapp.utilities.Constants;
import com.example.chatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends BaseActivity implements UserListener {

    RoundedImageView userBackButton;
    TextView selectUserText;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    PreferenceManager preferenceManager;
    TextView errorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        preferenceManager = new PreferenceManager(getApplicationContext());
        Log.d("UserActivity","Opened !");

        selectUserText = findViewById(R.id.selectUserText);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.userRecycleView);
        errorTextView = findViewById(R.id.errorTextView);
        userBackButton = findViewById(R.id.userBackButton);
        getUsers();

        userBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void getUsers(){
        Log.d("UserActivity","fetching users");
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() != null){
                        List<User> users = new ArrayList<>();

                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            //for skipping the current user and selecting other users
                            if(currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user = new User();

                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.id = queryDocumentSnapshot.getId();
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            users.add(user);
                        }
                        //now setting RecyclerView
                        if(users.size() > 0){

                            UserAdapter userAdapter = new UserAdapter(users,getApplicationContext(),this);
                            recyclerView.setAdapter(userAdapter);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                        else{
                            showErrorMessage();
                            recyclerView.setVisibility(View.INVISIBLE);
                            errorTextView.setVisibility(View.VISIBLE);
                        }
                    }
                    else{
                        showErrorMessage();
                    }

                });
    }

    private void showErrorMessage(){
        errorTextView.setText("No User Available");
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        errorTextView.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean flag){
        if(flag){
            recyclerView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }
        else{
            recyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}