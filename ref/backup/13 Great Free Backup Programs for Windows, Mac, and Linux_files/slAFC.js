//Set the SLM request parameters
var slURL=window.location.href;

var params = {callback:'', sch:'', ssch:'', surl:'', snum:'', of:'', rv:'', shints:''};
params.callback='parseSL';
params.sch= 'afc-weblogs-xml';
params.ssch = 'switched';
params.surl = slURL;
params.snum=slsnum;
params.of='js';
params.rv='1.3';
params.shints='technology';

//Request the Sponsored Links
$(get_links());