package com.handpay.ibenefit.framework.util;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.log4j.Logger;

public final class ImageUtils {
	
	private static final Logger LOGGER = Logger.getLogger(ImageUtils.class);
	
	private ImageUtils(){
	}

	public static BufferedImage rotateImage(final BufferedImage bufferedImage,final int radian){
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();

		BufferedImage dstImage = null;
		AffineTransform affineTransform = new AffineTransform();

		if (radian == 180) {
			affineTransform.translate(width, height);
			dstImage = new BufferedImage(width, height, bufferedImage.getType());
		} else if (radian == 90) {
			affineTransform.translate(height, 0);
			dstImage = new BufferedImage(height, width, bufferedImage.getType());
		} else if (radian == 270) {
			affineTransform.translate(0, width);
			dstImage = new BufferedImage(height, width, bufferedImage.getType());
		}

		affineTransform.rotate(java.lang.Math.toRadians(radian));
		AffineTransformOp affineTransformOp = new AffineTransformOp(affineTransform, null);
		return affineTransformOp.filter(bufferedImage, dstImage);
    }
	
	public static BufferedImage readImage(String fileName) {
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(new File(fileName));
		} catch (IOException e) {
			LOGGER.error("readImage",e);
		}
		return bi;
	}
	
	
	public static boolean writeImage(BufferedImage im,String fileName) {
		boolean result = false;
		String imgType = fileName.substring(fileName.lastIndexOf(".")+1);
		try {
			result = ImageIO.write(im, imgType, new File(fileName));
		} catch (IOException e) {
			LOGGER.error("writeImage",e);
		}
		return result;
	}
	
	public static void cutImage(int x, int y, int width, int height,String oldpath, String newpath) {
		FileInputStream is = null;
		ImageInputStream iis = null;
		String imgType = getImageType(newpath);;
		try {
			is = new FileInputStream(oldpath);
			Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName(imgType);
			ImageReader reader = it.next();
			iis = ImageIO.createImageInputStream(is);
			reader.setInput(iis, true);
			ImageReadParam param = reader.getDefaultReadParam();
			Rectangle rect = new Rectangle(x,y, width, height);
			param.setSourceRegion(rect);
			BufferedImage bi = reader.read(0, param);
			ImageIO.write(bi, imgType, new File(newpath));
		} catch (Exception e) {
			LOGGER.error("cutImage",e);
		}finally{
			try{
				is.close();
				iis.close();
			}catch (Exception e) {
			}
		}
	}
	
	public static void main(String[] args) {
	    String oldpath = "C:\\\\Users\\\\pubx\\\\Desktop\\\\FGHJ_090010.jpg";
	    String newpath = "C:\\\\Users\\\\pubx\\\\Desktop\\\\temp.jpg";
	   // ImageUtils.cutImage(0D, 0D, 100D, 100D, newpath, newpath);
    }
	
	public static void scaleImage(String oldpath, String newpath, int wdith, int height) {
		// 获取老的图片
		File oldimg = new File(oldpath);
		String imgType = getImageType(newpath);;
		try {
			BufferedImage bi = ImageIO.read(oldimg);
			Image temp = bi.getScaledInstance(wdith, height, BufferedImage.SCALE_SMOOTH);
			BufferedImage thumbnail = new BufferedImage(wdith, height, BufferedImage.TYPE_INT_ARGB);
			thumbnail.getGraphics().drawImage(temp, 0, 0, null);
			File file = new File(newpath);
			FileOutputStream newimage = new FileOutputStream(file);
			ImageIO.write(thumbnail,imgType, newimage);
			newimage.close();
			bi.flush();
			bi = null;
		} catch (IOException e) {
			LOGGER.error("scaleImage", e);
		}
	}

	
	public static void zoomImage(String old,String target,double zoomRatio) throws Exception {
		Image image = ImageIO.read(new File(old));
		int width = new Double(image.getWidth(null) * zoomRatio).intValue();
		int height = new Double(image.getHeight(null) * zoomRatio).intValue();
		AreaAveragingScaleFilter areaAveragingScaleFilter = new AreaAveragingScaleFilter(width,height);
		FilteredImageSource filteredImageSource = new FilteredImageSource(image.getSource(),areaAveragingScaleFilter);
		BufferedImage bufferedImage = new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);
		Graphics graphics = bufferedImage.createGraphics();
		Component component = new Canvas();
		graphics.drawImage(component.createImage(filteredImageSource),0,0,null);
		String imgType = getImageType(target);
		ImageIO.write(bufferedImage,imgType,new File(target));
	}
	
	public static String getImageType(String path){
		return "png";
//		String imgType = path.substring(path.lastIndexOf(".")+1);
//		if(imgType.equalsIgnoreCase("jpg") || imgType.equalsIgnoreCase("jpeg")){
//			return "JPEG";
//		}else{
//			return "png";
//		}
	}
}
