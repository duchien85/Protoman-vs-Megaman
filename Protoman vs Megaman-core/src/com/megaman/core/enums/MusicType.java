package com.megaman.core.enums;

public enum MusicType {
	MENU("mm3 title theme", "audio/music/menu.mp3"),
	MENU_QUIT(null, "audio/sounds/menu_quit.wav"),
	SPARKMAN("sparkman theme", "audio/music/sparkman.mp3"),
	SHADOWMAN("shadowman theme", "audio/music/shadowman.mp3"),
	SNAKEMAN("snakeman theme", "audio/music/snakeman.mp3"),
	NEEDLEMAN("needleman theme", "audio/music/needleman.mp3"),
	HARDMAN("hardman theme", "audio/music/hardman.mp3"),
	TOPMAN("topman theme", "audio/music/topman.mp3"),
	GEMINIMAN("geminiman theme", "audio/music/geminiman.mp3"),
	MAGNETMAN("magnetman theme", "audio/music/magnetman.mp3"),
	PROTOMAN("protoman theme", "audio/music/protoman_theme.mp3"),
	WILY_STAGE("mm3 wily stage 1", "audio/music/wily_stage_1.mp3");

	private final String	audioName;
	private final String	filePath;

	private MusicType(String audioName, String filePath) {
		this.audioName = audioName;
		this.filePath = filePath;
	}

	public String getFilePath() {
		return filePath;
	}

	public String getAudioName() {
		return audioName;
	}
}