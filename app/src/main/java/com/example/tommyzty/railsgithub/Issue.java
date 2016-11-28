package com.example.tommyzty.railsgithub;

import java.util.Date;

public class Issue {

    public int number;
    public String title;
    public String body;
    public String comments;
    public Date date;

    public Issue() {
    }

    public Issue(int number, String title, String body, String comments, Date date) {
        this.number = number;
        this.title = title;
        this.body = body;
        this.date = date;
        this.comments = comments;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getComments() {
        return this.comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}