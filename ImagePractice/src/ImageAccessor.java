import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

public class ImageAccessor {
	
	private String _path;
	private BufferedImage  _image;
	private BufferedImage _originalImage;
	
	//creates and loads the base image
	public ImageAccessor(String path, String fileName) {
		_path = path;
		
		try
		{
			String totalPath = getPath(fileName);
			_originalImage = ImageIO.read(new File(totalPath));
		}
		catch (IOException e)
		{
		    String workingDir = System.getProperty("user.dir");
		    System.out.println("Current working directory : " + workingDir);
		    e.printStackTrace();
		}
	}
	
	//write the image to a given file path
	public void write(String path, String fileName) {
		try {
			String totalPath = getPath(path, fileName);
			RenderedImage renderedImage = (RenderedImage)_image;
			ImageIO.write(renderedImage, "jpg", new File(totalPath));	
		}
		catch(IOException e) {
		    String workingDir = System.getProperty("user.dir");
		    System.out.println("Current working directory : " + workingDir);
		    e.printStackTrace();
		}
	}
	
	//saves a salt and pepper image to a path with a given file name and using standard deviation for the variation of the salt and pepper
	public void SaveSaltAndPepper(String path, String fileName, double standardDeviation) {
		setSaltAndPepper(standardDeviation);
		write(path, fileName);
	}
	
	//saves an images as black and white to a given file name and path
	public void SaveBlackAndWhite(String path, String fileName) {
		setBlackAndWhite();
		write(path, fileName);
	}
	
	
	//rotates an image
	private void rotate(double gaussian) {
	    double angle = Math.toRadians(gaussian*10);
	    double sin = Math.sin(angle);
	    double cos = Math.cos(angle);
	    
	    double x0 = 0.5 * (_image.getWidth() - 1);     // point to rotate about
	    double y0 = 0.5 * (_image.getHeight() -1);     // center of image

	    WritableRaster inRaster = _image.getRaster();
	    BufferedImage newPicture = new BufferedImage(_image.getWidth(), _image.getHeight(), BufferedImage.TYPE_INT_RGB);
	    WritableRaster outRaster = newPicture.getRaster();
	    int[] pixel = new int[3];
	    int height = _image.getHeight();
	    int width = _image.getWidth();
	    // rotation
	    for (int x = 0; x < width; x++) {
	        for (int y = 0; y < height; y++) {
	            double a = x - x0;
	            double b = y - y0;
	            int xx = (int) (+a * cos - b * sin + x0);
	            int yy = (int) (+a * sin + b * cos + y0);
	            if (xx >= 0 && xx < width && yy >= 0 && yy < height) {
	            	int[] inputValues= inRaster.getPixel(xx, yy, pixel);
	            	outRaster.setPixel(x, y, inputValues);
	            }
	        }
	    }	
	    whiteOutEdges(newPicture);
	    _image = getCroppedImage(newPicture);
	    
	}
	
	//crops an image so that the there isn't too much empty white space on the left/right of the image
	private BufferedImage getCroppedImage(BufferedImage bufferedImage) {
		int height = bufferedImage.getHeight();
		int width = bufferedImage.getWidth();
		int leftEdge = getLeftEdge(bufferedImage);
		if(leftEdge>30) {
			leftEdge -=30;
		}
		int rightEdge = getRightEdge(bufferedImage)-leftEdge;
		width -=leftEdge;
		if(rightEdge+30<width-1){
			rightEdge+=30;
		}
		return bufferedImage.getSubimage(leftEdge, 0, rightEdge, height);
	}
	
	//determines where the left edge of a photo begins
	private int getLeftEdge(BufferedImage bufferedImage) {
	    int width = bufferedImage.getWidth();
	    for (int x = 0; x < width; x++) {
	    	if(doesRowContainBlack(bufferedImage, x)) {
	    		return x;
	    	}
	    }
	    throw new RuntimeException("Could not find left edge");
	}
	
	//determines where the right edge of a photo begins
	private int getRightEdge(BufferedImage bufferedImage) {
	    int width = bufferedImage.getWidth();
	    for (int x = width-1; x>0; x--) {
	    	if(doesRowContainBlack(bufferedImage, x)) {
	    		return x;
	    	}
	    }
	    throw new RuntimeException("Could not find left edge");
	}
	
	// for a given x value, determines if the pixel 
	private boolean doesRowContainBlack(BufferedImage image, int x) {
		int height = image.getHeight();
        for (int y = 0; y < height; y++) {
			Color color = getColor(image, x, y);
			int average = (color.getRed()+color.getBlue()+color.getGreen())/3;
			if(average<255) {
				return true;
			}
        }
        return false;
	}
	
	
	//after an image is rotated, their is an issue of blacked out edges from the missing space
	private void whiteOutEdges(BufferedImage image) {
		int edgeBufferSize = 25; //pixel buffer that is checking on edges for black pixels
		for(int x = 0; x<edgeBufferSize; x++) {
			for(int y = 0; y<_image.getHeight(); y++){
				image.setRGB(x, y, -1);
			}
		}
		
		for(int x = _image.getWidth()-edgeBufferSize; x<_image.getWidth(); x++) {
			for(int y = 0; y<_image.getHeight(); y++){
				image.setRGB(x, y, -1);
			}
		}
		
		for(int y = 0; y<edgeBufferSize; y++) {
			for(int x = 0; x<_image.getWidth(); x++){
				image.setRGB(x, y, -1);
			}
		}
		
		for(int y =_image.getHeight()-edgeBufferSize; y<_image.getHeight(); y++) {
			for(int x = 0; x<_image.getWidth(); x++){
				image.setRGB(x, y, -1);
			}
		}
	}
	
	//Sets an entire image to become salt and peppered. input is the standard deviation used alongside
	//a calculated gaussian value.
	private void setSaltAndPepper(double standardDeviation) {
		_image = getDeepCopy();
		Random rnd=new Random();

		rotate( rnd.nextGaussian()*0.23);//0.13,0/16 playing with a good variation for an acceptable amount of randomization in the rotation of an image.
		int height = _image.getHeight();
		int width = _image.getWidth();
		
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				Color color = getColor(_image, x, y);
				int average = (color.getRed()+color.getBlue()+color.getGreen())/3;
				double rounder = rnd.nextGaussian()*standardDeviation;
				int round = (int)Math.round(rounder);
				//adding a bit of variance to add more white to an image if it is over the black pixels of an image.
				if(average <210 && Math.abs(rounder)<0.0003) {
					average = 255;
				}else {
					average += round;
				}

				if(round != 0) {
					average = average+0;
				}
				setRGBColor(x, y, average);
				setPixelBlackAndwhite(x, y);
			}
		}
	}
	
	//sets the entire image to become black and white
	private void setBlackAndWhite() {
		int height = _image.getHeight();
		int width = _image.getWidth();
		for(int x = 0; x<width; x++) {
			for(int y = 0; y<height; y++) {
				setPixelBlackAndwhite(x, y);
			}
		}		
	}
	
	//creates a deep copy of an image
	private BufferedImage getDeepCopy() {
		ColorModel cm = _originalImage.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = _originalImage.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
	
	//set a particular pixel to become black and white via x & y value 
	private void setPixelBlackAndwhite(int x, int y) {
		Color color = getColor(_image, x, y);
		int average = (color.getRed()+color.getBlue()+color.getGreen())/3;
		int blackDivisor = 90;// Threshold for deciding if a pixel is white or black
		if(average < blackDivisor) {
			average = 1;
		}
		else {
			average = 255;
		}
		setRGBColor(x, y, average);
	}
	
	//set the RGB of a particular pixel
	private void setRGBColor(int x, int y, int rgb) {
		rgb = (rgb << 8) + rgb;
		rgb = (rgb << 16) + rgb;
		_image.setRGB(x, y, rgb);
	}
	
	//return the color of an image a particular x, y value
	private Color getColor(BufferedImage image, int x, int y) {
		Color color = new Color(image.getRGB(x, y));
		return color;
	}
	
	private String getPath(String fileName) {
		return _path + "\\"  +fileName;
	}
	
	private String getPath(String directory, String fileName) {
		return directory + "\\"  +fileName;
	}

}
