function seeScreenShot(screenshot)
{
 gnuPage = "<HTML><HEAD><TITLE>Zmanda Screenshot - ZRM</TITLE>" +
  "</HEAD><BODY LEFTMARGIN=0 " +
  "MARGINWIDTH=0 TOPMARGIN=0 MARGINHEIGHT=0><CENTER>" +
  "<IMG SRC='" + screenshot + "' BORDER=0 NAME=image " +
  "onload='window.resizeTo(document.image.width+20,document.image.height+60)'></CENTER>" +
  "</BODY></HTML>";
 popup=window.open('','image','toolbar=0,location=0,directories=0,menuBar=0,scrollbars=0,resizable=1');
 popup.document.open();
 popup.document.write(gnuPage);
 popup.document.close()
 }
//-->