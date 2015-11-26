package com.handpay.ibenefit.framework.util;

import java.io.File;

public class UploadFile {
	private String fileName;
	private File file;

	public UploadFile(){
	}

	public UploadFile(String fileName,File file){
		this.fileName = fileName;
		this.file = file;
	}
	public String getFileName() {
    	return fileName;
    }
	public void setFileName(String fileName) {
    	this.fileName = fileName;
    }
	public File getFile() {
    	return file;
    }
	public void setFile(File file) {
    	this.file = file;
    }
}
