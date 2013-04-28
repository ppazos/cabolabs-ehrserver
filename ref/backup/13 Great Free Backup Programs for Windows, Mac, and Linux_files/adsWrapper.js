var adsLo;
try {adsLo=top.location.href}
catch (e){}
if (!adsLo||adsLo==null){
try {adsLo=window.location.href}
catch (e){}
}
adsLo=adsLo||""
var adsUAC=adsLo.indexOf('atwUAC='),adsUACH
function adSetMOAT(v){
if (v){
var d=document,s=d.createElement("script"),h=d.getElementsByTagName("head")[0]; 
s.src='http://s.moatads.com/aolalways5fd2/moatuac.js'; 
h.appendChild(s); 
} 
}
function adsLoadUAC(){
var n,d=document,z=d.createElement('script')
n=adsLo.substring(adsUAC+7,adsLo.length).replace(/&.*$/,'').split(/\||;/);
if (n[1]=='b'){
adsUACH='http://browsertest.web.aol.com/ads/'
if(/^[0-9A-Za-z\/.]+$/.test(unescape(n[0])))d.write('<script type="text/javascript" src="'+adsUACH+n[0]+'"></scr','ipt>')
}}
if (adsUAC>0&&!window.adsUACH)adsLoadUAC()
else{
if (window.adsIn!=1){
adsIn=1
var adsHt="http://at.atwola.com",adsNt='5113.1',adsPl='221794',adsESN='',adsATWM='',adsTp='J',
adsATOth='',adsATMob='',adsSrAT='',adsTacOK=1,adsD=new Date(),aolAdFdBkStr='',adsAddOn=1,adsAJAXAddOn=0,adsMob=0,
adsScr=adsD.getTime()%0x3b9aca00,adsVal='',adsCp=0,adsMNS,adsExcV='',adsLNm=0,
adsUA=navigator.userAgent.toLowerCase(),adsIE,adsAJAX=0,adsTzAT="aduho="+(-1*adsD.getTimezoneOffset())+";",
adsNMSG='',adsTile=1,adsPage='',adsDivs=[],adsQuigo=0,adsCA,adsCF=[],adsCW=[],adsCH=[],adsCAd=[];
if (!window.ATW3_AdObj){
try {
if (parent.window.ATW3_AdObj){
var ATW3_AdObj=parent.window.ATW3_AdObj;
adsScr=ATW3_AdObj.scr;
}else{
var ATW3_AdObj=new Object();
ATW3_AdObj.scr=adsScr;
ATW3_AdObj.tile=0;
parent.window.ATW3_AdObj=ATW3_AdObj; 
}}
catch (e){
var ATW3_AdObj=new Object();
ATW3_AdObj.scr=adsScr;
ATW3_AdObj.tile=0;
}}
else{
adsScr=ATW3_AdObj.scr;
}
function adsTacFn(){
var t1='http://cdn.atwola.com/_media/uac/tcodewads_at.html',t2='http://cdn.at.atwola.com/_media/uac/tcode3.html';
var n=2,d=document,r=d.referrer,q=0;
if (adsTacOK==2)n=1
if (adsTacOK){
try {
if (top.location.href!=location.href){
if (parent.window.adsIn==1)q=1}}
catch (e){}
if (q!=1){
var i,i1='',j,p=''
for (j=0;j<n;j++){
i=d.createElement('iframe')
i.style.display="none"
i.id="adTacFr"+j
i.style.width='0px'
i.style.height='0px'
if (j==0&&(adsESN||adsUA.indexOf("aol")!=-1)){
i1=t1
if (adsESN)i1+="#"+adsESN
}
if (j==1){
var x=''
if (window.tacProp){
for (var t in tacProp){x+="&"+t+"="+tacProp[t]}
}
p+=x
if ((typeof(r)!='undefined')&&(r!='')){
 if (r.indexOf('mapquest')!=-1)r=r.replace(/[?#].*$/,'')
 p+="&tacref="+r;
}
i1=(p)?t2+"#"+p:t2
}
if (i1){
i.src=i1
d.body.appendChild(i)
}}}}
}
function adsDisableTacoda(v){
if (v&&v.indexOf('aolws')!=-1)adsTacOK=2
else adsTacOK=0
}
function adUACInit(){
if (location.protocol=="http:"){
var w=window;
if (w.addEventListener)w.addEventListener("load",adsTacFn,false)
else if (w.attachEvent)w.attachEvent("onload",adsTacFn)
}
if (/(^|;)?RSP_COOKIE=.*?&name=(.*?)(&|;|$)/i.test(document.cookie))adsESN='&ESN='+unescape(RegExp.$2);
var at=adsLo.indexOf('atwCrPr=');
adsIE=(navigator.appName=="Microsoft Internet Explorer");
if (adsLo.indexOf('atwDisTcode=')>0)adsDisableTacoda()
if (at>0){
adsCA=adsLo.substr(at+8).split(/\||;/);
adsCp=1;
var z=adsCA.length;
for (var i=0,k=0;i<z;i=i+4,k++){adsCF[k]=adsCA[i];adsCW[k]=adsCA[i+1];adsCH[k]=adsCA[i+2];adsCAd[k]=adsCA[i+3]}
}
adsMNS=(/(\?|&)atwMN=(.*?)(&|$)/.test(adsLo))?(RegExp.$2).split(/\||;/):'';
if (!(/^[0-9A-Za-z,]+$/.test(unescape(adsMNS))))adsMNS='';
adsExcV=(/(\?|&)atwExc=(.*?)(&|$)/.test(adsLo))?(RegExp.$2):'';
}
adUACInit()
function adInList(u,s){
var l=s.length;
for (var i=0; i<l; i++)if(u.indexOf(s[i])>-1)return i+1;
return false;
}
function adsCkCol(f,d){
var dv=document.getElementById(f.divName),i=d.getElementById('adDiv').innerHTML;
if (f.textAd!=1){
var m=(/mnum=(.*?)\//i.test(i))?RegExp.$1:'',
a=(/aolAdId=("|')(.*?)("|')/i.test(i))?RegExp.$2:'|';
if (f.mn)aolAdFdBkStr+=f.mn+'|'+f.w+'|'+f.h+'|'+a+'|'+m+';';
}
var x=((i.indexOf('AOL - HTML - Blank HTML Ad')!=-1)||(i.indexOf('ATCollapse.gif')!=-1))
f.setAttribute('banId',a);
if (dv&&dv.col){
if (!x){
f.width=f.w;
f.height=f.h;
f.style.visibility='visible';
}}
if (x){
f.style.width="0px"
f.style.height="0px"
dv.width=0
dv.height=0 
f.style.display='none'
adsDevilObj(f.divName,'1','1')
return true
}
else return false
}
function adsDoOnL(f,d){
if (f){
if (!adsCkCol(f,d)&&f.divName){
var s=d.getElementById('adDiv').innerHTML,n=s.indexOf('\<\!--'),n2=s.indexOf('3PTextDynamic');
if (n2>0){
adsQuigo=2
adsRMIFOnL(f,d)
}else{
if (n>0){
var x=s.substr(n,s.length),p=document.getElementById(f.divName);
p.innerHTML=x}}}}}
function adSetNetId(v){adsNt=v}
function adSetPlId(v){adsPl=v}
function adSetHtNm(){}
function adSetHtNmAT(v){adsHt=(v.indexOf('http')==-1?"http://"+v:v)}
function adSetAMS(){}
function adSetTarget(){}
function adSetSN(v){var c
if (v){
v=v.toString()
if (v.indexOf('@')==-1){
if (window.btoa)c=btoa(v)
else{
var o="",c1,c2,c3,e1,e2,e3,e4,i=0,s="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
do {
c1=v.charCodeAt(i++)
c2=v.charCodeAt(i++)
c3=v.charCodeAt(i++)
e1=c1 >> 2
e2=((c1 & 3) << 4) | (c2 >> 4)
e3=((c2 & 15) << 2) | (c3 >> 6)
e4=c3 & 63
if (isNaN(c2))e3=e4=64
else if (isNaN(c3))e4=64
o=o+s.charAt(e1)+s.charAt(e2)+s.charAt(e3)+s.charAt(e4)
}
while (i<v.length);
c=o;
}
adsESN='&ESN='+c;}}
}
function adSetWM(v){adsATWM='kvwm='+escape(v)+';'}
function adSetOthAT(v){
if (v){
 if (v.charAt(v.length-1)!=';')v+=';'
 var x=v.split(';'),l=x.length;
 for (i=0;i<l-1;i++){
  if (x[i].charAt(x[i].length-1)!='=')adsATOth+=escape(x[i])+';';  
 }
 try {ATW3_AdObj.adsATOth=adsATOth;}
 catch(e){}
}
}
function adSetOthMob(v){
if (v){v=v.replace(/;/g,'&');
if (v[0]!='&')adsATMob='&'+v
else adsATMob=v;
try {ATW3_AdObj.adsATMob=adsATMob;}
catch(e){}
}}
function adSetAddOn(v){
if (adsAddOn!=2)adsAddOn=parseInt(v);
}
function adSetAJAXAddOn(v){adsAJAXAddOn=v}
function adSetType(v){
if (v=='')v='J'
adsTp=v.toUpperCase()
}
function adSetSearch(v){
v=v.replace(/ /g,'+')
v=(window.encodeURIComponent)?encodeURIComponent(v):escape(v)
adsSrAT="KEY="+v+";"
}
function adSendTerms(){}
function adSetAdURL(u){adsPage=u}
function adsShowDiv(d){
var v=adsGetObj(d);
v.style.display="block"
}
function adsHideDiv(d){
var v=adsGetObj(d);
v.style.display="none"
}
function adsResetPg(){
adsTile=1
adsDivs=[]
adsD=new Date()
adsScr=adsD.getTime()%0x3b9aca00
adsATOth=''
adsATMob=''
adsSrAT=''
adsAddOn=1
}
function adsReloadAll(){
adsD=new Date()
var z=adsDivs.length;
for (var i=0;i<z;i++)adsReloadAd(adsDivs[i],'','all')
}
function adsReloadAd(d,m,v){
if (v!='all')adsD=new Date()
var v=adsGetObj(d),s=v.adURL,dt=adsD.getTime()%0x3b9aca00;
if (s){
if (m)s=s.replace(/alias=[0-9]*;/,"alias="+m+";").replace(/kvmn=[0-9]*;/,"kvmn="+m+";");
var i=s.indexOf(';grp='),u=''
if (i==-1)u=s.replace(/ /, "")+" "
else u=s.substring(0,i+5)+dt
v.adURL=u
v.LoadAd()
}}
function adsReloadIframe(n,m,v){
var f='',s='';
try {f=document.getElementById(n)}
catch (e){}
if (f){
if (v!='all')adsD=new Date()
try {s=f.src}
catch (e){}
if (s){
if (m)s=s.replace(/alias=[0-9]*;/,"alias="+m+";").replace(/kvmn=[0-9]*;/,"kvmn="+m+";")
var dt=adsD.getTime()%0x3b9aca00,i=s.indexOf(';grp=');
try {f.src=s.substring(0,i+5)+dt}
catch(e){}}}
}
function adsReloadIframeAll(){
var n,f='';
adsD=new Date()
for (var i=0;i<adsTile;i++){
n='adsF'+i
try {f=document.getElementById(n)}
catch (e){break}
if (f)adsReloadIframe(n,'','all')}
}
function adSetOthDclk(v){
var x=v.split(';'),z=x.length-1;
for (var i=0;i<z;i++){
var y=x[i].split('=')
adsATOth+="kv"+escape(y[0])+"="+escape(y[1])+";"
}}
function adSetDelay(){}
function adSetExt(){}
function adsGetAdURL(w){
var d=w.frameElement.parentNode;
return d.adURL
}
function adsDevilObj(d,w,h){
try {
if (!window.adsDevilAd)adsDevilAd=new Object();
else if (window.adsDevilAd.hasOwnProperty('resized'))adsDevilAd.resized(d,w,h);
if (w!=1){
adsDevilAd.RRWidth=w;
adsDevilAd.RRHeight=h;
}}catch(e){}}
function adsRMIFOnL(w,d){
var f,wi='',h='';
if (adsQuigo>0)f=w
else f=w.frameElement
var v=f.parentNode;
var s=d.getElementById("adSpan"),aD=d.getElementById("adDiv"),aD1=aD.innerHTML;
if (adsQuigo==0&&(/aolSize=["']([\d]*?)\|([\d]*)["']/i.test(aD1))&&(unescape(RegExp.$2)>1)){
 wi=unescape(RegExp.$1);
 h=unescape(RegExp.$2);
}
else{
 if (!adsMob){
  if (/img (.*?)width=["']?(.*?)(\"|\'| )/i.test(aD1))wi=unescape(RegExp.$2);
  if (/img (.*?)height=["']?(.*?)[\"|\'| ]/i.test(aD1))h=unescape(RegExp.$2);
  if (!(/^[0-9]+$/.test(unescape(wi))))wi='';
  if (!(/^[0-9]+$/.test(unescape(h))))h='';
 }
 if (!(wi&&h)&&wi!=1&&h!=1){
  if ((v.childNodes.length==1)||(d.adsWidth&&d.adsHeight)){
   if (d.adsWidth&&d.adsHeight){wi=d.adsWidth;h=d.adsHeight}
   else{
    if (s){
     wi=s.offsetWidth
     if (adsIE&&adsUA.indexOf('trident/5')<0)h=s.offsetHeight
     else h=aD.offsetHeight
    }
   }
  }
  else if (adsMob){
   try{
    wi=f.contentWindow.document.body.scrollWidth;
    h=f.contentWindow.document.body.scrollHeight;
   }
   catch(e){}
  } 
 }
}
adsDevilObj(v.divName,wi,h)
if (((wi&&h)&&!(v.w==wi&&v.h==h)&&(aD1.indexOf('AOLDevilNoExpand')==-1))||(aD1.indexOf('AOLDevilExpand')!=-1)){
if (h!=1){
 f.width=wi
 f.height=h
}
}
if (wi&&h&&f&&adsQuigo==0)f.className="uac_"+wi+"x"+h;
adsQuigo=0
}
function adsRmChildren(o){
var f=null;
while (o.childNodes.length>0){
var c=o.childNodes[0],i=c.id
if (i){
if (i.toString().indexOf("atwAdFrame")!=-1){
f=c
f.src="about:blank"}
c.i=""}
if (c.childNodes.length>0)adsRmChildren(c)
o.removeChild(c)}
}
function adsClrDiv(){adsRmChildren(this)}
function adsClrAd(d){
var o=adsGetObj(d);
adsRmChildren(o)
}
function adsGetObj(d){
var o;
if (typeof(d)!='object')o=document.getElementById(d)
else o=d
return o
}
function adsLoadAd(){
this.ClearAd()
var f=document.createElement('iframe');
f.textAd=this.textAd
if ((this.textAd==1)||(this.col)){
f.visibility='hidden'
f.width=1
f.height=1
}else{
f.width=this.w
f.height=this.h
}
f.title="Ad"
f.marginWidth=0
f.marginHeight=0
f.setAttribute('allowtransparency','true')
f.frameBorder=0
f.scrolling="no"
f.w=this.w
f.h=this.h
f.mn=this.mn
f.divName=this.divName
this.appendChild(f)
if (this.iframe){f.id="adsF"+this.adNum
f.src=this.adURL
}else{
f.id="atwAdFrame"+this.adNum
if ((document.domain!=location.hostname)&&(this.adPage.indexOf('#')==-1))this.adPage=this.adPage+'#'+document.domain
if (this.adPage)f.src=this.adPage}
}
function adSetupDiv(w,h,u,dv1,pg,ds,m,sz,c){
if (!dv1||dv1==""){
var s="adsDiv"+adsDivs.length,d;
document.write("<div id='"+s+"'></div>")
d=document.getElementById(s)
dv1=s
}
else d=adsGetObj(dv1)
d.LoadAd=adsLoadAd
d.ClearAd=adsClrDiv
d.mn=m
if (ds=='text')d.textAd=1
if (ds&&ds!='text'&&ds!='iframe')d.dynSz=1
if (sz)d.sz=sz;
d.w=w;d.h=h;
d.divName=dv1
d.adURL=u
d.adPage=pg
d.adNum=adsDivs.length
d.col=c;
if (ds=='iframe')d.iframe=1
adsDivs[adsDivs.length]=d
}
function adsCkPlg(){
var dF='',n=navigator,a,d;
if (adsIE&&(adsUA.indexOf('win')!=-1)){
try {a=new ActiveXObject("ShockwaveFlash.ShockwaveFlash");
if (a){d=a.GetVariable("$version").split(" ")[1].split(",");
if (d[0]>=10)dF='F'
}}catch(e){}
}else{
var p=n.plugins
if (p){
var l=p.length
if (l>1){
var m=n.mimeTypes,fl=m['application/x-shockwave-flash']
if (m&&((fl&&fl.enabledPlugin&&(fl.suffixes.indexOf('swf')!=-1)))){
var ds,f="Flash ",fS
for (var i=0;i<l;i++){
ds=p[i].description
fS=ds.indexOf(f)
if (fS!=-1){
if (ds.substring(fS+6,fS+8)>=10){dF='F'}
}}}
if (fl==null)dF=''
}}}
adsNMSG=dF
}
function adsGetValues(){
var l=unescape(adsLo),p='',r='',s='',t='',v,x=0;
if (l.indexOf('&pLid')>0)v=l.match(/[?&]icid=.*?[|](.*?)[|](.*?)[|](.*?)&pLid=(.*?)($|\&|\|)/);
else v=l.match(/[?&]icid=.*?[|](.*?)[|](.*?)[|](.*?)[|](.*?)($|\&|\|)/);
if (v){
for (var i=1;i<=4;i++){
if (!(/^[0-9A-Za-z:\/._|\-]+$/.test(v[i]))){x=1;
break;
}
r+=v[i]+':'
}
if (!x)r='kvdl='+r.substring(0,r.length-1)+';';
else r='';
}
var exDom=["www.aim.com","www.aol.com","mail.aol.com","ads.web.aol.com"];
if ((adsLo.indexOf('https')==-1)&&(!adInList(adsLo,exDom))){
p=adsLo.substr(7).toLowerCase();
p=p.replace(/www\./,'');
p=p.replace(/\.com/,'');
p=p.replace(/[?#].*$/,'')
var l=p.length;
if (l>45)l=45;
p="kvpg="+escape(p.substr(0,l))+";";
p=p.replace(/\/;$/,';');
p=p.replace(/\//g,'%2F');
}
if (adsATOth.indexOf('kvugc')==-1){
 s='kvugc=';
 if (window.adSetUGC==0)s+='0;'
 else if (window.adSetUGC==1)s+='1;'
 else{
  if (adsATOth.indexOf('cmsid')==-1)s+='0;'
  else s+='1;'
 }
}
if (adsATOth.indexOf('kvpagetype')==-1)adsATOth+='kvpagetype=0;';
if (adsATOth.indexOf('kveditags')==-1)adsATOth+='kveditags=0;';
if (adsATOth.indexOf('kvmood')==-1)adsATOth+='kvmood=0;';
if (adsATOth.indexOf('kvpatcheditags')==-1)adsATOth+='kvpatcheditags=0;';
adsATOth+='kvag=0;kvinc=0;kvmar=0;kvch=0;kvseg=0;';
if (/(^|;)?UNAUTHID=(.*?)[.](.*?)[.]/i.test(document.cookie))t='kvui='+unescape(RegExp.$3)+';';
return p+r+s+t
}
function htmlAdWHDyn(m,s,t,dv,fn,ds){htmlAdWH(m,'','',t,dv,fn,ds,s.toString())}
function htmlAdWH(m,w,h,t,dv,fn,ds,sz){
if (!adsVal)adsVal=adsGetValues()
var d=document,inc='',s,r=0,st="<script type='text/javascript' src='",sp1,ye=0,c=0,f=0;
if (t){
t=t.toLowerCase()
if (t.indexOf('c')>0){c=1;t=t.substr(0,t.length-1)}
}
if (adsTp=='F'||t=='ajax'||t=='f')f=1
if (w=='RR'||w=='rr'){
 w=300;h=250;
 if (f)ds='r'
}
if (sz){
sp1=sz.split(',')[0].split('x');
w=sp1[0];
h=sp1[1];
if (f)ds='r'
}
if (t=='text'||f){
if (!fn||fn=='')fn=adsPage
if (fn==''||(adsUA.indexOf('opera')>-1)){adsTp='J';t=''}
}
if (w=='LB'||w=='lb'){
w=728,h=90;
if (f)ds='r'
sz='728x90,948x250,970x66,970x90,950x252,970x250,101x1'
}
if (adsCp){
var cl=adsCF.length;
for (var i=0;i<cl;i++){
if ((/http[s]{0,1}:\/\/[^\/]*?aol.com(:[0-9]*?){0,1}\//.test(adsCF[i]))&&(/^[0-9A-Za-z\/.:_\-]+$/.test(unescape(adsCF[i])))){
if ((adsCAd[i]=='I')&&(adsTile==1)){
if (adsIE)d.write(st+adsCF[i]+".js'></script>")
else{
var z=d.createElement('script')
z.src=adsCF[i]+".js"
d.body.appendChild(z)
}
}
if (sz){
var sp2=sz.split(','),le=sp2.length,sp3;
for (var j=0;j<le;j++){
sp3=sp2[j].split('x');
if (adsCW[i]==sp3[0]&&adsCH[i]==sp3[1])ye=1;
}}
if (ye||(ds=='r'&&!sz&&(adsCW[i]==300&&(adsCH[i]==250||adsCH[i]==600||adsCH[i]==1050)))||
   (adsCW[i]==w&&adsCH[i]==h)||(adsCAd[i]==adsTile))
{
if ((adsTp!='J')&&(adsTp!='F')&&(t!='f')&&(t!='text')&&(t!='ajax'))s=adsCF[i]+'.html'
else s=adsCF[i]+'.js'
adsCW[i]=0
r=1
break 
}}}}
if (adsMNS){
var mL=adsMNS.length;
for (var i=0;i<mL;i=i+2){
if (adsTile==adsMNS[i+1]){
m=adsMNS[i]
break
}}}
if (m=='0'){adsTile++;return 0}
if (r==0){
if (m[0]=='m'||m[0]=='M'){ 
adsDisableTacoda();
m=m.substring(1,m.length);
adsMob=1;
if (f)ds='r'
if (adsESN)adsESN=adsESN.replace(/&ESN/,"&sn");
if (adsATOth)adsATOth='&'+adsATOth.substring(0,adsATOth.length-1).replace(/;/g,'&');
s='http://mads.aol.com/mads/mediate.php?hw=web&appid='+m+'&format=js&width='+w+'&height='+h+'&useragent='+escape(adsUA)+'&pageurl='+escape(adsLo)+adsATMob+adsESN+adsATOth;
if (adsTp=='I'||t=='iframe')s=s.replace(/format=js/,"format=html");
}
else {
if (!adsNMSG&&adsUA.indexOf('ipad')==-1)adsCkPlg()
if (!adsNMSG)inc='artexc=art_flash,art_rrflash;'
s=adsHt+"/addyn/3.0/"+adsNt+"/"+adsPl+"/0/-1/"
if (sz)s+="allowedSizes="+sz+";"
else if (ds!='r')s+="size="+w+"x"+h+";"
s+="noperf=1;alias="+m+";"
if (adsTile!=1)s+="cfp=1;"
if (inc!=''||(t=='ajax'&&!adsAJAXAddOn)||adsAddOn==2){
s+="noaddonpl=y;";
adsAddOn=2;
}else{
 if (adsTile==1){
 if (adsAddOn==1)adsAddOn=2;
 else s+="noaddonpl=y;";
 }else {
 if (adsAddOn!=1)s+="noaddonpl=y;";
 else adsAddOn=2;
 }
}
if (adsExcV=='blank')inc='artexc=all;'
s+=inc+adsATOth+adsSrAT+adsATWM+adsVal+"kvmn="+m+";extmirroring=0;target=_blank;"+adsTzAT+"grp="+adsScr
}
}
if (t=='text'){
adSetupDiv(w,h,s,dv,fn,'text',m)
adsDivs[adsDivs.length-1].LoadAd()
}
else if (t=='ajax'){
adsAJAX=1
adSetupDiv(w,h,s,dv,fn,ds,m,sz,c)
adsDivs[adsDivs.length-1].LoadAd()
}
else if (t=='iframe'){
adSetupDiv(w,h,s.replace(/addyn\/3.0/,"adiframe/3.0"),dv,fn,'iframe',m)
adsDivs[adsDivs.length-1].LoadAd()
}
else if (adsTp=='F'||t=='f'){
adSetupDiv(w,h,s,dv,fn,ds,m,sz,c)
adsDivs[adsDivs.length-1].LoadAd()
}
else if (adsTp=='A0'||adsTp=='A1'){
var ai
if (adsTp=='A0')ai=d.getElementById('adsF0')
else ai=d.getElementById('adsF1')
adsD=new Date()
dt=adsD.getTime()%0x3b9aca00
var s1=s.replace(/addyn\/3.0/,"adiframe/3.0").replace(/grp=[0-9]*/, "grp=" + dt);
ai.src=s1
}
else if (adsTp!='J'){
var s1=s.replace(/addyn\/3.0/,"adiframe/3.0")
d.write("<iframe title='Ad' name='adsF"+adsLNm+"' id='adsF"+adsLNm+"' src='"+s1+"' width='"+w+"' height='"+h+"' scrolling=no frameborder=0 marginheight=0 marginwidth=0></iframe>")
adsLNm++
}
else if (adsTp=='J'){
if (dv==undefined||adsMob)d.write(st+s+"'></script>")
else {
 s=s.replace(/allowedSizes=.*?;/,"size="+w+"x"+h+";");
 if (s.indexOf('size=')==-1)s=s.replace(/\/0\/-1\//, "\/0\/-1\/size="+w+"x"+h+";");
 var div=adsGetObj(dv),img=d.createElement('img'),li=document.createElement('a'),sI=s.replace(/addyn/,"adserv"),sH=s.replace(/addyn/,"adlink");
 li.href=sH;
 li.target='_blank';
 img.src=sI;
 img.alt='Ad';
 img.height=h;
 img.width=w;
 li.appendChild(img);
 div.appendChild(li);
}}
adsTile++
}
function imageAdWH(){}
}}