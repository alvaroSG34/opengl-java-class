package com.graphics.flappybird;

/**
 * HudScreenRenderer: dibuja pantallas de inicio y game over.
 */
public class HudScreenRenderer {

    private final Renderer renderer;
    private final TextRenderer screenTextRenderer;
    private final int ancho;
    private final int alto;

    public HudScreenRenderer(Renderer renderer, TextRenderer screenTextRenderer, int ancho, int alto) {
        this.renderer = renderer;
        this.screenTextRenderer = screenTextRenderer;
        this.ancho = ancho;
        this.alto = alto;
    }

    public void drawStartScreen() {
        renderer.drawRect(0.0f, 0.28f, 1.30f, 1.05f, 0f, 0f, 0f);
        renderer.drawRectStyled(
                0.0f, 0.28f, 1.24f, 0.95f,
                0.09f, 0.16f, 0.29f,
                0.17f, 0.29f, 0.43f,
                1.0f, 0.020f, 1.25f, 0.040f
        );

        drawCenteredText("FLAPPY BIRD", alto * 0.20f, 0.98f, 0.98f, 0.85f, 0.20f);
        drawCenteredText("MODO 2 JUGADORES", alto * 0.30f, 0.98f, 0.88f, 0.95f, 1.0f);
        drawCenteredText("J1: W          J2: SPACE", alto * 0.40f, 0.98f, 1.0f, 1.0f, 1.0f);
        drawCenteredText("PRESIONA W O SPACE PARA INICIAR", alto * 0.50f, 0.98f, 0.20f, 0.78f, 0.30f);
    }

    public void drawGameOverScreen(int puntaje1, int puntaje2) {
        renderer.drawRect(0.0f, 0.26f, 1.30f, 1.05f, 0f, 0f, 0f);
        renderer.drawRectStyled(
                0.0f, 0.26f, 1.24f, 0.95f,
                0.18f, 0.10f, 0.14f,
                0.30f, 0.14f, 0.16f,
                1.0f, 0.026f, 1.45f, 0.055f
        );

        drawCenteredText("GAME OVER", alto * 0.28f, 0.98f, 0.95f, 0.25f, 0.35f);

        String marcador = "P1: " + puntaje1 + "   P2: " + puntaje2;
        drawCenteredText(marcador, alto * 0.38f, 0.98f, 1.0f, 1.0f, 1.0f);

        String ganador = getWinnerMessage(puntaje1, puntaje2).toUpperCase();
        drawCenteredText(ganador, alto * 0.46f, 0.98f, 0.92f, 0.92f, 1.0f);

        drawCenteredText("W, SPACE O R PARA REINICIAR", alto * 0.58f, 0.98f, 0.20f, 0.78f, 0.30f);
    }

    private void drawCenteredText(String text, float yPx, float scale, float r, float g, float b) {
        float textWidth = screenTextRenderer.measureTextWidth(text, scale);
        float xPx = (ancho * 0.5f) - (textWidth * 0.5f);
        screenTextRenderer.drawText(text, xPx, yPx, scale, r, g, b, ancho, alto);
    }

    private String getWinnerMessage(int puntaje1, int puntaje2) {
        if (puntaje1 > puntaje2) {
            return "Ganador: Jugador 1";
        }
        if (puntaje2 > puntaje1) {
            return "Ganador: Jugador 2";
        }
        return "Empate";
    }
}
