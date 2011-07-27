package com.gemserk.commons.artemis.components;

import com.artemis.Component;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public class SpriteComponent extends Component {

	private Sprite sprite;

	private int layer;

	private Color color;

	private Vector2 center; // x and y values between 0,1

	public Sprite getSprite() {
		return sprite;
	}

	// Used right now to set an animation frame, another option could be to implement a common interface which returns different sprite on getSprite().
	public void setSprite(Sprite sprite) {
		this.sprite.set(sprite);
	}

	public int getLayer() {
		return layer;
	}

	public Vector2 getCenter() {
		return center;
	}

	public Color getColor() {
		return color;
	}

	public SpriteComponent(Sprite sprite, int layer, Vector2 center, Color color) {
		this.sprite = sprite;
		this.layer = layer;
		this.color = new Color(color);
		this.center = center;
	}

	public SpriteComponent(Sprite sprite, int layer, Color color) {
		this(sprite, layer, new Vector2(0.5f, 0.5f), color);
	}

	public SpriteComponent(Sprite sprite, int layer) {
		this(sprite, layer, Color.WHITE);
	}

}
