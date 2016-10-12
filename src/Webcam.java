
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

public class Webcam {
	
	private VideoCapture camera;
	
	private Size imageRes;
	
	private Size imageSize;

	public Webcam(String filename) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		camera = new VideoCapture();
		
		if (camera.isOpened()) {
			camera.release();
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		camera.open(filename);
		
		Mat pic = getSampleMat();
		
		imageSize = pic.size();
		
		imageRes = pic.size();

		System.out.println("Camera Ready");
	}
	
	public Webcam(String filename, Size imageSize, Size imageRes) {
		this(filename);
		
		this.imageSize = imageSize;
		
		this.imageRes = imageRes;
		
	}
	
	public Size getImageRes() {
		return imageRes;
	}
	
	private Mat getSampleMat() {
		Mat frame = new Mat();

		camera.read(frame);
		
		return frame;
	}
	
	public Mat getMat() {
		Mat frame = new Mat();

		camera.read(frame);
		
		Mat newFrame = new Mat();
		
		Imgproc.resize(frame, newFrame, imageRes);
		
		Imgproc.resize(newFrame, frame, imageSize);
		
		return frame;
	}
	
	public Size getImageSize() {
		return imageSize;
	}
	
	public void shutoff() {
		camera.release();
	}
	
	public void changeRes(Size newRes) {
		imageRes = newRes;
	}
}