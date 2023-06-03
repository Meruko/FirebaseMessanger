package com.example.firebasemessenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {
    private EditText mEtEmail, mEtPass;
    private Button mBtnSignIn;
    private TextView mTvSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        FirebaseUser lastUser = auth.getCurrentUser();
        if (lastUser != null){
            finish();
            Intent profileIntent = new Intent(SignInActivity.this, MainActivity.class);
            profileIntent.putExtra("email", lastUser.getEmail());
            startActivity(profileIntent);
        }

        mEtEmail = findViewById(R.id.et_email);
        mEtPass = findViewById(R.id.et_pass);
        mBtnSignIn = findViewById(R.id.btn_sign_in);
        mTvSignUp = findViewById(R.id.tv_sign_up);

        mBtnSignIn.setOnClickListener(view -> {
            String email = mEtEmail.getText().toString().trim().toLowerCase();
            String pass = mEtPass.getText().toString().trim().toLowerCase();

            auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    finish();
                    Intent profileIntent = new Intent(SignInActivity.this, MainActivity.class);
                    startActivity(profileIntent);
                } else {
                    Toast.makeText(SignInActivity.this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(
                    e -> Toast.makeText(SignInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        mTvSignUp.setOnClickListener(view -> {
            Intent signUpIntent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(signUpIntent);
        });
    }
}