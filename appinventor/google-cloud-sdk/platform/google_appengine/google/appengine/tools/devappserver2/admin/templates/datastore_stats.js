/**
 * @fileoverview Supporting Javascript for the datastore stats page.
 */
$(document).ready(function() {
  $('#generate-stats').click(function() {
    var button = $(this);
    button.attr('disabled', true);
    button.addClass('disabled');
    var data = {'action:compute_stats': '1',
                'xsrf_token': '{{ xsrf_token }}',
               };
    var request = $.ajax({
      url: '/datastore-stats',
      type: 'POST',
      data: data
    })
    .done(function(data) {
      $('#stats-feedback').removeClass().addClass('messagebox').text(data);
    })
    .fail(function(xhr, textStatus) {
      $('#stats-feedback').removeClass().addClass('errorbox').text(
          'Stats generation failed' + request.status);
    })
    .always(function() {
      button.attr('disabled', false);
      button.removeClass('disabled');
    });
  });
});
