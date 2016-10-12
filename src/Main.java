import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class Main {
	public static void main(String[] args) {
		
		NetworkTable.setClientMode();
		NetworkTable.setIPAddress("10.24.85.1");
		NetworkTable table = NetworkTable.getTable("targetCenter");
		while (true) {
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			double x = 24, y = 244;
			table.putNumber("x", x);
			table.putNumber("y", y);
		}
	}
}
