package com.graphics.flappybird;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * Renderer: gestiona renderizado, shaders y geometria OpenGL.
 */
public class Renderer {

    private int programa;
    private int quadVao;
    private int quadVbo;
    private int triangleVao;
    private int triangleVbo;
    private int circleVao;
    private int circleVbo;
    private int circleVertexCount;

    private int uOffsetLocation;
    private int uScaleLocation;
    private int uColorLocation;
    private int uColor2Location;
    private int uGradientMixLocation;
    private int uNoiseAmountLocation;
    private int uPatternScaleLocation;
    private int uStripeAmountLocation;
    private int uTimeLocation;
    private int uWobbleLocation;
    private int uRotationLocation;
    private int uPivotLocation;
    private int uUseTextureLocation;
    private int uTexLocation;

    private int cloudTextureId;

    /**
     * Inicializa shaders y geometria base.
     */
    public void init() {
        crearShaders();
        crearQuadBase();
        crearTrianguloBase();
        crearCirculoBase();

        try {
            cloudTextureId = cargarTextura("/textures/clouds.png");
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar la textura de nubes", e);
        }
    }

    public void setTime(float time) {
        GL20.glUniform1f(uTimeLocation, time);
    }

    public void setWobble(float wobble) {
        GL20.glUniform1f(uWobbleLocation, wobble);
    }

    public void setRotation(float radians) {
        GL20.glUniform1f(uRotationLocation, radians);
    }

    public void setPivot(float x, float y) {
        GL20.glUniform2f(uPivotLocation, x, y);
    }

    private void crearShaders() {
        String vertexSrc = """
            #version 330 core
            layout (location = 0) in vec3 aPos;
            uniform vec2 uOffset;
            uniform vec2 uScale;
            uniform float uTime;
            uniform float uWobble;
            uniform float uRotation;
            uniform vec2 uPivot;
            out vec2 vLocalUv;

            void main() {
                vec2 wobble = vec2(0.0, 0.02 * uWobble * sin(uTime * 10.0 + aPos.x * 6.0));
                vec2 local = (aPos.xy + wobble) * uScale;
                vec2 world = local + uOffset;

                float c = cos(uRotation);
                float s = sin(uRotation);
                vec2 centered = world - uPivot;
                vec2 rotated = vec2(centered.x * c - centered.y * s,
                                    centered.x * s + centered.y * c) + uPivot;

                vLocalUv = aPos.xy + vec2(0.5);
                gl_Position = vec4(rotated, aPos.z, 1.0);
            }
            """;

        String fragmentSrc = """
            #version 330 core
            uniform vec3 uColor;
            uniform vec3 uColor2;
            uniform float uGradientMix;
            uniform float uNoiseAmount;
            uniform float uPatternScale;
            uniform float uStripeAmount;
            uniform float uTime;
            uniform int uUseTexture;
            uniform sampler2D uTex;
            in vec2 vLocalUv;
            out vec4 fragColor;

            float hash(vec2 p) {
                return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
            }

            void main() {
                if (uUseTexture == 1) {
                    vec4 tex = texture(uTex, vLocalUv);
                    if (tex.a <= 0.001) {
                        discard;
                    }
                    fragColor = tex;
                    return;
                }

                float t = clamp(vLocalUv.y, 0.0, 1.0);
                vec3 gradColor = mix(uColor, uColor2, t);
                vec3 baseColor = mix(uColor, gradColor, clamp(uGradientMix, 0.0, 1.0));

                float stripe = 0.5 + 0.5 * sin((vLocalUv.y + vLocalUv.x * 0.25 + uTime * 0.08) * (uPatternScale * 18.0));
                baseColor += (stripe - 0.5) * uStripeAmount;

                float grain = hash(vLocalUv * (uPatternScale * 110.0) + vec2(uTime * 0.05, 0.0)) - 0.5;
                baseColor += grain * uNoiseAmount;

                fragColor = vec4(clamp(baseColor, 0.0, 1.0), 1.0);
            }
            """;

        int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertexShader, vertexSrc);
        GL20.glCompileShader(vertexShader);
        checkShader(vertexShader, "Vertex");

        int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragmentShader, fragmentSrc);
        GL20.glCompileShader(fragmentShader);
        checkShader(fragmentShader, "Fragment");

        programa = GL20.glCreateProgram();
        GL20.glAttachShader(programa, vertexShader);
        GL20.glAttachShader(programa, fragmentShader);
        GL20.glLinkProgram(programa);

        if (GL20.glGetProgrami(programa, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException("Error al enlazar programa: " + GL20.glGetProgramInfoLog(programa));
        }

        uOffsetLocation = GL20.glGetUniformLocation(programa, "uOffset");
        uScaleLocation = GL20.glGetUniformLocation(programa, "uScale");
        uColorLocation = GL20.glGetUniformLocation(programa, "uColor");
        uColor2Location = GL20.glGetUniformLocation(programa, "uColor2");
        uGradientMixLocation = GL20.glGetUniformLocation(programa, "uGradientMix");
        uNoiseAmountLocation = GL20.glGetUniformLocation(programa, "uNoiseAmount");
        uPatternScaleLocation = GL20.glGetUniformLocation(programa, "uPatternScale");
        uStripeAmountLocation = GL20.glGetUniformLocation(programa, "uStripeAmount");
        uTimeLocation = GL20.glGetUniformLocation(programa, "uTime");
        uWobbleLocation = GL20.glGetUniformLocation(programa, "uWobble");
        uRotationLocation = GL20.glGetUniformLocation(programa, "uRotation");
        uPivotLocation = GL20.glGetUniformLocation(programa, "uPivot");
        uUseTextureLocation = GL20.glGetUniformLocation(programa, "uUseTexture");
        uTexLocation = GL20.glGetUniformLocation(programa, "uTex");

        if (uOffsetLocation == -1 || uScaleLocation == -1 || uColorLocation == -1 || uColor2Location == -1
                || uGradientMixLocation == -1 || uNoiseAmountLocation == -1 || uPatternScaleLocation == -1
                || uStripeAmountLocation == -1 || uTimeLocation == -1 || uWobbleLocation == -1
                || uRotationLocation == -1 || uPivotLocation == -1 || uUseTextureLocation == -1
                || uTexLocation == -1) {
            throw new RuntimeException("No se pudieron obtener uniforms del shader");
        }

        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
    }

    private void checkShader(int shader, String tipo) {
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException(tipo + " shader: " + GL20.glGetShaderInfoLog(shader));
        }
    }

    private void crearQuadBase() {
        float[] vertices = {
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,
            0.5f, 0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
            0.5f, 0.5f, 0.0f,
            -0.5f, 0.5f, 0.0f
        };

        quadVao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(quadVao);

        quadVbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, quadVbo);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    private void crearTrianguloBase() {
        float[] vertices = {
            -0.5f, -0.5f, 0.0f,
            0.5f, 0.0f, 0.0f,
            -0.5f, 0.5f, 0.0f
        };

        triangleVao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(triangleVao);

        triangleVbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, triangleVbo);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    private void crearCirculoBase() {
        int segmentos = 24;
        circleVertexCount = segmentos + 2;
        float[] vertices = new float[circleVertexCount * 3];

        int idx = 0;
        vertices[idx++] = 0.0f;
        vertices[idx++] = 0.0f;
        vertices[idx++] = 0.0f;

        for (int i = 0; i <= segmentos; i++) {
            float angulo = (float) ((Math.PI * 2.0) * i / segmentos);
            vertices[idx++] = 0.5f * (float) Math.cos(angulo);
            vertices[idx++] = 0.5f * (float) Math.sin(angulo);
            vertices[idx++] = 0.0f;
        }

        circleVao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(circleVao);

        circleVbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, circleVbo);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    public void beginRender() {
        setRotation(0.0f);
        setPivot(0.0f, 0.0f);
        GL11.glClearColor(0.52f, 0.80f, 0.92f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        GL20.glUseProgram(programa);
        GL20.glUniform1i(uTexLocation, 0);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void drawBackground(float time, float worldScroll) {
        setRotation(0.0f);
        setPivot(0.0f, 0.0f);
        setWobble(0.0f);

        // Cielo con degradado.
        drawRectStyled(
                0.0f, 0.0f, 2.0f, 2.0f,
                0.34f, 0.68f, 0.92f,
                0.85f, 0.95f, 1.0f,
                1.0f, 0.006f, 1.1f, 0.015f
        );

        // Montanas lejanas (parallax suave).
        float farShift = (worldScroll * 0.08f) % 0.9f;
        for (int i = -4; i <= 4; i++) {
            float x = i * 0.9f - farShift;
            drawMountain(x, -0.18f, 0.70f, 0.48f, 0.36f, 0.56f, 0.74f);
        }

        // Montanas medias.
        float midShift = (worldScroll * 0.14f) % 0.75f;
        for (int i = -4; i <= 4; i++) {
            float x = i * 0.75f - midShift;
            drawMountain(x, -0.30f, 0.58f, 0.38f, 0.29f, 0.48f, 0.62f);
        }

        // Nubes con textura real.
        float cloudShift = (worldScroll * 0.05f) % 1.0f;
        for (int i = -3; i <= 3; i++) {
            float x = i * 0.9f - cloudShift;
            float y = 0.62f + 0.08f * (float) Math.sin(time * 0.35f + i * 1.3f);
            drawTexturedQuad(x, y, 0.62f, 0.26f, cloudTextureId);
        }

        // Suelo base.
        drawRectStyled(
                0.0f, -0.88f, 2.0f, 0.24f,
                0.20f, 0.55f, 0.26f,
                0.12f, 0.40f, 0.18f,
                1.0f, 0.010f, 1.7f, 0.050f
        );

        // Tiras del suelo con parallax rapido.
        float floorShift = (worldScroll * 0.35f) % 0.36f;
        for (int i = -6; i <= 6; i++) {
            float x = i * 0.36f - floorShift;
            drawRect(x, -0.90f, 0.14f, 0.02f, 0.16f, 0.34f, 0.12f);
        }
    }

    private void drawMountain(float x, float baseY, float width, float height, float r, float g, float b) {
        drawTriangle(x - (width * 0.25f), baseY, width, height, r, g, b);
        drawTriangle(x + (width * 0.25f), baseY, -width, height, r * 0.92f, g * 0.92f, b * 0.92f);
    }

    public void drawRect(float x, float y, float ancho, float alto, float r, float g, float b) {
        GL30.glBindVertexArray(quadVao);
        GL20.glUniform1i(uUseTextureLocation, 0);
        GL20.glUniform2f(uOffsetLocation, x, y);
        GL20.glUniform2f(uScaleLocation, ancho, alto);
        GL20.glUniform3f(uColorLocation, r, g, b);
        GL20.glUniform3f(uColor2Location, r, g, b);
        GL20.glUniform1f(uGradientMixLocation, 0.0f);
        GL20.glUniform1f(uNoiseAmountLocation, 0.0f);
        GL20.glUniform1f(uPatternScaleLocation, 1.0f);
        GL20.glUniform1f(uStripeAmountLocation, 0.0f);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
    }

    public void drawRectStyled(
            float x, float y, float ancho, float alto,
            float r1, float g1, float b1,
            float r2, float g2, float b2,
            float gradientMix,
            float noiseAmount,
            float patternScale,
            float stripeAmount) {
        GL30.glBindVertexArray(quadVao);
        GL20.glUniform1i(uUseTextureLocation, 0);
        GL20.glUniform2f(uOffsetLocation, x, y);
        GL20.glUniform2f(uScaleLocation, ancho, alto);
        GL20.glUniform3f(uColorLocation, r1, g1, b1);
        GL20.glUniform3f(uColor2Location, r2, g2, b2);
        GL20.glUniform1f(uGradientMixLocation, gradientMix);
        GL20.glUniform1f(uNoiseAmountLocation, noiseAmount);
        GL20.glUniform1f(uPatternScaleLocation, patternScale);
        GL20.glUniform1f(uStripeAmountLocation, stripeAmount);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
    }

    public void drawTriangle(float x, float y, float ancho, float alto, float r, float g, float b) {
        GL30.glBindVertexArray(triangleVao);
        GL20.glUniform1i(uUseTextureLocation, 0);
        GL20.glUniform2f(uOffsetLocation, x, y);
        GL20.glUniform2f(uScaleLocation, ancho, alto);
        GL20.glUniform3f(uColorLocation, r, g, b);
        GL20.glUniform3f(uColor2Location, r, g, b);
        GL20.glUniform1f(uGradientMixLocation, 0.0f);
        GL20.glUniform1f(uNoiseAmountLocation, 0.0f);
        GL20.glUniform1f(uPatternScaleLocation, 1.0f);
        GL20.glUniform1f(uStripeAmountLocation, 0.0f);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
    }

    public void drawCircle(float x, float y, float ancho, float alto, float r, float g, float b) {
        GL30.glBindVertexArray(circleVao);
        GL20.glUniform1i(uUseTextureLocation, 0);
        GL20.glUniform2f(uOffsetLocation, x, y);
        GL20.glUniform2f(uScaleLocation, ancho, alto);
        GL20.glUniform3f(uColorLocation, r, g, b);
        GL20.glUniform3f(uColor2Location, r, g, b);
        GL20.glUniform1f(uGradientMixLocation, 0.0f);
        GL20.glUniform1f(uNoiseAmountLocation, 0.0f);
        GL20.glUniform1f(uPatternScaleLocation, 1.0f);
        GL20.glUniform1f(uStripeAmountLocation, 0.0f);
        GL11.glDrawArrays(GL11.GL_TRIANGLE_FAN, 0, circleVertexCount);
    }

    public void drawTexturedQuad(float x, float y, float ancho, float alto, int textureId) {
        GL30.glBindVertexArray(quadVao);
        GL20.glUniform1i(uUseTextureLocation, 1);
        GL20.glUniform2f(uOffsetLocation, x, y);
        GL20.glUniform2f(uScaleLocation, ancho, alto);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);

        GL20.glUniform1i(uUseTextureLocation, 0);
    }

    public void drawPipes(java.util.List<Pipe.SegmentoRender> segmentos) {
        for (Pipe.SegmentoRender segmento : segmentos) {
            float x = segmento.getX();
            float y = segmento.getY();
            float w = segmento.getAncho();
            float h = segmento.getAlto();

            // Cuerpo base del tubo.
            drawRectStyled(
                    x, y, w, h,
                    0.14f, 0.55f, 0.20f,
                    0.24f, 0.78f, 0.31f,
                    0.82f, 0.010f, 1.80f, 0.030f
            );

            // Franja de brillo principal.
            drawRect(
                    x - (w * 0.18f), y,
                    w * 0.14f, h,
                    0.37f, 0.90f, 0.43f
            );

            // Franja secundaria para darle profundidad.
            drawRect(
                    x + (w * 0.05f), y,
                    w * 0.08f, h,
                    0.28f, 0.74f, 0.33f
            );

            // Sombra lateral.
            drawRect(
                    x + (w * 0.43f), y,
                    w * 0.10f, h,
                    0.10f, 0.42f, 0.16f
            );

            // Aro del borde cercano al gap (estilo clasico).
            if (h > 0.04f) {
                float capH = Math.min(0.055f, Math.max(0.020f, h * 0.20f));
                float capW = w * 1.16f;
                float capY = y + (y < 0.0f ? (h * 0.5f) : -(h * 0.5f));

                drawRectStyled(
                        x, capY, capW, capH,
                        0.17f, 0.62f, 0.23f,
                        0.30f, 0.86f, 0.36f,
                        0.90f, 0.006f, 1.50f, 0.020f
                );

                // Borde inferior/superior del aro para volumen.
                float lipY = capY + (y < 0.0f ? -(capH * 0.36f) : (capH * 0.36f));
                drawRect(
                        x, lipY, capW * 0.98f, capH * 0.20f,
                        0.10f, 0.38f, 0.14f
                );
            }
        }
    }

    public void drawBird(Bird bird, float[] color) {
        float angulo = bird.getInclinationRadians();
        setRotation(angulo);
        setPivot(bird.getX(), bird.getY());

        setWobble(0.0f);
        for (float[] seg : BirdGeometry.getTailSegments()) {
            drawRect(
                    bird.getX() + seg[0],
                    bird.getY() + seg[1],
                    seg[2],
                    seg[2],
                    color[0], color[1], color[2]
            );
        }

        BirdGeometry.Rect body = BirdGeometry.body(bird);
        setWobble(1.0f);
        drawCircle(
                body.getX(),
                body.getY(),
                body.getWidth() * 1.12f,
                body.getHeight() * 0.88f,
                color[0], color[1], color[2]
        );

        BirdGeometry.Rect beak = BirdGeometry.beak(bird);
        setWobble(0.0f);
        drawTriangle(beak.getX(), beak.getY(), beak.getWidth(), beak.getHeight(), 1.0f, 0.55f, 0.0f);

        float dark = 0.7f;
        for (float[] seg : BirdGeometry.getWingSegments()) {
            float flap = bird.getFlap();
            float alaAlto = seg[2] * (0.12f + 1.35f * flap);
            float alaY = bird.getY() + seg[1] + (0.01f * flap);
            drawRect(
                    bird.getX() + seg[0],
                    alaY,
                    seg[2],
                    alaAlto,
                    color[0] * dark, color[1] * dark, color[2] * dark
            );
        }

        BirdGeometry.Rect eye = BirdGeometry.eye(bird);
        drawCircle(eye.getX(), eye.getY(), eye.getWidth(), eye.getHeight(), 1f, 1f, 1f);

        BirdGeometry.Rect pupil = BirdGeometry.pupil(bird);
        drawCircle(pupil.getX(), pupil.getY(), pupil.getWidth(), pupil.getHeight(), 0f, 0f, 0f);

        setRotation(0.0f);
        setPivot(0.0f, 0.0f);
    }

    public void endRender() {
        GL30.glBindVertexArray(0);
    }

    public void cleanup() {
        GL30.glDeleteVertexArrays(quadVao);
        GL15.glDeleteBuffers(quadVbo);
        GL30.glDeleteVertexArrays(triangleVao);
        GL15.glDeleteBuffers(triangleVbo);
        GL30.glDeleteVertexArrays(circleVao);
        GL15.glDeleteBuffers(circleVbo);
        GL11.glDeleteTextures(cloudTextureId);
        GL20.glDeleteProgram(programa);
    }

    private int cargarTextura(String resourcePath) throws IOException {
        BufferedImage image;
        try (InputStream in = Renderer.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("No se encontro la textura: " + resourcePath);
            }
            image = ImageIO.read(in);
        }

        if (image == null) {
            throw new IOException("No se pudo decodificar la textura: " + resourcePath);
        }

        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = pixels[y * width + x];
                buffer.put((byte) ((argb >> 16) & 0xFF));
                buffer.put((byte) ((argb >> 8) & 0xFF));
                buffer.put((byte) (argb & 0xFF));
                buffer.put((byte) ((argb >> 24) & 0xFF));
            }
        }
        buffer.flip();

        int textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                GL11.GL_RGBA,
                width,
                height,
                0,
                GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE,
                buffer
        );

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        return textureId;
    }
}
