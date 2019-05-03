
function notification_get( get_not_url, dismiss_url, forSection )
{
   $.get( get_not_url, { forSection: forSection }, 'json')
      .done(function(data) {
        //console.log(data);

        if (data.length > 0)
        {
           $('#top-notifications-menu > a').append('<span class="badge badge-notification">'+ data.length +'</span>');
        }

        notifications = '';
        $.each( data, function( i, notif ) {

           // TODO: add  class="dismissed" to the LIs of notifications that were dismissed
          $('#top-notifications-menu .drop-content').append('<li><div class="col-md-11">'+ notif.text +'<br/><span class="text-muted">'+ notif.dateCreated +'</span></div><div class="col-md-1 text-right"><a href="#" class="dismiss" data-id="'+ notif.id +'"><i class="fa fa-dot-circle-o"></i></a></div></li>');

          //notifications += '<div class="alert alert-info alert-dismissible global" role="alert" data-id="'+ notif.id +'"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>'+ notif.text +'</div>';
        });

        //$('body').append('<div class="global_alert_container">'+ notifications +'</div>');

        $('.dismiss').on('click', function (evn) {
           console.log('id', $(this).data('id'));
           // TODO: dismiss with the new notifications on the menu

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

        $('.alert').on('close.bs.alert', function (evn) {
           console.log($(evn.target).data('id'));

           $.get( dismiss_url, { id: $(evn.target).data('id') }, 'json')
           .done(function(data) {
             console.log(data);
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
