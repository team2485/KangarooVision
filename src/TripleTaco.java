
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class TripleTaco {

	private double[][] data;
	private BufferedImage latestImage;
	private static boolean stop;
	
	public static void main(String[] args) {
		new ImgProcThread().start();
	}

	 public static BufferedImage Mat2BufferedImage(Mat m) {
		    // Fastest code
		    // output can be assigned either to a BufferedImage or to an Image

		    int type = BufferedImage.TYPE_BYTE_GRAY;
		    if ( m.channels() > 1 ) {
		        type = BufferedImage.TYPE_3BYTE_BGR;
		    }
		    int bufferSize = m.channels()*m.cols()*m.rows();
		    byte [] b = new byte[bufferSize];
		    m.get(0,0,b); // get all the pixels
		    BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
		    final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		    System.arraycopy(b, 0, targetPixels, 0, b.length);  
		    return image;
		}
	  
	 public static Mat BufferedImageToMatPixels(BufferedImage image) {
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
			byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
			Mat m = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC(image.getColorModel().getNumComponents()) );
			m.put(0, 0, pixels);
			return m;
		}
	private static class ImgProcThread extends Thread {

		private ImgProcThread() {
			this.setPriority(MIN_PRIORITY);
		}

		@Override
		public void run() {
//			String pathToCameraFeed = "http://10.24.85.11/mjpg/video.mjpg";
//			NetworkTable.setClientMode();
//			NetworkTable.setIPAddress("10.24.85.1");
//			NetworkTable table = NetworkTable.getTable("targetCenter");
//			Webcam dankMemeLord = new Webcam(pathToCameraFeed);
			
//			VideoFrame frame = new VideoFrame();

			BufferedImage bob = null;
			try {
				bob = ImageIO.read(new File("/Users/jeremymcculloch/Desktop/vision.png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			while (!stop) {

				Mat matImg = BufferedImageToMatPixels(bob);
				
//				frame.setImg(Mat2BufferedImage(matImg));

				Mat hlsImg = new Mat();
				Imgproc.cvtColor(matImg, hlsImg, Imgproc.COLOR_RGB2HLS);

				ArrayList<Mat> hlsChannels = new ArrayList<Mat>();
				Core.split(hlsImg, hlsChannels);

				Mat cur = hlsChannels.get(0);
				
				//frame.setImg(Mat2BufferedImage(cur));
				Mat low = cur.clone();
				Mat high = cur.clone();

				Imgproc.threshold(cur, low, 60, 360, Imgproc.THRESH_BINARY);
				Imgproc.threshold(cur, high, 90, 360, Imgproc.THRESH_BINARY_INV);

				Core.bitwise_and(low, high, cur);
				
				//frame.setImg(Mat2BufferedImage(cur));

				hlsChannels.set(0, cur);

				for (int i = 1; i < hlsChannels.size(); i++) {

					cur = hlsChannels.get(i);
					low = cur.clone();
					high = cur.clone();

					Imgproc.threshold(cur, low, 47, 180, Imgproc.THRESH_BINARY);
					Imgproc.threshold(cur, high, 94, 180, Imgproc.THRESH_BINARY_INV);

					Core.bitwise_and(low, high, cur);

					hlsChannels.set(i, cur);
				}

				Mat thresholdedImage = cur.clone();

				Core.bitwise_and(hlsChannels.get(0), hlsChannels.get(1), thresholdedImage);
				Core.bitwise_and(thresholdedImage, hlsChannels.get(2), thresholdedImage);

				Mat erodeMat = cur.clone();
				Mat dilateMat = cur.clone();
				 Mat kernelSize= new Mat(3, 3, 3);
				Imgproc.erode(thresholdedImage, erodeMat, kernelSize, new Point(-1, -1), 0);
				Imgproc.dilate(erodeMat, dilateMat, kernelSize, new Point(-1, -1), 0);
				
				Mat edgesMat = new Mat();
				
				
				Imgproc.Canny(dilateMat, edgesMat, 0, 255);
				
				ArrayList<MatOfPoint> contoursList = new ArrayList<MatOfPoint>();
				Imgproc.findContours(edgesMat, contoursList, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
				
				int smallestDist = Integer.MAX_VALUE;
				Rect closestRect = null;
				//find contours
				for(int pos=0; pos< contoursList.size(); pos++){
					Rect rec = Imgproc.boundingRect(contoursList.get(pos));
					int xCoor = rec.x + (rec.width/2);
					int distFromCenter = Math.abs(xCoor-160);
					if(distFromCenter < smallestDist){
						smallestDist = distFromCenter;
						closestRect= rec;
					}
						  
				}
				if(closestRect != null){
				System.out.println(closestRect.x + ", " +closestRect.y);
				}

			}
		}

	}
}

class VideoFrame extends JPanel {
	
	JFrame frame;
	
	BufferedImage img;
	
	public VideoFrame() {
		frame = new JFrame();
		frame.setBounds(new Rectangle(0, 0, 244, 244));
		frame.add(this);
		
		frame.setVisible(true);
		
		new Timer(true).scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				repaint();
			}
		}, 0, 20);
	}
	
	public void setImg(BufferedImage img) {
	
		this.img = img;
		
	}
	
	public void paintComponent(Graphics g) {
		g.drawImage(img, 0, 0, null);
	}
}

