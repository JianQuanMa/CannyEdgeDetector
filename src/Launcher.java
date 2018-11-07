import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Launcher {

	public static void main(String[] args) {
		CannyEdgeDetector ced = new CannyEdgeDetector();

		BufferedImage input = null;
		try {
			input = ImageIO.read(new File("example.jpg"));
			File outputfile = new File("result.jpg");
			ImageIO.write(ced.process(input, 5, 50, 4), "jpg", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
