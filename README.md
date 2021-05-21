# ShivaCorp Banking App 2.0

## Project Description
An extension of [ShivaCorp Banking App 1.0](https://github.com/petergrace1618/revature-project-0.git) which was a console app simulating a banking experience. Version 2.0 adds a web frontend and a few added features. 

## Technologies used
Front end:
- HTML/CSS/Javascript
- Bootstrap 4
- jQuery

Back end:
- Javalin
- RESTful API
- JDBC
- PostgreSQL

## Features
Customers can: 
- Register for a login
- Apply for an account
- Deposit funds
- Withdraw funds
- Transfer funds
- View transactions per account

Employees can:
- Create a login
- Approve or deny customers' applications
- Search accounts by 
  - Account number
  - Username
  - Full name
  - Balance
  - Account status (PENDING, APPROVED, DENIED)
  - Employee ID who approved or denied the account
- Search transactions by
  - Transaction ID
  - Transaction date
  - Transaction amount
  - Account number
  - Other account number (in the case of transfers)
  - Transaction type: (ACCOUNT_CREATED, DEPOSIT, WITHDRAWAL, TRANSFER_CREDIT, TRANSFER_DEBIT

## Getting Started
To clone the repository
```
git clone https://github.com/petergrace1618/revature-project1.git
```
Open the folder `backend` in IntelliJ. Run `ShivacorpMain()`. This starts the Javalin server to accept REST API requests. Then in Visual Studio Code open `frontend/shivacorp-customer-login.html` and `fronted/shivacorp-employee-login.html` and open with Live Server (must have VS Code Live Server extension installed) by right-clicking on the source and choosing "Open with Live Server" or press Alt-L Alt-O. Then register for an account and start creating money out of thin air just like real banks do!

## License
Freely available under the [GNU General Public License version 3](https://opensource.org/licenses/GPL-3.0)
