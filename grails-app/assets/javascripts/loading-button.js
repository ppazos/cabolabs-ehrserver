(function( $ ) {

  var methods = {
    init : function(options) {
      console.log($(this), 'init');
      $(this).on('click', function(){
        $(this).addClass('disabled').prepend('<i class="fa fa-spinner fa-spin fa-fw"></i> ');
        return true;
      });
    },
    stop : function() {
      console.log($(this), 'stop');
      $(this).removeClass('disabled');
      $('.fa-spinner', this).remove();
    }
  };


  $.fn.loading = function(methodOrOptions) {
    if ( methods[methodOrOptions] )
    {
      return methods[ methodOrOptions ].apply( this, Array.prototype.slice.call( arguments, 1 ));
    }
    else if ( typeof methodOrOptions === 'object' || ! methodOrOptions )
    {
      // Default to "init"
      return methods.init.apply( this, arguments );
    }
    else
    {
      $.error( 'Method ' +  methodOrOptions + ' does not exist on jQuery.loading' );
    }
  };
}( jQuery ));
