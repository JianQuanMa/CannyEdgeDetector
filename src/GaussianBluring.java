
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class GaussianBluring {
	// process the image and output the processed one
	public static BufferedImage process(BufferedImage img, int radius) {
		List<List<Double>> matrixGauss = GaussianBluring.gaussianMatrixOfRadius(radius);
		int size = matrixGauss.size();
		double[] horiMatrix = new double[size];
		double[] vertMatrix = new double[size];

		BufferedImage outputImage = new BufferedImage(img.getWidth(), img.getHeight(),
				BufferedImage.TYPE_INT_RGB);

		for (int i = 0; i < size; i++) {
			horiMatrix[i] = matrixGauss.get(size / 2).get(i);
			vertMatrix[i] = matrixGauss.get(i).get(size / 2);
		}

		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int rgb = pixelization(y, x, vertMatrix, img, true);
				outputImage.setRGB(x, y, rgb);
			}
		}

		for (int y = 0; y < outputImage.getHeight(); y++) {
			for (int x = 0; x < outputImage.getWidth(); x++) {
				outputImage.setRGB(x, y, pixelization(y, x, horiMatrix, outputImage, false));
			}
		}
		return outputImage;

	}

	private static int[] pixelizationPrep(int rgb, int[] sum, double[] matrixGauss, int index) {
		sum[0] += (char) Utility.findHexLiteral(rgb, 16) * matrixGauss[index];
		sum[1] += (char) Utility.findHexLiteral(rgb, 8) * matrixGauss[index];
		sum[2] += (char) Utility.findHexLiteral(rgb, 0) * matrixGauss[index];
		return sum;
	}

	// get the normalization of the matrix
	private static int[] normalization(double[] gaussMatrix, int[] sum) {
		int[] rgbArray = new int[3];
		double div = 0;
		for (int j = 0; j < gaussMatrix.length; j++) {
			div += gaussMatrix[j];
		}

		for (int k = 0; k < 3; k++) {
			rgbArray[k] = Utility.restrict((int) (sum[k] / div));
		}
		return rgbArray;
	}


	// change the pixel according to the orientation, with orientation
	// implying vertical, false implying horizontal
	public static int pixelization(int num1, int num2, double[] matrixGauss, BufferedImage img, boolean orientation) {
		int[] sum = new int[3];
		int sizeMatrix = matrixGauss.length;
		int x = orientation ? num1 : num2;
		int length = orientation ? img.getHeight() : img.getWidth();
		for (int p = x - sizeMatrix / 2, i = 0; p <= x + sizeMatrix / 2; p++, i++) {
			if (p >= 0 && p < length) {
				
				int rgb = orientation ? img.getRGB(num2, p) : img.getRGB(p, num1);
				sum = pixelizationPrep(rgb, sum, matrixGauss, i);
			}
		}
		sum = normalization(matrixGauss, sum);

		return Utility.findHexLiteral(sum[0], sum[1], sum[2]);
	}

	// get the Gaussian-matrix of the input radius
	public static List<List<Double>> gaussianMatrixOfRadius(int radius) {
		List<List<Double>> matrixGauss = new ArrayList<List<Double>>();
		int matrixSize = radius * 2 + 1;
		double sum = 0;

		for (int i = 0; i < matrixSize; i++) {
			List<Double> row = new ArrayList<Double>();
			matrixGauss.add(row);
		}

		int dev = (int) Math.ceil(radius * radius / (2 * Math.log(Integer.MAX_VALUE / (matrixSize * 255))));
		double x;
		for (int row = 0; row < matrixSize; row++) {
			for (int column = 0; column < matrixSize; column++) {
				x = gaussian(row, radius, dev) * gaussian(column, radius, dev);
				matrixGauss.get(row).add(x);
				sum += x;
			}
		}

		for (int row = 0; row < matrixSize; row++) {
			for (int col = 0; col < matrixSize; col++) {
				matrixGauss.get(row).add(matrixGauss.get(row).get(col) / sum);
			}
		}
		return matrixGauss;
	}

	// calculate the gaussian
	public static double gaussian(double x, double radius, double dev) {
		return Math.exp(-(((x - radius) / dev) * ((x - radius) / dev)) / 2.0);
	}
}