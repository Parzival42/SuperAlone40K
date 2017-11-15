package superAlone40k.ecs;

public enum EntityIndex {
	ENTITY_TYPE_ID(0),
	SYSTEM_MASK(1),
	POSITION_X(2),
	POSITION_Y(3),
	EXTENT_X(4),		// Extents from the center to both sides
	EXTENT_Y(5),
	COLOR_R(6),
	COLOR_G(7),
	COLOR_B(8),
	COLOR_A(9),
	AABB_CENTER_X(10),
	AABB_CENTER_Y(11),
	AABB_EXTENT_X(12),
	AABB_EXTENT_Y(13),
	COLLISION_TYPE(14),	// Static/Dynamic
	VELOCITY_X(15),
	VELOCITY_Y(16),
	GRAVITATION_INFLUENCE(17),
	DRAG(18),
	
	// Border rays for screen shadow collision
	BORDER_ORIGIN_X(19),
	BORDER_ORIGIN_Y(20),
	BORDER_DIR_X(21),
	BORDER_DIR_Y(22),

	//trigger system box
	TRIGGER_POSITION_X(23),
	TRIGGER_POSITION_Y(24),
	TRIGGER_EXTENT_X(25),
	TRIGGER_EXTENT_Y(26);
	
	private final int index;
	EntityIndex(final int index) {
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}
}