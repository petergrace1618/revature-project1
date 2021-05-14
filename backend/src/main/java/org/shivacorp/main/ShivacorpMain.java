package org.shivacorp.main;

import io.javalin.Javalin;
import org.shivacorp.exception.ErrorMessage;
import org.shivacorp.model.Account;
import org.shivacorp.model.Transaction;
import org.shivacorp.model.User;
import org.shivacorp.service.ShivacorpService;
import org.shivacorp.service.impl.ShivacorpServiceImpl;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;

public class ShivacorpMain {
    private static final Logger log = Logger.getLogger(ShivacorpMain.class);
    public static void main(String[] args) {
        ShivacorpService service = new ShivacorpServiceImpl();

        Javalin app = Javalin
                .create(config->config.enableCorsForAllOrigins())
                .start(8000);

        log.info("App started");

        //// CREATE

        // CREATE EMPLOYEE
        app.post("/employee", ctx -> {
            log.info("Request: ("+ctx.method()+") "+ctx.path());
            try {
                User user = service.register(ctx.bodyAsClass(User.class));
                ctx.json(user);
            } catch (Exception e) {
                ctx.json(new ErrorMessage(e.getMessage()));
            }
            log.info("Response: "+ctx.resultString());
        });

        // CREATE CUSTOMER
        app.post("/customer", ctx -> {
            log.info("Request: ("+ctx.method()+") "+ctx.path());
            log.info("Body: "+ctx.body());
            try {
                User user = service.register(ctx.bodyAsClass(User.class));
                ctx.json(user);
            } catch (Exception e) {
                ctx.json(new ErrorMessage(e.getMessage()));
            }
            log.info("Response: "+ctx.resultString());
        });

        // CREATE ACCOUNT FOR CUSTOMER
        app.post("/account", ctx -> {
            log.info("Request: ("+ctx.method()+") "+ctx.path());
            try {
                Account account = service.addAccount(ctx.bodyAsClass(User.class));
                ctx.json(account);
            } catch (Exception e) {
                ctx.json(new ErrorMessage(e.getMessage()));
            }
            log.info("Response: "+ctx.resultString());
        });

        //// READ

        // GET EMPLOYEE
        app.get("/employee/:username", ctx->{
            log.info("Request: ("+ctx.method()+") "+ctx.path());
            try {
                User user = service.getUserByUsernameAndUserType(
                        new User(ctx.pathParam("username"), User.Usertype.EMPLOYEE)
                );
                ctx.json(user);
            } catch (Exception e) {
                // username not found or not an employee or other SQLException
                ctx.json(new ErrorMessage(e.getMessage()));
            }
            log.info("Response: "+ctx.resultString());
        });

        // GET CUSTOMER
        app.get("/customer/:username", ctx -> {
            log.info("Request: ("+ctx.method()+") "+ctx.path());
            try {
                User user = service.getUserByUsernameAndUserType(
                        new User(ctx.pathParam("username"), User.Usertype.CUSTOMER)
                );
                ctx.json(user);
            } catch (Exception e) {
                // username not found or username is not an customer
                ctx.json(new ErrorMessage(e.getMessage()));
            }
            log.info("Response: "+ctx.resultString());
        });

        // GET ACCOUNT BY ID
        app.get("/accounts/id/:id", ctx -> {
            log.info("Request: ("+ctx.method()+") "+ctx.path());
            try {
                Account account = service.getAccountById(Integer.parseInt(ctx.pathParam("id")));
                System.out.println(account);
                ctx.json(account);
            } catch (Exception e) {
                ctx.json(new ErrorMessage(e.getMessage()));
            }
            log.info("Response: "+ctx.resultString());
        });

        // GET ACCOUNTS BY STATUS
        app.get("/accounts/status/:status",ctx -> {
            log.info("Request: ("+ctx.method()+") "+ctx.path());
            try {
                List<Account> accounts =
                        service.getAccountsByStatus(
                                Account.StatusType.valueOf(
                                        ctx.pathParam("status").toUpperCase())
                        );
                ctx.json(accounts);
            } catch (Exception e) {
                ctx.json(new ErrorMessage(e.getMessage()));
            }
            log.info("Response: "+ctx.resultString());
        });

        // GET ACCOUNTS BY CUSTOMER
        app.get("/accounts/customerid/:id", ctx -> {
            log.info("Request: ("+ctx.method()+") "+ctx.path());
            try {
                List<Account> accounts =
                        service.getAccountsByUserId(Integer.parseInt(ctx.pathParam("id")));
                ctx.json(accounts);
            } catch (Exception e) {
                ctx.json(new ErrorMessage(e.getMessage()));
            }
            log.info("Response: "+ctx.resultString());
        });

        // GET ACCOUNTS BY APPROVEDBY
        app.get("/accounts/approvedby/:id", ctx -> {
            log.info("Request: ("+ctx.method()+") "+ctx.path());
            try {
                List<Account> accounts =
                        service.getAccountsByApprovedBy(Integer.parseInt(ctx.pathParam("id")));
                ctx.json(accounts);
            } catch (Exception e) {
                ctx.json(new ErrorMessage(e.getMessage()));
            }
            log.info("Response: "+ctx.resultString());
        });

        // GET ACCOUNTS BY BALANCE
        app.get("/accounts/balance/:amount", ctx -> {
            log.info("Request: ("+ctx.method()+") "+ctx.path());
            try {
                List<Account> accounts =
                        service.getAccountsByBalance(Double.parseDouble(ctx.pathParam("amount")));
                ctx.json(accounts);
            } catch (Exception e) {
                ctx.json(new ErrorMessage(e.getMessage()));
            }
            log.info("Response: "+ctx.resultString());
        });

        // GET TRANSACTIONS BY ID
        app.get("/transactions/id/:id", ctx -> {
            log.info("Request: ("+ctx.method()+") "+ctx.path());
            try {
                Transaction transaction =
                        service.getTransactionsById(
                                Integer.parseInt(ctx.pathParam("id")));
                ctx.json(transaction);
                System.out.println(transaction);
            } catch (Exception e) {
                ctx.json(new ErrorMessage(e.getMessage()));
            }
            log.info("Response: "+ctx.resultString());
        });

        // GET TRANSACTIONS BY DATE
        app.get("/transactions/datetime/:date", ctx -> {
            log.info("Request: ("+ctx.method()+") "+ctx.path());
            try {
                List<Transaction> transactions =
                        service.getTransactionsByDate(ctx.pathParam("date"));
                ctx.json(transactions);
            } catch (Exception e) {
                ctx.json(new ErrorMessage(e.getMessage()));
            }
            log.info("Response: "+ctx.resultString());
        });

        // GET TRANSACTIONS BY TYPE
        app.get("/transactions/transactiontype/:type", ctx -> {
            log.info("Request: ("+ctx.method()+") "+ctx.path());
            try {
                List<Transaction> transactions =
                        service.getTransactionsByType(
                                Transaction.TransactionType.valueOf(
                                        ctx.pathParam("type")
                                )
                        );
                ctx.json(transactions);
            } catch (Exception e) {
                ctx.json(new ErrorMessage(e.getMessage()));
            }
            log.info("Response: "+ctx.resultString());
        });

        // GET TRANSACTIONS BY ACCOUNT
        app.get("/transactions/accountid/:id", ctx -> {
            log.info("Request: ("+ctx.method()+") "+ctx.path());
            try {
                List<Transaction> transactions =
                        service.getTransactionsByAccountId(Integer.parseInt(ctx.pathParam("id")));
                ctx.json(transactions);
            } catch (Exception e) {
                ctx.json(new ErrorMessage(e.getMessage()));
            }
            log.info("Response: "+ctx.resultString());
        });

        // GET TRANSACTIONS BY CUSTOMER
        app.get("/transactions/customerid/:id", ctx -> {
            log.info("Request: ("+ctx.method()+") "+ctx.path());
            try {
                List<Transaction> transactions =
                        service.getTransactionsByUserId(
                                Integer.parseInt(ctx.pathParam("id")));
                ctx.json(transactions);
            } catch (Exception e) {
                ctx.json(new ErrorMessage(e.getMessage()));
            }
            log.info("Response: "+ctx.resultString());
        });

        // GET TRANSACTIONS BY AMOUNT
        app.get("/transactions/amount/:amount", ctx -> {
            log.info("Request: ("+ctx.method()+") "+ctx.path());
            try {
                List<Transaction> transactions =
                        service.getTransactionsByAmount(
                                Double.parseDouble(ctx.pathParam("amount")));
                ctx.json(transactions);
            } catch (Exception e) {
                ctx.json(new ErrorMessage(e.getMessage()));
            }
            log.info("Response: "+ctx.resultString());
        });

        //// UPDATE

        // UPDATE ACCOUNT STATUS
        app.put("/account/status", ctx -> {
            log.info("Request: ("+ctx.method()+") "+ctx.path());
            try {
                System.out.println("Request body: "+ctx.body());
                System.out.println("bodyAsClass before update: "+ctx.bodyAsClass(Account.class));
                Account account = service.updateAccountStatus(ctx.bodyAsClass(Account.class));
                System.out.println("Account after update: "+account);
                ctx.json(account);
                System.out.println("After json(account)");
            } catch (Exception e) {
                ctx.json(new ErrorMessage(e.getMessage()));
            }
            log.info("Response: "+ctx.resultString());
        });

        // DEPOSIT TO ACCOUNT
        app.put("/deposit/:id/:amount", ctx -> {
            log.info("Request: ("+ctx.method()+") "+ctx.path());
            try {
                Account account = service.deposit(
                        Integer.parseInt(ctx.pathParam("id")),
                        Double.parseDouble(ctx.pathParam("amount"))
                );
                ctx.json(account);
            } catch (Exception e) {
                ctx.json(new ErrorMessage(e.getMessage()));
            }
            log.info("Response: "+ctx.resultString());
        });

        // WITHDRAWAL FROM ACCOUNT
        app.put("/withdraw/:id/:amount", ctx -> {
            log.info("Request: ("+ctx.method()+") "+ctx.path());
            try {
                Account account = service.withdraw(
                        Integer.parseInt(ctx.pathParam("id")),
                        Double.parseDouble(ctx.pathParam("amount"))
                );
                ctx.json(account);
            } catch (Exception e) {
                ctx.json(new ErrorMessage(e.getMessage()));
            }
            log.info("Response: "+ctx.resultString());
        });

        // TRANSFER FROM TO AMOUNT
        app.put("/transfer/:from/:to/:amount", ctx -> {
            log.info("Request: ("+ctx.method()+") "+ctx.path());
            try {
                Account account = service.transfer(
                        Integer.parseInt(ctx.pathParam("from")),
                        Integer.parseInt(ctx.pathParam("to")),
                        Double.parseDouble(ctx.pathParam("amount"))
                );
                ctx.json(account);
            } catch (Exception e) {
                ctx.json(new ErrorMessage(e.getMessage()));
            }
            log.info("Response: "+ctx.resultString());
        });

//        app.get("/accounts/", ctx -> {
//            log.info("Request: ("+ctx.method()+") "+ctx.path());
//            try {
//                ctx.json();
//            } catch (Exception e) {
//                ctx.json(new ErrorMessage(e.getMessage()));
//            }
//            log.info("Response: "+ctx.resultString());
//        });
    }

}
