<!DOCTYPE HTML>
<html lang="en-US">
<head>
    <meta charset="UTF-8">
    <script type="text/javascript">
    // BEGIN ---- ADDED by CMD from HDS Fed Corp --- 20Mar2013 -----		
		function onloaded_alternate() {
			try {
				console.log("Loading script");
			} catch (e) {
				console = {
					log : function() {
					}
				};
			}

			//banner content made visible
			document.getElementById("banner_content").style.display="block";
			//original content made invisible
			document.getElementById("original_content").style.display="none";
	
			var client = new XMLHttpRequest();
			client.open('GET', 'banner.html');
				client.onreadystatechange = function() {
				console.log("loading banner into object");
				document.getElementById("banner").innerHTML=client.responseText;
				console.log(client.responseText);
			}
			client.send();
		}
		function switchOut() {
			//hide banner, reveal rest of page
			document.getElementById("banner_content").style.display="none";
			document.getElementById("original_content").style.display="block";
		
			
			//original onload function		
			//onloaded();
			window.location.href = "/MetaCatalog"
		}
    
    </script>
    <title>Page Redirection</title>
</head>
<body onload="onloaded_alternate()">
<!-- Note: don't tell people to `click` the link, just tell them that it is a link -->


<!-- banner content -->
		<div id="banner_content">
			
			<span id="banner">Loading...</span>
			<br />	
			<center><button id="acknowledgement" onmousedown="switchOut()">I Agree</button></center>
		</div>
<!-- original content -->
<div id="original_content">
If you are not redirected automatically, follow the <a href='/MetaCatalog'>link to start COMET interface</a>
</div>


</body>
</html>
