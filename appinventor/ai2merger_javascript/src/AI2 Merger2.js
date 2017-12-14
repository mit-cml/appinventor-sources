/* AI2 Merger.js: enables users to merge two or more App Inventor projects.
 *
 * Author: Arezu Esmaili (arezuesmaili1@gmail.com)
 *
 */

// Returns the file name.
function getFileName(path) {
  return path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'));
}

// Returns the file extension.
function getExt(path) {
  return path.substring(path.lastIndexOf('.'));
}

// Replaces the old name with the new name in the project's path.
function createNewFilePath(path, newName) {
  return path.substring(0, path.lastIndexOf('/') + 1) + newName + 
      path.substring(path.lastIndexOf('.'));
}

// Creates the final .aia file.
function makeZip(zip, name) {
  var content = zip.generate({type:'blob'});
  var fileName = name + ".aia"; //changes extension from .zip to .aia
	
  saveAs(content, fileName);
}

// Merges projects.
function merge() {
  var finalZip = new JSZip();
  var num = 0;
  var screenNum = 0;
  var newName;
		
  (function () {
		  
    // See http://stackoverflow.com/questions/30564244/jszip-read-zip-file-and-execute-from-an-existing-zip-file	
		  
    // Show error if zip is corrupted.	  
    if (!window.FileReader || !window.ArrayBuffer) {
      $("#error_block").removeClass("hidden").addClass("show");
	  return;
    }

    // Show contents.
    var $result = $("#result");
    $("#file").on("change", function(evt) {
      // Remove content.
	  $result.html("");
	  // Be sure to show the results.
	  $("#result_block").removeClass("hidden").addClass("show");
		
	  var numFiles = document.getElementById("file").files.length;
			
	  // See http://www.html5rocks.com/en/tutorials/file/dndfiles/
			
	  var files = evt.target.files;
	  for (var i = 0, f; f = files[i]; i++) {
	    var reader = new FileReader();

		// Closure to capture the file information.
		reader.onload = (function(theFile) {
		  return function(e) {
			var $title = $("<h4>", {
			  text : theFile.name
			});
				  
		    var fileName = theFile.name;
				  
		    $result.append($title);
		    var $fileContent = $("<ul>");
		    try {
		  	  // Read the content of the file with JSZip.
			  var zip = new JSZip(e.target.result);

			  $.each(zip.files, function (index, zipEntry) {
			    $fileContent.append($("<li>", {
			      text : zipEntry.name
			    }));
			    // The content is here: zipEntry.asText()
					
			    if (!(getFileName(zipEntry.name) == "Screen1") || num < 1) {
				  if (!(getExt(zipEntry.name) == ".scm") || !(getExt(zipEntry.name) == ".bky") || 
				      !(getExt(zipEntry.name) == ".yail") || 
				      !(getExt(zipEntry.name) == ".properties")) {
				    // For media files.
				    finalZip.file(zipEntry.name, zipEntry.asUint8Array());
				  } else {
				    // For text files.
				    finalZip.file(zipEntry.name, zipEntry.asText());
				  }
			    } else { //Rename duplicate Screen1s.
				  if (screenNum == 0) {
				    newName = prompt("Your project can only have one Screen1. Rename Screen1 from " + 
				        fileName + " to continue.");
				  }
				  var text = zipEntry.asText();

				  if (getExt(zipEntry.name) == ".scm") {
				    text = zipEntry.asText().replace("\"$Name\":\"Screen1\"", "\"$Name\":\"" + 
				        newName + "\"");
				    text = text.replace("\"Title\":\"Screen1\"", "\"Title\":\"" + newName + "\"");
				  } 
						
				  finalZip.file(createNewFilePath(zipEntry.name, newName), text);
				  screenNum += 1;
			    }
					  
			  });
			  num += 1;
			  screenNum = 0;

		    } catch(e) {
		      $fileContent = $("<div>", {
			    "class" : "alert alert-danger",
			    text : "Error reading " + theFile.name + " : " + e.message
		      });
		    } 
				  
		    if (num == numFiles && num > 1) {
			  var name = prompt("Enter new file name");
					
			  makeZip(finalZip, name);
		    }
	
		    $result.append($fileContent);
		  }
	    })(f);
	    // Produces valid content for JSZip.
        // Could be done with both readAsArrayBuffer and readAsBinaryString.
	    reader.readAsArrayBuffer(f);
	  }
    });
  })();
}