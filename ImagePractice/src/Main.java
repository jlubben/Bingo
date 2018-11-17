
public class Main {
	
	private static String  _basePath = "C:\\Users\\Lubben\\Desktop\\Image_Test\\";
	
    public static void main(String[] args) {   
    	long now = System.currentTimeMillis();
        SaveSaltAndPeppers();
        long later = System.currentTimeMillis() - now;
        later = 0;
    }
    
    //can be used to save the series of photos just as their black and white counterpart via main
    //this is assuming the files are in the proper directory
    public static void SaveBlackAndWhite() {
    	String path = _basePath+"BW";
    	String fileName = "tb";
    	String fileExtension = ".png";
    
    	String writePostfix = "bw";
    	for(char a = 'a'; a<= 'i'; a++) {
    		String totalFileName = fileName+a+fileExtension;
    		
            ImageAccessor imageAccessor = new ImageAccessor(path, totalFileName);
            String writeFileName = fileName+a+"_"+writePostfix+fileExtension;
            imageAccessor.SaveBlackAndWhite(path, writeFileName);
    	}
    }
    
    //salt and peppers the images
    public static void SaveSaltAndPeppers() {
    	String path = _basePath+"editing";

    	SaveSaltAndPepper(path, 5);
    	SaveSaltAndPepper(path, 11);
    	SaveSaltAndPepper(path, 22);
    	SaveSaltAndPepper(path, 29);
    	SaveSaltAndPepper(path, 32);
    	SaveSaltAndPepper(path, 38);
    	SaveSaltAndPepper(path, 50);
    	SaveSaltAndPepper(path, 52);
    	SaveSaltAndPepper(path, 66);
    	SaveSaltAndPepper(path, 70);
    }
    
    //Sets a particular file to become salt and peppered
    public static void SaveSaltAndPepper(String basePath, int number) {
    	String path = basePath + "\\"+number;
    	
    	String fileNamePrefix = "tb";
    	String fileExtension = ".png";
    	int counter = 0;
    	for(char a = 'a'; a<= 'i'; a++) {
        	String fileName = fileNamePrefix+a+"_"+number+fileExtension;
    		ImageAccessor imageAccessor = new ImageAccessor(path, fileName);
    		for(double i = 0.08; i<0.15; i+=0.00005) {
    		//some meddling has been down to use a good gaussian value and create enough records
    		//for(double i = 0.13; i<0.20; i+=0.00005) {
    		//for(double i = 0.13; i<0.20; i+=0.01) {
	        	String tempFileName = fileNamePrefix+a+"_"+number+"_"+i+fileExtension;
	        	String writePath = path;
	        	if(counter %4 == 0) {
	        		writePath+="\\Test";
	        	}
	        	else {
	        		writePath+="\\Train";
	        	}
	        	imageAccessor.SaveSaltAndPepper(writePath, tempFileName, i);
	        	counter++;
	        }
    	}
    }
}
