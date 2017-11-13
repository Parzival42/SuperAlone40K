package superAlone40k.util;

public class Ray implements Comparable<Ray> {
	// Public -> Save getter and setter
	public final Vector2 origin;
	public final Vector2 direction;
	
	public Vector2 hitPoint = null;
	
	public Ray(Vector2 origin, Vector2 direction) {
		this.origin = origin;
		this.direction = direction;
		this.direction.normalize();
	}
	
	public boolean isValidHit() {
		return hitPoint == null ? false : true;
	}
	
	public void updateHitInformation(Vector2 hit) {
		if(hitPoint == null) {
			hitPoint = hit;
		} else {
			final double currentOriginDistance = Vector2.distance2(hitPoint, origin);
			final double newOriginDistance = Vector2.distance2(hit, origin);
			
			// Only update hit point if the new distance is smaller
			if(newOriginDistance < currentOriginDistance) {
				hitPoint = hit;
			}
		}
	}
	
	public Vector2 collideWith(Ray other, boolean ignoreBackCollision) {
		Vector2 otherEndpoint = other.origin.copy().add(other.direction);
		Vector2 originalEndpoint = origin.copy().add(direction);	// TODO: Length ?
		
		// Other line in form of Ax + By = C
		final double[] otherLine = new double[] {otherEndpoint.y - other.origin.y, other.origin.x - otherEndpoint.x, 0};
		otherLine[2] = otherLine[0] * other.origin.x + otherLine[1] * otherEndpoint.y;
		
		// Original line in form of Ax + By = C
		final double[] originalLine = new double[] {originalEndpoint.y - origin.y, origin.x - originalEndpoint.x, 0};
		originalLine[2] = originalLine[0] * origin.x + originalLine[1] * originalEndpoint.y;
		
		final double determinant = otherLine[0] * originalLine[1] - originalLine[0] * otherLine[1];
//		System.out.println(determinant);
		
		if(ignoreBackCollision && determinant < 0) {
			return null;
		}
		
		if(Math.abs(determinant) > 0.0000000001) {
			return new Vector2(
					(originalLine[1] * otherLine[2] - otherLine[1] * originalLine[2]) / determinant,
					(otherLine[0] * originalLine[2] - originalLine[0] * otherLine[2]) / determinant);
		}
		return null;
	}

	@Override
	public int compareTo(Ray other) {
		return Double.compare(direction.getAngle(), other.direction.getAngle());
	}
}