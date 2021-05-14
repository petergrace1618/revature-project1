package org.shivacorp.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Transaction {
    int id;
    Timestamp datetime;
    TransactionType transactionType;
    int accountId;
    double amount;

    // constructor must be private to use builder class
    private Transaction() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public enum TransactionType {
        ACCOUNT_CREATED, ACCOUNT_APPROVED, ACCOUNT_DENIED,
        DEPOSIT, WITHDRAWAL, TRANSFER_DEBIT, TRANSFER_CREDIT
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public Timestamp getDatetime() {
        return datetime;
    }

    public void setDatetime(Timestamp datetime) {
        this.datetime = datetime;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public static class Builder {
        int id;
        Timestamp datetime;
        TransactionType transactionType;
        int accountId;
        double amount;

        public Builder() {}

        public Builder withId(int id) {
            this.id = id;
            return this;
        }
        public Builder withTimestamp(Timestamp datetime) {
            this.datetime = datetime;
            return this;
        }
        public Builder withTimestamp() {
            withTimestamp(Timestamp.valueOf(LocalDateTime.now()));
            return this;
        }
        public Builder withTransactionType(TransactionType transactionType) {
            this.transactionType = transactionType;
            return this;
        }
        public Builder withAccountId(int accountId) {
            this.accountId = accountId;
            return this;
        }
        public Builder withAmount(double amount) {
            this.amount = amount;
            return this;
        }
        public Transaction build() {
            Transaction transaction = new Transaction();
            transaction.id = this.id;
            transaction.datetime = this.datetime;
            transaction.transactionType = this.transactionType;
            transaction.accountId = this.accountId;
            transaction.amount = this.amount;
            return transaction;
        }
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", datetime=" + datetime +
                ", transactionType=" + transactionType +
                ", accountId=" + accountId +
                ", amount=" + amount +
                '}';
    }
}
