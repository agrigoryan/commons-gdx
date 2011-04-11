package com.gemserk.commons.artemis.systems;

import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;

public class SpriteUpdateSystem extends EntitySystem {

	@SuppressWarnings("unchecked")
	public SpriteUpdateSystem() {
		super(SpatialComponent.class, SpriteComponent.class);
	}

	@Override
	protected void processEntities(ImmutableBag<Entity> entities) {
		for (int i = 0; i < entities.size(); i++) {

			Entity entity = entities.get(i);

			SpatialComponent spatialComponent = entity.getComponent(SpatialComponent.class);
			SpriteComponent spriteComponent = entity.getComponent(SpriteComponent.class);

			Vector2 position = spatialComponent.getPosition();
			Vector2 size = spatialComponent.getSize();

			Sprite sprite = spriteComponent.getSprite();

			sprite.setRotation(spatialComponent.getAngle());
			sprite.setOrigin(size.x / 2, size.y / 2);
			sprite.setSize(size.x, size.y);
			sprite.setPosition(position.x - size.x / 2, position.y - size.y / 2);

		}
	}

	@Override
	public void initialize() {

	}

	@Override
	protected boolean checkProcessing() {
		return true;
	}
}