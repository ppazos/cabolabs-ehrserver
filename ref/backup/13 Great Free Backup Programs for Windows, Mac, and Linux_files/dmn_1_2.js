server_url="http://switcher.dmn.aol.com/sw/a";

params = {
       sch:"",
       ssch:"",
       squery:"",
       snum:"",
       stest:"",
       locale:"",
       z:"",
       page:"",
       nt:"",
       lf:"",
       callback:"",
       ssafe:"",
       sh_ie:"",
       sh_oe:"",
       sclient:"",
       spch:"",
       csp:"",
       scoco:"",
       sbrand:"",
       sview:"",
       sit:"",
       surl:"",
       skw:"",
       skwt:"",
       skw2:"",
       callback:"done"
};



function get_links(){	

	if ( typeof( window[ '__slm_nt' ] ) != "undefined" )
	{	
	if(__slm_nt!=(null))
			{
			params.nt=__slm_nt;
			}
			else
			{
			params.nt='null';
		}
	}	
        	
	var s = "";
	for (var anItem in params) if(params[anItem]!="") s+="&"+anItem+"="+escape(params[anItem]);	
	var scriptElem =  document.createElement("script");
	scriptElem.src = server_url+"?"+s.slice(1);
	scriptElem.type ='text/javascript';
	document.body.appendChild(scriptElem);
}

