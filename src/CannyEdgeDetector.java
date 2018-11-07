import java.awt.image.BufferedImage;

public class CannyEdgeDetector {
    private double thresholdHigh;
    private double thresholdLow;
    private int[][] gradientDirection;
    private int[][] gradientX;
    private int[][] gradientY;
    private double[][] directionMask;


	public BufferedImage process(BufferedImage srcImage, double thresholdHigh, double thresholdLow, int radius) {
		int[][] greyMatrix;
        BufferedImage resultImage = null;
		this.thresholdHigh = thresholdHigh;
		this.thresholdLow = thresholdLow;

		srcImage = GaussianBluring.process(srcImage, radius);
		greyMatrix = this.greyitize(srcImage);
		gradientX = this.sobelKernel(greyMatrix, false);
		gradientY = this.sobelKernel(greyMatrix, true);
		// calculate the direction mask
		this.calDirectionMask();
		// calculate the direction
		this.calDirection();
		// apply suppression
		this.suppress();
		// get the result image
		resultImage = convertToRGB(calHysteresis());
        return resultImage;
    }

//	// convert to grey
	public int[][] greyitize(BufferedImage img) {

		int height = img.getHeight();
		int width = img.getWidth();
		int[][] matrix = new int[height][width];

		for (int x = 0; x < height; x++) {
			for (int y = 0; y < width; y++) {
				int rgb = img.getRGB(y, x);
				long grey = Math.round((Utility.findHexLiteral(rgb, 16)
						+ (Utility.findHexLiteral(rgb, 8) + (Utility.findHexLiteral(rgb, 0)))) / 3.0);
				matrix[x][y] = (int) grey;

			}
		}
		return matrix;
	}

//	public int[][] greyitize(BufferedImage img) {
//		int width = img.getWidth();
//		int height = img.getHeight();
//		int[][] matrix = new int[height][width];
//		for (int x = 0; x < height; ++x)
//			for (int y = 0; y < width; ++y) {
//				int rgb = img.getRGB(y, x);
//				int r = (rgb >> 16) & 0xFF;
//				int g = (rgb >> 8) & 0xFF;
//				int b = (rgb & 0xFF);
//				int greyLevel = (int) (0.2126 * r + 0.7152 * g + 0.0722 * b) / 3;
//				int grey = (greyLevel << 16) + (greyLevel << 8) + greyLevel;
//				matrix[x][y] = grey;
//			}
//		return matrix;
//	}

	// convert to rgb
    public BufferedImage convertToRGB(int[][] gray) {
		BufferedImage resultImage = null;
        int height = gray.length;
        int width = gray[0].length;

		resultImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int rgb = gray[i][j];
				resultImage.setRGB(j, i, Utility.findHexLiteral(rgb, rgb, rgb));
			}
        }
        return resultImage;
    }

	// calculate the direction mask
    private void calDirectionMask() {
        int height = gradientX.length;
        int width = gradientX[0].length;
        directionMask = new double[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                directionMask[i][j] = Math.sqrt(gradientX[i][j] * gradientX[i][j] + gradientY[i][j] * gradientY[i][j]);
            }
        }
    }

	// calculate the direction
    private void calDirection() {
		gradientDirection = new int[gradientX.length][gradientX[0].length];

		for (int i = 0; i < gradientX.length; i++) {
			for (int j = 0; j < gradientX[0].length; j++) {
				double angle = Math.atan2(gradientY[i][j], gradientX[i][j]) * (180 / Math.PI);
                if (angle < 0) {
					angle += 360;
                }
				// to determine what direction the edge is heading toward
				gradientDirection[i][j] = Utility.retify(angle, new int[] { 0, 45, 90, 135 });
            }
        }
    }

	// apply non-maximum suppression
    private void suppress() {
        int height = directionMask.length - 1;
        int width = directionMask[0].length - 1;

        for (int i = 1; i < height; i++) {
            for (int j = 1; j < width; j++) {
                double magnitude = directionMask[i][j];
				int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
                switch (gradientDirection[i][j]) {
                    case 0 :
					x1 = i;
					y1 = j - 1;
					x2 = i;
					y2 = j + 1;
                        break;
                    case 45 :
					x1 = i - 1;
					y1 = j + 1;
					x2 = i + 1;
					y2 = j - 1;
                        break;
                    case 90 :
					x1 = i - 1;
					y1 = j;
					x2 = i + 1;
					y2 = j;
                        break;
                    case 135 :
					x1 = i - 1;
					y1 = j - 1;
					x2 = i + 1;
					y2 = j + 1;
                        break;
                }
				if (magnitude < directionMask[x1][y1] && magnitude < directionMask[x2][y2]) {
					directionMask[i - 1][j - 1] = 0;
				}
            }
        }
    }

	// apply hysteresis
    private int[][] calHysteresis() {
        int height = directionMask.length - 1;
        int width = directionMask[0].length - 1;
        int[][] output = new int[height - 1][width - 1];
		double magnitude;
        for (int i = 1; i < height; i++) {
            for (int j = 1; j < width; j++) {
				magnitude = directionMask[i][j];

                if (magnitude >= thresholdHigh) {
                    output[i - 1][j - 1] = 255;
                } else if (magnitude < thresholdLow) {
                    output[i - 1][j - 1] = 0;
                } else {
					output[i - 1][j - 1] = (this.isFlag(i, j)) ? 255 : 0;
                }
            }
        }
        return output;
    }

	private boolean isFlag(int i, int j) {
		for (int nr = -1; nr < 2; nr++) {
			for (int nc = -1; nc < 2; nc++) {
				if (directionMask[i + nr][j + nc] >= thresholdHigh) {
					return true;
				}
			}
		}
		return false;
	}

	// apply Sobel filtering and return the filtered matrix
	private int[][] sobelKernel(int[][] matrix, boolean orientation) {
		int height = matrix.length;
		int width = matrix[0].length;
		int[][] result = new int[height - 2][width - 2];
		// filtering of the matrix
		for (int i = 1; i < height - 1; i++) {
			for (int j = 1; j < width - 1; j++) {
				this.filtering(orientation, matrix, i, j, result);
			}
		}
		return result;
	}

	// filtering of an entry
	public void filtering(boolean orientation, int[][] matrix, int i, int j, int[][] result) {
		final int[][] horizontalMask = { { -1, 0, 1 }, { -2, 0, 2 }, { -1, 0, 1 } };
		final int[][] verticalMask = { { -1, -2, -1 }, { 0, 0, 0 }, { 1, 2, 1 } };
		int[][] mask = orientation ? verticalMask : horizontalMask;
		int sum = 0;
		for (int kr = -1; kr < 2; kr++) {
			for (int kc = -1; kc < 2; kc++) {
				sum += (mask[kr + 1][kc + 1] * matrix[i + kr][j + kc]);
			}
			result[i - 1][j - 1] = sum;
		}
	}
}