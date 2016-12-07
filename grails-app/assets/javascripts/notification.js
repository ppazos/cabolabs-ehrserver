
function notification_get( get_not_url, dismiss_url, forSection )
{
   $.get( get_not_url, { forSection: forSection }, 'json')
      .done(function(data) {
        //console.log(data);
        
        notifications = '';
        $.each( data, function( i, notif ) {
        
          notifications += '<div class="alert alert-info alert-dismissible global" role="alert" data-id="'+ notif.id +'"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>'+ notif.text +'</div>';
        });
        
        $('body').append('<div class="global_alert_container">'+ notifications +'</div>');

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

