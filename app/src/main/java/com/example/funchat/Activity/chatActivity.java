package com.example.funchat.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Activity;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.abedelazizshe.lightcompressorlibrary.CompressionListener;
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor;
import com.abedelazizshe.lightcompressorlibrary.VideoQuality;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.example.funchat.Adapter.MessagesAdapter;
import com.example.funchat.BuildConfig;
import com.example.funchat.ModelClass.Messages;
import com.example.funchat.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.FileUtils;
import com.iceteck.silicompressorr.SiliCompressor;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.internal.Intrinsics;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class chatActivity extends AppCompatActivity {
    private static final int REQUEST_CORD_PERMISSION = 332;

    Button sendVideo;
    String fpath;

    private Uri imagemsgUri,videoUri;
    FusedLocationProviderClient fusedLocationProviderClient;
    List<Address> addresses;

    String ReceiverImage, ReceiverUID, ReceiverName, SenderUID,currAddress;
    CircleImageView profileImage;
    TextView receiverName;
    FirebaseDatabase database;
    FirebaseAuth firebaseAuth;
    FirebaseStorage storage;
    FirebaseUser firebaseUser;
    public static String sImage;
    public static String rImage;

    CardView cameraBtn,videoBtn;
    FloatingActionButton sendBtn;
    EditText edtMessage;
    ImageButton attachFileBtn;

    String senderRoom, receiverRoom;

    RecyclerView messageAdapter;
    ArrayList<Messages> messagesArrayList;

    MessagesAdapter adapter;

    RecordView recordView;
    RecordButton recordButton;
    //Audio
    private MediaRecorder mediaRecorder;
    private String audio_path;
    private String sTime;
   // private Uri fileUri;

   // VideoView videoView;
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //videoView=findViewById(R.id.video_msg);
        attachFileBtn=findViewById(R.id.attach_file);
      //  sendVideo=findViewById(R.id.send_video);
        videoBtn=findViewById(R.id.videoBtn);
        cameraBtn = findViewById(R.id.cameraBtn);
        sendBtn = findViewById(R.id.sendBtn);
        edtMessage = findViewById(R.id.edtMessage);


        recordView = (RecordView) findViewById(R.id.record_view);
        recordButton = (RecordButton) findViewById(R.id.record_button);

        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        firebaseUser=FirebaseAuth.getInstance().getCurrentUser();

        ReceiverName = getIntent().getStringExtra("name");
        ReceiverImage = getIntent().getStringExtra("ReceiverImage");
        ReceiverUID = getIntent().getStringExtra("uid");
        Log.d("chatActivity", "onCreate: " + ReceiverUID + " " + firebaseAuth.getUid());


        profileImage = findViewById(R.id.user_image);
        receiverName = findViewById(R.id.receiver_name);
        messageAdapter = findViewById(R.id.messageAdapter);

        Picasso.get().load(ReceiverImage).into(profileImage);
        receiverName.setText("" + ReceiverName);

        messagesArrayList = new ArrayList<>();
        adapter = new MessagesAdapter(chatActivity.this, messagesArrayList);

        builder=new AlertDialog.Builder(chatActivity.this);


        attachFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[]=new CharSequence[]
                        {
                                "Images",
                                "Videos",
                        };

                builder.setTitle("Select the File");

                Log.d("chatActivity", "onClick: "+"attachfile");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0){
                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Select Video"),434);

                        }
                        if(i==1){
                            //checker="video";
                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("video/*");
                            startActivityForResult(intent.createChooser(intent,"Select Video"),438);
                        }

//                        if(i==2){
//                            Intent intent=new Intent();
//                            intent.setAction(Intent.ACTION_GET_CONTENT);
//                            intent.setType("application/pdf/*");
//                            startActivityForResult(intent.createChooser(intent,"Select Video"),438);
//                        }
                    }
                });

                builder.create().show();
            }
        });


        edtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(edtMessage.getText().toString())) {
                    sendBtn.setVisibility(View.INVISIBLE);
                    recordButton.setVisibility(View.VISIBLE);
                } else {
                    sendBtn.setVisibility(View.VISIBLE);
                    recordButton.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //linearLayoutManager.setStackFromEnd(true);
        messageAdapter.setLayoutManager(linearLayoutManager);
        messageAdapter.setAdapter(adapter);
        messageAdapter.getLayoutManager().scrollToPosition(adapter.getItemCount()-1);

        SenderUID = firebaseAuth.getUid();

        senderRoom = SenderUID + ReceiverUID;
        receiverRoom = ReceiverUID + SenderUID;


        //VOICE
        recordButton.setRecordView(recordView);

        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {


                if (!checkPermissionFromDevice()) {
                    edtMessage.setVisibility(View.INVISIBLE);
                    cameraBtn.setVisibility(View.INVISIBLE);

                    startRecord();
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator != null) {
                        vibrator.vibrate(100);
                    }

                } else {
                    requestPermission();
                }


            }

            @Override
            public void onCancel() {
                try {
                    mediaRecorder.reset();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish(long recordTime) {

                cameraBtn.setVisibility(View.VISIBLE);
                edtMessage.setVisibility(View.VISIBLE);

                //Stop Recording..
                try {
                    sTime = getHumanTimeText(recordTime);
                    stopRecord();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onLessThanSecond() {
                cameraBtn.setVisibility(View.VISIBLE);
                edtMessage.setVisibility(View.VISIBLE);
            }
        });


        //Log.d("chatActivity", "onCreate: "+firebaseAuth.getUid());

        DatabaseReference reference = database.getReference().child("user").child(firebaseAuth.getUid());
        DatabaseReference chatReference = database.getReference().child("chats").child(senderRoom).child("messages");


        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                messagesArrayList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    try {

                            if(messages.getSenderId().equals(firebaseUser.getUid()) && messages.getReceiverId().equals(ReceiverUID) || messages.getReceiverId().equals(firebaseUser.getUid()) && messages.getSenderId().equals(ReceiverUID)){
                                messagesArrayList.add(messages);
                            }

                    }catch(Exception e){
                        e.printStackTrace();
                    }


                }

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sImage = snapshot.child("imageUri").getValue().toString();
                rImage = ReceiverImage;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//
        sendBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //sendText();
                //askLocationPermission();
                String message = edtMessage.getText().toString();
                if (message.isEmpty()) {
                    Toast.makeText(chatActivity.this, "Please enter valid message", Toast.LENGTH_SHORT).show();
                    return;
                }

                edtMessage.setText("");
                Date dateTime = new Date();
                String date= new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dateTime);
                String time=new SimpleDateFormat("hh:mm a",Locale.getDefault()).format(dateTime);
                if (ActivityCompat.checkSelfPermission(chatActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //getCurrLocation();

                    fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            Location location = task.getResult();
                            Log.d("chatActivity", "onComplete: "+location);
                            Toast.makeText(chatActivity.this, "location", Toast.LENGTH_SHORT).show();
                            if (location != null) {
                                try {
                                    Geocoder geocoder = new Geocoder(chatActivity.this, Locale.getDefault());

                                    //initialize address list
                                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    currAddress=addresses.get(0).getAddressLine(0);

                                    Messages messages = new Messages(message, SenderUID,ReceiverUID,"TEXT", "",currAddress, date,time);

                                    database = FirebaseDatabase.getInstance();
                                    database.getReference().child("chats")
                                            .child(senderRoom).
                                            child("messages").
                                            push()
                                            .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            database.getReference().child("chats")
                                                    .child(receiverRoom).
                                                    child("messages").
                                                    push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                }
                                            });
                                        }
                                    });

                                    DatabaseReference chatRef1=FirebaseDatabase.getInstance().getReference("Chatlist").child(firebaseUser.getUid()).child(ReceiverUID);
                                    chatRef1.child("chatid").setValue(ReceiverUID);

                                    DatabaseReference chatRef2=FirebaseDatabase.getInstance().getReference("Chatlist").child(ReceiverUID).child(firebaseUser.getUid());
                                    chatRef1.child("chatid").setValue(firebaseUser.getUid());

                                    //Log.d("chatActivity", "onComplete1: "+currAddress);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                } else {
                    ActivityCompat.requestPermissions(chatActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
                }

            }
        });

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(chatActivity.this, "camera button is clicked", Toast.LENGTH_SHORT).show();
                askCameraPermission("image");

            }
        });


        videoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(chatActivity.this, "video button is clicked", Toast.LENGTH_SHORT).show();
                askCameraPermission("video");
            }
        });


    }






    private void askCameraPermission(String type) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        } else {

            if(type == "image")
            openCamera();
            else if(type == "video")
             openVideoRecorder();

        }
    }




    private void openVideoRecorder() {

        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        //videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
//
//        if(videoIntent.resolveActivity(getPackageManager()) != null){
//            try{
//                videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,10);
//                videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,1);
//
//            }
//
//        }

       startActivityForResult(videoIntent, 103);


    }



    private void openCamera() {
        //Toast.makeText(this, "Camera Open request", Toast.LENGTH_SHORT).show();
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(cameraIntent.resolveActivity(getPackageManager()) !=null){
         startActivityForResult(cameraIntent, 102);
        }
        String timeStamp = new SimpleDateFormat("yyyyMMDD_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";

        try {
            File file = File.createTempFile("IMG_" + timeStamp, ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            if (file != null) {
                imagemsgUri = FileProvider.getUriForFile(this, "com.example.funchat.provider", file);
                Log.d("chatActivity", "openCamera: " + imagemsgUri);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imagemsgUri);
                cameraIntent.putExtra("listPhotoName", imageFileName);
                startActivityForResult(cameraIntent, 102);
            } else {
                Log.d("chatActivity", "openCamera: " + "hello");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }




    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 102) && (resultCode == RESULT_OK) && data!=null) {

            Log.d("TAG", "onActivityimageResult: "+imagemsgUri);
//            Bundle extras=data.getExtras();
//            Bitmap imageBitmap= (Bitmap) extras.get("data");
            uploadToFirebase();
        } else if(requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            videoUri=data.getData();

            Log.d("chatActivity", "onActivityResult: "+"uploadvideo");

            uploadVideoToFirebase(videoUri);

        }else if(requestCode==434 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            imagemsgUri=data.getData();
            Log.d("TAG", "onActivityResult: "+imagemsgUri);
            uploadToFirebase();

        }else if(((requestCode == 103) && (resultCode == RESULT_OK))){
           Log.d("103", "onActivityResultvideo: " + Environment.getExternalStorageState());
           videoUri=data.getData();


         //new VideoCompressAsyncTask(this).execute("false",videoUri.toString(), f.getPath());
           // new VideoCompressAsyncTask(this).execute(false,f.getAbsolutePath(),f.getParent());

//            try {
//                fpath=getMediaPath(this,videoUri);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//
//            try {
//                fpath=saveVideoFile(this,fpath).getPath(); //the compressed video location
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//           Log.d("103", "onActivityResultvideo: " + fpath);
//            try {
//                Log.d("TAG", "onActivityResult: "+getMediaPath(this,videoUri));
//                compressVideo(getMediaPath(this,videoUri));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }




       uploadVideoToFirebase(videoUri);
        }else{
            Log.d("chatActivity", "onActivityResult: " + data);
        }

    }


    private void uploadToFirebase() {
        // final Uri uriAudio = Uri.fromFile(new File(audio_path));
        askLocationPermission();

//        StorageReference audioRef=storage.getReference().child("voice/"+System.currentTimeMillis());
        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("Images/" + System.currentTimeMillis());
        Log.d("chatActivity", "uploadToFirebase: " + imagemsgUri);
        //  Log.d("chatActivity", "onComplete: "+currAddress);

        if (imagemsgUri != null) {

            imageRef.putFile(imagemsgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;
                    Uri downloadUrl = uriTask.getResult();

                    String imageUrl = String.valueOf(downloadUrl);

                    Log.d("chatActivity", "onSuccess: " + imageUrl);
                    Date dateTime = new Date();
                    String date= new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dateTime);
                    String time=new SimpleDateFormat("hh:mm a",Locale.getDefault()).format(dateTime);

                    Messages messages = new Messages("", SenderUID, ReceiverUID,"IMAGE", imageUrl,currAddress,date,time);


                    database = FirebaseDatabase.getInstance();
                    database.getReference().child("chats")
                            .child(senderRoom).
                            child("messages").
                            push()
                            .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            database.getReference().child("chats")
                                    .child(receiverRoom).
                                    child("messages").
                                    push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });
                        }
                    });
//
                }
            });
        } else {
            Log.d("chatActivity", "onFailure: " + imagemsgUri);
        }
    }


    private void uploadVideoToFirebase(Uri finalvideoUri) {
        askLocationPermission();

        StorageMetadata vediometadata = new StorageMetadata.Builder()
                .setContentType("video/mp4")
                .build();

        StorageReference videoRef = FirebaseStorage.getInstance().getReference().child("Videos/" + System.currentTimeMillis());
        Log.d("chatActivity", "uploadvideoToFirebase: " + finalvideoUri);
        //  Log.d("chatActivity", "onComplete: "+currAddress);

        if (finalvideoUri != null) {


//            File file=new File(SiliCompressor.with(this).compress(FileUtils.getPath(this,videoUri),new File(this.getCacheDir(),"temp")));
//            Uri uri=Uri.fromFile(file);
      Log.d("chatActivity", "uploadvideoToFirebase: " + finalvideoUri);

            videoRef.putFile(finalvideoUri,vediometadata).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;
                    Uri downloadUrl = uriTask.getResult();

                    String fileUrl = String.valueOf(downloadUrl);

                    Log.d("chatActivity", "onSuccessVideo: " + fileUrl);
                    Date dateTime = new Date();
                    String date= new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dateTime);
                    String time=new SimpleDateFormat("hh:mm a",Locale.getDefault()).format(dateTime);

                    Messages messages = new Messages("", SenderUID, ReceiverUID,"VIDEO", fileUrl,currAddress,date,time);


                    database = FirebaseDatabase.getInstance();
                    database.getReference().child("chats")
                            .child(senderRoom).
                            child("messages").
                            push()
                            .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            database.getReference().child("chats")
                                    .child(receiverRoom).
                                    child("messages").
                                    push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });
                        }
                    });
//
                }
            });
        } else {
            Log.d("chatActivity", "onFailure: " + videoUri);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 101){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                openCamera();
            }else{
                Toast.makeText(this,"Camera Permission is required to use camera",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startRecord(){
        setUpMediaRecorder();

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
           // mediaRecorder.reset();
            //  Toast.makeText(InChatActivity.this, "Recording...", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(chatActivity.this, "Recording Error , Please restart your app ", Toast.LENGTH_LONG).show();
        }

    }

    private void setUpMediaRecorder() {


        //String path_save = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + UUID.randomUUID().toString() + "audio_record.m4a";
        String path_save=getApplicationContext().getFilesDir().getPath();
        Log.d("chatActivity", "setUpMediaRecorder: "+path_save);
        File file= new File(path_save);

        Long date=new Date().getTime();
        Date current_time = new Date(Long.valueOf(date));


        mediaRecorder=new MediaRecorder();

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setAudioChannels(1);
        mediaRecorder.setAudioSamplingRate(8000);
        mediaRecorder.setAudioEncodingBitRate(44100);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);//AudioEncoder.AMR_NB
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);//AudioEncoder.AMR_NB

        if (!file.exists()){
            file.mkdirs();
        }


        audio_path =file.getAbsolutePath()+"/"+"_"+current_time+".m4a";//.amr
        mediaRecorder.setOutputFile(file.getAbsolutePath()+"/"+"_"+current_time+".m4a");



    }

    private void stopRecord(){
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;

                sendVoice(audio_path);


            } else {
                Toast.makeText(getApplicationContext(), "Null", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Stop Recording Error :" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("DefaultLocale")
    private String getHumanTimeText(long milliseconds) {
        return String.format("%02d",
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    private boolean checkPermissionFromDevice() {
        int write_external_strorage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return write_external_strorage_result == PackageManager.PERMISSION_DENIED || record_audio_result == PackageManager.PERMISSION_DENIED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        }, REQUEST_CORD_PERMISSION);
    }

    private void sendVoice(String audio_path){


        askLocationPermission();
        final Uri uriAudio = Uri.fromFile(new File(audio_path));

        StorageMetadata audiometadata = new StorageMetadata.Builder()
                .setContentType("audio/mp3")
                .build();


        StorageReference audioRef=storage.getReference().child("voice/"+System.currentTimeMillis());
       // Log.d("chatActivity", "stopRecord: "+audio_path);
        //final StorageReference audioRef = FirebaseStorage.getInstance().getReference().child("Chats/Voice/" + System.currentTimeMillis());
        audioRef.putFile(uriAudio,audiometadata).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot audioSnapshot) {
                Task<Uri> urlTask = audioSnapshot.getStorage().getDownloadUrl();
                while (!urlTask.isSuccessful()) ;
                Uri downloadUrl = urlTask.getResult();
                String voiceUrl = String.valueOf(downloadUrl);
               // Log.d("chatActivity", "onSuccess: "+voiceUrl);
                Date dateTime=new Date();
                String date= new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dateTime);
                String time=new SimpleDateFormat("hh:mm a",Locale.getDefault()).format(dateTime);
                Messages messages=new Messages("",SenderUID,ReceiverUID,"VOICE",voiceUrl,currAddress,date,time);


                database=FirebaseDatabase.getInstance();
                database.getReference().child("chats")
                        .child(senderRoom).
                        child("messages").
                        push()
                        .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        database.getReference().child("chats")
                                .child(receiverRoom).
                                child("messages").
                                push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
                    }
                });


            }
        });

    }

    private void askLocationPermission() {

        if (ActivityCompat.checkSelfPermission(chatActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //getCurrLocation();

            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    Log.d("chatActivity", "onComplete: "+location);
                    Toast.makeText(chatActivity.this, "location", Toast.LENGTH_SHORT).show();
                    if (location != null) {
                        try {
                            Geocoder geocoder = new Geocoder(chatActivity.this, Locale.getDefault());

                            //initialize address list
                            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            currAddress=addresses.get(0).getAddressLine(0);

                            Log.d("chatActivity", "onComplete1: "+currAddress);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(chatActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }

    }


//
//
//class  VideoCompressAsyncTask extends AsyncTask<String,String,String>{
//
//
//        Context mcontext;
//        Dialog dialog;
//
//    public VideoCompressAsyncTask(Context mcontext) {
//        this.mcontext = mcontext;
//    }
//
//    @Override
//    protected void onPreExecute() {
//        super.onPreExecute();
//        dialog= ProgressDialog.show(mcontext,"","Compressing....");
//    }
//
//    @Override
//    protected String doInBackground(String... strings) {
//        String videoPath=null;
//        Uri uri=Uri.parse(strings[1]);
//        Log.d("TAG1", "original: "+uri+strings[2]);
//        try {
//            videoPath=SiliCompressor.with(mcontext).compressVideo(uri,strings[2]);
//            Log.d("TAG1", "doInBackground: "+videoPath);
//        }catch (URISyntaxException e){
//            e.printStackTrace();
//        }
//        return videoPath;
//    }
//
//    @Override
//    protected void onPostExecute(String s) {
//        super.onPostExecute(s);
//
//        dialog.dismiss();
//
//        File compressedFile=new File(s);
//        float size=compressedFile.length()/1024f; //size in KB
//
//        String value;
//        if(size>=1024)
//            value=size/1024f +"MB";
//        else
//            value=size+"KB";
//
//        Uri uri=Uri.fromFile(compressedFile);
//
//
//        Log.d("TAG1", "onPostExecute: "+uri+" "+size);
//        uploadVideoToFirebase(uri);
//
//
//    }
//}
  //  selectedVideo = data.getData();




    private static File saveVideoFile(Context context, String filePath) throws IOException {
        if (filePath != null) {
            File videoFile = new File(filePath);
            String videoFileName = "" + System.currentTimeMillis() + '_' + videoFile.getName();
            String folderName = Environment.DIRECTORY_MOVIES;
            if (Build.VERSION.SDK_INT < 30) {
                File downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File desFile = new File(downloadsPath, videoFileName);
                if (desFile.exists()) {
                    desFile.delete();
                }

                try {
                    desFile.createNewFile();
                } catch (IOException var61) {
                    var61.printStackTrace();
                }

                return desFile;
            }

            ContentValues var10 = new ContentValues();
            boolean var11 = false;
            boolean var12 = false;
            var10.put("_display_name", videoFileName);
            var10.put("mime_type", "video/mp4");
            var10.put("relative_path", folderName);
            var10.put("is_pending", 1);
            ContentValues values = var10;
            Uri collection = MediaStore.Video.Media.getContentUri("external_primary");
            Uri fileUri = context.getContentResolver().insert(collection, values);
            Void var10000;
            if (fileUri != null) {
                boolean var13 = false;
                Closeable var18 = (Closeable)context.getContentResolver().openFileDescriptor(fileUri, "rw");
                boolean var19 = false;
                boolean var20 = false;
                Throwable var73 = (Throwable)null;

                try {
                    ParcelFileDescriptor descriptor = (ParcelFileDescriptor)var18;
                    if (descriptor != null) {
                        boolean var24 = false;
                        boolean var25 = false;
                        Closeable var28 = (Closeable)(new FileOutputStream(descriptor.getFileDescriptor()));
                        boolean var29 = false;
                        boolean var30 = false;
                        Throwable var74 = (Throwable)null;

                        try {
                            FileOutputStream out = (FileOutputStream)var28;
                            Closeable var33 = (Closeable)(new FileInputStream(videoFile));
                            boolean var34 = false;
                            boolean var35 = false;
                            Throwable var76 = (Throwable)null;

                            try {
                                FileInputStream inputStream = (FileInputStream)var33;
                                byte[] buf = new byte[4096];

                                while(true) {
                                    int sz = inputStream.read(buf);
                                    if (sz <= 0) {
                                        Unit var77 = Unit.INSTANCE;
                                        break;
                                    }

                                    out.write(buf, 0, sz);
                                }
                            } catch (Throwable var62) {
                                var76 = var62;
                                throw var62;
                            } finally {
                                //CloseableKt.closeFinally(var33, var76);
                            }

                            Unit var75 = Unit.INSTANCE;
                        } catch (Throwable var64) {
                            var74 = var64;
                            throw var64;
                        } finally {
                            //CloseableKt.closeFinally(var28, var74);
                        }

                        Unit var72 = Unit.INSTANCE;
                    } else {
                        var10000 = null;
                    }
                } catch (Throwable var66) {
                    var73 = var66;
                    throw var66;
                } finally {
                    //CloseableKt.closeFinally(var18, var73);
                }

                values.clear();
                values.put("is_pending", 0);
                context.getContentResolver().update(fileUri, values, (String)null, (String[])null);
                return new File(getMediaPath(context, fileUri));
            }

            var10000 = (Void)null;
        }

        return null;
    }


    @NotNull
    public static String getMediaPath(@NotNull Context context, @NotNull Uri uri) throws IOException {
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(uri, "uri");
        ContentResolver resolver = context.getContentResolver();
        String[] projection = new String[]{"_data"};
        Cursor cursor = (Cursor)null;

        String var30;
        try {
            File file;
            String var57;
            try {
                cursor = resolver.query(uri, projection, (String)null, (String[])null, (String)null);
                if (cursor != null) {
                    int columnIndex = cursor.getColumnIndexOrThrow("_data");
                    cursor.moveToFirst();
                    var57 = cursor.getString(columnIndex);
                    Intrinsics.checkNotNullExpressionValue(var57, "cursor.getString(columnIndex)");
                } else {
                    var57 = "";
                }
                return var57;
            } catch (Exception var53) {

                String filePath = context.getApplicationInfo().dataDir + File.separator + System.currentTimeMillis();
                file = new File(filePath);
                InputStream var10000 = resolver.openInputStream(uri);
                if (var10000 != null) {
                    Closeable var13 = (Closeable)var10000;

                    InputStream inputStream = (InputStream)var13;
                    Closeable var18 = (Closeable)(new FileOutputStream(file));
                    FileOutputStream outputStream = (FileOutputStream)var18;
                    byte[] buf = new byte[4096];

                    while(true) {
                        int var25 = inputStream.read(buf);
                        if (var25 <= 0) {
                            break;
                        }
                        outputStream.write(buf, 0, var25);
                    }

                }
            }
            var57 = file.getAbsolutePath();
            Intrinsics.checkNotNullExpressionValue(var57, "file.absolutePath");
            var30 = var57;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return var30;
    }




    private void compressVideo(String path){

        VideoCompressor.start(this,videoUri,path,fpath , new CompressionListener() {
            Dialog dialog;
            @Override
            public void onStart() {
                // Compression start
                dialog= ProgressDialog.show(chatActivity.this,"","Compressing....");
            }

            @Override
            public void onSuccess() {
                // On Compression success
                dialog.dismiss();
                Uri uploadUri = Uri.fromFile(new File(fpath));
                Log.e("is dir", String.valueOf(new File(fpath).isDirectory()));
                Log.d("is dir", String.valueOf(new File(fpath).isDirectory()));
                uploadVideoToFirebase(uploadUri); //upload the video
            }

            @Override
            public void onFailure(String failureMessage) {
                // On Failure
                Log.e("fail", failureMessage);
                Toast.makeText(chatActivity.this, "failed to compress video", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onProgress(float v) {
                // Update UI with progress value
                dialog= ProgressDialog.show(chatActivity.this,"","Compressing....");
                runOnUiThread(new Runnable() {
                    public void run() {
                        //progressDialog.setMessage(" جاري تهيئة الفيديو "+String.valueOf(Math.round(v))+"%");

                        Log.e("progress", String.valueOf(v));
                    }
                });
            }

            @Override
            public void onCancelled() {
                // On Cancelled
            }
        }, VideoQuality.MEDIUM, false, false);
    }

}