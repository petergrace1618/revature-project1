package org.shivacorp.test;

import org.junit.jupiter.api.Test;
import org.shivacorp.exception.BusinessException;
import org.shivacorp.model.User;
import org.shivacorp.service.ShivacorpService;
import org.shivacorp.service.impl.ShivacorpServiceImpl;

import static org.junit.jupiter.api.Assertions.*;

class ShivacorpServiceImplTest {
    ShivacorpService service = new ShivacorpServiceImpl();

    @Test
    void loginAdminSuccess() {
        try {
            User expectedUser = service.getUserById(1);
            User actualUser = service.login(new User("admin", "admin", User.Usertype.EMPLOYEE));
            assertEquals(expectedUser, actualUser);
        } catch (BusinessException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    void loginAdminBadPassword() {
        assertThrows(BusinessException.class,
                ()-> service.login(new User( "admin", "wrongpassword", User.Usertype.EMPLOYEE)));
    }

    @Test
    void loginBadUsername() {
        assertThrows(BusinessException.class,
                ()-> service.login(new User("badusername", ".", User.Usertype.CUSTOMER)));
    }

    // TODO: change User initialization
//    @Test
//    void withdrawNSF() { // attempt to withdraw with insufficient funds
//        assertThrows(BusinessException.class,
//                ()-> service.withdraw(new User("zerobalance", "."),1));
//    }

//    @Test
//    void transferNSF() {
//        User user = new User();
//        user.setId(5); // zero balance account
//        assertThrows(BusinessException.class,
//                ()-> service.transfer(user, 111111,1.0));
//    }

    @Test
    void loginSuccessMixedCaseUsername() {
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
