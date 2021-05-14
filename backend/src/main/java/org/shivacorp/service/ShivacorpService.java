package org.shivacorp.service;

import org.shivacorp.exception.BusinessException;
import org.shivacorp.model.Account;
import org.shivacorp.model.Transaction;
import org.shivacorp.model.User;

import java.util.Date;
import java.util.List;

public interface ShivacorpService {
        // CREATE
    User register(User user) throws BusinessException;
    Account addAccount(User user) throws BusinessException;
    Account addAccountForUserId(int id) throws BusinessException;

        // READ
    User login(User user) throws BusinessException;
    List<Account> getUsers() throws BusinessException;
    List<Account> getAccounts() throws BusinessException;
    User getUserById(int id) throws BusinessException;
    User getUserByUsernameAndUserType(User user) throws BusinessException;
    List<Account> getAccountsByUserId(int id) throws BusinessException;
    List<Account> getAccountsByApprovedBy(int id) throws BusinessException;
    List<Account> getAccountsByBalance(Double amount) throws BusinessException;
    Account getAccountById(int id) throws BusinessException;
    List<Account> getAccountsByStatus(Account.StatusType status) throws BusinessException;
    List<Transaction> getTransactionsByUserId(int id) throws BusinessException;
    Transaction getTransactionsById(int id) throws BusinessException;
    List<Transaction> getTransactionsByType(Transaction.TransactionType type) throws BusinessException;
    List<Transaction> getTransactionsByAccountId(int id) throws BusinessException;
    List<Transaction> getTransactionsByAmount(Double amount) throws BusinessException;
    List<Transaction> getTransactionsByDate(String date) throws BusinessException;
//    boolean hasActiveAccount(User user) throws BusinessException;

        // UPDATE

    Account updateAccountStatus(Account account) throws BusinessException;
    Account withdraw(int accountId, double amount) throws BusinessException;
    Account deposit(int accountID, double amount) throws BusinessException;
    Account transfer(int fromAccountId, int toAccountId, double amount) throws BusinessException;

        // DELETE
    //void deleteAccount(Account account) throws BusinessException;
}
