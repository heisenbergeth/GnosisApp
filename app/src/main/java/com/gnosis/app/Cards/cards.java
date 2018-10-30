package com.gnosis.app.Cards;

/**
 * Created by manel on 9/5/2017.
 */

public class cards {
    private String userId;
    private String name;
    private String school;
    private String profileImageUrl;


    public cards (String userId, String name, String school, String profileImageUrl){
        this.userId = userId;
        this.name = name;
        this.school=school;
        this.profileImageUrl = profileImageUrl;
    }

    public String getUserId(){
        return userId;
    }
    public void setUserID(String userID){
        this.userId = userId;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getSchool(){
        return school;
    }
    public void setSchool(String school){
        this.school = school;
    }

    public String getProfileImageUrl(){
        return profileImageUrl;
    }
    public void setProfileImageUrl(String profileImageUrl){
        this.profileImageUrl = profileImageUrl;
    }
}
