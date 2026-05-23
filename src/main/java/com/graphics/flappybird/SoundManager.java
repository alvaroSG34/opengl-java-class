package com.graphics.flappybird;

import java.io.BufferedInputStream;
import java.io.IOException;
import javax.sound.sampled.*;

public class SoundManager {

    private Clip jumpClip;
    private Clip pointClip;
    private Clip deathClip;
    private Clip gameOverClip;

    public void init() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        jumpClip = loadClip("/sfx/jump.wav");
        pointClip = loadClip("/sfx/point.wav");
        deathClip = loadClip("/sfx/death.wav");
        gameOverClip = loadClip("/sfx/gameover.wav");
    }

    public void playJump() {
        play(jumpClip);
    }

    public void playPoint() {
        play(pointClip);
    }

    public void playGameOver() {
        play(gameOverClip);
    }

    public void playDeath() {
        play(deathClip);
    }

    public void cleanup() {
        closeClip(jumpClip);
        closeClip(pointClip);
        closeClip(deathClip);
        closeClip(gameOverClip);
    }

    private Clip loadClip(String resource) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        var in = SoundManager.class.getResourceAsStream(resource);
        if (in == null) {
            throw new IOException("No se encontro el recurso: " + resource);
        }
        try (var bin = new BufferedInputStream(in)) {
            AudioInputStream ais = AudioSystem.getAudioInputStream(bin);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            return clip;
        }
    }

    private void play(Clip clip) {
        if (clip == null) {
            return;
        }
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);
        clip.start();
    }

    private void closeClip(Clip clip) {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }
}
