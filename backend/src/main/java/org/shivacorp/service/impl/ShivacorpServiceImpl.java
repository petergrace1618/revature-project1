package org.shivacorp.service.impl;

import org.shivacorp.dao.ShivacorpDAO;
import org.shivacorp.dao.impl.ShivacorpDAOImpl;
import org.shivacorp.exception.BusinessException;
import org.shivacorp.model.Account;
import org.shivacorp.model.Transaction;
import org.shivacorp.model.User;
import org.shivacorp.service.ShivacorpService;

import java.util.Date;
import java.util.List;

public class ShivacorpServiceImpl implements ShivacorpService {
    private final ShivacorpDAO dao = new ShivacorpDAOImpl();

    //// CREATE
    @Override
    public User register(User user) throws BusinessException {
        user.setUsername(user.getUsername().toLowerCase());
        if (dao.userExists(user)) {
            throw new BusinessException("Username '"+user.getUsername()+"' already exists. Please try again.");
        } else {
            user = dao.addUser(user);
            return user;
        }
    }

    @Override
    public Account addAccount(User user) throws BusinessException {
        Account account = new Account(user, 0, Account.StatusType.PENDING);
        account = dao.addAccount(account);
        return account;
    }

    @Override
    public Account addAccountForUserId(int id) throws BusinessException {
        User user = getUserById(id);
        if (user == null) {
            throw new BusinessException("Customer does not exist");
        }
        if (user.getUsertype() != User.Usertype.CUSTOMER) {
            throw new BusinessException("Cannot create account for non-customer.");
        }
        return addAccount(user);
    }

    //// READ
    @Override
    public User login(User user) throws BusinessException {
        // lookup username
        user.setUsername(user.getUsername().toLowerCase());
        User found = dao.getUserByUsernameAndUsertype(user);

        // invalid username
        if (found == null)
            throw new BusinessException("Username '"+user.getUsername()+"' does not exist");

        // valid username, invalid password
        if (!found.getPassword().equals(user.getPassword()))
            throw new BusinessException("Authentication failed");

        // user authenticated
        return found;
    }

    @Override
    public List<Account> getUsers() throws BusinessException {
        List<Account> users =  dao.getUsers();
        if (users.isEmpty())
            throw new BusinessException("No existing accounts");
        return users;
    }

    @Override
    public User getUserById(int id) throws BusinessException{
        return dao.getUserById(id);
    }

    @Override
    public User getUserByUsernameAndUserType(User user) throws BusinessException {
        user.setUsername(user.getUsername().toLowerCase());
        User searchResult = dao.getUserByUsernameAndUsertype(user);
        if (searchResult == null)
            throw new BusinessException("Invalid username");
        return searchResult;
    }

    @Override
    public List<Account> getAccounts() throws BusinessException {
        List<Account> accounts =  dao.getAccounts();
        if (accounts.isEmpty())
            throw new BusinessException("No existing accounts");
        return accounts;
    }

    @Override
    public Account getAccountById(int id) throws BusinessException {
        Account account = dao.getAccountById(id);

        if (account == null) {
            throw new BusinessException("Account does not exist");
        }
        return account;
    }

    @Override
    public List<Account> getAccountsByUserId(int id) throws BusinessException {
        List<Account> accounts = dao.getAccountsByUserId(id);
        if (accounts.isEmpty()) {
            throw new BusinessException("No accounts");
        }
        return accounts;
    }

    @Override
    public List<Account> getAccountsByApprovedBy(int id) throws BusinessException {
        List<Account> accounts = dao.getAccountsByApprovedBy(id);
        if (accounts.isEmpty()) {
            throw new BusinessException("No accounts");
        }
        return accounts;
    }

    @Override
    public List<Account> getAccountsByBalance(Double amount) throws BusinessException {
        List<Account> accounts = dao.getAccountsByBalance(amount);
        if (accounts.isEmpty()) {
            throw new BusinessException("No accounts");
        }
        return accounts;
    }

    @Override
    public List<Account> getAccountsByStatus(Account.StatusType status) throws BusinessException {
        List<Account> accountList = dao.getAccountsByStatus(status);
        if (accountList.isEmpty())
            throw new BusinessException("No "+status.name().toLowerCase()+" accounts");
        return accountList;
    }

    @Override
    public List<Transaction> getTransactionsByUserId(int id) throws BusinessException {
        List<Transaction> transactions = dao.getTransactionsByUserId(id);
        if (transactions.isEmpty())
            throw new BusinessException("No transactions");
        return transactions;
    }

    @Override
    public Transaction getTransactionsById(int id) throws BusinessException {
        Transaction transaction = dao.getTransactionsById(id);
        if (transaction == null)
            throw new BusinessException("No transactions");
        return transaction;
    }

    @Override
    public List<Transaction> getTransactionsByType(Transaction.TransactionType type) throws BusinessException {
        List<Transaction> transactions = dao.getTransactionsByType(type);
        if (transactions.isEmpty())
            throw new BusinessException("No transactions");
        return transactions;
    }

    @Override
    public List<Transaction> getTransactionsByAccountId(int id) throws BusinessException {
        List<Transaction> transactions = dao.getTransactionsByAccountId(id);
        if (transactions.isEmpty())
            throw new BusinessException("No transactions");
        return transactions;
    }

    @Override
    public List<Transaction> getTransactionsByAmount(Double amount) throws BusinessException {
        List<Transaction> transactions = dao.getTransactionsByAmount(amount);
        if (transactions.isEmpty())
            throw new BusinessException("No transactions");
        return transactions;
    }

    @Override
    public List<Transaction> getTransactionsByDate(String date) throws BusinessException {
        return dao.getTransactionsByDate(date);
    }

//    @Override
//    public boolean hasActiveAccount(User user) throws BusinessException {
//        Account account = dao.getAccountByUser(user);
//        if (account == null)
//            throw new BusinessException("N");
//        return (account != null) && (account.getStatus() == Account.StatusType.APPROVED);
//    }

    // UPDATE
    @Override
    public Account updateAccountStatus(Account account) throws BusinessException {
        // make sure approvedBy employee exists
        User approvedBy = dao.getUserById(account.getApprovedBy().getId());
        if (approvedBy == null) {
            throw new BusinessException("No such employee ("+account.getApprovedBy().getId()+")");
        }

        Account currentAccount = dao.getAccountById(account.getId());

        // Don't update if status hasn't changed
        if (account.getStatus() == currentAccount.getStatus()) {
            System.out.println("New status same as current status. Aborting update.");
            return currentAccount;
        }
        // update account record
        account = dao.updateAccountStatus(account);

        // attach foreign key references
        account.setApprovedBy(approvedBy);
        account.setUser(currentAccount.getUser());

        // add transaction
        Transaction.TransactionType transactionType =
                (account.getStatus() == Account.StatusType.APPROVED)
                        ? Transaction.TransactionType.ACCOUNT_APPROVED
                        : Transaction.TransactionType.ACCOUNT_DENIED;
        Transaction transaction = new Transaction.Builder()
                .withTimestamp()
                .withTransactionType(transactionType)
                .withAccountId(account.getId())
                .build();
        dao.addTransaction(transaction);

        return account;
    }

    @Override
    public Account deposit(int accountId, double amount) throws BusinessException {
        Account account = getAccountById(accountId);

        // account doesn't exist
        if (account == null) {
            throw new BusinessException("Account "+accountId+" doesn't exist.");
        }

        // account is pending
        if (account.getStatus() != Account.StatusType.APPROVED) {
            throw new BusinessException("Account "+accountId+" is pending or inactive.");
        }

        // negative amount
        if (amount <= 0)
            throw new BusinessException("Deposit amount must be greater than zero");

        // make deposit and add to transactions
        account = dao.updateBalance(account, account.getBalance() + amount);
        Transaction transaction = new Transaction.Builder()
                .withTimestamp()
                .withTransactionType(Transaction.TransactionType.DEPOSIT)
                .withAccountId(account.getId())
                .withAmount(amount)
                .build();
        dao.addTransaction(transaction);
        return account;
    }

    @Override
    public Account withdraw(int accountId, double amount) throws BusinessException {
        Account account = getAccountById(accountId);

        // account doesn't exist
        if (account == null) {
            throw new BusinessException("Account "+accountId+" doesn't exist.");
        }

        // account is pending
        if (account.getStatus() != Account.StatusType.APPROVED) {
            throw new BusinessException("Account "+accountId+" is pending or inactive.");
        }

        // negative amount
        if (amount <= 0)
            throw new BusinessException("Withdrawal amount must be greater than zero");

        // insufficient funds
        if (amount > account.getBalance())
            throw new BusinessException("Account "+accountId+" has insufficient funds to cover transfer");

        // make withdrawal and add to transactions
        account = dao.updateBalance(account, account.getBalance() - amount);
        Transaction transaction = new Transaction.Builder()
                .withTimestamp()
                .withTransactionType(Transaction.TransactionType.WITHDRAWAL)
                .withAccountId(account.getId())
                .withAmount(amount)
                .build();
        dao.addTransaction(transaction);
        return account;
    }

    @Override
    public Account transfer(int fromAccountId, int toAccountId, double amount) throws BusinessException {
        Account fromAccount = dao.getAccountById(fromAccountId);
        Account toAccount = dao.getAccountById(toAccountId);

        // source account doesn't exist
        if (fromAccount == null)
            throw new BusinessException("Account "+fromAccountId+" doesn't exist");

        // source account is pending
        if (fromAccount.getStatus() != Account.StatusType.APPROVED)
            throw new BusinessException("Account "+fromAccountId+" is pending or inactive");

        // destincation account doesn't exist
        if (toAccount == null)
            throw new BusinessException("Account "+toAccountId+" doesn't exist");

        // destination account is not active
        if (toAccount.getStatus() != Account.StatusType.APPROVED)
            throw new BusinessException("Account "+toAccountId+" is pending or inactive");

        // source and destination accounts are the same
        if (fromAccountId == toAccountId)
            throw new BusinessException("Source and destination accounts are the same");

        // source account has insufficient funds
        if (fromAccount.getBalance() < amount)
            throw new BusinessException("Account "+fromAccountId+" has insufficient funds to cover transfer");

        // update source account
        fromAccount =  dao.updateBalance(fromAccount, fromAccount.getBalance() - amount);

        // add transaction showing debit from source account
        Transaction transaction = new Transaction.Builder()
                .withTimestamp()
                .withTransactionType(Transaction.TransactionType.TRANSFER_DEBIT)
                .withAccountId(fromAccount.getId())
                .withAmount(amount)
                .build();
        dao.addTransaction(transaction);

        // update destination account
        dao.updateBalance(toAccount, toAccount.getBalance() + amount);

        // add transaction showing credit to destination account
        transaction = new Transaction.Builder()
                .withTimestamp()
                .withTransactionType(Transaction.TransactionType.TRANSFER_CREDIT)
                .withAccountId(toAccount.getId())
                .withAmount(amount)
                .build();
        dao.addTransaction(transaction);

        return fromAccount;
    }
}
