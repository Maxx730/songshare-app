package com.squidswap.songshare.songshare;


public class MusicData {

    private String title,artist,album,art;
    private int duration;

    public MusicData(String title,String artist){
        this.title = title;
        this.artist = artist;
    }

    public String getTitle(){
        return this.title;
    }

    public String getArtist(){
        return this.artist;
    }

    public String getArt(){
        return this.art;
    }

    public void setAlbum(String album){
        this.album = album;
    }

    public void setArt(String url){
        this.art = art;
    }

    public void setDuration(int duration){
        this.duration = duration;
    }
}
