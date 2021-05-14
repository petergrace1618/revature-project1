//// ELEMENT REFERENCES ////

let employeeObject;

$(document).ready(()=>{
  // get user object from cookie
  console.log(document.cookie);
  let s = document.cookie
      .split('; ')
      .find(entry => entry.startsWith('employeeObject='));
  s = s.slice(s.indexOf('=') + 1);    
  employeeObject = JSON.parse(decodeURIComponent(s));
  console.log('Employee object', employeeObject);

  // display greeting
  $('#greeting-name').text(employeeObject.fullname);
  
  // add event listeners to search-accounts inputs
  $("input[name='searchAccountsCriteria']").next().get().forEach(()=>{
    addEventListener('input', (ev) => {
      let selector = '#' + $(ev.target).attr('id') + '-radio';
      $(selector).prop('checked', 'true');
    });
  });

  // add event listeners to search-transactions inputs
  $("input[name='searchTransactionsCriteria']").next().get().forEach(()=>{
    addEventListener('input', (ev) => {
      let selector = '#' + $(ev.target).attr('id') + '-radio';
      $(selector).prop('checked', 'true');
    });
  });

  // load pending accounts
  fetch(`http://localhost:8000/accounts/status/pending`)
  .then(r=>r.json())
  .then(accounts => {
    console.log(accounts);

    if (accounts.message) {
      $('#pending-msg')
        .text(accounts.message)
        .addClass('text-danger')
        .show();
        return;
    }
    
    // remove previous results
    $('#pending-accounts-table-body').empty();
    
    accounts.forEach(a => {
      let row = `<tr id='pending-row'>`+
      '<td>'+a.id+'</td>'+
      '<td>'+a.userId+'</td>'+
      // '<td>'+el.approvedBy+'</td>'+
      '<td>'+a.balance+'</td>'+
      `<td id='pending-status-parent-${a.id}'>`+
      `<select id="pending-status-${a.id}" class="custom-select">`+
      '<option value="PENDING">PENDING</option>'+
      '<option value="APPROVED">APPROVED</option>'+
      '<option value="DENIED">DENIED</option>'+
      '</select>'+
      '</td>'+
      "<td>"+
      `<button id='commit-btn-${a.id}' class='btn btn-info btn-sm'>Commit</button>`+
      "</td>"+
      '</tr>';
      
      $('#pending-accounts-table-body').append(row);

      $(`#commit-btn-${a.id}`)[0].addEventListener('click', ()=>{
        let status = $(`#pending-status-${a.id}`).val();
        
        if (status == 'PENDING') {
          console.log('Status unchanged.');
          return;
        }
        
        let approvedBy = employeeObject.id;
        let url = `http://localhost:8000/account/status`;
        let request = {
          'method': 'PUT',
          'Content-Type':'application/json',
          'body': JSON.stringify({
            'id': a.id,
            'approvedBy': approvedBy,
            'status': status
          })
        };        
        
        fetch(url, request)
        .then(r=>r.json())
        .then(r=>{
          console.log(r);
          if (r.message) {
            $('#pending-msg')
              .text(r.message)
              .addClass('text-danger')
              .show();
            return;
          }

          // hide commit button to prevent additional updates
          $('#pending-msg')
            .text(`Account ${a.id} status updated`)
            .addClass('text-success')
            .show();
            $(`#commit-btn-${a.id}`).hide();
          
          // change select to text
          let status = $(`#pending-status-${a.id}`).val();
          $(`#pending-status-parent-${a.id}`).empty();
          $(`#pending-status-parent-${a.id}`).text(status);
        });
      });
    });
  })
});

// SEARCH ACCOUNTS 

$('#search-accounts-form')[0].addEventListener('submit', (ev) => {
  ev.preventDefault();
  $('#search-accounts-msg').hide();
  
  // get search criteria and search data
  let searchColumn = $("input[name='searchAccountsCriteria']:checked").next().attr('id');
  searchColumn = searchColumn.slice(searchColumn.lastIndexOf('-') + 1);
  let searchData = $("input[name='searchAccountsCriteria']:checked").next().val();
  console.log(searchColumn, searchData);

  // do nothin if searchData blank
  if (!searchData) {
    console.log('search data blank');
    return;
  }

  // fetch search results
  let url = `http://localhost:8000/accounts/${searchColumn}/${searchData}`;
  console.log(url)
  fetch(url)
  .then(r=>r.json())
  .then(r=>{
    console.log(r)
    // remove previous results
    $('#search-accounts-table-body').empty();
    
    if(r.message) {
      $('#search-accounts-msg')
          .text(r.message)
          .addClass('text-danger')
          .show();
      
      return;
    }

    // result is single object
    if (r.length === undefined) {
      $('#search-accounts-table-body').append(
        `<tr>`+
        '<td>'+r.id+'</td>'+
        '<td>'+r.userId+'</td>'+
        '<td>'+r.approvedBy.id+'</td>'+
        '<td>'+r.balance+'</td>'+
        `<td>`+r.status+'</td>'+
        '</tr>'
      );
    
    // result is an array
    } else {
      r.forEach(el => {
        $('#search-accounts-table-body').append(
          `<tr>`+
          '<td>'+el.id+'</td>'+
          '<td>'+el.userId+'</td>'+
          '<td>'+el.approvedBy.id+'</td>'+
          '<td>'+el.balance+'</td>'+
          `<td>`+el.status+'</td>'+
          '</tr>'
        );
      });
    }

    return false;
  });
});

// SEARCH TRANSACTIONS

$('#search-transactions-form')[0].addEventListener('submit', (ev) => {
  ev.preventDefault();
  $('#search-transactions-msg').hide();
  
  // get search criteria and search data
  let searchColumn = $("input[name='searchTransactionsCriteria']:checked").next().attr('id');
  searchColumn = searchColumn.slice(searchColumn.lastIndexOf('-') + 1);
  let searchData = $("input[name='searchTransactionsCriteria']:checked").next().val();
  console.log(searchColumn, searchData);

  // do nothin if searchData blank
  if (!searchData) {
    console.log('search data blank');
    return;
  }

  // fetch search results
  let url = `http://localhost:8000/transactions/${searchColumn}/${searchData}`;
  console.log(url)
  fetch(url)
  .then(r=>r.json())
  .then(r=>{
    console.log(r)
    // remove previous results
    $('#search-transactions-table-body').empty();
    
    if(r.message) {
      $('#search-transactions-msg')
          .text(r.message)
          .addClass('text-danger')
          .show();
      
      return;
    }

    // result is single object
    if (r.length === undefined) {
      $('#search-transactions-table-body').append(
        `<tr>`+
        '<td>'+r.id+'</td>'+
        '<td>'+new Date(r.datetime).toLocaleDateString()+'</td>'+
        '<td>'+r.transactionType+'</td>'+
        '<td>'+r.accountId+'</td>'+
        `<td>`+r.amount+'</td>'+
        '</tr>'
      );
    
    // result is an array
    } else {
      r.forEach(el => {
        $('#search-transactions-table-body').append(
          `<tr>`+
          '<td>'+el.id+'</td>'+
          '<td>'+new Date(el.datetime).toLocaleString()+'</td>'+
          '<td>'+el.transactionType+'</td>'+
          '<td>'+el.accountId+'</td>'+
          `<td>`+el.amount+'</td>'+
          '</tr>'
        );
      });
    }

    return false;
  });
});

// LOGOUT

logout.addEventListener('click', ()=>{
  // reset the customer cookie
  let yesterday = new Date(Date.now()-24*60*60*1000).toUTCString();
  document.cookie = `employeeObject=; ${yesterday}`;
  
  // redirect to login page
  location.href = 'shivacorp-employee-login.html';
});

