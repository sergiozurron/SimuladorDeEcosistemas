package simulator.misc;

import java.util.Random;

public class Utils {
	public static final Random randomGenerator = new Random();

	// So that there is no instance of this class
	private Utils() {
	}

	public static double constrainValueInRange(double value, double min, double max) {
		assert (max >= min);
		value = Math.min(value, max);
		value = Math.max(value, min);
		return value;
	}

	public static int constrainValueInRange(int value, int min, int max) {
		assert (max >= min);
		value = Math.min(value, max);
		value = Math.max(value, min);
		return value;
	}

	public static double getRandomizedParameter(double value, double tolerance) throws IllegalArgumentException {
		if (tolerance <= 0 || tolerance > 1)
			throw new IllegalArgumentException("Invalid tolerance value");
		double t = (randomGenerator.nextDouble() - 0.5) * 2 * tolerance;
		return value * (1 + t);
	}

	public static Vector2D getRandomPosition(double width, double height) {
		return new Vector2D(randomGenerator.nextDouble(width), randomGenerator.nextDouble(height));
	}

}
