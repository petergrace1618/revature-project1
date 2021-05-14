//// GET ELEMENT REFERENCES ////

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

//// ADD EVENT HANDLERS ////

$(document).ready(()=>{
  loginUsername.focus();
  console.log(document.cookie);
});

loginForm.addEventListener('submit', submitLogin);
registerForm.addEventListener('submit', submitRegister);

loginUsername.addEventListener('input', ()=>{
  registerResults.style.display = 'none';
  if (loginUsername.validity.patternMismatch) {
    loginUsername.setCustomValidity(customeValidityMessage);
  } else {
    loginUsername.setCustomValidity('');
  }  
});  

registerUsername.addEventListener('input', ()=>{
  registerResults.style.display = 'none';
  if (registerUsername.validity.patternMismatch) {
    registerUsername.setCustomValidity(customeValidityMessage);
  } else {
    registerUsername.setCustomValidity('');
  }  
});  

//// EVENT HANDLER FUNCTIONS ////

// EMPLOYEE LOGIN

function submitLogin (ev) {
  ev.preventDefault();

  // hide previous messages
  registerResults.style.display = 'none';

  if (!loginUsername.checkValidity()) {
    loginUsername.select();
    return false;
  }

  const url = "http://localhost:8000/employee/" + loginUsername.value;
  const xhr = new XMLHttpRequest();
  
  // get user object
  xhr.onload = function() {
    const response = JSON.parse(this.responseText);
    
    // invalid username or other exception
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
    // save user object in a cookie
    delete response.password;
    let tomorrow = new Date(Date.now()+24*60*60*1000).toUTCString();
    document.cookie = `employeeObject=${encodeURIComponent(JSON.stringify(response))}; ${tomorrow}`;
    console.log(document.cookie)
    console.log(decodeURIComponent(
      document.cookie
          .split('; ')
          .find(entry => entry.startsWith('userobject='))
    ));

    // redirect to main page
    location.href = 'shivacorp-employee-main.html';
    return false;
  };
  xhr.open("GET", url, true);
  xhr.send();
}

// REGISTER EMPLOYEE

function submitRegister(ev) {
  ev.preventDefault();

  // hide previous messages
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

  const url = "http://localhost:8000/employee";
  let xhr = new XMLHttpRequest();
  
  // build user object
  let user = {};
  user.fullname = registerFullname.value;
  user.username = registerUsername.value;
  user.password = registerPassword.value;
  user.usertype = 'EMPLOYEE';
  console.log('Sending: '+JSON.stringify(user));

  xhr.onload = function() {
    console.log('Received: '+this.responseText);
    const response = JSON.parse(this.responseText);
    
    // user already exists or other exception
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
