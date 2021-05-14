//// GET ELEMENT REFERENCES

let loginForm = document.querySelector('#login-form');
let loginUsername = document.querySelector("#login-username");
let loginPassword = document.querySelector("#login-password");
let loginResults = document.querySelector('#login-results');

let registerForm = document.querySelector('#register-form');
let registerFullname = document.querySelector('#register-fullname');
let registerUsername = document.querySelector("#register-username");
let registerPassword = document.querySelector("#register-password");
let registerPasswordConfirm = document.querySelector('#register-password-confirm');
let registerResults = document.querySelector('#register-results');

const customeValidityMessage = 'Usernames are case-insensitive, must be 20 characters or less, and can only contain the following characters: a-zA-Z0-9.-_@';

//// ADD EVENT HANDLERS

$(document).ready(()=>{
  loginUsername.focus();
  console.log(document.cookie);
});

loginForm.addEventListener('submit', submitLogin);
registerForm.addEventListener('submit', submitRegister);

loginUsername.addEventListener('input', ()=>{
  if (loginUsername.validity.patternMismatch) {
    loginUsername.setCustomValidity(customeValidityMessage);
  } else {
    loginUsername.setCustomValidity('');
  }  
});  

registerUsername.addEventListener('input', ()=>{
  if (registerUsername.validity.patternMismatch) {
    registerUsername.setCustomValidity(customeValidityMessage);
  } else {
    registerUsername.setCustomValidity('');
  }  
});  

//// EVENT HANDLER FUNCTIONS

// CUSTOMER LOGIN

function submitLogin (ev) {
  ev.preventDefault();

  // hide previous results message
  registerResults.style.display = 'none';

  if (!loginUsername.checkValidity()) {
    loginUsername.select();
    return false;
  }

  const url = "http://localhost:8000/customer/" + loginUsername.value;
  const xhr = new XMLHttpRequest();
  
  // get user object
  xhr.onload = function() {
    console.log('Received: '+this.responseText)
    const response = JSON.parse(this.responseText);

    // invalid username or password
    if (response.message !== undefined) {
      loginResults.innerHTML = response.message;
      loginResults.style.display = 'block';
      loginUsername.select();
      return;
    } 
    // invalid password
    else if (response.password != loginPassword.value) {
      loginResults.innerHTML = "Invalid username or password";
      loginResults.style.display = 'block';
      loginUsername.select();
      return;
    }

    //// valid username, valid password
    // save user object in a cookie that expires tomorrow
    delete response.password;
    let tomorrow = new Date(Date.now()+24*60*60*1000).toUTCString();
    document.cookie = `customerObject=${encodeURIComponent(JSON.stringify(response))}; ${tomorrow}`;

    // redirect to main page
    location.href = 'shivacorp-customer-main.html';
    return false;
  };
  xhr.open("GET", url, true);
  xhr.send();
}

// REGISTER CUSTOMER

function submitRegister(ev) {
  ev.preventDefault();

  // hide previous results message
  loginResults.style.display = 'none';

  if (!registerUsername.checkValidity()) {
    console.log('invalid inputs')
    return false;
  }

  // passwords don't match
  if (registerPassword.value != registerPasswordConfirm.value) {
    registerResults.innerText = "Passwords don't match";
    $('#register-results').removeClass('text-success').addClass('text-danger');
    registerResults.style.display = 'block';
    return;
  }

  const url = "http://localhost:8000/customer";
  let xhr = new XMLHttpRequest();
  
  // build user object
  let user = {};
  user.fullname = registerFullname.value;
  user.username = registerUsername.value;
  user.password = registerPassword.value;
  user.usertype = 'CUSTOMER';
  console.log('Sending: '+JSON.stringify(user));

  xhr.onload = function() {
    console.log('Received: '+this.responseText);
    const response = JSON.parse(this.responseText);
    
    // user already exists
    if (response.message !== undefined) {
      registerResults.innerHTML = response.message;
      $('#register-results').removeClass('text-success').addClass('text-danger');
      registerResults.style.display = 'block';
      return;
    }
    
    // user created
    registerResults.innerHTML = "User created. Please login with your new username."
    $('#register-results').removeClass('text-danger').addClass('text-success');
    registerResults.style.display = 'block';
  };
  xhr.open('POST', url);
  xhr.setRequestHeader('Content-Type', 'application/json');
  xhr.send(JSON.stringify(user));
  return false;
}
