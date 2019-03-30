/**
 * @fileoverview Supporting Javascript for the mail page.
 */
$(document).ready(function() {
  $('#mail-form').submit(function() {
    var data = {'from': $('#from').val(),
                'to': $('#to').val(),
                'cc': $('#cc').val(),
                'subject': $('#subject').val(),
                'body': $('#body').val(),
                'xsrf_token': '{{ xsrf_token }}',
               };

    var request = $.ajax({
      url: '/mail',
      type: 'POST',
      data: data
    })
    .done(function() {
      $('#mail-feedback').removeClass().addClass('messagebox').text(
          'Request succeeded!');
    })
    .fail(function(xhr, textStatus) {
      $('#mail-feedback').removeClass().addClass('errorbox').text(
          'Request failed with status: ' + request.status);
    });
    return false;
  });
});

