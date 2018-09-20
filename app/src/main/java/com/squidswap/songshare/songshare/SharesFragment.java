package com.squidswap.songshare.songshare;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.BaseTarget;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.blurry.Blurry;

//FRAGMENT ACTIVITY CLASS THAT WILL DISPLAY THE SHARE VIEW IN THE VIEWPAGER OBJECT.
public class SharesFragment extends Fragment {

    private SharedPreferences prefs;

    private GridView SharesList;
    private RequestQueue req;
    private ImageButton ToggleTracks,ToggleVideos;
    private ListView GroupShareList;
    private RelativeLayout NoTracksLayout;
    private RelativeLayout CommentAnimate,CommentDialog;
    private TextView CommentDialogText;
    private ImageView CommentSendImage;
    private LinearLayout ComposeCommentLayout;

    //UI elements for comments dialog.
    private TextView DialogTitle,DialogArtist,DialogUser;
    private ImageView DialogArt;
    private RelativeLayout DialogBackground;
    private ImageButton DialogCancel,SendComment;
    private EditText DialogComposeField;
    private ListView DialogCommentList;


    //Our objects for the comment dialog.
    private Share share;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView;
        String value = "",id = "";
        req = Volley.newRequestQueue(getActivity().getApplicationContext());

        if(getArguments() != null){
            value = getArguments().getString("type");
            id = getArguments().getString("group_id");
        }

        prefs = getActivity().getSharedPreferences("SongShareLogin",Context.MODE_PRIVATE);

        if(value.equals("group") && value != null){
            rootView = (ViewGroup) inflater.inflate(R.layout.group_shares_fragment,container,false);
            NoTracksLayout = (RelativeLayout) rootView.findViewById(R.id.ZeroSharesOverlay);
            GroupShareList = rootView.findViewById(R.id.GroupShareListView);
            LoadShares("group",id);
        }else{
            rootView = (ViewGroup) inflater.inflate(R.layout.shares_view_fragment,container,false);
            SharesList = rootView.findViewById(R.id.SharesGridView);
            LoadShares("all",null);

            //Load UI components here.
            ToggleTracks = rootView.findViewById(R.id.ToggleMainTracks);
            ToggleVideos = rootView.findViewById(R.id.ToggleMainVideos);

            //Get UI elements for comments Dialog.
            CommentAnimate = rootView.findViewById(R.id.SendingCommentAnimation);
            CommentDialog = rootView.findViewById(R.id.CommentSendDialog);
            CommentDialogText = rootView.findViewById(R.id.SendingCommentText);
            CommentSendImage = rootView.findViewById(R.id.SendCommentImage);
            ComposeCommentLayout = rootView.findViewById(R.id.SendCommentFields);
            DialogTitle = rootView.findViewById(R.id.ComposeTrackTitle);
            DialogArtist = rootView.findViewById(R.id.ComposeTrackArtist);
            DialogUser = rootView.findViewById(R.id.ComposeTrackSharer);
            DialogArt = rootView.findViewById(R.id.ComposeTrackArt);
            DialogCancel = rootView.findViewById(R.id.ComposeCommentCancel);
            DialogBackground = rootView.findViewById(R.id.ComposeCommentBackground);
            DialogComposeField = rootView.findViewById(R.id.ComposeCommentField);
            SendComment = rootView.findViewById(R.id.SendComment);
            DialogCommentList = rootView.findViewById(R.id.ComposeCommentList);

            //Set the videos button to not focused.
            ToggleVideos.getDrawable().setColorFilter(getResources().getColor(R.color.light_gray_icon), PorterDuff.Mode.MULTIPLY);

            ToggleTracks.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ResetColorsToggle();
                    ToggleTracks.getDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.MULTIPLY);
                }
            });

            ToggleVideos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ResetColorsToggle();
                    ToggleVideos.getDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.MULTIPLY);
                }
            });

            DialogCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogBackground.animate().alpha(0f).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            DialogBackground.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    }).setDuration(300).start();
                }
            });
        }

        return rootView;
    }

    private void ResetColorsToggle(){
        ToggleTracks.getDrawable().setColorFilter(getResources().getColor(R.color.light_gray_icon), PorterDuff.Mode.MULTIPLY);
        ToggleVideos.getDrawable().setColorFilter(getResources().getColor(R.color.light_gray_icon), PorterDuff.Mode.MULTIPLY);
    }

    //Loads all the shares for the given user from the database.
    private void LoadShares(String type,final String id){

        switch(type){
            case "group":
                StringRequest getGroupShares = new StringRequest(Request.Method.POST, "http://104.236.66.72:5698/group/shared", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject mainObj = new JSONObject(response);
                            ArrayList<JSONObject> objs = new ArrayList<JSONObject>();
                            JSONArray sour = mainObj.getJSONArray("PAYLOAD");

                            if(sour.length() > 0){
                                NoTracksLayout.setVisibility(View.GONE);
                            }

                            if(sour.length() > 0){
                                for(int i = 0;i < sour.length();i++){
                                    objs.add(sour.getJSONObject(i));
                                }

                                ShareAdapter shareAdapt = new ShareAdapter(getActivity().getApplicationContext(),R.layout.single_share_item,objs,"group");
                                GroupShareList.setAdapter(shareAdapt);
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        HashMap<String,String> params = new HashMap<>();

                        params.put("group_id",id);

                        return params;
                    }
                };
                req.add(getGroupShares);
                break;
            default:
                SharedPreferences prefs = getActivity().getSharedPreferences("SongShareLogin",getActivity().MODE_PRIVATE);
                StringRequest getShares = new StringRequest(Request.Method.GET, "http://104.236.66.72:5698/user/" + String.valueOf(prefs.getInt("SongShareId", 0)) + "/shares", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject mainObj = new JSONObject(response);
                            ArrayList<JSONObject> objs = new ArrayList<JSONObject>();
                            JSONArray sour = mainObj.getJSONArray("PAYLOAD");

                            if(sour.length() > 0){
                                for(int i = 0;i < sour.length();i++){
                                    objs.add(sour.getJSONObject(i));
                                }

                                ShareAdapter shareAdapt = new ShareAdapter(getActivity().getApplicationContext(),R.layout.single_share_item,objs,"all");
                                SharesList.setAdapter(shareAdapt);
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                            Toast.makeText(getActivity().getApplicationContext(),"ERROR PULLING SHARE DATA",Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
                req.add(getShares);
                break;
        }
    }

    private void AnimateCommentSend(){
        CommentDialog.setVisibility(View.VISIBLE);
        final RelativeLayout parent = (RelativeLayout) CommentAnimate.getParent();

        parent.post(new Runnable() {
            @Override
            public void run() {
                ValueAnimator anim = ValueAnimator.ofInt(10,parent.getMeasuredWidth());

                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        CommentAnimate.getLayoutParams().width = (int) animation.getAnimatedValue();
                        CommentAnimate.requestLayout();
                    }
                });

                anim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        CommentDialogText.setText("Comment Sent!");
                        CommentSendImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_round_done_24px));

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                CommentDialog.animate().setDuration(500).alpha(0f).setListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        CommentDialogText.setText("Sending Comment...");
                                        CommentSendImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_round_access_time_24px));
                                        CommentDialog.setVisibility(View.GONE);
                                        CommentDialog.setAlpha(1f);
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                }).start();
                            }
                        },500);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

                anim.setDuration(1000);
                anim.start();
            }
        });
    }

    //Shows/Animates in the dialog for adding a comment.
    private void OpenComposeComment(final Share share){

        DialogTitle.setText(share.getTitle());
        DialogArtist.setText(share.getArtist());
        DialogUser.setText(share.getUser().GetUsername());

        DialogBackground.setVisibility(View.VISIBLE);
        DialogBackground.animate().setDuration(300).alpha(1f).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                share.GetComments(new SongshareArrayInterface() {
                    @Override
                    public void AfterRetrieveArray(ArrayList<JSONObject> objs) {
                        CommentAdapter com = new CommentAdapter(getActivity().getApplicationContext(),0,objs);
                        DialogCommentList.setAdapter(com);
                    }
                });
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();

        Glide.with(getActivity().getApplicationContext()).load(share.getArt()).into(DialogArt);

        ValueAnimator anim = ValueAnimator.ofInt(0,1200);

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ComposeCommentLayout.getLayoutParams().height = (int) animation.getAnimatedValue();
                ComposeCommentLayout.requestLayout();
            }
        });

        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                SendComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!DialogComposeField.getText().toString().matches("")){
                            share.AddComment(DialogComposeField.getText().toString(),prefs.getInt("SongShareId",0));
                            DialogComposeField.setText("");
                            DialogBackground.animate().alpha(0f).setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    DialogBackground.setVisibility(View.GONE);
                                    AnimateCommentSend();
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            }).setDuration(300).start();
                        }else{
                            Toast.makeText(getActivity().getApplicationContext(),"Comment cannot be empty...",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        anim.setDuration(450);
        anim.start();
    }

    //Adapter that pulls in the JSONObject array list and applies the single share layout
    //to each object in the list for the listview.
    class ShareAdapter extends ArrayAdapter<JSONObject> {

        private Context con;
        private int layout;
        private ArrayList<JSONObject> shares;
        private String type;

        public ShareAdapter(@NonNull Context context, int resource, ArrayList<JSONObject> shares,String type) {
            super(context, resource,shares);

            this.con = context;
            this.layout = resource;
            this.shares = shares;
            this.type = type;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(this.type == "group"){
                convertView = getLayoutInflater().inflate(R.layout.group_share_item,parent,false);

                TextView GroupTrackTitle = convertView.findViewById(R.id.GroupShareTitle);
                TextView GroupArtistView = convertView.findViewById(R.id.GroupShareArtist);
                TextView GroupSharerText = convertView.findViewById(R.id.GroupShareSharer);
                ImageView GroupShareImage = convertView.findViewById(R.id.GroupShareImage);
                RelativeLayout ToggleComments = convertView.findViewById(R.id.ToggleShareComments);

                try{
                    GroupTrackTitle.setText(getItem(position).getString("title"));
                    GroupArtistView.setText(getItem(position).getString("artist"));
                    GroupSharerText.setText(getItem(position).getString("username"));

                    Glide.with(getActivity().getApplicationContext()).load(getItem(position).getString("art")).into(GroupShareImage);

                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try{
                                Intent i = new Intent(getActivity().getApplicationContext(),SingleShare.class);
                                i.putExtra("TrackID",getItem(position).getInt("_id"));
                                startActivity(i);
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                }catch(Exception e){
                    e.printStackTrace();
                }
            }else{
                LayoutInflater inflate = getLayoutInflater().from(getActivity().getApplicationContext());
                convertView = inflate.inflate(R.layout.single_share_item,parent,false);
                final TextView singleShareTitle = convertView.findViewById(R.id.SingleTrackTitle);
                TextView singleShareArtist = convertView.findViewById(R.id.SingleTrackArtist);
                final ImageView sharedArtwork = convertView.findViewById(R.id.SingleAlbumArt);
                final ImageView likeHeart = convertView.findViewById(R.id.TrackLikeHeart);
                TextView singleShareShaerer = convertView.findViewById(R.id.SingleShareSharer);
                final ProgressBar shareAnimation = convertView.findViewById(R.id.ShareLoadingAnim);
                ImageView ShareUserImage = convertView.findViewById(R.id.SharedUserProfile);
                RelativeLayout ToggleComments = convertView.findViewById(R.id.ToggleShareComments);
                ImageView TrackSource = convertView.findViewById(R.id.TrackSourceImage);

                try{
                    final int shareId,userId;
                    final String title,artist,username,art;

                    art = getItem(position).getString("art");
                    shareId = getItem(position).getInt("_id");
                    userId = getItem(position).getInt("user_id");
                    title = getItem(position).getString("title");
                    artist = getItem(position).getString("artist");
                    username = getItem(position).getString("username");

                    singleShareTitle.setText(title);
                    singleShareArtist.setText(artist);
                    singleShareShaerer.setText("Shared by " + username);


                    if(getItem(position).getString("spotify_id").equals("") == false){
                        RelativeLayout indi = convertView.findViewById(R.id.ShareIndicator);
                        indi.setBackgroundColor(getResources().getColor(R.color.spotify_green));
                    }else if(getItem(position).getString("youtube_id").equals("") == false){
                        RelativeLayout indi = convertView.findViewById(R.id.ShareIndicator);
                        indi.setBackgroundColor(getResources().getColor(R.color.youtube_red));

                        sharedArtwork.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        TrackSource.setImageDrawable(getResources().getDrawable(R.drawable.yt_logo_rgb_dark));
                    }else{
                        RelativeLayout indi = convertView.findViewById(R.id.ShareIndicator);
                        indi.setBackgroundColor(getResources().getColor(R.color.google_play_orange));
                    }

                    likeHeart.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            likeHeart.animate().scaleX(1.2f).scaleY(1.2f).setDuration(500).start();
                            likeHeart.setImageDrawable(getResources().getDrawable(R.drawable.heart_full));
                        }
                    });

                    Glide.with(getActivity().getApplicationContext()).load(art).listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            sharedArtwork.setVisibility(View.VISIBLE);
                            shareAnimation.setVisibility(View.GONE);
                            return false;
                        }
                    }).into(sharedArtwork);

                    Glide.with(getActivity().getApplicationContext()).load(getItem(position).getString("profile")).into(ShareUserImage);

                    sharedArtwork.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(getActivity().getApplicationContext(),SingleShare.class);
                            //Add the id of the track in here to pull details on the details page.
                            try{
                                i.putExtra("TrackID",getItem(position).getInt("_id"));
                                startActivity(i);
                            }catch(Exception e){
                                Toast.makeText(getActivity().getApplicationContext(),"Problem opening track details.",Toast.LENGTH_LONG).show();
                            }
                        }
                    });


                    ToggleComments.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            share = new Share(shareId,title,artist,art,new User(userId,username),getActivity().getApplicationContext());
                            OpenComposeComment(share);
                        }
                    });
                }catch(Exception e){
                    e.printStackTrace();
                }
            }


            return convertView;
        }
    }

    class CommentAdapter extends ArrayAdapter<JSONObject>{
        private Context con;
        private ArrayList<JSONObject> comments;

        public CommentAdapter(Context context,int resource,ArrayList<JSONObject> comments){
            super(context,0,comments);

            this.con = context;
            this.comments = comments;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.single_comment,parent,false);
            TextView Commentor = convertView.findViewById(R.id.CommentUser);
            TextView CommentContent = convertView.findViewById(R.id.CommentContent);

            try{
                Commentor.setText(comments.get(position).getString("username"));
                CommentContent.setText(comments.get(position).getString("content"));
            }catch(Exception e){
                e.printStackTrace();
            }

            return convertView;
        }
    }
}
