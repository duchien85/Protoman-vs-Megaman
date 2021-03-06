package com.gdxgame.core.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.gdxgame.core.constants.GameConstants;
import com.gdxgame.core.enums.MusicType;
import com.gdxgame.core.enums.SkinType;
import com.gdxgame.core.enums.SoundType;
import com.gdxgame.core.enums.TextureType;
import com.gdxgame.core.graphics.AnimatedSprite;

/**
 * The ResourceManager singleton is the core singleton to load and dispose resources. It supports loading and disposing of the following objects:
 * 		- libgdx TextureAtlas
 * 		- libgdx Skin
 * 		- AnimatedSprite
 * 		- libgdx Sound
 * 		- libgdx Music
 * 
 * ResourceManager keeps track of already loaded resources to not load them multiple times. It also has a convenient method to dispose
 * all current resources. This is useful when the game is closing.
 * 
 * Each resource has its own "accesscounter" meaning that if you call a load method for the same resource twice you also need to dispose it twice.
 * The accesscounter is increased whenever calling a load method and is decreased whenever calling a dispose method. If the counter is 0 then the
 * resource is disposed completely.
 * 
 * F.e. if one of your gamestates requires texture A and another one also requires texture A and both gamestates are loaded within GDXGame then
 * disposing the resources of one gamestate does not make texture A invalid for the other gamestate since it is not disposed (accesscounter is one).
 * 
 */
public enum ResourceManager {
	INSTANCE;

	/**
	 * Resource is a simple class to counter how many times a specific resource (textureatlas, sound, ...) is accessed.
	 * The counter is increased whenever a load method is called.
	 * The counter is decreased whenever a dispose method is called.
	 * Whenever the counter of a resource is 0 then the resource can be completely disposed.
	 */
	private class Resource<T> {
		/**
		 * reference to the resource (textureatlas,sprite,sound,music,skin)
		 */
		private T	resource;
		/**
		 * accesscounter of the resource. if 0 then the resource is no longer needed
		 */
		private int	counter;

		public Resource(T resource) {
			this.resource = resource;
			counter = 1;
		}

		public void incCounter() {
			++counter;
		}

		public int decCounter() {
			return --counter;
		}

		public T getResource() {
			return resource;
		}
	}

	/**
	 * map for storing texture atlases. Key is the filepath of the texture atlas
	 */
	private Map<String, Resource<TextureAtlas>>					textureAtlasMap;
	/**
	 * map for storing skins. Key is the skintype defined in the SkinType enum
	 */
	private Map<SkinType, Resource<Skin>>						skinMap;
	/**
	 * map for storing the AtlasRegions of an texture atlas. The region
	 * is automatically linked with the TextureType containing the texture.
	 */
	private Map<TextureAtlas, Map<TextureType, AtlasRegion>>	atlasRegionMap;
	/**
	 * map for storing AnimatedSprites. Key is the TextureType of the sprite that describes the used texture.
	 */
	private Map<TextureType, Resource<AnimatedSprite>>			spriteMap;
	/**
	 * map for storing sounds. Key is the soundtype defined in the SoundType enum
	 */
	private Map<SoundType, Resource<Sound>>						soundMap;
	/**
	 * map for storing music. Key is the musictype defined in the MusicType enum
	 */
	private Map<MusicType, Resource<Music>>						musicMap;

	private Map<String, Resource<TiledMap>>						tmxMap;

	private ResourceManager() {
		skinMap = new HashMap<SkinType, Resource<Skin>>();
		textureAtlasMap = new HashMap<String, Resource<TextureAtlas>>();
		atlasRegionMap = new HashMap<TextureAtlas, Map<TextureType, AtlasRegion>>();
		spriteMap = new HashMap<TextureType, Resource<AnimatedSprite>>();
		soundMap = new HashMap<SoundType, Resource<Sound>>();
		musicMap = new HashMap<MusicType, Resource<Music>>();
		tmxMap = new HashMap<String, Resource<TiledMap>>();
	}

	/**
	 * loads a specific texture atlas. The loaded atlas can then be retrieved with the 
	 * getTextureAtlas() method.
	 * Increases the accesscounter of the texture atlas if already loaded.
	 * 
	 * @param internalAtlasName internal file path of the texture atlas
	 */
	public void loadTextureAtlas(String internalAtlasName) {
		if (textureAtlasMap != null && !textureAtlasMap.containsKey(internalAtlasName)) {
			// load texture atlas
			TextureAtlas textureAtlas = new TextureAtlas(Gdx.files.internal(internalAtlasName));
			textureAtlasMap.put(internalAtlasName, new Resource<TextureAtlas>(textureAtlas));
			HashMap<TextureType, AtlasRegion> regionMap = new HashMap<TextureType, TextureAtlas.AtlasRegion>();
			atlasRegionMap.put(textureAtlas, regionMap);

			// build the atlas region map by storing each atlas region by its
			// corresponding TextureType to the map
			for (AtlasRegion region : textureAtlas.getRegions()) {
				TextureType key = TextureType.getGraphicsConstantByAtlasRegionName(region.name);
				if (key != null) {
					regionMap.put(key, region);
				} else {
					Gdx.app.log(GameConstants.LOG_TAG_INFO, "Undefined TextureType for region: " + region.name);
				}
			}
		} else {
			// increase resource counter
			textureAtlasMap.get(internalAtlasName).incCounter();
		}
	}

	/**
	 * returns a specific texture atlas that was already loaded with the loadTextureAtlas() method
	 * 
	 * @param internalAtlasName internal file path of the texture atlas
	 * 
	 * @return <b>null</b> if the texture atlas was not successfully loaded yet. Otherwise the texture atlas will be returned.
	 */
	public TextureAtlas getTextureAtlas(String internalAtlasName) {
		if (textureAtlasMap != null && textureAtlasMap.containsKey(internalAtlasName)) {
			return textureAtlasMap.get(internalAtlasName).getResource();
		} else {
			Gdx.app.log(GameConstants.LOG_TAG_INFO, "TextureAtlas was not successfully loaded yet: " + internalAtlasName);
			return null;
		}
	}

	/**
	 * Reduces the accesscounter of the texture atlas. If the accesscounter is 0 then
	 * the texture atlas that was already loaded with the loadTextureAtlas() method
	 * gets disposed.
	 * 
	 * @param internalAtlasName internal file path of the texture atlas
	 */
	public void disposeTextureAtlasAndSprites(String internalAtlasName) {
		if (textureAtlasMap != null && textureAtlasMap.containsKey(internalAtlasName)) {
			Resource<TextureAtlas> resource = textureAtlasMap.get(internalAtlasName);
			if (resource.decCounter() <= 0) {
				TextureAtlas textureAtlas = textureAtlasMap.get(internalAtlasName).getResource();

				// dispose all related animated sprites of the texture atlas' regions
				Map<TextureType, AtlasRegion> regionMap = atlasRegionMap.get(textureAtlas);
				for (TextureType type : regionMap.keySet()) {
					disposeAnimatedSprite(type);
				}

				// dispose the atlas and remove it from the textureAtlas map
				textureAtlas.dispose();
				regionMap.clear();
				textureAtlasMap.remove(internalAtlasName);
			}
		}
	}

	/**
	 * loads a specific animated sprite. The loaded sprite can then be retrieved with the 
	 * getAnimatedSprite() method.
	 * Increases the accesscounter of the sprite if already loaded.
	 * 
	 * @param texture type of texture to be used for the sprite
	 */
	public void loadAnimatedSprite(TextureType texture) {
		// check if texture atlas map contains the atlas type that should be used 
		if (spriteMap != null && !spriteMap.containsKey(texture) && textureAtlasMap != null && textureAtlasMap.containsKey(texture.getTextureAtlasPath())) {
			// check if the atlas regions were successfully loaded for the texture atlas
			TextureAtlas textureAtlas = textureAtlasMap.get(texture.getTextureAtlasPath()).getResource();
			if (atlasRegionMap != null && atlasRegionMap.containsKey(textureAtlas)) {
				Map<TextureType, AtlasRegion> regionMap = atlasRegionMap.get(textureAtlas);
				if (regionMap != null && regionMap.containsKey(texture)) {
					// create sprite out of texture atlas region's texture
					AnimatedSprite animatedSprite = new AnimatedSprite(regionMap.get(texture), texture);
					spriteMap.put(texture, new Resource<AnimatedSprite>(animatedSprite));
				}
			} else {
				Gdx.app.log(GameConstants.LOG_TAG_INFO, "TextureAtlas does not contain region: " + texture.getAtlasRegionName());
			}
		} else {
			// increase resource counter
			spriteMap.get(texture).incCounter();
		}
	}

	/**
	 * returns a specific animated sprite that was already loaded with the loadAnimatedSprite() method
	 * 
	 * @param texture type of texture of the sprite
	 * 
	 * @return <b>null</b> if the sprite was not successfully loaded yet. Otherwise the sprite will be returned.
	 */
	public AnimatedSprite getAnimatedSprite(TextureType texture) {
		if (spriteMap != null && spriteMap.containsKey(texture)) {
			return spriteMap.get(texture).getResource();
		} else {
			Gdx.app.log(GameConstants.LOG_TAG_INFO, "AnimatedSprite was not successfully loaded yet: " + texture.getOriginalFilePath());
			return null;
		}
	}

	/**
	 * Reduces the accesscounter of the sprite. If the accesscounter is 0 then
	 * the sprite that was already loaded with the loadAnimatedSprite() method
	 * gets disposed.
	 * 
	 * @param texture type of texture of the sprite
	 */
	public void disposeAnimatedSprite(TextureType texture) {
		if (spriteMap != null && spriteMap.containsKey(texture)) {
			Resource<AnimatedSprite> resource = spriteMap.get(texture);
			if (resource.decCounter() <= 0) {
				spriteMap.remove(texture);
			}
		}
	}

	/**
	 * loads a specific sound. The loaded sound can then be retrieved with the getSound() method.
	 * Increases the accesscounter of the sound if already loaded.
	 * 
	 * @param sound type of sound to be used for the sound
	 */
	public void loadSound(SoundType sound) {
		if (soundMap != null && !soundMap.containsKey(sound)) {
			soundMap.put(sound, new Resource<Sound>(Gdx.audio.newSound(Gdx.files.internal(sound.getFilePath()))));
		} else {
			// increase resource counter
			soundMap.get(sound).incCounter();
		}
	}

	/**
	 * returns a specific sound that was already loaded with the loadSound() method
	 * 
	 * @param sound type of sound
	 * 
	 * @return <b>null</b> if the sound was not successfully loaded yet. Otherwise the sound will be returned.
	 */
	public Sound getSound(SoundType sound) {
		if (soundMap != null && soundMap.containsKey(sound)) {
			return soundMap.get(sound).getResource();
		} else {
			Gdx.app.log(GameConstants.LOG_TAG_INFO, "Sound was not successfully loaded yet: " + sound.getFilePath());
			return null;
		}
	}

	/**
	 * Reduces the accesscounter of the sound. If the accesscounter is 0 then
	 * the sound that was already loaded with the loadSound() method
	 * gets disposed.
	 * 
	 * @param sound type of sound
	 */
	public void disposeSound(SoundType sound) {
		if (soundMap != null && soundMap.containsKey(sound)) {
			Resource<Sound> resource = soundMap.get(sound);
			if (resource.decCounter() <= 0) {
				soundMap.get(sound).getResource().dispose();
				soundMap.remove(sound);
			}
		}
	}

	/**
	 * loads a specific music. The loaded music can then be retrieved with the getMusic() method.
	 * Increases the accesscounter of the music if already loaded.
	 * 
	 * @param music type of music to be used for the music
	 */
	public void loadMusic(MusicType music) {
		if (musicMap != null && !musicMap.containsKey(music)) {
			musicMap.put(music, new Resource<Music>(Gdx.audio.newMusic(Gdx.files.internal(music.getFilePath()))));
		} else {
			// increase resource counter
			musicMap.get(music).incCounter();
		}
	}

	/**
	 * returns a specific music that was already loaded with the loadMusic() method
	 * 
	 * @param music type of music
	 * 
	 * @return <b>null</b> if the music was not successfully loaded yet. Otherwise the music will be returned.
	 */
	public Music getMusic(MusicType music) {
		if (musicMap != null && musicMap.containsKey(music)) {
			return musicMap.get(music).getResource();
		} else {
			Gdx.app.log(GameConstants.LOG_TAG_INFO, "Music was not successfully loaded yet: " + music.getFilePath());
			return null;
		}
	}

	/**
	 * Reduces the accesscounter of the music. If the accesscounter is 0 then
	 * the music that was already loaded with the loadMusic() method
	 * gets disposed.
	 * 
	 * @param music type of music
	 */
	public void disposeMusic(MusicType music) {
		if (musicMap != null && musicMap.containsKey(music)) {
			Resource<Music> resource = musicMap.get(music);
			if (resource.decCounter() <= 0) {
				musicMap.get(music).getResource().dispose();
				musicMap.remove(music);
			}
		}
	}

	/**
	 * loads a specific skin. The loaded skin can then be retrieved with the getSkin() method.
	 * Increases the accesscounter of the skin if already loaded.
	 * 
	 * @param skin type of skin to be used for the skin
	 */
	public void loadSkin(SkinType skin) {
		if (skinMap != null && !skinMap.containsKey(skin)) {
			loadTextureAtlas(skin.getTextureAtlasFilePath());
			TextureAtlas textureAtlas = getTextureAtlas(skin.getTextureAtlasFilePath());
			skinMap.put(skin, new Resource<Skin>(new Skin(Gdx.files.internal(skin.getFilePath()), textureAtlas)));
		} else {
			// increase resource counter
			skinMap.get(skin).incCounter();
		}
	}

	/**
	 * returns a specific skin that was already loaded with the loadSkin() method
	 * 
	 * @param skin type of skin
	 * 
	 * @return <b>null</b> if the skin was not successfully loaded yet. Otherwise the skin will be returned.
	 */
	public Skin getSkin(SkinType skin) {
		if (skinMap != null && skinMap.containsKey(skin)) {
			return skinMap.get(skin).getResource();
		} else {
			Gdx.app.log(GameConstants.LOG_TAG_INFO, "Skin was not successfully loaded yet: " + skin.getFilePath());
			return null;
		}
	}

	/**
	 * Reduces the accesscounter of the skin. If the accesscounter is 0 then
	 * the skin that was already loaded with the loadSkin() method
	 * gets disposed.
	 * 
	 * @param skin type of skin
	 */
	public void disposeSkin(SkinType skin) {
		if (skinMap != null && skinMap.containsKey(skin)) {
			Resource<Skin> resource = skinMap.get(skin);
			if (resource.decCounter() <= 0) {
				disposeTextureAtlasAndSprites(skin.getTextureAtlasFilePath());
				skinMap.get(skin).getResource().dispose();
				skinMap.remove(skin);
			}
		}
	}

	/**
	 * Loads a map created with <b>Tiled</b>. Checks for animations within each tile layer by going through tile properties.
	 * If a property is defined with key <b>AnimationXXX</b> then the animation <b>XXX</b> will be stored to use it for
	 * {@link AnimatedTiledMapTile}. The values for animation <b>XXX</b> must be numbers between <b>1</b> and maximum number
	 * of animations used.
	 * If a property is defined with key <b>AnimationsPerSecond</b> then the animation that is also defined within the same tile
	 * will use the given animations per second defined.
	 * 
	 * @param tmxMapPath file path to the tmx map
	 */
	public void loadTMXMap(String tmxMapPath) {
		if (tmxMap != null && !tmxMap.containsKey(tmxMapPath)) {
			TiledMap map = new TmxMapLoader().load(tmxMapPath);

			// check if there animated tiles within the map
			// animated tiles are detected if they have a property that contains the string "Animation"

			// first store all static tiles that are used for an animated tile within a map
			// key String = propertyname that contained the string "Animation"
			// key Integer = value of property that contained the string "Animation"
			Map<String, Map<Integer, StaticTiledMapTile>> tilesForAnimatedTile = new HashMap<String, Map<Integer, StaticTiledMapTile>>();
			for (TiledMapTileSet tilemap : map.getTileSets()) {
				for (TiledMapTile tile : tilemap) {
					if (tile instanceof StaticTiledMapTile && tile.getProperties() != null) {
						Iterator<String> keys = tile.getProperties().getKeys();
						while (keys.hasNext()) {
							String key = keys.next();
							if (key.contains("Animation") && !"AnimationsPerSecond".equals(key)) {
								// found a tile that should be part of an animation
								if (tilesForAnimatedTile.containsKey(key)) {
									// there were already tiles found for the animation "key"
									// add the current tile with its animation index value (=property value) to the treemap
									Map<Integer, StaticTiledMapTile> animatedTileMap = tilesForAnimatedTile.get(key);
									animatedTileMap.put(Integer.parseInt(tile.getProperties().get(key, String.class)), (StaticTiledMapTile) tile);
								} else {
									// there are no tiles available yet for animation "key"
									// add the current tile with its animation index value (=property value) to the treemap
									Map<Integer, StaticTiledMapTile> animatedTileMap = new TreeMap<Integer, StaticTiledMapTile>();
									animatedTileMap.put(Integer.parseInt(tile.getProperties().get(key, String.class)), (StaticTiledMapTile) tile);
									tilesForAnimatedTile.put(key, animatedTileMap);
								}
								break;
							}
						}
					}
				}
			}

			// replace StaticTiledMapTiles with new AnimatedTiledMapTiles
			if (tilesForAnimatedTile.size() > 0) {
				// there are static tiles that needs to be replaced
				MapLayers layers = map.getLayers();
				for (MapLayer layer : layers) {
					if (layer instanceof TiledMapTileLayer) {
						TiledMapTileLayer tiledlayer = (TiledMapTileLayer) layer;
						for (int x = 0; x < tiledlayer.getWidth(); x++) {
							for (int y = 0; y < tiledlayer.getHeight(); y++) {
								TiledMapTileLayer.Cell cell = tiledlayer.getCell(x, y);
								if (cell != null) {
									Iterator<String> keys = cell.getTile().getProperties().getKeys();
									while (keys.hasNext()) {
										String key = keys.next();
										if (key.contains("Animation") && !"AnimationsPerSecond".equals(key)) {
											Map<Integer, StaticTiledMapTile> animatedTileMap = tilesForAnimatedTile.get(key);
											if (animatedTileMap.size() > 1) {
												// animation found with at least 2 animations
												Array<StaticTiledMapTile> animationTiles = new Array<StaticTiledMapTile>();
												for (StaticTiledMapTile statictile : animatedTileMap.values()) {
													animationTiles.add(statictile);
												}

												if (cell.getTile().getProperties().containsKey("AnimationsPerSecond")) {
													cell.setTile(new AnimatedTiledMapTile(1.0f / Integer.parseInt(cell.getTile().getProperties().get("AnimationsPerSecond", String.class)), animationTiles));
												} else {
													Gdx.app.log(GameConstants.LOG_TAG_INFO, "TMX map contains animation without property 'AnimationsPerSecond': " + key);
													cell.setTile(new AnimatedTiledMapTile(1, animationTiles));
												}

											} else {
												Gdx.app.log(GameConstants.LOG_TAG_INFO, "TMX map contains an incorrect animation with only one animation: " + key);
											}
											break;
										}
									}

								}
							}
						}
					}
				}
			}

			tmxMap.put(tmxMapPath, new Resource<TiledMap>(map));
		} else {
			// increase resource counter
			tmxMap.get(tmxMapPath).incCounter();
		}
	}

	/**
	 * returns tmxmap that was already loaded with the {@link #loadTMXMap(String)} method.
	 * 
	 * @param tmxMapPath file path to the tmx map
	 * 
	 * @return <b>null</b> if map was not successfully loaded yet. Otherwise the loaded {@link TiledMap} is returned.
	 */
	public TiledMap getTMXMap(String tmxMapPath) {
		if (tmxMap != null && tmxMap.containsKey(tmxMapPath)) {
			return tmxMap.get(tmxMapPath).getResource();
		} else {
			Gdx.app.log(GameConstants.LOG_TAG_INFO, "TMXMap was not successfully loaded yet: " + tmxMapPath);
			return null;
		}
	}

	/**
	 * disposes tmxmap that was already loaded with the {@link #loadTMXMap(String)} method.
	 * 
	 * @param tmxMapPath file path to the tmx map
	 */
	public void disposeTMXMap(String tmxMapPath) {
		if (tmxMap != null && tmxMap.containsKey(tmxMapPath)) {
			Resource<TiledMap> resource = tmxMap.get(tmxMapPath);
			if (resource.decCounter() <= 0) {
				tmxMap.get(tmxMapPath).getResource().dispose();
				tmxMap.remove(tmxMapPath);
			}
		}
	}

	/**
	 * disposes all resources (texture atlas, sprites, sounds, music, ...) that were loaded with the ResourceManager.
	 * There are no more resources available after a call to this method.
	 * 
	 * This method is automatically called when the game is closing.
	 */
	public void disposeAllResources() {
		if (textureAtlasMap != null) {
			for (Map.Entry<String, Resource<TextureAtlas>> entry : textureAtlasMap.entrySet()) {
				Gdx.app.log(GameConstants.LOG_TAG_INFO, "Undisposed texture atlas in disposeAllResources(): " + entry.getKey() + "\tRemaining counter was:" + entry.getValue().counter);
				entry.getValue().getResource().dispose();
			}
			textureAtlasMap.clear();
			atlasRegionMap.clear();
			spriteMap.clear();
		}

		if (soundMap != null) {
			for (Map.Entry<SoundType, Resource<Sound>> entry : soundMap.entrySet()) {
				Gdx.app.log(GameConstants.LOG_TAG_INFO, "Undisposed sound in disposeAllResources(): " + entry.getKey() + "\tRemaining counter was:" + entry.getValue().counter);
				entry.getValue().getResource().dispose();
			}
			soundMap.clear();
		}

		if (musicMap != null) {
			for (Map.Entry<MusicType, Resource<Music>> entry : musicMap.entrySet()) {
				Gdx.app.log(GameConstants.LOG_TAG_INFO, "Undisposed music in disposeAllResources(): " + entry.getKey() + "\tRemaining counter was:" + entry.getValue().counter);
				entry.getValue().getResource().dispose();
			}
			musicMap.clear();
		}

		if (skinMap != null) {
			for (Map.Entry<SkinType, Resource<Skin>> entry : skinMap.entrySet()) {
				Gdx.app.log(GameConstants.LOG_TAG_INFO, "Undisposed skin in disposeAllResources(): " + entry.getKey() + "\tRemaining counter was:" + entry.getValue().counter);
				entry.getValue().getResource().dispose();
			}
			skinMap.clear();
		}

		if (tmxMap != null) {
			for (Entry<String, Resource<TiledMap>> entry : tmxMap.entrySet()) {
				Gdx.app.log(GameConstants.LOG_TAG_INFO, "Undisposed map in disposeAllResources(): " + entry.getKey() + "\tRemaining counter was:" + entry.getValue().counter);
				entry.getValue().getResource().dispose();
			}
			tmxMap.clear();
		}
	}
}
