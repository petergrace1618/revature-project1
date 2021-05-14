package org.shivacorp.model;

import java.util.Objects;

public class User {
    int id;
    String username;
    String password;
    String fullname;
    Usertype usertype;

    public User() {
//        this.username = "";
//        this.password = "";
//        this.fullname = "";
//        this.usertype = Usertype.UNASSIGNED;
    }

    public User(int id) {
        this.id = id;
//        this.username = "";
//        this.password = "";
//        this.fullname = "";
//        this.usertype = Usertype.UNASSIGNED;
    }

    public User(int id, Usertype usertype) {
        this.id = id;
//        this.username = "";
//        this.password = "";
//        this.fullname = "";
        this.usertype = usertype;
    }

    public User(String username, Usertype usertype) {
        this.username = username.toLowerCase();
//        this.password = "";
//        this.fullname = "";
        this.usertype = usertype;
    }

    public User(String username, String password, Usertype usertype) {
        this.username = username.toLowerCase();
        this.password = password;
//        this.fullname = "";
        this.usertype = usertype;
    }

    public User(String username, String password, String fullName, Usertype usertype) {
        this.username = username.toLowerCase();
        this.password = password;
        this.fullname = fullName;
        this.usertype = usertype;
    }

    public enum Usertype {
        CUSTOMER, EMPLOYEE, UNASSIGNED;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) { this.id = id; }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username.toLowerCase();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullname() { return fullname; }

    public void setFullname(String fullname) { this.fullname = fullname; }

    public Usertype getUsertype() {
        return usertype;
    }

    public void setUsertype(Usertype usertype) {
        this.usertype = usertype;
    }

    @Override
    public String toString() {
        return "User {" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullname='" + fullname + '\'' +
                ", usertype=" + usertype +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && Objects.equals(username, user.username) && Objects.equals(password, user.password) && Objects.equals(fullname, user.fullname) && usertype == user.usertype;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, password, fullname, usertype);
    }
}
