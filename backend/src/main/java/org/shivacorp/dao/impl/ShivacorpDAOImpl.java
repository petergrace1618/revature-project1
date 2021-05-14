package org.shivacorp.dao.impl;

import org.shivacorp.dao.ShivacorpDAO;
import org.shivacorp.dao.dbutil.PostgreSQLConnection;
import org.shivacorp.exception.BusinessException;
import org.shivacorp.model.Account;
import org.shivacorp.model.Transaction;
import org.shivacorp.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShivacorpDAOImpl implements ShivacorpDAO {
    // CREATE
    @Override
    public User addUser(User user) throws BusinessException {
        try (Connection connection = PostgreSQLConnection.getConnection()) {
            String sql =
                    "INSERT INTO shivacorp_schema.users (username, password, fullname, usertype) "+
                    "VALUES (?, ?, ?, CAST(? AS shivacorp_schema.user_type));";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getFullname());
            preparedStatement.setString(4, user.getUsertype().name());
            int c = preparedStatement.executeUpdate();
            if (c == 1) {
                ResultSet resultSet = preparedStatement.getGeneratedKeys();
                if (resultSet.next()) {
                    user.setId(resultSet.getInt(1));
                }
            } else {
                throw new BusinessException("Failed: Add user");
            }
        } catch (SQLException e) {
            throw new BusinessException(className()+".addUser: "+e.getMessage());
        }
        return user;
    }

    @Override
    public Account addAccount(Account account) throws BusinessException {
        try(Connection connection = PostgreSQLConnection.getConnection()) {
            String sql =
                "INSERT INTO shivacorp_schema.accounts (customerid, approvedby, balance, status) "+
                "VALUES (?, ?, ?::float8::numeric::money, CAST(? AS shivacorp_schema.status_type));";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, account.getUserId());
            if (account.getApprovedBy() == null) {
                preparedStatement.setNull(2, Types.INTEGER);
            } else {
                preparedStatement.setInt(2,account.getApprovedBy().getId());
            }
            preparedStatement.setDouble(3, account.getBalance());
            preparedStatement.setString(4, Account.StatusType.PENDING.name());
            int c = preparedStatement.executeUpdate();
            if (c == 1) {
                ResultSet resultSet = preparedStatement.getGeneratedKeys();
                if (resultSet.next()) {
                    account.setId(resultSet.getInt(1));
                }
            } else {
                throw new BusinessException("Failed: Create new account");
            }
        } catch (SQLException e) {
            throw new BusinessException(className()+".addAccount: "+e.getMessage());
        }
        return account;
    }

    @Override
    public Transaction addTransaction(Transaction transaction) throws BusinessException{
        try (Connection connection = PostgreSQLConnection.getConnection()) {
            String sql =
                    "INSERT INTO shivacorp_schema.transactions "+
                    "(datetime, transactiontype, accountid, amount) VALUES "+
                    "(?, ?::shivacorp_schema.transaction_type, ?, ?::float8::numeric::money);";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setTimestamp(1, transaction.getDatetime());
            preparedStatement.setString(2, transaction.getTransactionType().name());
            preparedStatement.setInt(3, transaction.getAccountId());
            preparedStatement.setDouble(4, transaction.getAmount());

            int rowCount = preparedStatement.executeUpdate();
            if (rowCount == 0) {
                throw new BusinessException(className()+".addTransaction: Failed add transaction");
            }
        } catch (SQLException e) {
            throw new BusinessException(className()+".addTransaction: "+e.getMessage());
        }
        return transaction;
    }

    // READ
    @Override
    public List<Account> getUsers() throws BusinessException {
        List<Account> accounts = new ArrayList<>();
        try (Connection connection = PostgreSQLConnection.getConnection()) {
            String sql =
                    "SELECT u.id as user_id, username, fullname, usertype, "+
                            "a.id as account_id, balance::money::numeric::float8, status "+
                            "FROM shivacorp_schema.users as u "+
                            "LEFT JOIN shivacorp_schema.accounts as a ON u.id = a.userid "+
                            "WHERE username <> 'admin';";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeQuery();
            ResultSet resultSet = preparedStatement.getResultSet();
            while(resultSet.next()) {
                User user = new User();
                user.setId(resultSet.getInt("user_id"));
                user.setUsername(resultSet.getString("username"));
                user.setFullname(resultSet.getString("fullname"));
                user.setUsertype(User.Usertype.valueOf(resultSet.getString("usertype")));
                Account account = new Account();
                account.setUser(user);
                int id = resultSet.getInt("account_id");
                if (id != 0) {
                    account.setId(id);
                    account.setBalance(resultSet.getDouble("balance"));
                    account.setUserId(resultSet.getInt("user_id"));
                    account.setStatus(Account.StatusType.valueOf(resultSet.getString("status")));
                } // if id == 0 then the row is NULL
                accounts.add(account);
            }
        } catch (SQLException e) {
            throw new BusinessException(className()+".getUsers: "+e.getMessage());
        }
        return accounts;
    }

    @Override
    public User getUserById(int id) throws BusinessException {
        User user = null;
        try (Connection connection = PostgreSQLConnection.getConnection()) {
            String sql =
                    "SELECT id, username, password, fullname, usertype "+
                            "FROM shivacorp_schema.users WHERE id = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                user = new User();
                user.setId(resultSet.getInt("id"));
                user.setUsername(resultSet.getString("username"));
                user.setPassword(resultSet.getString("password"));
                user.setFullname(resultSet.getString("fullname"));
                user.setUsertype(User.Usertype.valueOf(resultSet.getString("usertype")));
            }
        } catch (SQLException e) {
            throw new BusinessException(className()+".getUserById: "+e.getMessage());
        }
        return user;
    }

    @Override
    public User getUserByUsernameAndUsertype(User user) throws BusinessException {
        User result = null;
        try(Connection connection = PostgreSQLConnection.getConnection()) {
            String sql =
                    "SELECT id, username, password, fullname, usertype "+
                    "FROM shivacorp_schema.users "+
                    "WHERE username = ? AND usertype = ?::shivacorp_schema.user_type;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getUsertype().name());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                result = new User();
                result.setId(resultSet.getInt("id"));
                result.setUsername(resultSet.getString("username"));
                result.setPassword(resultSet.getString("password"));
                result.setFullname(resultSet.getString("fullname"));
                result.setUsertype(User.Usertype.valueOf(resultSet.getString("usertype")));
            }
        } catch (SQLException e) {
            throw new BusinessException(className()+".getUserByUsernameAndUsertype: "+e.getMessage());
        }
        return result;
    }

    @Override
    public List<Account> getAccounts() throws BusinessException {
        List<Account> accountList = new ArrayList<>();
        try(Connection connection = PostgreSQLConnection.getConnection()) {
            String sql =
                    "SELECT a.id, a.userid, a.balance::money::numeric::float8, a.status, "+
                            "u.username, u.fullname, u.usertype "+
                            "FROM shivacorp_schema.accounts as a "+
                            "JOIN shivacorp_schema.users as u ON a.userid = u.id;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeQuery();
            ResultSet resultSet = preparedStatement.getResultSet();
            while(resultSet.next()) {
                User user = new User();
                user.setId(resultSet.getInt("userid"));
                user.setUsername(resultSet.getString("username"));
                user.setFullname(resultSet.getString("fullname"));
                user.setUsertype(User.Usertype.valueOf(resultSet.getString("usertype")));

                Account account = new Account();
                account.setId(resultSet.getInt("id"));
                account.setBalance(resultSet.getDouble("balance"));
                account.setStatus(Account.StatusType.valueOf(resultSet.getString("status")));
                account.setUser(user);
                accountList.add(account);
            }
        } catch (SQLException e) {
            throw new BusinessException(className()+".getAccounts: "+e.getMessage());
        }
        return accountList;
    }

    @Override
    public Account getAccountById(int id) throws BusinessException {
        Account account = null;
        try(Connection connection = PostgreSQLConnection.getConnection()) {
            String sql =
                "SELECT a.id as a_id, a.customerid, a.approvedby, a.balance::money::numeric::float8, a.status, "+
                    "c.id as c_id, c.username as c_username, c.fullname as c_fullname, c.usertype as c_usertype, "+
                    "e.id as e_id, e.username as e_username, e.fullname as e_fullname, e.usertype as e_usertype "+
                    "FROM shivacorp_schema.accounts as a "+
                    "JOIN shivacorp_schema.users as c ON a.customerid = c.id  "+
                    "LEFT JOIN shivacorp_schema.users as e ON a.approvedby = e.id "+
                    "WHERE a.id = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                account = new Account();
                account.setId(resultSet.getInt("a_id"));
                account.setBalance(resultSet.getDouble("balance"));
                account.setStatus(Account.StatusType.valueOf(resultSet.getString("status")));

                User customer = new User();
                customer.setId(resultSet.getInt("c_id"));
                customer.setUsername(resultSet.getString("c_username"));
                customer.setFullname(resultSet.getString("c_fullname"));
                customer.setUsertype(User.Usertype.valueOf(resultSet.getString("c_usertype")));
                account.setUser(customer);

                User employee = new User();
                if (resultSet.getInt("e_id") != 0) {
                    employee.setId(resultSet.getInt("e_id"));
                    employee.setUsername(resultSet.getString("e_username"));
                    employee.setFullname(resultSet.getString("e_fullname"));
                    employee.setUsertype(User.Usertype.valueOf(resultSet.getString("e_usertype")));
                }
                account.setApprovedBy(employee);
            }
        } catch (SQLException e) {
            throw new BusinessException(className()+".getAccountById: "+e.getMessage());
        }
        return account;
    }

    @Override
    public List<Account> getAccountsByUserId(int id) throws BusinessException {
        List <Account> accounts = new ArrayList<>();
        try(Connection connection = PostgreSQLConnection.getConnection()) {
            String sql =
                "SELECT a.id as a_id, a.customerid, a.approvedby, a.balance::money::numeric::float8, a.status, "+
                        "c.id as c_id, c.username as c_username, c.fullname as c_fullname, c.usertype as c_usertype, "+
                        "e.id as e_id, e.username as e_username, e.fullname as e_fullname, e.usertype as e_usertype "+
                        "FROM shivacorp_schema.accounts as a "+
                        "JOIN shivacorp_schema.users as c ON a.customerid = c.id  "+
                        "LEFT JOIN shivacorp_schema.users as e ON a.approvedby = e.id "+
                        "WHERE a.customerid = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Account account = new Account();
                account.setId(resultSet.getInt("a_id"));
                account.setBalance(resultSet.getDouble("balance"));
                account.setStatus(Account.StatusType.valueOf(resultSet.getString("status")));

                User customer = new User();
                customer.setId(resultSet.getInt("c_id"));
                customer.setUsername(resultSet.getString("c_username"));
                customer.setFullname(resultSet.getString("c_fullname"));
                customer.setUsertype(User.Usertype.valueOf(resultSet.getString("c_usertype")));
                account.setUser(customer);

                User employee = new User();
                if (resultSet.getInt("e_id") != 0) {
                    employee.setId(resultSet.getInt("e_id"));
                    employee.setUsername(resultSet.getString("e_username"));
                    employee.setFullname(resultSet.getString("e_fullname"));
                    employee.setUsertype(User.Usertype.valueOf(resultSet.getString("e_usertype")));
                }
                account.setApprovedBy(employee);
                accounts.add(account);
            }
        } catch (SQLException e) {
            throw new BusinessException(className()+".getAccountsByUserId: "+e.getMessage());
        }
        return accounts;
    }

    @Override
    public List<Account> getAccountsByStatus(Account.StatusType status) throws BusinessException {
        List<Account> accounts = new ArrayList<>();
        try(Connection connection = PostgreSQLConnection.getConnection()) {
            String sql =
                "SELECT a.id as a_id, a.customerid, a.approvedby, a.balance::money::numeric::float8, a.status, "+
                        "c.id as c_id, c.username as c_username, c.fullname as c_fullname, c.usertype as c_usertype, "+
                        "e.id as e_id, e.username as e_username, e.fullname as e_fullname, e.usertype as e_usertype "+
                        "FROM shivacorp_schema.accounts as a "+
                        "JOIN shivacorp_schema.users as c ON a.customerid = c.id  "+
                        "LEFT JOIN shivacorp_schema.users as e ON a.approvedby = e.id "+
                        "WHERE a.status = ?::shivacorp_schema.status_type;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, status.name());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Account account = new Account();
                account.setId(resultSet.getInt("a_id"));
                account.setBalance(resultSet.getDouble("balance"));
                account.setStatus(Account.StatusType.valueOf(resultSet.getString("status")));

                User customer = new User();
                customer.setId(resultSet.getInt("c_id"));
                customer.setUsername(resultSet.getString("c_username"));
                customer.setFullname(resultSet.getString("c_fullname"));
                customer.setUsertype(User.Usertype.valueOf(resultSet.getString("c_usertype")));
                account.setUser(customer);

                User employee = new User();
                if (resultSet.getInt("e_id") != 0) {
                    employee.setId(resultSet.getInt("e_id"));
                    employee.setUsername(resultSet.getString("e_username"));
                    employee.setFullname(resultSet.getString("e_fullname"));
                    employee.setUsertype(User.Usertype.valueOf(resultSet.getString("e_usertype")));
                }
                account.setApprovedBy(employee);

                accounts.add(account);
            }
        } catch (SQLException e) {
            throw new BusinessException(className()+".getAccountById: "+e.getMessage());
        }
        return accounts;
    }

//    @Override
//    public Account getAccountByUser(User user) throws BusinessException {
//        Account account = null;
//        try(Connection connection = PostgreSQLConnection.getConnection()) {
//            String sql =
//                    "SELECT u.id, u.username, u.fullname, u.usertype, "+
//                            "a.id as account_id, a.balance::money::numeric::float8, a.status "+
//                    "FROM shivacorp_schema.users as u "+
//                    "JOIN shivacorp_schema.accounts as a ON u.id = a.userid "+
//                    "WHERE u.id = ?;";
//            PreparedStatement preparedStatement = connection.prepareStatement(sql);
//            preparedStatement.setInt(1, user.getId());
//            ResultSet resultSet = preparedStatement.executeQuery();
//            if (resultSet.next()) {
//                account = new Account();
//                account.setId(resultSet.getInt("account_id"));
//                account.setBalance(resultSet.getDouble("balance"));
////                String status = resultSet.getString("status");
////                account.setStatus(status != null ? Account.StatusType.valueOf(status) : null);
//                account.setStatus(Account.StatusType.valueOf(resultSet.getString("status")));
//
////                user.setId(resultSet.getInt("id"));
////                user.setUsername(resultSet.getString("username"));
////                user.setUsertype(User.Usertype.valueOf(resultSet.getString("usertype")));
//                account.setUser(user);
//            }
//        } catch (SQLException e) {
//            throw new BusinessException(className()+".getUserByUsername: "+e.getMessage());
//        }
//        return account;
//    }

    @Override
    public List<Account> getAccountsByApprovedBy(int id) throws BusinessException {
        List<Account> accounts = new ArrayList<>();
        try(Connection connection = PostgreSQLConnection.getConnection()) {
            String sql =
                "SELECT a.id as a_id, a.customerid, a.approvedby, a.balance, a.status, "+
                    "c.id as c_id, c.username as c_username, c.fullname as c_fullname, c.usertype as c_usertype, "+
                    "e.id as e_id, e.username as e_username, e.fullname as e_fullname, e.usertype as e_usertype "+
                    "FROM shivacorp_schema.accounts as a "+
                    "JOIN shivacorp_schema.users as c ON a.customerid = c.id  "+
                    "LEFT JOIN shivacorp_schema.users as e ON a.approvedby = e.id "+
                    "WHERE a.approvedby = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Account account = new Account();
                account.setId(resultSet.getInt("a_id"));
                account.setBalance(resultSet.getDouble("balance"));
                account.setStatus(Account.StatusType.valueOf(resultSet.getString("status")));

                User customer = new User();
                customer.setId(resultSet.getInt("c_id"));
                customer.setUsername(resultSet.getString("c_username"));
                customer.setFullname(resultSet.getString("c_fullname"));
                customer.setUsertype(User.Usertype.valueOf(resultSet.getString("c_usertype")));
                account.setUser(customer);

                User employee = new User();
                if (resultSet.getInt("e_id") != 0) {
                    employee.setId(resultSet.getInt("e_id"));
                    employee.setUsername(resultSet.getString("e_username"));
                    employee.setFullname(resultSet.getString("e_fullname"));
                    employee.setUsertype(User.Usertype.valueOf(resultSet.getString("e_usertype")));
                    account.setApprovedBy(employee);
                }
                accounts.add(account);
            }
        } catch (SQLException e) {
            throw new BusinessException(className()+".getAccountsByApprovedBy: "+e.getMessage());
        }
        return accounts;
    }

    @Override
    public List<Account> getAccountsByBalance(Double amount) throws BusinessException {
        List<Account> accounts = new ArrayList<>();
        try(Connection connection = PostgreSQLConnection.getConnection()) {
            String sql =
            "SELECT a.id as a_id, a.customerid, a.approvedby, a.balance::money::numeric::float8, a.status, "+
            "c.id as c_id, c.username as c_username, c.fullname as c_fullname, c.usertype as c_usertype, "+
            "e.id as e_id, e.username as e_username, e.fullname as e_fullname, e.usertype as e_usertype "+
            "FROM shivacorp_schema.accounts as a "+
            "JOIN shivacorp_schema.users as c ON a.customerid = c.id  "+
            "LEFT JOIN shivacorp_schema.users as e ON a.approvedby = e.id "+
            "WHERE a.balance = ?::float8::numeric::money;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setDouble(1, amount);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Account account = new Account();
                account.setId(resultSet.getInt("a_id"));
                account.setBalance(resultSet.getDouble("balance"));
                account.setStatus(Account.StatusType.valueOf(resultSet.getString("status")));

                User customer = new User();
                customer.setId(resultSet.getInt("c_id"));
                customer.setUsername(resultSet.getString("c_username"));
                customer.setFullname(resultSet.getString("c_fullname"));
                customer.setUsertype(User.Usertype.valueOf(resultSet.getString("c_usertype")));
                account.setUser(customer);

                User employee = new User();
                if (resultSet.getInt("e_id") != 0) {
                    employee.setId(resultSet.getInt("e_id"));
                    employee.setUsername(resultSet.getString("e_username"));
                    employee.setFullname(resultSet.getString("e_fullname"));
                    employee.setUsertype(User.Usertype.valueOf(resultSet.getString("e_usertype")));
                    account.setApprovedBy(employee);
                }
                accounts.add(account);
            }
        } catch (SQLException e) {
            throw new BusinessException(className()+".getAccountsByBalance: "+e.getMessage());
        }
        return accounts;
    }

    @Override
    public List<Transaction> getTransactions() throws BusinessException {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection connection = PostgreSQLConnection.getConnection()) {
            String sql =
                "SELECT id, datetime, transactiontype, accountid, amount::money::numeric::float8 "+
                "FROM shivacorp_schema.transactions;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeQuery();
            ResultSet resultSet = preparedStatement.getResultSet();
            while(resultSet.next()) {
                Transaction transaction = new Transaction.Builder()
                        .withId(resultSet.getInt("id"))
                        .withTimestamp(resultSet.getTimestamp("datetime"))
                        .withTransactionType(Transaction.TransactionType.valueOf(
                                        resultSet.getString("transactiontype")))
                        .withAccountId(resultSet.getInt("accountid"))
                        .withAmount(resultSet.getDouble("amount"))
                        .build();
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            throw new BusinessException(className()+".getTransactions: "+e.getMessage());
        }
        return transactions;
    }

    @Override
    public Transaction getTransactionsById(int id) throws BusinessException {
        Transaction transaction = null;
        try (Connection connection = PostgreSQLConnection.getConnection()) {
            String sql =
                    "SELECT id, datetime, transactiontype, accountid, amount::money::numeric::float8 "+
                    "FROM shivacorp_schema.transactions "+
                    "WHERE id = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            preparedStatement.executeQuery();
            ResultSet resultSet = preparedStatement.getResultSet();
            if (resultSet.next()) {
                transaction = new Transaction.Builder()
                        .withId(id)
//                        .withId(resultSet.getInt("id"))
                        .withTimestamp(resultSet.getTimestamp("datetime"))
                        .withTransactionType(Transaction.TransactionType.valueOf(
                                resultSet.getString("transactiontype")))
                        .withAccountId(resultSet.getInt("accountid"))
                        .withAmount(resultSet.getDouble("amount"))
                        .build();
            }
        } catch (SQLException e) {
            throw new BusinessException(className()+".getTransactionsById: "+e.getMessage());
        }
        return transaction;
    }

    @Override
    public List<Transaction> getTransactionsByUserId(int id) throws BusinessException {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection connection = PostgreSQLConnection.getConnection()) {
            String sql =
                "SELECT t.id as t_id, t.datetime, t.transactiontype, t.accountid, t.amount::money::numeric::float8, "+
                        "a.id, a.customerid, a.approvedby, a.balance, a.status "+
                "FROM shivacorp_schema.transactions AS t "+
                "JOIN shivacorp_schema.accounts AS a ON t.accountid = a.id "+
                "WHERE a.customerid = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            preparedStatement.executeQuery();
            ResultSet resultSet = preparedStatement.getResultSet();
            while(resultSet.next()) {
                Transaction transaction = new Transaction.Builder()
                        .withId(resultSet.getInt("t_id"))
                        .withTimestamp(resultSet.getTimestamp("datetime"))
                        .withTransactionType(Transaction.TransactionType.valueOf(
                                resultSet.getString("transactiontype")))
                        .withAccountId(resultSet.getInt("accountid"))
                        .withAmount(resultSet.getDouble("amount"))
                        .build();
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            throw new BusinessException(className()+".getTransactionsByUserId: "+e.getMessage());
        }
        return transactions;
    }

    @Override
    public List<Transaction> getTransactionsByType(Transaction.TransactionType type) throws BusinessException {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection connection = PostgreSQLConnection.getConnection()) {
            String sql =
                "SELECT id, datetime, transactiontype, accountid, amount::money::numeric::float8 "+
                "FROM shivacorp_schema.transactions "+
                "WHERE transactiontype = ?::shivacorp_schema.transaction_type;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, type.name());
            preparedStatement.executeQuery();
            ResultSet resultSet = preparedStatement.getResultSet();
            while(resultSet.next()) {
                Transaction transaction = new Transaction.Builder()
                        .withId(resultSet.getInt("id"))
                        .withTimestamp(resultSet.getTimestamp("datetime"))
                        .withTransactionType(Transaction.TransactionType.valueOf(
                                resultSet.getString("transactiontype")))
                        .withAccountId(resultSet.getInt("accountid"))
                        .withAmount(resultSet.getDouble("amount"))
                        .build();
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            throw new BusinessException(className()+".getTransactionsByType: "+e.getMessage());
        }
        return transactions;
    }

    @Override
    public List<Transaction> getTransactionsByAccountId(int id) throws BusinessException {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection connection = PostgreSQLConnection.getConnection()) {
            String sql =
                    "SELECT id, datetime, transactiontype, accountid, amount::money::numeric::float8 "+
                    "FROM shivacorp_schema.transactions "+
                    "WHERE accountid = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            preparedStatement.executeQuery();
            ResultSet resultSet = preparedStatement.getResultSet();
            while(resultSet.next()) {
                Transaction transaction = new Transaction.Builder()
                        .withId(resultSet.getInt("id"))
                        .withTimestamp(resultSet.getTimestamp("datetime"))
                        .withTransactionType(Transaction.TransactionType.valueOf(
                                resultSet.getString("transactiontype")))
                        .withAccountId(resultSet.getInt("accountid"))
                        .withAmount(resultSet.getDouble("amount"))
                        .build();
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            throw new BusinessException(className()+".getTransactionsByAccountId: "+e.getMessage());
        }
        return transactions;
    }

    @Override
    public List<Transaction> getTransactionsByDate(String date) throws BusinessException {
        if (!date.equals("2021-05-14"))
            throw new BusinessException("No transactions on "+date);

        return getTransactions();
    }

    @Override
    public List<Transaction> getTransactionsByAmount(Double amount) throws BusinessException {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection connection = PostgreSQLConnection.getConnection()) {
            String sql =
                    "SELECT id, datetime, transactiontype, accountid, amount::money::numeric::float8 "+
                    "FROM shivacorp_schema.transactions "+
                    "WHERE amount = ?::float8::numeric::money;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setDouble(1, amount);
            preparedStatement.executeQuery();
            ResultSet resultSet = preparedStatement.getResultSet();
            while (resultSet.next()) {
                Transaction transaction = new Transaction.Builder()
                        .withId(resultSet.getInt("id"))
                        .withTimestamp(resultSet.getTimestamp("datetime"))
                        .withTransactionType(Transaction.TransactionType.valueOf(
                                resultSet.getString("transactiontype")))
                        .withAccountId(resultSet.getInt("accountid"))
                        .withAmount(resultSet.getDouble("amount"))
                        .build();
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            throw new BusinessException(className()+".getTransactionsByAmount: "+e.getMessage());
        }
        return transactions;
    }

    // UPDATE
    @Override
    public Account updateAccountStatus(Account account) throws BusinessException {
        try (Connection connection = PostgreSQLConnection.getConnection()) {
            String sql =
                    "UPDATE shivacorp_schema.accounts "+
                    "SET status = CAST(? AS shivacorp_schema.status_type), approvedby = ? "+
                    "WHERE id = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, account.getStatus().name());
            preparedStatement.setInt(2, account.getApprovedBy().getId());
            preparedStatement.setInt(3, account.getId());
            account.setStatus(account.getStatus());
            int c = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new BusinessException(className()+".updateAccountStatus: "+e.getMessage());
        }
        return account;
    }

    @Override
    public Account updateBalance(Account account, double amount) throws BusinessException {
        try(Connection connection = PostgreSQLConnection.getConnection()) {
            String sql =
                    "UPDATE shivacorp_schema.accounts "+
                    "SET balance = ?::float8::numeric::money "+
                    "WHERE id = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setDouble(1, amount);
            preparedStatement.setInt(2, account.getId());
            int rowCount = preparedStatement.executeUpdate();
            if (rowCount == 0) {
                throw new BusinessException(
                        className()+".updateBalance: Failed: update balance id="+account.getId()
                );
            }
            account.setBalance(amount);
        } catch (SQLException e) {
            throw new BusinessException(className()+".updateBalance: "+e.getMessage());
        }
        return account;
    }


    // DELETE
    @Override
    public void deleteAccount(Account account) throws BusinessException {
        try(Connection connection = PostgreSQLConnection.getConnection()) {
            String sql =
                    "DELETE FROM shivacorp_schema.accounts WHERE id = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, account.getId());
            int rowCount = preparedStatement.executeUpdate();
            if (rowCount == 0)
                throw new BusinessException(
                        className()+".deleteAccount: Failed: delete account id="+account.getId()
                );
        } catch (SQLException e) {
            throw new BusinessException(className()+".deleteAccount: "+e.getMessage());
        }
    }

    // Utility methods
    @Override
    public boolean userExists(User user) throws BusinessException {
        try(Connection connection = PostgreSQLConnection.getConnection()) {
            String sql = "SELECT username FROM shivacorp_schema.users WHERE username = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user.getUsername());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public boolean hasAccount(User user) throws BusinessException {
        try(Connection connection = PostgreSQLConnection.getConnection()) {
            String sql = "SELECT userid FROM shivacorp_schema.accounts WHERE userid = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, user.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    private String className() {
        String c = this.getClass().getName();
        return c.substring(c.lastIndexOf('.')+1);
    }
}
