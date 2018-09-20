package com.squidswap.songshare.songshare;

//Class for each user item.
public class User {
    private int id;
    private String username;

    public User(int id,String username){
        this.id = id;
        this.username = username;
    }

    public int GetId(){
        return this.id;
    }

    public String GetUsername(){
        return this.username;
    }
}
