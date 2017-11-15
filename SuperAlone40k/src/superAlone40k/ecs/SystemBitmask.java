package superAlone40k.ecs;

public enum SystemBitmask {
	// Use always powers of two!
	HORIZONTAL_MOVEMENT(1),
	VERTICAL_MOVEMENT(2),
	INPUT(4),
	COLLIDER_SORTING(8),
	MOVEMENT_SYSTEM(16),
	LIGHT_SYSTEM(32),
	TRIGGER_SYSTEM(64);
	
	private final int systemMask;

	private SystemBitmask(int systemMask) {
		this.systemMask = systemMask;
	}

	public int getSystemMask() {
		return systemMask;
	}
}