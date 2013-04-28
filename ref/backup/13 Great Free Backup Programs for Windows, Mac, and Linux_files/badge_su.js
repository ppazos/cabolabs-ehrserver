var SUJS = {};
	SUJS.buttonWin = function(){	
	var w = 425,
		h = 370,
		is_chrome = navigator.userAgent.toLowerCase().indexOf('chrome') > -1,
		left,
		top,
		title = 'StumbleUpon',
		//popState = 'normal',
		targetWin;
		
	if (is_chrome){
		w = 425;
		h = 470;
	} 
	left = (screen.width/2)-(w/2);
	top = (screen.height/2)-(h/2);
	 
	return {
		open : function (pageURL) {
			targetWin = window.open (pageURL, title, 'toolbar=no, location=no, directories=no, status=no, menubar=no, scrollbars=no, resizable=no, copyhistory=no, chrome=no, width='+w+', height='+h+', top='+top+', left='+left);						
			targetWin.focus();			
		},
		
		close : function () {
			targetWin.close();
		}	
	}
}();

if (typeof($) != "undefined"){
	$(function() {
		cbOpener = function (cbThis) {
			location.reload();
			cbThis();
		};
		var $btnRate = $('#wrapper .rate a');
		if ($btnRate.hasClass('loggedIn'))
		{
			$btnRate.click(function () {
				var action = 'like';
				if ($btnRate.hasClass('active'))
					action = 'unlike';

				$btnRate.toggleClass('active');
				var publicid = $btnRate.attr('id');
				$.ajax({
					url: "/badge/ajax/"+publicid+"/"+action+"/",
					type: "POST",
					data: { 
						token: __SUJStoken
					},
							 
					success: function(response) {
						if (response.status == 'fail')
						{
							window.location.reload();
						}
					},			
					error: function() {
						if($btnRate.hasClass('active'))
						{
							$btnRate.removeClass('active');
						}
						else
						{
							$btnRate.addClass('active');
						}
					},
					dataType: 'json'
				});
			});
		}
	}); 
}
