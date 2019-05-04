
function notification_get( get_not_url, dismiss_url, forSection )
{
   $.get( get_not_url, { forSection: forSection }, 'json')
      .done(function(data) {
        //console.log(data);

        var new_nts_count = data.filter(function(sts) { return sts.status == 'new'; }).length;

        if (new_nts_count > 0)
        {
           $('#top-notifications-menu > a').append('<span class="badge badge-notification">'+ new_nts_count +'</span>');
        }

        notifications = '';
        $.each( data, function( i, nstatus ) {

           // TODO: add  class="dismissed" to the LIs of notifications that were dismissed
           if (nstatus.status == 'new')
             $('#top-notifications-menu .drop-content').append('<li><div class="col-md-11">'+ nstatus.notification.text +'<br/><span class="text-muted">'+ nstatus.notification.dateCreated +'</span></div><div class="col-md-1 text-right"><a href="#" class="dismiss" data-id="'+ nstatus.notification.id +'"><i class="fa fa-dot-circle-o"></i></a></div></li>');
           else
             $('#top-notifications-menu .drop-content').append('<li class="dismissed"><div class="col-md-11">'+ nstatus.notification.text +'<br/><span class="text-muted">'+ nstatus.notification.dateCreated +'</span></div></li>');
        });


        $('.dismiss').on('click', function (evn) {
           //console.log('id', $(this).data('id'));

           // dismiss with the new notifications on the menu
           var notif_dom = $(this).closest('li');

           $.get( dismiss_url, { id: $(this).data('id') }, 'json')
           .done(function(data) {
             console.log(data);
             // TODO: update the DOM adding the dismissed class to the LI
             console.log(notif_dom);
             notif_dom.addClass('dismissed');
           })
           .fail(function() {
             //alert( "error" );
           })
           .always(function() {
             //alert( "finished" );
           });
        });
      })
      .fail(function() {
        //alert( "error" );
      })
      .always(function() {
        //alert( "finished" );
      });
}
