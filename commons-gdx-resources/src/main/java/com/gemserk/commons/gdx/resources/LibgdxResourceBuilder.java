package com.gemserk.commons.gdx.resources;

import java.util.List;

import org.w3c.dom.Document;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.gemserk.animation4j.FrameAnimationImpl;
import com.gemserk.animation4j.gdx.Animation;
import com.gemserk.commons.gdx.graphics.ParticleEmitterUtils;
import com.gemserk.commons.gdx.resources.dataloaders.DisposableDataLoader;
import com.gemserk.commons.gdx.resources.dataloaders.MusicDataLoader;
import com.gemserk.commons.gdx.resources.dataloaders.SoundDataLoader;
import com.gemserk.commons.gdx.resources.dataloaders.TextureDataLoader;
import com.gemserk.commons.svg.inkscape.DocumentParser;
import com.gemserk.resources.Resource;
import com.gemserk.resources.ResourceManager;
import com.gemserk.resources.dataloaders.DataLoader;

public class LibgdxResourceBuilder {

	// TODO: Define folders for each type of resource?

	protected ResourceManager<String> resourceManager;

	private boolean cacheWhenLoad = false;

	public void setCacheWhenLoad(boolean cacheWhenLoad) {
		this.cacheWhenLoad = cacheWhenLoad;
	}

	public LibgdxResourceBuilder(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public FileHandle internal(String file) {
		return Gdx.files.internal(file);
	}

	public FileHandle absolute(String file) {
		return Gdx.files.absolute(file);
	}

	public void texture(String id, String file) {
		texture(id, internal(file), true);
	}

	public void texture(String id, String file, boolean linearFilter) {
		texture(id, internal(file), linearFilter);
	}

	public void texture(String id, FileHandle fileHandle, boolean linearFilter) {
		resourceManager.add(id, new TextureDataLoader(fileHandle, linearFilter));
		if (cacheWhenLoad)
			resourceManager.get(id).get();
	}

	/**
	 * Registers a texture atlas in resources manager.
	 * 
	 * @param id
	 *            The id of the resource.
	 * @param file
	 *            The libgdx internal file path to create the texture atlas.
	 */
	public void textureAtlas(String id, String file) {
		resourceManager.add(id, new DisposableDataLoader<TextureAtlas>(Gdx.files.internal(file)) {
			@Override
			public TextureAtlas load() {
				return new TextureAtlas(fileHandle);
			}
		});
	}

	/**
	 * registers a new sprite resource builder returning a new sprite each time it is called.
	 */
	public void sprite(String id, final String textureId) {
		resourceManager.addVolatile(id, new DataLoader<Sprite>() {
			@Override
			public Sprite load() {
				Resource<Texture> texture = resourceManager.get(textureId);
				return new Sprite(texture.get());
			}
		});
	}

	/**
	 * registers a new sprite resource builder returning a new sprite each time it is called.
	 */
	public void sprite(String id, final String textureId, final int x, final int y, final int width, final int height) {
		resourceManager.addVolatile(id, new DataLoader<Sprite>() {
			@Override
			public Sprite load() {
				Resource<Texture> texture = resourceManager.get(textureId);
				return new Sprite(texture.get(), x, y, width, height);
			}
		});
	}

	/**
	 * Registers with id a new Sprite based on a TextureAtlas region with the same name.
	 * 
	 * @param id
	 *            The identifier of the Resource to register, also the texture region name inside the TextureAtlas.
	 * @param textureAtlasId
	 *            the TextureAtlas resource identifier.
	 */
	public void spriteAtlas(final String id, final String textureAtlasId, final String regionId) {
		resourceManager.addVolatile(id, new DataLoader<Sprite>() {

			private AtlasRegion region = null;

			@Override
			public Sprite load() {
				TextureAtlas textureAtlas = resourceManager.getResourceValue(textureAtlasId);
				if (region == null) {
					region = textureAtlas.findRegion(regionId);
					if (region == null)
						throw new RuntimeException("Failed to create Sprite resource " + id + " from region " + regionId + " from texture atlas " + textureAtlasId);
				}
				return new Sprite(region);
			}
		});
	}

	public void animation(final String id, final String textureAtlasId, final String prefix, final boolean loop, final int time, final int... times) {
		resourceManager.addVolatile(id, new DataLoader<Animation>() {

			List<Sprite> sprites = null;

			@Override
			public Animation load() {
				TextureAtlas textureAtlas = resourceManager.getResourceValue(textureAtlasId);

				if (sprites == null) {
					try {
						sprites = textureAtlas.createSprites(prefix);
					} catch (GdxRuntimeException e) {
						throw new RuntimeException("Failed to create animation " + id + " from texture atlas " + textureAtlasId, e);
					}
				}

				Sprite[] frames = new Sprite[sprites.size()];
				for (int i = 0; i < frames.length; i++) {
					frames[i] = new Sprite(sprites.get(i));
				}

				int framesCount = frames.length;

				float[] newTimes = new float[framesCount - 1];
				int lastTime = time;

				// added convert from int time in milliseconds to float time in seconds

				for (int i = 0; i < framesCount - 1; i++) {
					if (i < times.length) {
						newTimes[i] = ((float) times[i]) * 0.001f;
						lastTime = times[i];
					} else
						newTimes[i] = ((float) lastTime) * 0.001f;
				}

				FrameAnimationImpl frameAnimation = new FrameAnimationImpl(0.001f * (float) time, newTimes);
				frameAnimation.setLoop(loop);

				return new Animation(frames, frameAnimation);

			}

		});
	}

	public void animation(String id, final String spriteSheetId, final int x, final int y, final int w, final int h, final int framesCount, //
			final boolean loop, final int time, final int... times) {
		resourceManager.addVolatile(id, new DataLoader<Animation>() {

			@Override
			public Animation load() {
				Texture spriteSheet = resourceManager.getResourceValue(spriteSheetId);
				Sprite[] frames = new Sprite[framesCount];

				int xOffset = 0;
				int yOffset = 0;

				for (int i = 0; i < frames.length; i++) {
					frames[i] = new Sprite(spriteSheet, x + xOffset, y + yOffset, w, h);

					xOffset += w;

					if (xOffset >= spriteSheet.getWidth()) {
						yOffset += h;
						xOffset = 0;
					}
				}

				float[] newTimes = new float[framesCount - 1];
				int lastTime = time;

				// added convert from int time in milliseconds to float time in seconds

				for (int i = 0; i < framesCount - 1; i++) {
					if (i < times.length) {
						newTimes[i] = ((float) times[i]) * 0.001f;
						lastTime = times[i];
					} else
						newTimes[i] = ((float) lastTime) * 0.001f;
				}

				FrameAnimationImpl frameAnimation = new FrameAnimationImpl(0.001f * (float) time, newTimes);
				frameAnimation.setLoop(loop);

				return new Animation(frames, frameAnimation);
			}

		});
	}

	public void font(String id, String imageFile, String fontFile) {
		font(id, imageFile, fontFile, false);
	}

	public void font(String id, final String imageFile, final String fontFile, final boolean linearFilter) {
		resourceManager.add(id, new DisposableDataLoader<BitmapFont>(internal(imageFile)) {
			@Override
			public BitmapFont load() {
				Texture texture = new Texture(internal(imageFile));
				if (linearFilter)
					texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
				return new BitmapFont(internal(fontFile), new Sprite(texture), false);
			}
		});
	}

	public void sound(String id, String file) {
		sound(id, internal(file));
	}

	public void sound(String id, FileHandle fileHandle) {
		resourceManager.add(id, new SoundDataLoader(fileHandle));
		if (cacheWhenLoad)
			resourceManager.get(id).get();
	}

	public void music(String id, String file) {
		resourceManager.add(id, new MusicDataLoader(internal(file)));
	}

	public void xmlDocument(String id, final String file) {
		resourceManager.add(id, new DataLoader<Document>() {
			@Override
			public Document load() {
				return new DocumentParser().parse(internal(file).read());
			}
		});
	}

	public void particleEffect(String id, final String effectFile, final String imagesDir) {
		resourceManager.add(id, new DataLoader<ParticleEffect>() {
			@Override
			public ParticleEffect load() {
				ParticleEffect particleEffect = new ParticleEffect();
				particleEffect.load(Gdx.files.internal(effectFile), Gdx.files.internal(imagesDir));
				return particleEffect;
			}

			@Override
			public void unload(ParticleEffect t) {
				t.dispose();
			}
		});
	}

	public void particleEmitter(String id, final String particleEffectId, final String particleEmitterId) {
		this.particleEmitter(id, particleEffectId, particleEmitterId, 1f);
	}

	public void particleEmitter(String id, final String particleEffectId, final String particleEmitterId, final float scale) {
		resourceManager.addVolatile(id, new DataLoader<ParticleEmitter>() {

			private ParticleEmitter cachedEmitter;

			@Override
			public ParticleEmitter load() {
				ParticleEffect particleEffect = resourceManager.getResourceValue(particleEffectId);
				if (cachedEmitter == null) {
					cachedEmitter = particleEffect.findEmitter(particleEmitterId);
					ParticleEmitterUtils.scaleEmitter(cachedEmitter, scale);
				}
				return new ParticleEmitter(cachedEmitter);
			}
		});
	}

	// / TESTING STUFF

	@SuppressWarnings({ "rawtypes" })
	public void resource(final String id, final ResourceBuilder resourceBuilder) {

		DataLoader dataLoader = new DataLoader() {
			@Override
			public Object load() {
				try {
					return resourceBuilder.build();
				} catch (Exception e) {
					throw new RuntimeException("Failed to load resource " + id, e);
				}
			}

			@Override
			public void unload(Object data) {
				if (data instanceof Disposable) {
					((Disposable) data).dispose();
				}
			}
		};

		if (resourceBuilder.isVolatile())
			resourceManager.addVolatile(id, dataLoader);
		else
			resourceManager.add(id, dataLoader);

	}

	public SpriteResourceBuilder sprite2() {
		return new SpriteResourceBuilder(resourceManager);
	}

	public TextureResourceBuilder texture2(FileHandle fileHandle) {
		return new TextureResourceBuilder(fileHandle);
	}

	public AnimationResourceBuilder animation2(String textureId) {
		return new AnimationResourceBuilder(resourceManager, textureId);
	}

	public XmlDocumentDeclaration xmlDocument(String file) {
		return new XmlDocumentDeclaration(file);
	}

	public FontResourceBuilder font2(String imageFile, String fontFile) {
		return new FontResourceBuilder(internal(imageFile), internal(fontFile));
	}

	public static class XmlDocumentDeclaration implements ResourceBuilder<Document> {

		private final String file;

		private boolean cached = false;

		private FileType fileType = FileType.Internal;

		public XmlDocumentDeclaration fileType(FileType fileType) {
			this.fileType = fileType;
			return this;
		}

		public XmlDocumentDeclaration cached() {
			this.cached = true;
			return this;
		}

		public XmlDocumentDeclaration(String file) {
			this.file = file;
		}

		@Override
		public Document build() {
			return new DocumentParser().parse(Gdx.files.getFileHandle(file, fileType).read());
		}

		@Override
		public boolean isVolatile() {
			return cached;
		}

	}

}