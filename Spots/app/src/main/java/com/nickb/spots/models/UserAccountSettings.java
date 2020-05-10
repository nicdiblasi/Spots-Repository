package com.nickb.spots.models;

public class UserAccountSettings {

    private String description;
    private String display_name;
    private long spot_count;
    private long rating;
    private String home_town;
    private String home_city;
    private String username;
    private String profile_photo;
    private String user_id;

    public UserAccountSettings(String description, String display_name, long spot_count, long rating, String home_town, String home_city, String username, String profile_photo, String user_id) {
        this.description = description;
        this.display_name = display_name;
        this.spot_count = spot_count;
        this.rating = rating;
        this.home_town = home_town;
        this.home_city = home_city;
        this.username = username;
        this.profile_photo = profile_photo;
        this.user_id = user_id;
    }

    public UserAccountSettings() {
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getProfile_photo() {
        return profile_photo;
    }

    public void setProfile_photo(String profile_photo) {
        this.profile_photo = profile_photo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public long getSpot_count() {
        return spot_count;
    }

    public void setSpot_count(long spot_count) {
        this.spot_count = spot_count;
    }

    public long getRating() {
        return rating;
    }

    public void setRating(long rating) {
        this.rating = rating;
    }

    public String getHome_town() {
        return home_town;
    }

    public void setHome_town(String home_town) {
        this.home_town = home_town;
    }

    public String getHome_city() {
        return home_city;
    }

    public void setHome_city(String home_city) {
        this.home_city = home_city;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "UserAccountSettings{" +
                "description='" + description + '\'' +
                ", display_name='" + display_name + '\'' +
                ", spot_count=" + spot_count +
                ", rating=" + rating +
                ", home_town='" + home_town + '\'' +
                ", home_city='" + home_city + '\'' +
                ", username='" + username + '\'' +
                ", profile_photo='" + profile_photo + '\'' +
                '}';
    }
}
