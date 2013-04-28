d=document;
function ge(obj){return document.getElementById(obj);}
var iColWidth=2;
var slObj=new Object();
function parseSL(sponsorData){

//	if(params.target==null){
//		params.target=' '
//	}

	slObj.count=0;
	if(sponsorData.length>0){
		var slArrays=new Array();
		for(var i=0;i<sponsorData.length;i++){
			var sl='';
			var vTitle=sponsorData[i].title;
			var vURL=null;
			if(sponsorData[i].url!=null){
				vURL=sponsorData[i].url;
				if(vURL.substring(0,7)=='http://'){
					vURL=vURL.substring(7);
				}
			}
			if(iColWidth==1){
				if(vTitle.length>21){
					vTitle=vTitle.substring(0,20);
				}
				if((vURL!=null)&&(vURL.substring(0,4)=='www.')){
					vURL=vURL.substring(4);
				}
				if((vURL!=null)&&(vURL.length>=21)){
					vURL=vURL.substring(0,21)+'..';
				}
			}else{
				if(vTitle.length>27){
					vTitle=vTitle.substring(0,26);
				}
				if((vURL!=null)&&(vURL.length>39)){
					vURL=vURL.substring(0,36)+'..';
				}
			}
			var trackedSUrl=sponsorData[i].redirect_url;
			sl+='<div class="sponsorPromo">';
			sl+='<div class="title">';
			sl+='<a '+params.target+' href="'+trackedSUrl+'" onmouseover="self.status=\''+sponsorData[i].url+params.target+'\';return true" onmouseout="self.status=\'\'; return true">';
			sl+=sponsorData[i].title+'</a>';
			sl+='</div>';
			
			sl+='<div>'+sponsorData[i].d1+sponsorData[i].d2 + '</div>';
			if(vURL!=null){
				sl+='<div class="link">';
                                sl+='<a '+params.target+' href="'+trackedSUrl+'" onmouseover="self.status=\''+sponsorData[i].url+params.target+'\';return true" onmouseout="self.status=\'\'; return true">';				
				sl+=sponsorData[i].url+'</a>';
				sl+='</div>';
			}
			
			sl+='</div>';
			slArrays[i]=sl;
			
		}

		slObj.count=slArrays.length;slObj.list=slArrays;
		
		
		slObj.slt=$(document.createElement("a")).addClass('sponsorLink').attr("href","http://about-search.aol.com/index.html#sl").append("Sponsored Links");
		slObj.b=$(document.createElement("div")).addClass('body');
	}
	if(ge('slTop')){build_sl_data('Top',slPlacement.top.s,slPlacement.top.e);}
	if(ge('slMiddle')){build_sl_data('Middle',slPlacement.middle.s,slPlacement.middle.e);}
        if(ge('slBottom')!= undefined){build_sl_data('Bottom',slPlacement.bottom.s,slPlacement.bottom.e);}
}
function build_sl_data(aID,slF,slT){
	var wObj=$("#sl"+aID);
	
	//alert(document.createElement("a").ownerDocument);
        //alert(document.createElement("div").ownerDocument);
	
	if(wObj.length>0 && slT>0){
		var bObj=$(slObj.b).clone();
		$(slObj.slt).clone(true).appendTo(bObj);
		for(var i=slF;i<slT;i++){
			bObj.append(slObj.list[i]);
		}

		wObj.append(bObj);
	}
}