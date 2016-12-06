
function notification_get( url, forSection )
{
   $.get( url, { forSection: forSection }, 'json')
      .done(function(data) {
        console.log(data);
        
        /* needs alerts.js http://getbootstrap.com/javascript/#alerts
<div class="alert alert-info alert-dismissible" role="alert">
  <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
  <strong>Warning!</strong> Better check yourself, you're not looking too good.
</div>
         */
      })
      .fail(function() {
        alert( "error" );
      })
      .always(function() {
        alert( "finished" );
      });
}
