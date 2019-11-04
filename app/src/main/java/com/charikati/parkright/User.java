package com.charikati.parkright;

public class User {

    private String mFirstName;
    private String mLastName;
    private String mEmail;


    public User(String first_name, String last_name, String email) {
        this.mFirstName = first_name;
        this.mLastName = last_name;
        this.mEmail = email;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public String getEmail() {
        return mEmail;
    }


}
