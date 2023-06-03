package com.example.firebasemessenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.firebasemessenger.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference mUsersRef = db.collection("users");

    private EditText mEtEmail, mEtPass, mEtPassConfirm, mEtNickname, mEtSurname, mEtName;
    private Button mBtnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mEtEmail = findViewById(R.id.et_email);
        mEtPass = findViewById(R.id.et_pass);
        mEtPassConfirm = findViewById(R.id.et_pass_confirm);
        mEtNickname = findViewById(R.id.et_nickname);
        mEtSurname = findViewById(R.id.et_surname);
        mEtName = findViewById(R.id.et_name);
        mBtnSignUp = findViewById(R.id.btn_sign_up);

        mBtnSignUp.setOnClickListener(view -> {
            String email = mEtEmail.getText().toString().trim().toLowerCase();
            String pass = mEtPass.getText().toString().trim().toLowerCase();
            String passConfirm = mEtPassConfirm.getText().toString().trim().toLowerCase();
            String nickname = mEtNickname.getText().toString().trim();
            String surname = mEtSurname.getText().toString().trim();
            String name = mEtName.getText().toString().trim();

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(this, "Почта не похожа на почту (╬▔皿▔)╯", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(passConfirm) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(passConfirm)){
                Toast.makeText(this, "Поля паролей не совпадают", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(nickname) || TextUtils.isEmpty(surname) || TextUtils.isEmpty(name)){
                Toast.makeText(this, "Поля должны быть заполнены", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    FirebaseUser user = auth.getCurrentUser();

                    User newUser = new User(user.getEmail(), user.getUid(), nickname, name, surname);
                    FirebaseFirestore db= FirebaseFirestore.getInstance();
                    CollectionReference usersRef = db.collection("users");
                    usersRef.document(newUser.getKey()).set(newUser);
                    Toast.makeText(SignUpActivity.this, "Пользователь " + newUser.getEmail() + " зарегистрирован", Toast.LENGTH_SHORT).show();

                    finish();
                    Intent profileIntent = new Intent(SignUpActivity.this, MainActivity.class);
                    startActivity(profileIntent);
                } else{
                    Toast.makeText(this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(
                    e -> Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}