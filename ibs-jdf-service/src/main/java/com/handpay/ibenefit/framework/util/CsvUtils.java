package com.handpay.ibenefit.framework.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;

public final class CsvUtils {
	private CsvUtils(){
	}
	public static List<String> splitCSV(String src) throws Exception {
		if (src == null || src.equals("")){
			return new ArrayList<String>(0);
		}
		StringBuilder st = new StringBuilder();
		List<String> result = new ArrayList<String>();
		boolean beginWithQuote = false;
		for (int i = 0; i < src.length(); i++) {
			char ch = src.charAt(i);
			if (ch == '\"') {
				if (beginWithQuote) {
					i++;
					if (i >= src.length()) {
						result.add(st.toString());
						st = new StringBuilder();
						beginWithQuote = false;
					} else {
						ch = src.charAt(i);
						if (ch == '\"') {
							st.append(ch);
						} else if (ch == ',') {
							result.add(st.toString());
							st = new StringBuilder();
							beginWithQuote = false;
						} else {
							throw new Exception(
									"Single double-quote char mustn't exist in filed "
											+ (result.size() + 1)
											+ " while it is begined with quote\nchar at:"
											+ i);
						}
					}
				} else if (st.length() == 0) {
					beginWithQuote = true;
				} else {
					throw new Exception(
							"Quote cannot exist in a filed which doesn't begin with quote!\nfield:"
									+ (result.size() + 1));
				}
			} else if (ch == ',') {
				if (beginWithQuote) {
					st.append(ch);
				} else {
					result.add(st.toString());
					st = new StringBuilder();
					beginWithQuote = false;
				}
			} else {
				st.append(ch);
			}
		}
		if (st.length() != 0) {
			if (beginWithQuote) {
				throw new Exception("last field is begin with but not end with double quote");
			} else {
				result.add(st.toString());
			}
		}
		return result;
	}

	public static String convertStringToCSV(String convertString) {
		StringBuilder tempBuffer = new StringBuilder(20);
		if (convertString.contains(",") && !convertString.contains("\""))
			tempBuffer.append("\"").append(convertString).append("\"");
		else if (!convertString.contains("\"") && !convertString.contains(",")) {
			tempBuffer.append(convertString);
		} else {
			tempBuffer.append("\"").append(convertString.replace("\"", "\"\""))
					.append("\"");
		}
		return tempBuffer.toString();
	}
	
	/**
	   * 生成为CVS文件 
	   * @param exportData
	   *			  源数据List
	   * @param map
	   *			  csv文件的列表头map
	   * @param outPutPath
	   *			  文件路径
	   * @param fileName
	   *			  文件名称
	   * @return
	   */
	  @SuppressWarnings("rawtypes")
	  public static File createCSVFile(List exportData, LinkedHashMap map, String outPutPath,String fileName,String charset) {
	    File csvFile = null;
	    BufferedWriter csvFileOutputStream = null;
	    try {
	      File file = new File(outPutPath);
	      if (!file.exists()) {
	        file.mkdir();
	      }
	      //定义文件名格式并创建
	      csvFile = File.createTempFile(fileName, ".csv", new File(outPutPath));
	      // UTF-8使正确读取分隔符","  
	      csvFileOutputStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), charset), 1024);
	      // 写入文件头部  
	      for (Iterator propertyIterator = map.entrySet().iterator(); propertyIterator.hasNext();) {
		        java.util.Map.Entry propertyEntry = (java.util.Map.Entry) propertyIterator.next();
		        csvFileOutputStream.write("\"" + (String) propertyEntry.getValue() != null ? (String) propertyEntry.getValue() : "" + "\"");
		        if (propertyIterator.hasNext()) {
		          csvFileOutputStream.write(",");
		        }
	      }
	      csvFileOutputStream.newLine();
	      // 写入文件内容  
	      if(exportData!=null && exportData.size()>0){
	    	  for (Iterator iterator = exportData.iterator(); iterator.hasNext();) {
	  	        Object row = (Object) iterator.next();
	  		        for (Iterator propertyIterator = map.entrySet().iterator(); propertyIterator.hasNext();) {
	  		          java.util.Map.Entry propertyEntry = (java.util.Map.Entry) propertyIterator.next();
	  		          if(row!=null && propertyEntry.getKey()!=null){
	  		        	  csvFileOutputStream.write((String) BeanUtils.getProperty(row, (String) propertyEntry.getKey()));
	  		          }
	  		          if (propertyIterator.hasNext()) {
	  		            csvFileOutputStream.write(",");
	  		          }
	  	        }
	  	        if (iterator.hasNext()) {
	  	          csvFileOutputStream.newLine();
	  	        }
	  	      }
	  	      csvFileOutputStream.flush();
	      }

	    } catch (Exception e) {
	      e.printStackTrace();
	    } finally {
	      try {
	    	  if(csvFileOutputStream != null){
	    		  csvFileOutputStream.close();
	    	  }
	      } catch (IOException e) {
	        e.printStackTrace();
	      }
	    }
	    return csvFile;
	  }

	  /**
	   * 下载文件
	   * @param response
	   * @param csvFilePath
	   *			  文件路径
	   * @param fileName
	   *			  文件名称
	   * @throws IOException
	   */
	  public static void exportFile(HttpServletResponse response, String csvFilePath, String fileName,String charset)
	                                                  throws IOException {
	    response.setContentType("application/csv;charset="+charset);
	    response.setHeader("Content-Disposition",
	      "attachment;  filename=" + URLEncoder.encode(fileName, charset));
	    
	    InputStream in = null;
	    try {
	      in = new FileInputStream(csvFilePath);
	      int len = 0;
	      byte[] buffer = new byte[1024];
	      response.setCharacterEncoding(charset);
	      OutputStream out = response.getOutputStream();
	      while ((len = in.read(buffer)) > 0) {
	        out.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
	        out.write(buffer, 0, len);
	      }
	    } catch (FileNotFoundException e) {
	      e.printStackTrace();
	    } finally {
	      if (in != null) {
	        try {
	          in.close();
	        } catch (Exception e) {
	          throw new RuntimeException(e);
	        }
	      }
	    }
	  }
}
