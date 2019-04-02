/**
 * @fileoverview Supporting Javascript for the cron page.
 */
$(document).ready(function() {
  $('.ae-cron-run').click(function() {
    var url = $(this).attr('name');
    var target = $(this).attr('target');
    var data = {'url': url, 'target': target, 'xsrf_token': '{{ xsrf_token }}'};
    var request = $.ajax({
      url: '/cron',
      type: 'POST',
      data: data
    })
    .done(function() {
      $('#cron-feedback').removeClass().addClass('messagebox').text(
          'Request to ' + url + ' succeeded!');
    })
    .fail(function(xhr, textStatus) {
      $('#cron-feedback').removeClass().addClass('errorbox').text(
          'Request failed with status: ' + request.status);
    });
  });
});
