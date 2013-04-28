jQuery.noConflict();

cfct = {}

cfct.loading = function() {
	return '<div class="loading"><span>Loading...</span></div>';
}

cfct.ajax_post_content = function() {
	jQuery('ol.archive .excerpt .entry-title a').unbind().click(function() {
		var post_id = jQuery(this).attr('rev').replace('post-', '');
		var excerpt = jQuery('#post-excerpt-' + post_id);
		var target = jQuery('#post-content-' + post_id + '-target');
		excerpt.hide();
		target.html(cfct.loading()).show().load(CFCT_URL + '/index.php?cfct_action=post_content&id=' + post_id, function() {
			cfct.ajax_post_comments();
			jQuery('#post_close_' + post_id + ' a').click(function() {
				target.slideUp(function() {
					excerpt.show();
				});
				return false;
			});
			jQuery(this).hide().slideDown();
		});
		return false;
	});
}

cfct.ajax_post_comments = function() {
	jQuery('p.comments-link a').unbind().click(function() {
		var a = jQuery(this);
		var post_id = a.attr('rev').replace('post-', '');
		var target = jQuery('#post-comments-' + post_id + '-target');
		target.html(cfct.loading()).show().load(CFCT_URL + '/index.php?cfct_action=post_comments&id=' + post_id, function() {
			jQuery(this).hide().slideDown(function() {
				a.attr('rel', a.html()).html('Hide Comments').unbind().click(function() {
					target.slideUp(function() {
						a.html(a.attr('rel'));
						cfct.ajax_post_comments();
					});
					return false;
				});
			});
		});
		return false;
	});
}

// Moves form for threaded comments: taken and modified from WordPress 2.7
addComment = {
	moveForm : function(commId, parentId, respondId, postId) {
		var t = this, div, comm = t.I(commId), respond = t.I(respondId), cancel = t.I('cancel-comment-reply-link-p' + postId), parent = t.I('comment_parent_p' + postId), post = t.I('comment_post_ID_p' + postId);

		if ( ! comm || ! respond || ! cancel || ! parent )
			return;

		t.respondId = respondId;
		postId = postId || false;

		if ( ! t.I('wp-temp-form-div-p' + postId) ) {
			div = document.createElement('div');
			div.id = 'wp-temp-form-div-p' + postId;
			div.style.display = 'none';
			respond.parentNode.insertBefore(div, respond);
		}

		comm.parentNode.insertBefore(respond, comm.nextSibling);
		if ( post && postId )
			post.value = postId;
		parent.value = parentId;
		cancel.style.display = '';

		cancel.onclick = function() {
			var t = addComment, temp = t.I('wp-temp-form-div-p' + postId), respond = t.I(t.respondId);

			if ( ! temp || ! respond )
				return;

			t.I('comment_parent_p' + postId).value = '0';
			temp.parentNode.insertBefore(respond, temp);
			temp.parentNode.removeChild(temp);
			this.style.display = 'none';
			this.onclick = null;
			return false;
		}

		try { t.I('comment-p' + postId).focus(); }
		catch(e) {}

		return false;
	},

	I : function(e) {
		return document.getElementById(e);
	}
}

jQuery(function($) {
	// :first-child fix for IE on nav
	$('.nav li:first-child').addClass('first-child');
	// :first-child fix for IE
	$('.comment .comment-content p:last-child').addClass('last-child');
	// :hover fix for full articles in IE
	$('.full').mouseover(function() {
		$(this).addClass('hover');
	});
	$('.full').mouseout(function() {
		$(this).removeClass('hover');
	});

	// Suckerfish dropdowns IE :hover fix
	$('.nav li').mouseover(function() {
		$(this).addClass('hover');
	});
	$('.nav li').mouseout(function() {
		$(this).removeClass('hover');
	});
	
	if ((!$.browser.msie || $.browser.version.substr(0,1) != '6') && typeof CFCT_AJAX_LOAD != 'undefined' && CFCT_AJAX_LOAD) {
		cfct.ajax_post_content();
		cfct.ajax_post_comments();
	}
	$('.nav li a').removeAttr('title');
	
});