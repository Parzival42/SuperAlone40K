package superAlone40k.ecs;

public enum EntityType {
	LIGHT(0),
	SCREEN_BORDER(1),
	BOX_SHADOW(2),
	RAIN_DROP_SPLATTER(4), 
	RAIN_DROP(8),
	PLAYER(16);
	
	final int entityType;
	private EntityType(final int entityType) {
		this.entityType = entityType;
	}
	
	public int getEntityType() {
		return entityType;
	}
}
