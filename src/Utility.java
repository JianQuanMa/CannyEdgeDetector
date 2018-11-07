public class Utility {
	// return the hex literal of the rgb
	public static int findHexLiteral(int r, int g, int b) {
		return (r << 16) + (g << 8) + b;
	}

	// return the hex literal of one color of the rgb
	public static int findHexLiteral(int rgb, int bitPower) {
		return ((rgb >> bitPower) & 0xff);
	}

	// clamp it at [0,255]
	public static int restrict(int i) {
		i = i < 0 ? 0 : i;
		return (i > 255 ? 255 : i);
	}

	// input a number and a list of numbers, set the input number to the element in
	// the numbers that it is the closest to
	public static int retify(double input, int[] magnets) {
		int closest = Integer.MAX_VALUE;
		for (int i = 0; i < magnets.length; i++) {
			closest = Math.abs(closest - input) > Math.abs(magnets[i] - input) ? magnets[i] : closest;
		}
		return closest;
	}

}