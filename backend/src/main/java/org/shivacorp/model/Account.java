package org.shivacorp.model;

public class Account {
    int id;
    User user;
    User approvedBy;
    double balance;
    StatusType status;

    public enum StatusType { PENDING, APPROVED, DENIED; }

    public Account() { }

    public Account(User user, int balance, StatusType status) {
        this.user = user;
        this.balance = balance;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return user.getId();
    }

    public void setUserId(int userId) { user.setId(userId); }

    public User getUser() { return user; }

    public void setUser(User user) {
        this.user = user;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public StatusType getStatus() {
        return status;
    }

    public void setStatus(StatusType status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", user=" + user +
                ", approvedBy=" + approvedBy +
                ", balance=" + balance +
                ", status=" + status +
                '}';
    }
}
