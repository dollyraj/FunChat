package com.example.funchat.Adapter;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
//import androidx.media2.exoplayer.external.source.ExtractorMediaSource;
//import androidx.media2.exoplayer.external.source.MediaSource;
import androidx.recyclerview.widget.RecyclerView;

import com.example.funchat.ModelClass.Messages;
import com.example.funchat.R;
import com.example.funchat.tools.AudioService;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.funchat.Activity.chatActivity.rImage;
import static com.example.funchat.Activity.chatActivity.sImage;




public class MessagesAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<Messages> messagesArrayList;
    int ITEM_SEND=1;
    int ITEM_RECEIVE=2;
    AudioService audioService;
    ImageButton tmpBtnPlay;
    MediaPlayer mediaPlayer;


//    LinearLayout layoutVoice;

    public MessagesAdapter(Context context,ArrayList<Messages> messagesArrayList) {
        this.context=context;
        this.messagesArrayList = messagesArrayList;
        this.audioService=new AudioService(context);

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType==ITEM_SEND){
            View view= LayoutInflater.from(context).inflate(R.layout.sender_layout_item,parent,false);
            return new SenderViewHolder(view);

        }else {
            View view= LayoutInflater.from(context).inflate(R.layout.receiver_layout_item,parent,false);
            return new ReceiverViewHolder(view);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Messages messages=messagesArrayList.get(position);
       // Log.d("MessagesAdapter", "onBindViewHolder: "+messages.getUrl()+" "+messages.getType()+messages.getCurrAddress());

        switch (messages.getType()) {
            case "TEXT":
          if (holder.getClass() == SenderViewHolder.class) {

                SenderViewHolder viewHolder = (SenderViewHolder) holder;
                viewHolder.layoutText.setVisibility(View.VISIBLE);
                viewHolder.layoutVoice.setVisibility(View.GONE);
              viewHolder.layoutVideo.setVisibility(View.GONE);
              viewHolder.layoutImage.setVisibility(View.GONE);
                viewHolder.txtMessage.setText(messages.getMessage()+"\n"+messages.getDate()+"\n"+messages.getTime());
                Picasso.get().load(sImage).into(viewHolder.circleImageView);

            } else {
                ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
                viewHolder.layoutText.setVisibility(View.VISIBLE);
              viewHolder.layoutVideo.setVisibility(View.GONE);
              viewHolder.layoutImage.setVisibility(View.GONE);
                viewHolder.layoutVoice.setVisibility(View.GONE);
                viewHolder.txtMessage.setText(messages.getMessage()+"\n"+messages.getDate()+"\n"+messages.getTime());
                Picasso.get().load(rImage).into(viewHolder.circleImageView);
            }

            break;

            case "VOICE":
                if (holder.getClass() == SenderViewHolder.class) {
                    SenderViewHolder viewHolder = (SenderViewHolder) holder;

                    viewHolder.layoutText.setVisibility(View.GONE);
                    viewHolder.layoutVideo.setVisibility(View.GONE);
                    viewHolder.layoutImage.setVisibility(View.GONE);
                    viewHolder.layoutVoice.setVisibility(View.VISIBLE);
                    viewHolder.layoutVoice.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(tmpBtnPlay!=null){

                                //Log.d("MessagesAdapter", "onClick: "+"here");
                                tmpBtnPlay.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
                            }

                            //viewHolder.btnPlay.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_pause_circle_filled_24));
                            viewHolder.btnPlay.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
                            audioService.playAudioFromUrl(messages.getUrl(), new AudioService.OnPlayCallBack() {
                                @Override
                                public void onFinished() {
                                    //viewHolder.btnPlay.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_play_circle_filled_24));
                                    viewHolder.btnPlay.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
                                }

                            });


                            tmpBtnPlay=viewHolder.btnPlay;
                        }
                    });






                    viewHolder.date_time.setText(messages.getDate()+"\n"+messages.getTime());
                    Picasso.get().load(sImage).into(viewHolder.circleImageView);

                } else {
                    ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
                    viewHolder.layoutText.setVisibility(View.GONE);
                    viewHolder.layoutVideo.setVisibility(View.GONE);
                    viewHolder.layoutImage.setVisibility(View.GONE);
                   viewHolder.layoutVoice.setVisibility(View.VISIBLE);
                    viewHolder.layoutVoice.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(tmpBtnPlay!=null){
                                tmpBtnPlay.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_play_circle_filled_24));
                            }

                            viewHolder.btnPlay.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_pause_circle_filled_24));
                            audioService.playAudioFromUrl(messages.getUrl(), new AudioService.OnPlayCallBack() {
                                @Override
                                public void onFinished() {
                                    viewHolder.btnPlay.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_play_circle_filled_24));
                                }
                            });

                            tmpBtnPlay=viewHolder.btnPlay;
                        }
                    });



                    viewHolder.date_time.setText(messages.getDate()+"\n"+messages.getTime());
                    Picasso.get().load(rImage).into(viewHolder.circleImageView);
                }

                break;
            case "IMAGE":
                if (holder.getClass() == SenderViewHolder.class) {

                    SenderViewHolder viewHolder = (SenderViewHolder) holder;
                    viewHolder.layoutText.setVisibility(View.GONE);
                    viewHolder.layoutVoice.setVisibility(View.GONE);
                    viewHolder.layoutImage.setVisibility(View.VISIBLE);
                    viewHolder.layoutVideo.setVisibility(View.GONE);
                    Picasso.get().load(messages.getUrl()).into(viewHolder.msgImage);
                   // Log.d("MessagesAdapter", "onBindViewHolder: "+messages.getUrl());

                    Picasso.get().load(sImage).into(viewHolder.circleImageView);
                    viewHolder.imgdateTime.setText(messages.getDate()+"\n"+messages.getTime());

                } else {
                    ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
                     viewHolder.layoutText.setVisibility(View.GONE);
                    viewHolder.layoutVoice.setVisibility(View.GONE);
                    viewHolder.layoutVideo.setVisibility(View.GONE);
                    viewHolder.layoutImage.setVisibility(View.VISIBLE);
                    Picasso.get().load(messages.getUrl()).into(viewHolder.msgImage);
                    Picasso.get().load(rImage).into(viewHolder.circleImageView);
                    viewHolder.imgdateTime.setText(messages.getDate()+"\n"+messages.getTime());
                }

                break;

            case "VIDEO":
                if (holder.getClass() == SenderViewHolder.class) {

                    SenderViewHolder viewHolder = (SenderViewHolder) holder;
                    viewHolder.layoutText.setVisibility(View.GONE);
                    viewHolder.layoutVoice.setVisibility(View.GONE);
                    viewHolder.layoutImage.setVisibility(View.GONE);
                    viewHolder.layoutVideo.setVisibility(View.VISIBLE);

                    // Log.d("MessagesAdapter", "onBindViewHolder: "+messages.getUrl());
                    SimpleExoPlayer exoPlayer;

                    String videoUrl=messages.getUrl();

                    LoadControl loadControl=new DefaultLoadControl();




                    try{
                        exoPlayer=new SimpleExoPlayer.Builder(context).build();
                        Uri videoUri=Uri.parse(videoUrl);



                        ProgressiveMediaSource mediaSource=new ProgressiveMediaSource.Factory(new DefaultHttpDataSource.Factory())
                                .createMediaSource(MediaItem.fromUri(videoUri));

                        viewHolder.exoPlayerView.setPlayer(exoPlayer);
                        viewHolder.exoPlayerView.setKeepScreenOn(true);
                        exoPlayer.prepare(mediaSource);

                        exoPlayer.setPlayWhenReady(false);

                        exoPlayer.addListener(new Player.EventListener() {

                            @Override
                            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                                       //check condition
                                if(playbackState==Player.STATE_BUFFERING){
                                    viewHolder.progressBar.setVisibility(View.VISIBLE);
                                }else if(playbackState== Player.STATE_READY){
                                    viewHolder.progressBar.setVisibility(View.GONE);
                                }
                            }


                            @Override
                            public void onRepeatModeChanged(int repeatMode) {

                            }


                            @Override
                            public void onPlayerError(ExoPlaybackException error) {

                            }




                        });


                    }catch (Exception e){
                        Log.e("TAG", "Error : "+e.toString());
                    }

  //            boolean flag=false;

//                viewHolder.btFullscreen.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        //check condition
//                        if(flag){
//                            viewHolder.btFullscreen.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_fullscreen));
//                            //set  portrait orientation
//
//
//                        }
//                    }
//                });




//                    MediaController mediaController=new MediaController(context);
//                    mediaController.setAnchorView(viewHolder.videoMsg);
//
//                    Uri videoUri=Uri.parse(videoUrl);
//                    viewHolder.videoMsg.setMediaController(mediaController);
//                    viewHolder.videoMsg.setVideoURI(videoUri);
//                    viewHolder.videoMsg.start();

                   // viewHolder.videoMsg.requestFocus();

//                    viewHolder.videoMsg.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                        @Override
//                        public void onPrepared(MediaPlayer mediaPlayer) {
//                            //video is ready to play
//                            mediaPlayer.start();
//                        }
//                    });
//
//                    viewHolder.videoMsg.setOnInfoListener(new MediaPlayer.OnInfoListener() {
//                        @Override
//                        public boolean onInfo(MediaPlayer mPlayer, int what, int extra) {
//                            //to check if buffering
//                            switch (what){
//                                case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START: {
//                                    //rendering started
//                                    return true;
//                                }
//                                case MediaPlayer.MEDIA_INFO_BUFFERING_START:{
//                                    //buffering started
//                                    return true;
//                                }
//                                case MediaPlayer.MEDIA_INFO_BUFFERING_END:{
//                                    return true;
//                                }
//                            }
//                            return false;
//                        }
   //                 });

                    //Picasso.get().load(sImage).into(viewHolder.circleImageView);

//                    viewHolder.videoMsg.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                        @Override
//                        public void onCompletion(MediaPlayer mediaPlayer) {
//
//                        }
//                    });
                    viewHolder.videodateTime.setText(messages.getDate()+"\n"+messages.getTime());

                } else {
                    ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
                    viewHolder.layoutText.setVisibility(View.GONE);
                    viewHolder.layoutVoice.setVisibility(View.GONE);
                    viewHolder.layoutImage.setVisibility(View.GONE);
                    viewHolder.layoutVideo.setVisibility(View.VISIBLE);
                    //Picasso.get().load(rImage).into(viewHolder.circleImageView);
                    SimpleExoPlayer exoPlayer;
                    String videoUrl=messages.getUrl();

                    try{
                        exoPlayer=new SimpleExoPlayer.Builder(context).build();
                        Uri videoUri=Uri.parse(videoUrl);



                        ProgressiveMediaSource mediaSource=new ProgressiveMediaSource.Factory(new DefaultHttpDataSource.Factory()).createMediaSource(MediaItem.fromUri(videoUri));

                        viewHolder.exoPlayerView.setPlayer(exoPlayer);
                        viewHolder.exoPlayerView.setKeepScreenOn(true);

                        exoPlayer.prepare(mediaSource);

                        exoPlayer.setPlayWhenReady(false);

                        exoPlayer.addListener(new Player.EventListener() {



                            @Override
                            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                            }

                            @Override
                            public void onPlaybackStateChanged(int playbackState) {
                                if(playbackState==Player.STATE_BUFFERING){
                                    viewHolder.progressBar.setVisibility(View.VISIBLE);
                                }else if(playbackState== Player.STATE_READY){
                                    viewHolder.progressBar.setVisibility(View.GONE);
                                }
                            }



                            @Override
                            public void onRepeatModeChanged(int repeatMode) {

                            }


                            @Override
                            public void onPlayerError(ExoPlaybackException error) {

                            }



                        });

                    }catch (Exception e){
                        Log.e("TAG", "Error : "+e.toString());
                    }




                    viewHolder.imgdateTime.setText(messages.getDate()+"\n"+messages.getTime());
                }

        }
//
   }




    @Override
    public int getItemCount() {
        return messagesArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Messages messages=messagesArrayList.get(position);
        if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(messages.getSenderId())){
            return  ITEM_SEND;
        }else {
            return ITEM_RECEIVE;
        }
    }



    class SenderViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageView;
        TextView txtMessage,date_time,imgdateTime,videodateTime;
        LinearLayout layoutText,layoutVoice,layoutImage,layoutVideo;
        ImageButton btnPlay;
        ImageView msgImage;

       // ImageButton imagePlay,imagePause;


        TextView textCurrentTime,textTotalDuration;
        SeekBar playerSeekbar;


//        VideoView videoMsg;
//        Button videoSend;

//        SimpleExoPlayer exoPlayer;
        PlayerView exoPlayerView;
        ProgressBar progressBar;
        ImageView btFullscreen;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);

            circleImageView=itemView.findViewById(R.id.profile_img);
            txtMessage=itemView.findViewById(R.id.txtMessages);
            layoutText=itemView.findViewById(R.id.layoutText);
            layoutVoice=itemView.findViewById(R.id.layout_voice);
            layoutImage=itemView.findViewById(R.id.layout_image);
            //imagePlay=itemView.findViewById(R.id.btn_play_chat);
            //imagePause=itemView.findViewById(R.id.btn_pause_chat);
            btnPlay=itemView.findViewById(R.id.btn_play_chat);
            msgImage=itemView.findViewById(R.id.image_msg);
            date_time=itemView.findViewById(R.id.date_time);
            imgdateTime=itemView.findViewById(R.id.dateTime);
            layoutVideo=itemView.findViewById(R.id.layout_video);
           // videoMsg=itemView.findViewById(R.id.video_msg);
            exoPlayerView=itemView.findViewById(R.id.exoplayer_view);
            progressBar=itemView.findViewById(R.id.progress_bar);
           // btFullscreen=exoPlayerView.findViewById(R.id.bt_fullscreen);
            videodateTime=itemView.findViewById(R.id.date_Time);


//            textCurrentTime=itemView.findViewById(R.id.textCurrentTime);
//            textTotalDuration=itemView.findViewById(R.id.textTotalDuration);
//
//            playerSeekbar=itemView.findViewById(R.id.playerSeekbar);
        }


    }

    class ReceiverViewHolder extends RecyclerView.ViewHolder {
        PlayerView exoPlayerView;
        //ImageButton imagePlay,imagePause;
         CircleImageView circleImageView;
         TextView txtMessage,date_time,imgdateTime,videodateTime;
         LinearLayout layoutText,layoutVoice,layoutImage,layoutVideo;
         ImageButton btnPlay;
         ImageView msgImage;
        VideoView videoMsg;
        ProgressBar progressBar;
        ImageView btFullscreen;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
           // imagePlay=itemView.findViewById(R.id.btn_play_chat);
            //imagePause=itemView.findViewById(R.id.btn_pause_chat);
            circleImageView=itemView.findViewById(R.id.profile_img);
            txtMessage=itemView.findViewById(R.id.txtMessages);
            layoutText=itemView.findViewById(R.id.layoutText);
            layoutVoice=itemView.findViewById(R.id.layout_voice);
            layoutImage=itemView.findViewById(R.id.layout_image);
            btnPlay=itemView.findViewById(R.id.btn_play_chat);
            msgImage=itemView.findViewById(R.id.image_msg);
            date_time=itemView.findViewById(R.id.date_time);
            imgdateTime=itemView.findViewById(R.id.dateTime);
            layoutVideo=itemView.findViewById(R.id.layout_video);
            //videoMsg=itemView.findViewById(R.id.video_msg);
            progressBar=itemView.findViewById(R.id.progress_bar);
            exoPlayerView=itemView.findViewById(R.id.exoplayer_view);
            videodateTime=itemView.findViewById(R.id.date_Time);
        }




    }






}
