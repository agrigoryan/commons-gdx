package com.gemserk.commons.artemis.systems;

import com.artemis.Entity;
import com.artemis.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.commons.artemis.components.AntiGravityComponent;
import com.gemserk.commons.artemis.components.LinearVelocityLimitComponent;
import com.gemserk.commons.artemis.components.PhysicsComponent;

public class PhysicsSystem extends EntityProcessingSystem implements ActivableSystem {

	private static final Vector2 antiGravity = new Vector2(0, 10f);

	private ActivableSystem activableSystem = new ActivableSystemImpl();

	private final Vector2 bodyAntiGravity = new Vector2(0, 10f);

	World physicsWorld;

	private PhysicsContactListener physicsContactListener;

	public PhysicsSystem(World physicsWorld) {
		super(PhysicsComponent.class);
		this.physicsWorld = physicsWorld;
		physicsContactListener = new PhysicsContactListener();
	}

	@Override
	protected void begin() {
		physicsWorld.step(Gdx.app.getGraphics().getDeltaTime(), 3, 3);
	}

	@Override
	protected void process(Entity e) {

		// synchronize sizes between spatial and physics components.

		PhysicsComponent physicsComponent = e.getComponent(PhysicsComponent.class);
		Body body = physicsComponent.getBody();

		AntiGravityComponent antiGravityComponent = e.getComponent(AntiGravityComponent.class);
		if (antiGravityComponent != null) {

			bodyAntiGravity.set(antiGravity);
			bodyAntiGravity.mul(body.getMass());

			body.applyForce(bodyAntiGravity, body.getTransform().getPosition());
		}

		LinearVelocityLimitComponent limitComponent = e.getComponent(LinearVelocityLimitComponent.class);
		if (limitComponent != null) {
			Vector2 linearVelocity = body.getLinearVelocity();

			float speed = linearVelocity.len();
			float maxSpeed = limitComponent.getLimit();

			if (speed > maxSpeed) {
				float factor = maxSpeed / speed;
				linearVelocity.mul(factor);
				body.setLinearVelocity(linearVelocity);
			}
		}

	}

	@Override
	protected boolean checkProcessing() {
		return isEnabled();
	}

	@Override
	protected void removed(Entity e) {

		// on entity removed, we should remove body from physics world

		PhysicsComponent component = e.getComponent(PhysicsComponent.class);

		if (component == null) {
			return;
		}

		Body body = component.getBody();
		body.setUserData(null);

		com.gemserk.commons.gdx.box2d.Contact contact = component.getContact();

		// removes contact from the other entity
		for (int i = 0; i < contact.getContactCount(); i++) {
			if (!contact.isInContact(i))
				continue;

			Body otherBody = contact.getBody(i);
			if (otherBody == null)
				continue;

			Entity otherEntity = (Entity) otherBody.getUserData();
			if (otherEntity == null)
				continue;

			PhysicsComponent otherPhyiscsComponent = otherEntity.getComponent(PhysicsComponent.class);
			otherPhyiscsComponent.getContact().removeContact(body);
		}

		physicsWorld.destroyBody(body);

	}

	@Override
	public void initialize() {
		physicsWorld.setContactListener(physicsContactListener);
	}

	public World getPhysicsWorld() {
		return physicsWorld;
	}

	public void toggle() {
		activableSystem.toggle();
	}

	public boolean isEnabled() {
		return activableSystem.isEnabled();
	}

}
