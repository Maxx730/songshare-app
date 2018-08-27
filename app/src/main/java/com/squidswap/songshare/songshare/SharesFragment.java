package com.squidswap.songshare.songshare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

//FRAGMENT ACTIVITY CLASS THAT WILL DISPLAY THE SHARE VIEW IN THE VIEWPAGER OBJECT.
public class SharesFragment extends Fragment {

    private ListView SharesList;
    private RequestQueue req;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.shares_view_fragment,container,false);

        SharesList = rootView.findViewById(R.id.ShareList);
        req = Volley.newRequestQueue(getActivity().getApplicationContext());
        LoadShares();

        return rootView;
    }

    //Loads all the shares for the given user from the database.
    private void LoadShares(){
        SharedPreferences prefs = getActivity().getSharedPreferences("SongShareLogin",getActivity().MODE_PRIVATE);
        StringRequest getShares = new StringRequest(Request.Method.GET, "http://104.236.66.72:5698/user/" + String.valueOf(prefs.getInt("SongShareId", 0)) + "/shares", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("FROM SONGSHARE SERVER",response);
                try{
                    JSONObject mainObj = new JSONObject(response);
                    ArrayList<JSONObject> objs = new ArrayList<JSONObject>();
                    JSONArray sour = mainObj.getJSONArray("PAYLOAD");

                    if(sour.length() > 0){
                        for(int i = 0;i < sour.length();i++){
                            objs.add(sour.getJSONObject(i));
                        }

                        ShareAdapter shareAdapt = new ShareAdapter(getActivity().getApplicationContext(),R.layout.single_share_item,objs);
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
    }

    //Adapter that pulls in the JSONObject array list and applies the single share layout
    //to each object in the list for the listview.
    class ShareAdapter extends ArrayAdapter<JSONObject> {

        private Context con;
        private int layout;
        private ArrayList<JSONObject> shares;

        public ShareAdapter(@NonNull Context context, int resource, ArrayList<JSONObject> shares) {
            super(context, resource,shares);

            this.con = context;
            this.layout = resource;
            this.shares = shares;

        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflate = getLayoutInflater().from(getActivity().getApplicationContext());
            convertView = inflate.inflate(R.layout.single_share_item,parent,false);
            final TextView singleShareTitle = convertView.findViewById(R.id.SingleTrackTitle);
            TextView singleShareArtist = convertView.findViewById(R.id.SingleTrackArtist);
            final ImageView sharedArtwork = convertView.findViewById(R.id.SingleAlbumArt);
            TextView singleShareShaerer = convertView.findViewById(R.id.SingleShareSharer);
            final ProgressBar shareAnimation = convertView.findViewById(R.id.ShareLoadingAnim);

            try{
                singleShareTitle.setText(getItem(position).getString("title"));
                singleShareArtist.setText(getItem(position).getString("artist"));
                singleShareShaerer.setText("Shared by " + getItem(position).getString("username"));

                if(getItem(position).getString("spotify_id").equals("") == false){
                    RelativeLayout indi = convertView.findViewById(R.id.ShareIndicator);
                    indi.setBackgroundColor(getResources().getColor(R.color.spotify_green));
                }else if(getItem(position).getString("youtube_id").equals("") == false){
                    RelativeLayout indi = convertView.findViewById(R.id.ShareIndicator);
                    indi.setBackgroundColor(getResources().getColor(R.color.youtube_red));
                }else{
                    RelativeLayout indi = convertView.findViewById(R.id.ShareIndicator);
                    indi.setBackgroundColor(getResources().getColor(R.color.google_play_orange));
                }

                Glide.with(convertView).load(getItem(position).getString("art")).transition(DrawableTransitionOptions.withCrossFade()).listener(new RequestListener<Drawable>() {
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
            }catch(Exception e){

            }

            return convertView;
        }
    }
}
