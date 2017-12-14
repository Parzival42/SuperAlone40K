package superAlone40k.ecs;

public enum SystemBitmask {
	// Use always powers of two!
	INPUT(1),
	COLLIDER_SORTING(2),
	MOVEMENT_SYSTEM(4),
	LIGHT_SYSTEM(8),
	TRIGGER_SYSTEM(16),
	LIFETIME_SYSTEM(32),
	CLEANUP_SYSTEM(64),
	PLATFORM_MOVEMENT_SYSTEM(128);
	
	private final int systemMask;

	private SystemBitmask(int systemMask) {
		this.systemMask = systemMask;
	}

	public int getSystemMask() {
		return systemMask;
	}
}