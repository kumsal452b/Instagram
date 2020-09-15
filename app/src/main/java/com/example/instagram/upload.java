package com.example.instagram;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.PermissionInfoCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

public class upload extends AppCompatActivity {
    ImageView imageView;
    EditText command;
    Button btn;
    Bitmap selectedImage;
    FirebaseDatabase database;
    DatabaseReference myRef;
    FirebaseAuth firebaseAuth;
    StorageReference storageReference;
    Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        imageView=findViewById(R.id.imageView);
        btn=findViewById(R.id.button);
        command=findViewById(R.id.commandpost);
        database=FirebaseDatabase.getInstance();
        myRef=database.getReference();
        firebaseAuth=FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(upload.this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){

                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},2);
                }else{
                    Intent intent=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent,1);
                }

            }
        });
    }
    public void tikla(View view){
        final UUID uuid=UUID.randomUUID();

        final StorageReference reference=FirebaseStorage.getInstance().getReference();
        reference.child("images/"+uuid+".png").putFile(uri).addOnSuccessListener(upload.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                StorageReference newreferans=FirebaseStorage.getInstance().getReference("images/"+uuid+".png");
                newreferans.getDownloadUrl().addOnSuccessListener(upload.this, new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String string=uri+"";
                        Toast.makeText(upload.this,string,Toast.LENGTH_LONG).show();
                        FirebaseUser user=firebaseAuth.getCurrentUser();
                        String email=user.getEmail();
                        String userCommit=command.getText().toString();
                        UUID uuid1=UUID.randomUUID();
                        myRef.child("Post").child(uuid1+"").child("userEmail").setValue(email);
                        myRef.child("Post").child(uuid1+"").child("Commit").setValue(userCommit);
                        myRef.child("Post").child(uuid1+"").child("Dowload URL").setValue(string);
                        Toast.makeText(upload.this, "Uploaded Success", Toast.LENGTH_SHORT).show();
                    }
                });


            }
        }).addOnFailureListener(upload.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(upload.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==1 && grantResults.length>0 && permissions[0]==Manifest.permission.READ_EXTERNAL_STORAGE){
            Intent intent=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,1);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1 && data!=null){
            try {
                 uri=MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                selectedImage=MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
                imageView.setImageBitmap(selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}