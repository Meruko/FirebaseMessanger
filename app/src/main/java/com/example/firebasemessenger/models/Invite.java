package com.example.firebasemessenger.models;

public class Invite {
    private boolean invited;
    private User friend;

    public Invite(boolean invited, User friend) {
        this.invited = invited;
        this.friend = friend;
    }

    public boolean isInvited() {
        return invited;
    }

    public void setInvited(boolean invited) {
        this.invited = invited;
    }

    public User getFriend() {
        return friend;
    }

    public void setFriend(User friend) {
        this.friend = friend;
    }
}
