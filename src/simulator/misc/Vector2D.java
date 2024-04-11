package simulator.misc;

import org.json.JSONArray;

public class Vector2D {

	double x;
	double y;

	// create the zero vector
	public Vector2D() {
		x = y = 0.0;
	}

	// copy constructor
	public Vector2D(Vector2D v) {
		x = v.x;
		y = v.y;
	}

	// create a vector from an array
	public Vector2D(double x, double y) {
		this.x = x;
		this.y = y;
	}

	// return the inner product of this Vector a and b
	public double dot(Vector2D that) {
		return x * that.x + y * that.y;
	}

	// return the length of the vector
	public double magnitude() {
		return Math.sqrt(dot(this));
	}

	// return the distance between this and that
	public double distanceTo(Vector2D that) {
		return minus(that).magnitude();
	}

	// create and return a new object whose value is (this + that)
	public Vector2D plus(Vector2D that) {
		return new Vector2D(x + that.x, y + that.y);
	}

	// create and return a new object whose value is (this - that)
	public Vector2D minus(Vector2D that) {
		return new Vector2D(x - that.x, y - that.y);
	}

	// return the corresponding coordinate
	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	// create and return a new object whose value is (this * factor)
	public Vector2D scale(double factor) {
		return new Vector2D(x * factor, y * factor);
	}

	// return the corresponding unit vector
	public Vector2D direction() {
		if (magnitude() > 0.0)
			return scale(1.0 / magnitude());
		else
			return new Vector2D(this);
	}

	public Vector2D rotate(int deg) {

		assert (deg >= -180.0 && deg <= 180.0);

		double angle = deg * Math.PI / 180.0;
		double sine = Math.sin(angle);
		double cosine = Math.cos(angle);

		Vector2D r = new Vector2D();

		r.x = cosine * x + (-sine) * y;
		r.y = sine * x + cosine * y;

		return r;
	}

	public double angle(Vector2D v) {
		double a2 = Math.atan2(v.getX(), v.getY());
		double a1 = Math.atan2(x, y);
		double angle = a1 - a2;
		double k = a1 > a2 ? -2.0 * Math.PI : 2.0 * Math.PI;
		angle = (Math.abs(k + angle) < Math.abs(angle)) ? k + angle : angle;
		return angle * 180.0 / Math.PI;
	}

	public static Vector2D getRandomVector(double min, double max) {
		assert (max >= min);
		double x = min + Utils.randomGenerator.nextDouble(max - min);
		double y = min + Utils.randomGenerator.nextDouble(max - min);
		assert (x >= min && x <= max);
		assert (y >= min && y <= max);
		return new Vector2D(x, y);
	}

	public JSONArray asJSONArray() {
		JSONArray a = new JSONArray();
		a.put(x);
		a.put(y);
		return a;
	}

	public Vector2D adjust(int width, int height) {
		double xNew = this.x;
		double yNew = this.y;

		while (xNew > width)
			xNew = (xNew - width);
		while (xNew < 0)
			xNew = (xNew + width);

		while (yNew > height)
			yNew = (yNew - height);
		while (yNew < 0)
			yNew = (yNew + height);

		return new Vector2D(xNew, yNew);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vector2D other = (Vector2D) obj;
		return (Double.doubleToLongBits(x) == Double.doubleToLongBits(other.x)
				&& Double.doubleToLongBits(y) == Double.doubleToLongBits(other.y));
	}

	// return a string representation of the vector
	public String toString() {
		return "[" + x + "," + y + "]";
	}
}
