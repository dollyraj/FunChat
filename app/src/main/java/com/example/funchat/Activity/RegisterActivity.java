package com.example.funchat.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.funchat.R;
import com.example.funchat.ModelClass.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {

    TextView signIn;
    Button btn_signUp;
    CircleImageView profile_image;
    EditText reg_name,reg_email,reg_password,reg_cPassword;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;

    Uri imageUri;
    String imageURI;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);

        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();

        signIn=findViewById(R.id.signup_signin_btn);
        profile_image=findViewById(R.id.profile_img);
        reg_name=findViewById(R.id.ed_signin_name);
        reg_email=findViewById(R.id.ed_signin_email);
        reg_password=findViewById(R.id.ed_signin_pass);
        reg_cPassword=findViewById(R.id.ed_signin_cnfrm_pass);
        btn_signUp=findViewById(R.id.signup_btn);

        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                String name=reg_name.getText().toString();
                String email=reg_email.getText().toString();
                String password=reg_password.getText().toString();
                String cPassword=reg_cPassword.getText().toString();
                String status="Hey There I'm using this application";


                if(TextUtils.isEmpty(name) || TextUtils.isEmpty(email) ||
                        TextUtils.isEmpty(password) || TextUtils.isEmpty(cPassword)){
                    Toast.makeText(RegisterActivity.this,"Please enter valid data",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    reg_email.setError("Please enter valid email");
                    Toast.makeText(RegisterActivity.this,"Please enter valid Email",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }else if(!password.equals(cPassword)){
                    Toast.makeText(RegisterActivity.this,"Password doesn't match",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }else if(password.length()<6){

                    Toast.makeText(RegisterActivity.this,"Please enter minimum 6 character password",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }else {


                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                               // progressDialog.dismiss();
                                //Toast.makeText(RegisterActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                                DatabaseReference reference=database.getReference().child("user").child(auth.getUid());
                                StorageReference storageReference=storage.getReference().child("upload").child(auth.getUid());

                                if(imageUri!=null){
                                    storageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            if(task.isSuccessful()){
                                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        imageURI=uri.toString();
                                                        Users users=new Users(name,email,status,imageURI,auth.getUid());
                                                        reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                                                                }else{
                                                                    Toast.makeText(RegisterActivity.this,"Error in creating new user",Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });

                                                    }
                                                });
                                            }
                                        }
                                    });
                                }else{
                                    String status="Hey There I'm using this application";
                                    Uri uri = Uri.parse("android.resource://com.example.funchat/drawable/profile_pic");
                                    try {
                                        InputStream stream = getContentResolver().openInputStream(uri);
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    imageURI=uri.toString();
                                   // imageURI="https://firebasestorage.googleapis.com/v0/b/funchat-7f2d7.appspot.com/o/default%20profile%20image%2Fprofile_pic.png?alt=media&token=43a6d7dd-6648-4c22-9697-dc1c7f5fbca6";
                                    Users users=new Users(name,email,status,imageURI,auth.getUid());
                                    reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegisterActivity.this,HomeActivity.class));
                                            }else{
                                                Toast.makeText(RegisterActivity.this,"Error in creating new user",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            } else {
                                progressDialog.dismiss();
                                    Toast.makeText(RegisterActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
                                task.getException().printStackTrace();
                            }
                        }
                    });
                }
            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==10){
            if(data!=null){
                imageUri=data.getData();
                profile_image.setImageURI(imageUri);
            }
        }
    }
}