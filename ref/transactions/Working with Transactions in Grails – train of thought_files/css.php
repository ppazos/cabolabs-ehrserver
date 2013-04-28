/*
// This file is part of the Carrington Blog Theme for WordPress
// http://carringtontheme.com
//
// Copyright (c) 2008-2009 Crowd Favorite, Ltd. All rights reserved.
// http://crowdfavorite.com
//
// Released under the GPL license
// http://www.opensource.org/licenses/gpl-license.php
//
// **********************************************************************
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
// **********************************************************************
*/

/* Reset: based on http://meyerweb.com/eric/tools/css/reset v1.0 | 20080212, sans table styles
============================================================================================== */

html, body, div, span, applet, object, iframe,
h1, h2, h3, h4, h5, h6, p, blockquote, pre,
a, abbr, acronym, address, big, cite, code,
del, dfn, em, font, img, ins, kbd, q, s, samp,
small, strike, strong, sub, sup, tt, var,
b, u, i, center,
dl, dt, dd, ol, ul, li,
fieldset, form, label, legend,
hr{
	margin: 0;
	padding: 0;
	border: 0;
	outline: 0;
	font-size: 100%;
	vertical-align: baseline;
	background: transparent;
}
blockquote, q {
	quotes: none;
}
blockquote:before, blockquote:after,
q:before, q:after {
	content: '';
	content: none;
}
:focus {
	outline: 0;
}
cite,
address {
	display:inline;
	font-style:normal;
}

/* Typography
======================================================*/
body {
	background: #fff;
	color: #51555c;
	font: 12px/18px helvetica, arial, sans-serif;
}
code {
	font-family: "Lucida Console", Monaco, monospace;
}
*[lang] {
	font-style: italic;
}
del {
	text-decoration: line-through;
}
acronym,
.caps {
	text-transform: uppercase;
}
.small-caps {
	font-variant: small-caps;
}
acronym,
.num,
.caps,
.small-caps {
	letter-spacing: 0.1em;
}
pre {
	overflow: auto;
}
textarea {
	font: 12px/18px helvetica, arial, sans-serif;
	padding: 3px;
}
/* Links */
a,
a:visited {
	color: #a00004;
	text-decoration: none;
}
/* specify all three for accessibility */
a:focus,
a:hover,
a:active {
	text-decoration: underline;
}
h1 a,
h2 a,
h3 a,
h4 a,
h5 a,
h6 a,
h1 a:visited,
h2 a:visited,
h3 a:visited,
h4 a:visited,
h5 a:visited,
h6 a:visited {
	color: #51555c;
}
h1 a:hover,
h2 a:hover,
h3 a:hover,
h4 a:hover,
h5 a:hover,
h6 a:hover {
	color: #a00004;
	text-decoration: none;
}
/* elements with title attribute */
abbr[title],
acronym[title],
dfn[title],
span[title],
del[title] {
	cursor: help;
}
/* typogrify: inset initial double quotes. */
.dquo {
	margin-left: -.45em;
}
 /* fancy ampersands */
.amp {
	font-family: Baskerville, "Goudy Old Style", "Palatino", "Book Antiqua", serif;
	font-size: 13px;
	font-style: italic;
}
/* Font sizes and vertical measure */
h1,
h2,
h3,
h4,
h5,
h6,
.h1,
.h2,
.h3 {
	display:block;
	margin-bottom:9px;
}
h1,
.h1 {
	font-size: 24px;
	line-height:1;
}
h2,
.h2 {
	font-size: 18px;
}
h3,
.h3 {
	font-size: 14px;
}
p,
pre,
dl,
ul,
ol {
	margin-bottom:18px;
}
blockquote {
	border-left:2px solid #e9eaea;
	margin:0 9px 9px;
	padding-left:10px;
}
ol {
	margin-left:36px;
}
dd {
	margin-left:18px;
}
li ul,
li ol {
	margin-bottom: 0;
}
li,
dd,
.tight {
	margin-bottom:9px;
}
ul li {
	background: url(../img/arrow-bullet.gif) no-repeat left top;
	list-style:none;
	padding-left:12px;
}
acronym,
.caps,
.small {
	font-size: 11.5px;
}
hr {
	background:#ccc;
	color:#ccc;
	height:1px;
	margin:0 30px 18px 30px;
}
.rule-major{
	background-color:#e9eaea;
}
.rule,
.rule-minor {
	border-top:1px solid #cecfd1;
	height:9px;
	margin:0 0 18px;
}
.rule-major {
	border-top:1px solid #cecfd1;
	height:35px;
	margin:0 0 18px;
}
.rule hr,
.rule-minor hr,
.rule-major hr {
	display:none;
}

/* No formatting class */
.plain,
.plain li {
	background:transparent;
	border:0;
	font-style:normal;
	list-style:none;
	margin:0;
	padding:0;
}
.alt-font {
	color: #999;
	font-family: Georgia, Palatino, "Palatino Linotype", Baskerville, serif;
	font-style:italic;
}
.alt-font a {
	font-family:helvetica, arial, sans-serif;
	font-style:normal;
}

/*
// This file is part of the Carrington Blog Theme for WordPress
// http://carringtontheme.com
//
// Copyright (c) 2008-2009 Crowd Favorite, Ltd. All rights reserved.
// http://crowdfavorite.com
//
// Released under the GPL license
// http://www.opensource.org/licenses/gpl-license.php
//
// **********************************************************************
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
// **********************************************************************
*/

/* UI: Shared classnames
================================================================= */
.accessibility {
	left:-32000px;
	position:absolute;
}
.lofi {
	display:none;
}
/* http://sonspring.com/journal/clearing-floats */
html body div.clear,
html body span.clear {
	background: none;
	border: 0;
	clear: both;
	display: block;
	float: none;
	font-size: 0;
	margin: 0;
	padding: 0;
	overflow: hidden;
	visibility: hidden;
	width: 0;
	height: 0;
}
/* http://www.positioniseverything.net/easyclearing.html */
.clearfix:after {
	clear: both;
	content: '.';
	display: block;
	visibility: hidden;
	height: 0;
}
.clearfix {
	display: inline-block;
}
* html .clearfix {
	height: 1%;
}
.clearfix {
	display: block;
}

h1.page-title {
	color: #999;
	font: italic 14px Georgia, serif;
}

/* General horizontal navigation lists with dropdown magic */
.nav,
.nav ul,
.nav li {
	background:transparent;
	list-style:none;
	margin:0;
	padding:0;
}
.nav ul {
	background: #e9eaea;
	border: 1px solid #cecfd1;
	border-color: #cecfd1 #aaa #aaa #cecfd1;
	z-index: 9999;
}
.nav li {
	float:left;
	margin-right:12px;
}
.nav li a {
	display:block;
}
.nav li li a {
	color: #a00004 !important; /* These need to retain a contrasting color with dropdown background */
	padding:6px 9px;
}
.nav li ul,
.nav li li {
	margin-right:0;
	width: 200px;
}
.nav li li ul {
	margin:-31px 0 0 200px;
}
.nav li.secondary {
	float:right;
	margin-left:12px;
	margin-right:0;
}
/* dropdown action */
.nav li ul,
.nav li:hover li ul,
.nav li li:hover li ul,
.nav li.hover li ul,
.nav li li.hover li ul {
	left: -32697px;
	position: absolute;
}
.nav li:hover ul,
.nav li li:hover ul,
.nav li li li:hover ul,
.nav li.hover ul,
.nav li li.hover ul,
.nav li li li.hover ul {
	left: auto;
}

.pagination,
.pagination-single {
	background-color:#e9eaea;
	margin-top: 20px;
	overflow:hidden;
}
.pagination .previous a,
.pagination-single .previous {
	float:left;
	padding:18px;
}
.pagination .next a,
.pagination-single .next {
	float:right;
	padding:18px;
}
.loading {
	background: url(../img/spinner.gif) 10px 17px no-repeat;
	clear: both;
	color: #ccc;
	height: 50px;
}
.loading span {
	display: block;
	padding: 17px 32px 0;
}
.close {
	background-color:#e9eaea;
	border-bottom: 1px solid #ddd;
	padding: 8px 0 6px;
	text-align: center;
}
/* including for compatibility */
.alignleft {
	float:left;
	margin-right:1em;
	margin-bottom:1em;
}
.alignright {
	float:right;
	margin-left:1em;
	margin-bottom:1em;
}
.aligncenter {
	display: block;
	margin-left: auto;
	margin-right: auto;
}
.wp-caption {
	border: 1px solid #ddd;
	text-align: center;
	background-color: #f3f3f3;
	padding-top: 4px;
	margin: 10px;
	/* optional rounded corners for browsers that support it */
	-moz-border-radius: 3px;
	-khtml-border-radius: 3px;
	-webkit-border-radius: 3px;
	border-radius: 3px;
}
.wp-caption img {
	margin: 0;
	padding: 0;
	border: 0 none;
}
.wp-caption p.wp-caption-text {
	font-size: 11px;
	line-height: 17px;
	padding: 0 4px 5px;
	margin: 0;
}

/* Structure
================================================
	Total width: 960
	Columns: 12
	Column width: 58px
	Gutter width: 24px
	Unit (column + gutter): 82px
	Interior padding: 6px = colum width 50px
	#content = 7 units
	#sidebar = 5 units
*/
.section { /* Full-width areas */
	min-width: 980px; /* cut-off background fix */
}
.wrapper {
	clear: both;
	margin: 0 auto;
	width: 960px;
}
#header {
	background-color: #51555c;
	background-repeat:repeat-x;
	background-position:left bottom;
	color: #cecfd1;
}
#header .wrapper {
	background-repeat:repeat-x;
	background-position:center bottom;
	padding: 13px 10px 10px;
}
#header a,
#header a:visited {
	color: #fff;
}
#header #blog-title {
	font-family: "Avenir Light", "Futura Light", helvetica, arial, sans-serif;
	font-size: 36px;
	font-weight:normal;
	line-height: 1;
	margin-bottom: 9px;
}
#header #blog-title a:hover {
	text-decoration: none;
}

#navigation a,
#navigation a:visited {
	font-weight: bold;
	text-transform: uppercase;
}
#navigation .secondary a,
#navigation .secondary a:visited,
#navigation li li a,
#navigation li li a:visited {
	font-weight:normal;
	text-transform:none;
}

#sub-header {
	background-color: #e9eaea;
	padding: 9px 0;
}
#sub-header form#cfct-search {
	float:right;
}
#all-categories {
	padding:2px 0 0;
	width:760px;
}
#all-categories-title {
	float: left;
	font-weight:normal;
	margin-right:12px;
}

#main {
	margin:24px 0;
}
#content {
	float: left;
	padding-bottom: 24px;
	width: 670px;
}
#sidebar {
	background-color:#e9eaea;
	float:right;
	margin-left:24px;
	padding:14px;
	width: 225px;
}
#sidebar #primary-sidebar,
#sidebar #secondary-sidebar {
	float:left;
	width:173px;
}
#sidebar #primary-sidebar {
	margin-right:12px;
}
#footer {
	background-color: #51555c;
	background-repeat:repeat-x;
	background-position:left top;
	clear: both;
	color: #999;
	padding: 32px 0 64px;
}
#footer a,
#footer a:visited {
	color: #CECFD1;
}
#footer .wrapper {
	overflow:hidden;
	padding-bottom:28px;
}
#footer p#generator-link {
	float:left;
	padding-top: 12px;
}
#footer p#developer-link {
	display:block;
	text-indent:-32697px;
}

/* Posts
 =============================== */
.entry-content,
.entry-summary {
	overflow:auto;
}
.hentry {
	position:relative;
}
.hentry .edit {
	background-color:#900;
	border-radius:4px;
	-webkit-border-radius:4px;
	-khtml-border-radius:4px;
	-moz-border-radius:4px;
	font-size: 11.5px;
	opacity:.60;
	padding:3px 5px 1px;
	left:-32697px;
	position:absolute;
	top:0;
}
.hentry:hover .edit,
.post:hover .edit-post,
.comment.hentry:hover .edit-comment {
	left: auto;
	right:0;
}
.hentry:hover .edit-comment {
	left:-32697px;
	right: auto;
}
.comment.hentry.bypostauthor:hover .edit-comment {
	top: 2px;
	right: 2px;
}
.edit a,
.edit a:visited,
.edit a:hover{
	color:#fff;
}
.full .full-content {
	clear:both;
	padding-top: 9px;
	width: 670px;
}
.full .pages-link,
.page-numbers {
	font-weight:bold;
}
.full .pages-link a,
a.page-numbers {
	border:1px solid #cecfd1;
	font-weight:normal;
}
.full .pages-link a,
.page-numbers{
	padding:2px 6px;
}
.full .pages-link a:hover {
	border-color:#999;
	text-decoration:none;
}
.full p.comments-link {
	float:right;
}

.hentry ul {
	margin-left: 20px;
}
.hentry ol {
	margin-left: 32px;
}

.archive {
	border-top: 1px dotted #cecfd1;
	list-style: none;
	margin: 1.5em 0 0;
}
.archive li {
	margin-bottom:0;
}
.archive .full {
	margin-top:18px;
}
.excerpt {
	background: url(../img/arrow-bullet.gif) no-repeat 0 12px;
	border-bottom: 1px dotted #cecfd1;
	color: #999;
	padding: 12px 146px 12px 20px;
	position:relative;
}
.excerpt strong.entry-title {
	font-size: 14px;
}
.excerpt strong.entry-title a,
.excerpt strong.entry-title a:visited {
	color: #51555c;
}
.excerpt strong.entry-title a:hover {
	color: #a00004;
	text-decoration: none;
}
.excerpt .date {
	margin-top:1.5em;
}
.excerpt p {
	margin:0;
}
.excerpt .date,
.excerpt .comments-link a,
.excerpt .comments-link a:visited,
.excerpt .comments-link span {
	display: block;
	width: 140px;
}
.excerpt .date,
.excerpt .comments-link {
	position:absolute;
	right:0;
	top:14px;
}
.search {
	border-top: 1px dotted #cecfd1;
	padding-top:18px;
}

/* Comments and comment form
=================================== */
.commentlist{
	clear:both;
	list-style:none;
	margin:0 0 18px;
}
li.li-comment{
	background:transparent;
	margin:0;
	padding:0;
	position:relative;
}
li.li-comment li.li-comment {
	background:url(../img/comment-thread.gif) no-repeat 0 3px;
	padding-left:24px;
}
.commentlist .comment {
	margin-bottom:8px;
	overflow:hidden;
	padding:0 0 0 60px;
}
.commentlist .comment .photo img{
	margin-left:-60px;
	position:absolute;
}
.comment cite.fn {
	font-size:14px;
	font-weight:bold;
}
.comment .comment-content {
	overflow:hidden;
	margin-bottom:4px;
}
.comment .comment-content p:last-child,
.comment .comment-content p.last-child {
	margin-bottom:0;
}
.comment-meta {
	color:#999;
	font-size: 10.5px;
}
.comment-meta .date {
	float:right;
}
.comment-reply-link,
.comment-reply-link:visited,
.comment-reply-link:hover {
	background:#900;
	border-radius:4px;
	-webkit-border-radius:4px;
	-khtml-border-radius:4px;
	-moz-border-radius:4px;
	color:#fff;
	font-size:9.5px;
	opacity:.7;
	padding:3px 5px 2px;
}
/* author comments */
.commentlist .bypostauthor {
	background-color:#efeff1;
	padding:8px 12px 6px 72px;
}

/* trackbacks and pingbacks */
.ping {
	border-top:1px dotted #cecfd1;
	padding:9px;
}
.ping .entry-summary,
.ping .entry-summary p {
	border:0;
	margin:0;
	padding:0;
}
form.comment-form {
	background-color:#efeff1;
	border:1px solid #e9eaea;
	margin-bottom:8px;
	padding:9px 9px 0;
}
.comment-form label {
	color:#51555c;
	font-weight:bold;
}
.comment-form-user-info input {
	margin-right:9px;
	width:200px;
}
.comment-form em {
	color:#999;
	font-weight:normal;
	font-style:normal;
	font-size:12px;
}
.comment-form textarea {
	height:9em;
	width:95%;
}

/* Sidebar and Widgets
============================== */
.widget {
	margin-bottom:18px;
	overflow:hidden;
	width:100%;
}
h2.widget-title,
.widget_search label {
	font-size: 1.16666667em; /* 14 */
	line-height: 1.28571429;
	margin: 0 0 .58333334em 0;
}
.widget li {
	margin:0;
}
.widget_search label {
	display:block;
	font-weight:bold;
}
.widget_search input {
	margin-bottom:6px;
}
#carrington-subscribe h2.widget-title {
	color:#999;
	float:left;
	font:normal 1.16666667em/1.28571429 helvetica, arial, sans-serif; /* 14 */
	margin:7px 0 0;
	text-transform:uppercase;
}
#carrington-about {
	padding-bottom:24px;
}
#carrington-about .about{
	background-color:#fff;
	overflow:hidden;
	padding:12px;
}
#carrington-about p {
	margin:0;
}
#carrington-about a.more,
#carrington-about a.more:visited {
	float:right;
}
#carrington-archives ul {
	border-top: 1px dotted #cecfd1;
}
#carrington-archives li {
	background-position:left 6px;
	border-bottom: 1px dotted #cecfd1;
}
#carrington-archives li a {
	display:block;
	padding:6px 0;
}
#sidebar ol {
	margin-left: 26px;
}

/* Misc
=================================== */

#footer p#developer-link a,
#footer p#developer-link a:visited {
	background:url(../img/footer/by-crowd-favorite-light.png) no-repeat left top;
	float:right;
	height:30px;
	text-indent:-32697px;
	width:270px;
}

#TB_secondLine {
	color:#999;
	font-family: Georgia, Palatino, "Palatino Linotype", Baskerville, serif;
	font-style:italic;
}
#TB_title{
	background-color: #51555c;
	color:#fff;
	font-size:11.5px;
	line-height:18px;
}
#TB_title a,
#TB_title a:visited {
	color:#fff;
}
a#TB_prev:hover,
a#TB_next:hover {
	height:100%;
}
a#TB_prev:hover {
	background:url(../img/lightbox/prevlabel.gif) no-repeat left center;
}
a#TB_next:hover {
	background:url(../img/lightbox/nextlabel.gif) no-repeat right center;
}

