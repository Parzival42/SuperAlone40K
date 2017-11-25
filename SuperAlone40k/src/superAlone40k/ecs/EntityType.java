package superAlone40k.ecs;

public enum EntityType {
	// Use always powers of two!
	NONE(0),
	LIGHT(1),
	SCREEN_BORDER(2),
	BOX_SHADOW(4),
	RAIN_DROP_SPLATTER(8),
	RAIN_DROP(16),
	PLAYER(32),
	BULLET(64),
	CHECKPOINT(128);
	
	final int entityType;
	private EntityType(final int entityType) {
		this.entityType = entityType;
	}
	
	public int getEntityType() {
		return entityType;
	}
}
