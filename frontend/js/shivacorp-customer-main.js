//// ELEMENT REFERENCES ////

let applyBtn = $('#apply-btn')[0];
let newAccountMsg = $('#new-account-msg')[0];
let depositForm = $('#deposit-form')[0];
let depositAmount = $('#deposit-amount')[0];
let depositMsg = $('#deposit-msg')[0];
let withdrawForm = $('#withdraw-form')[0];
let withdrawAmount = $('#withdraw-amount')[0];
let transferForm = $('#transfer-form')[0];
let transferAmount = $('#transfer-amount')[0];
let transferFromAccount = $('#transfer-from-account')[0];

//// LOCALS ////

let customerObject;

//// EVENT LISTENERS ////

// PAGE LOAD

$(document).ready(()=>{
  // get user object from cookie
  console.log(document.cookie);
  let s = document.cookie
      .split('; ')
      .find(entry => entry.startsWith('customerObject='));
  s = s.slice(s.indexOf('=') + 1);    
  customerObject = JSON.parse(decodeURIComponent(s));
  console.log('Customer object', customerObject);

  // display greeting
  $('#greeting-name').text(customerObject.fullname);

  // hide new account message
  $('#new-account-msg').hide();
  
  // populate Choose Account dropdown
  populateAccountDropdown();
});  

function populateAccountDropdown() {
  // get accounts
  let url = `http://localhost:8000/accounts/customerid/${customerObject.id}`;
  console.log(url)
  fetch(url)
    .then(response => response.json())
    .then(accounts => {
    
      console.log('Accounts',accounts)
      if (accounts.message) { 
        $('#balance-msg')
          .text(accounts.message)
          .removeClass('text-dark')
          .addClass('text-danger');
        return;
      } else {
        
        // Populate dropdowns with account numbers
        accounts.forEach((a)=>{
          // if (a.status != 'DENIED')
            $('#account-picker').append(`<option value='${a.id}'>${a.id}</option>`);
        });

      }
    });
}

// CHOOSE ACCOUNT 

$('#account-picker')[0].addEventListener('change', (ev)=>{
  fetch(`http://localhost:8000/accounts/id/${ev.target.value}`)
  .then(r=>r.json())
  .then(r=>{
    // update balance
    $('#balance-msg span')
      .text(r.balance)
      .addClass('text-dark');
    
    // display account in Transfer From
    if (transferFromAccount.value) {
      $(transferFromAccount)
          .text('Transfer from account '+r.id)
          .removeClass('text-danger')
          .addClass('text-dark');
    } else {
      $(transferFromAccount)
          .text("Please choose an account above to transfer funds from")
          .removeClass('text-danger')
          .addClass('text-dark')
    }
  })
  .catch(err=>console.log(err));

  // SHOW TRANSACTIONS BY ACCOUNT
  fetch(`http://localhost:8000/transactions/accountid/${ev.target.value}`)
  .then(r=>r.json())
  .then(transactions=>{
    console.log('Transactions',transactions);
    $('#transactions-table-body').empty();

    // exception occurred
    if(transactions.message) {  
      $('#transactions-msg')
        .text(transactions.message)
        .show();
    
    } else {

      if (transactions.value === 0) {
        $('#')
      }

      transactions.forEach((t)=>{
        let row = '<tr>'+
          '<td>'+t.id+'</td>'+
          '<td>'+new Date(t.datetime).toLocaleDateString()+'</td>'+
          '<td>'+t.transactionType+'</td>'+
          '<td>'+t.accountId+'</td>'+
          '<td>'+t.amount+'</td>'+
          '</tr>';
        $('#transactions-table-body').append(row);
      });
    }
  });  
});

// APPLY FOR NEW ACCOUNT

$('#apply-btn')[0].addEventListener('click', ()=>{
  // prepare request
  let url = `http://localhost:8000/account`;
  let request = {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(customerObject)
  };

  // create account
  fetch(url, request)
  .then(response => response.json())
  .then(account => {
    // show error msg if any
    if (account.message) {
      $('#new-account-msg')
          .text(account.message)
          .removeClass('text-success')
          .addClass('text-danger')
          .show();
      return;
    }

    // display success message
    console.log('Success:', account)
    $('#new-account-msg')
        .text(`Your new account (${account.id}) has been created and is pending approval.`)
        .removeClass('text-danger')
        .addClass('text-success')
        .show();
    
    // add new account to Choose Account dropdown
    $('#account-picker').append(
      `<option value='${account.id}'>${account.id}</option>`
    );
  })
  .catch((err) => {
    console.error('Error:', err)
  });
});

// DEPOSIT

depositForm.addEventListener('submit', (ev)=>{
  ev.preventDefault();
  if (!depositAmount.checkValidity()) {
    console.log('invalid deposit amount');
    return false;
  }
  
  let account = Number.parseInt($('#account-picker').val());
  let amount = $('#deposit-amount').val();  
  let url = `http://localhost:8000/deposit/${account}/${amount}`;
  let request = {method: 'PUT'};

  if (!account) {
    $('#deposit-msg')
      .text("Please choose an account.")
      .addClass('text-danger')
      .show();
      return false;
  }

  fetch(url, request)
      .then(r=>r.json())
      .then(r=>{
        console.log(r);
        if (r.message) {
          $('#deposit-msg')
            .text(r.message)
            .addClass('text-danger')
            .show();
        } else {
          $('#deposit-msg')
            .text('Success. Your new balance is $'+r.balance)
            .removeClass('text-danger')
            .addClass('text-success')
            .show();
          $('#balance-msg span').text(r.balance);
        }
      })
      .catch(err=>console.log(err));
  return false;
});

// WITHDRAW

withdrawForm.addEventListener('submit', (ev)=>{
  ev.preventDefault();
  if (!withdrawAmount.checkValidity()) {
    console.log('invalid deposit amount');
    return false;
  }
  
  let account = Number.parseInt($('#account-picker').val());
  let amount = $('#withdraw-amount').val();  
  let url = `http://localhost:8000/withdraw/${account}/${amount}`;
  let request = {method: 'PUT'};

  if (!account) {
    $('#withdraw-msg')
      .text("Please choose an account.")
      .addClass('text-danger')
      .show();
      return false;
  }

  fetch(url, request)
      .then(r=>r.json())
      .then(r=>{
        console.log(r);
        if (r.message) {
          $('#withdraw-msg')
            .text(r.message)
            .addClass('text-danger')
            .show();
        } else {
          $('#withdraw-msg')
            .text('Success. Your new balance is $'+r.balance)
            .removeClass('text-danger')
            .addClass('text-success')
            .show();
          $('#balance-msg span').text(r.balance);
        }
      })
      .catch(err=>console.log(err));
  return false;
});

// TRANSFER

transferForm.addEventListener('submit', (ev)=>{
    ev.preventDefault();
    if (!transferAmount.checkValidity()) {
      console.log('invalid deposit amount');
      return false;
    }
    
    let fromAccount = Number.parseInt($('#account-picker').val());
    let toAccount = $('#transfer-to-account').val();
    let amount = $('#transfer-amount').val();  
    let url = `http://localhost:8000/transfer/${fromAccount}/${toAccount}/${amount}`;
    let request = {method: 'PUT'};
  
    if (!fromAccount) {
      $('#transfer-from-account')
        .text("Please choose an account.")
        .addClass('text-danger')
        .show();
        return false;
    }
  
    fetch(url, request)
        .then(r=>r.json())
        .then(r=>{
          console.log(r);
          if (r.message) {
            $('#transfer-msg')
              .text(r.message)
              .addClass('text-danger')
              .show();
          } else {
            $('#transfer-msg')
              .text('Success. Your new balance is $'+r.balance)
              .removeClass('text-danger')
              .addClass('text-success')
              .show();
            $('#balance-msg span').text(r.balance);
          }
        })
        .catch(err=>console.log(err));
    return false;
  });

// LOGOUT

logout.addEventListener('click', ()=>{
  // reset the customer cookie
  let yesterday = new Date(Date.now()-24*60*60*1000).toUTCString();
  document.cookie = `customerObject=; ${yesterday}`;
  
  // redirect to login page
  location.href = 'shivacorp-customer-login.html';
});
