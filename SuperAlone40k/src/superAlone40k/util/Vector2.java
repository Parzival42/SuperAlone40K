package superAlone40k.util;

public class Vector2 {
	public double x;
	public double y;
	
	public Vector2() {
		this.x = 0;
		this.y = 0;
	}

	public Vector2(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vector2 add(final Vector2 other) {
		x += other.x;
		y += other.y;
		return this;
	}
	
	public Vector2 mulAdd(final Vector2 other, double scale) {
		return add(other.copy().scale(scale));
	}

	public Vector2 sub(final Vector2 other) {
		x -= other.x;
		y -= other.y;
		return this;
	}

	public Vector2 scale(double scale) {
		x *= scale;
		y *= scale;
		return this;
	}

	public double length() {
		return Math.sqrt(x * x + y * y);
	}

	public double length2() {
		return (x * x) + (y * y);
	}

	public Vector2 normalize() {
		final double length = length();
		x /= length;
		y /= length;
		return this;
	}

	public Vector2 setAngle(double radians) {
		x = Math.cos(radians);
		y = -Math.sin(radians);
		return this;
	}

	public double getAngle() {
		return Math.atan2(x, -y);
	}

	public double dot(Vector2 other) {
		return (x * other.x) + (y * other.y);
	}

	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vector2 copy() {
		return new Vector2(x, y);
	}
	
	public static double distance(Vector2 first, Vector2 second) {
		return Math.sqrt((second.x - first.x) * (second.x - first.x) + (second.y - first.y) * (second.y - first.y));
	}

	public static double distance2(Vector2 first, Vector2 second) {
		return (second.x - first.x) * (second.x - first.x) + (second.y - first.y) * (second.y - first.y);
	}

	public static Vector2 lerp(Vector2 start, Vector2 end, double percent) {
		return start.copy().add(end.copy().sub(start).scale(percent));
	}
	
	public Vector2 rotateBy(double radians) {
		final double sin = Math.sin(radians);
		final double cos = Math.cos(radians);
		
		final double tx = x;
		final double ty = y;
		
		x = (cos * tx) - (sin * ty);
		y = (sin * tx) + (cos * ty);
		
		normalize();
		return this;
	}
	
	@Override
	public String toString() {
		return "X: " + x + ", Y: " + y;
	}
}