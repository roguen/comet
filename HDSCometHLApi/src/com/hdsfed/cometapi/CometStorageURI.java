package com.hdsfed.cometapi;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class CometStorageURI {
	private static String ingestTempDirectory="/tmp/ingest-tempdir/";

	private File file=null;  // /opt/COMETDist/InputDir/Media/DigitalVideo/myfile.m4v
	private URL hcpURL=null;  // 
	private String uri=null;
	private File ingestInputDir=null;
	private URL destURL=null;
	
	public void Recalculate() throws MalformedURLException {
		uri=file.getAbsolutePath().substring(ingestInputDir.getAbsolutePath().length());
		
		if(CometProperties.getInstance().alwaysUsePrefix()) {
			uri="/"+ingestInputDir.getName()+uri;
		}
		
		hcpURL=new URL(AnnotationHelper.URIEncoder(destURL.toString()+uri));
	}
	
	public CometStorageURI(File localFile, File inIngestInputDir, URL inDestURL) throws IOException {
		Init(localFile,inIngestInputDir, inDestURL);
	}
	
	public CometStorageURI(File localFile) throws IOException {
		Init(localFile,null,null);
	}
	
	private void Init(File localFile, File inIngestInputDir, URL inDestURL) throws IOException {
		file=localFile;
		if(inDestURL==null) {
			inDestURL=CometProperties.getInstance().getDestinationRootPath();
		}
		
		
		if(inIngestInputDir==null) {
			if(CometProperties.getInstance().getSourcePath().contains(",")) ingestInputDir=new File(CometProperties.getInstance().getSourcePath().split(",")[0]);
			else ingestInputDir=new File(CometProperties.getInstance().getSourcePath());
		} else {
				ingestInputDir=inIngestInputDir;
		}
		setDestURL(inDestURL);
		Recalculate();
	}
	public void ChangeExtension(String newext) throws MalformedURLException {
		String path=file.getAbsolutePath();
		int index_last_dot=path.lastIndexOf(".");
		file=new File(path.substring(0,index_last_dot)+"."+newext);
		Recalculate();
	}	

	public File getTempFile() {
		return new File(CometStorageURI.getIngestTempDirectory()+file.getName());
	}
	
	public static String getIngestTempDirectory() {
		return ingestTempDirectory;
	}

	public static void setIngestTempDirectory(String ingestTempDirectory) {
		CometStorageURI.ingestTempDirectory = ingestTempDirectory;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public URL getHcpURL() {
		return hcpURL;
	}

	public void setHcpURL(URL hcpURL) {
		this.hcpURL = hcpURL;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public File getIngestInputDir() {
		return ingestInputDir;
	}

	public void setIngestPrefix(File inIngestInputDir) {
		this.ingestInputDir = inIngestInputDir;
	}

	public URL getDestURL() {
		return destURL;
	}

	public void setDestURL(URL destURL) {
		this.destURL = destURL;
	}
	
	
	
	
}
