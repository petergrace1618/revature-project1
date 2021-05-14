package org.shivacorp.test;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.shivacorp.dao.ShivacorpDAO;
import org.shivacorp.dao.impl.ShivacorpDAOImpl;
import org.shivacorp.exception.BusinessException;
import org.shivacorp.model.Account;
import org.shivacorp.model.User;
import org.shivacorp.service.ShivacorpService;
import org.shivacorp.service.impl.ShivacorpServiceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShivacorpServiceImplTest {

    @Test
    void loginAdminSuccess() {
        ShivacorpDAO mockedDAO = mock(ShivacorpDAO.class);
        ShivacorpService service = new ShivacorpServiceImpl(mockedDAO);
        User testUser = new User("admin", "admin", User.Usertype.EMPLOYEE);
        try {
            when(mockedDAO.getUserByUsernameAndUsertype(testUser))
                    .thenReturn(testUser);
            User expectedUser = testUser;
            User actualUser = service.login(testUser);
            assertEquals(expectedUser, actualUser);
        } catch (BusinessException e) {
            System.out.println("loginAdminSuccess: "+e.getMessage());
        }
    }

    @Test
    void loginAdminBadPassword() {
//        ShivacorpService service = new ShivacorpServiceImpl(new ShivacorpDAOImpl());
        User wrongPassword = new User( "admin", "wrongpassword", User.Usertype.EMPLOYEE);
        ShivacorpDAO mockedDAO = mock(ShivacorpDAO.class);
        ShivacorpService service = new ShivacorpServiceImpl(mockedDAO);
        User expected = new User("admin", "admin", User.Usertype.EMPLOYEE);
        try {
            when(mockedDAO.getUserByUsernameAndUsertype(wrongPassword)).thenReturn(null);
        } catch (BusinessException e) {
            e.printStackTrace();
        }
        assertThrows(BusinessException.class, ()-> {service.login(wrongPassword);});
    }

    @Test
    void loginBadUsername() {
        ShivacorpDAO mockedDAO = mock(ShivacorpDAO.class);
        ShivacorpService service = new ShivacorpServiceImpl(mockedDAO);
//        ShivacorpService service = new ShivacorpServiceImpl(new ShivacorpDAOImpl());
        User badUsername = new User("badusername", ".", User.Usertype.CUSTOMER);
        assertThrows(BusinessException.class, ()-> service.login(badUsername));
    }

    // TODO: change User initialization
    @Test
    void withdrawNSF() { // attempt to withdraw with insufficient funds
        ShivacorpDAO mockedDAO = mock(ShivacorpDAO.class);
        ShivacorpService service = new ShivacorpServiceImpl(mockedDAO);
        // Account withdraw(int accountId, double amount)
        // Account getAccountById(int)
        // Account(User user, int balance, StatusType status)
        Account accountNSF = new Account(null, 0, Account.StatusType.APPROVED);
        try {
            when(mockedDAO.getAccountById(1)).thenReturn(accountNSF);
        } catch (BusinessException e) {
            e.printStackTrace();
        }
        assertThrows(BusinessException.class,
                ()-> service.withdraw(1,1));
    }

    @Test
    void transferNSF() {
        ShivacorpDAO mockedDAO = mock(ShivacorpDAO.class);
        ShivacorpService service = new ShivacorpServiceImpl(mockedDAO);
        try {
            when(mockedDAO.getAccountById(1)).thenReturn(null);
        } catch (BusinessException e) {
            e.printStackTrace();
        }
        assertThrows(BusinessException.class,
                ()-> service.transfer(1, 2,1.0));
    }

    @Test
    void loginSuccessMixedCaseUsername() {
        ShivacorpDAO mockedDAO = mock(ShivacorpDAO.class);
        ShivacorpService service = new ShivacorpServiceImpl(new ShivacorpDAOImpl());
        try {
            User expectedUser = service.getUserById(1);
            User actualUser = service.getUserByUsernameAndUserType(new User("ADMIN", User.Usertype.EMPLOYEE));
            assertEquals(expectedUser, actualUser);
            expectedUser = service.getUserById(2);
            actualUser = service.getUserByUsernameAndUserType(new User("FOO", User.Usertype.CUSTOMER));
            assertEquals(expectedUser, actualUser);
        } catch (Exception e) {
            System.out.println("loginSuccessMixedCaseUsername: "+e.getMessage());
        }
    }

    @Test
    void newUserSavedToLowercase() {
        ShivacorpDAO mockedDAO = mock(ShivacorpDAO.class);
        ShivacorpService service = new ShivacorpServiceImpl(mockedDAO);
//        ShivacorpService service = new ShivacorpServiceImpl(new ShivacorpDAOImpl());
        try {
            service.register(new User("TEST", "PassWord",  User.Usertype.UNASSIGNED));
            User expectedUser = new User("test", "PassWord",  User.Usertype.UNASSIGNED);
            User actualUser = service.getUserByUsernameAndUserType(new User());
            assertEquals(expectedUser, actualUser);
        } catch (Exception e) {
            System.out.println("newUserSavedToLowercase: "+e.getMessage());
        }
    }
}
