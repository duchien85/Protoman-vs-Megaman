package com.megaman.gamestates;

import com.megaman.core.GameLogic;
import com.megaman.core.GameState;
import com.megaman.core.enums.GameStateType;
import com.megaman.core.enums.MusicType;
import com.megaman.core.enums.SkinType;
import com.megaman.core.enums.SoundType;
import com.megaman.core.enums.TextureType;
import com.megaman.core.utils.ResourceManager;

public class GSGameOver extends GameState {
	public GSGameOver(GameStateType type, GameLogic logic) {
		super(type, logic);
	}

	@Override
	protected void loadResources() {
		// load skin and texture atlas
		ResourceManager.INSTANCE.loadSkin(SkinType.SKIN_MAIN_MENU);

		// load sprites
		ResourceManager.INSTANCE.loadAnimatedSprite(TextureType.TEXTURE_MENU_MEGAMAN);
		ResourceManager.INSTANCE.loadAnimatedSprite(TextureType.TEXTURE_MENU_PROTOMAN);
		ResourceManager.INSTANCE.loadAnimatedSprite(TextureType.TEXTURE_MENU_MISSLE);

		// load music
		ResourceManager.INSTANCE.loadMusic(MusicType.MENU_QUIT);
		ResourceManager.INSTANCE.loadMusic(MusicType.PROTOMAN);

		// load sounds
		ResourceManager.INSTANCE.loadSound(SoundType.MENU_MOVE);
		ResourceManager.INSTANCE.loadSound(SoundType.MENU_SELECT);
		ResourceManager.INSTANCE.loadSound(SoundType.MENU_SELECT_SHOOT);
	}

	@Override
	protected void disposeResources() {
		// dispose skin, textureatlas and sprites
		ResourceManager.INSTANCE.disposeSkin(SkinType.SKIN_MAIN_MENU);

		// dispose music
		ResourceManager.INSTANCE.disposeMusic(MusicType.MENU_QUIT);
		ResourceManager.INSTANCE.disposeMusic(MusicType.PROTOMAN);

		// dispose sounds
		ResourceManager.INSTANCE.disposeSound(SoundType.MENU_MOVE);
		ResourceManager.INSTANCE.disposeSound(SoundType.MENU_SELECT);
		ResourceManager.INSTANCE.disposeSound(SoundType.MENU_SELECT_SHOOT);

	}

}
