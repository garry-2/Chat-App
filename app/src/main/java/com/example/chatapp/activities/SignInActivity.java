package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.R;
import com.example.chatapp.utilities.Constants;
import com.example.chatapp.utilities.PreferenceManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {

    TextView createAccountText;
    MaterialButton signInButton;
    EditText signInEmailEditText;
    EditText signInPasswordEditText;
    ProgressBar signInProgressBar;
    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        preferenceManager = new PreferenceManager(getApplicationContext());

        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }



        signInEmailEditText = findViewById(R.id.singInEmailEditText);
        signInPasswordEditText = findViewById(R.id.signInPasswordEditText);
        createAccountText = findViewById(R.id.createNewAccountText);
        signInButton = findViewById(R.id.signInButton);
        signInProgressBar = findViewById(R.id.signInProgressBar);


        createAccountText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),SignUpActivity.class);
                startActivity(intent);
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isValidSignIn()){
                    signIn();
                }
            }
        });


    }

    private void signIn(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,signInEmailEditText.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,signInPasswordEditText.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME,documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE,documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    else{
                        loading(false);
                        showToast("Sign In failed !");
                    }
                });


    }
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void loading(Boolean signin){
        if(signin){
            signInButton.setVisibility(View.INVISIBLE);
            signInProgressBar.setVisibility(View.VISIBLE);
        }
        else{
            signInButton.setVisibility(View.VISIBLE);
            signInProgressBar.setVisibility(View.INVISIBLE);
        }
    }
    private Boolean isValidSignIn(){
        if(signInEmailEditText.getText().toString().trim().isEmpty()){
            showToast("Enter Email Address");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(signInEmailEditText.getText().toString().trim()).matches()){
            showToast("Enter valid Email Address");
            return false;
        }
        else if(signInPasswordEditText.getText().toString().trim().isEmpty()){
            showToast("Enter Password");
            return false;
        }
        else{
            return true;
        }

    }

}