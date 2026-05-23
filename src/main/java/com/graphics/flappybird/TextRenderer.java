package com.graphics.flappybird;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTruetype;

public class TextRenderer {

    private static final int BITMAP_W = 512;
    private static final int BITMAP_H = 512;
    private static final int FIRST_CHAR = 32;
    private static final int NUM_CHARS = 96;

    private int program;
    private int vao;
    private int vbo;
    private int texId;
    private int uColorLocation;

    private STBTTBakedChar.Buffer cdata;

    public void init(String fontResourcePath, int fontSize) throws IOException {
        ByteBuffer ttf = readResource(fontResourcePath);

        ByteBuffer bitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);
        cdata = STBTTBakedChar.malloc(NUM_CHARS);

        int result = STBTruetype.stbtt_BakeFontBitmap(
                ttf, fontSize, bitmap, BITMAP_W, BITMAP_H, FIRST_CHAR, cdata
        );
        if (result <= 0) {
            throw new IOException("No se pudo hornear la fuente");
        }

        texId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RED, BITMAP_W, BITMAP_H, 0, GL11.GL_RED, GL11.GL_UNSIGNED_BYTE, bitmap);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        createShader();
        createBuffers();

        GL20.glUseProgram(program);
        int uTex = GL20.glGetUniformLocation(program, "uTex");
        GL20.glUniform1i(uTex, 0);
    }

    public void drawText(String text, float xPx, float yPx, float scale, float r, float g, float b, int screenW, int screenH) {
        if (text == null || text.isEmpty()) {
            return;
        }

        FloatBuffer buffer = BufferUtils.createFloatBuffer(text.length() * 6 * 4);
        float x = xPx;
        float y = yPx;

        FloatBuffer xBuf = BufferUtils.createFloatBuffer(1);
        FloatBuffer yBuf = BufferUtils.createFloatBuffer(1);

        STBTTAlignedQuad q = STBTTAlignedQuad.malloc();

        for (int i = 0; i < text.length(); i++) {
            int c = text.charAt(i);
            if (c < FIRST_CHAR || c >= FIRST_CHAR + NUM_CHARS) {
                continue;
            }

            xBuf.put(0, x);
            yBuf.put(0, y);

            STBTruetype.stbtt_GetBakedQuad(cdata, BITMAP_W, BITMAP_H, c - FIRST_CHAR, xBuf, yBuf, q, true);

            x = xBuf.get(0);
            y = yBuf.get(0);

            float x0 = ndcX(q.x0() * scale, screenW);
            float x1 = ndcX(q.x1() * scale, screenW);
            float y0 = ndcY(q.y0() * scale, screenH);
            float y1 = ndcY(q.y1() * scale, screenH);

            // Triangulo 1
            buffer.put(x0).put(y0).put(q.s0()).put(q.t0());
            buffer.put(x1).put(y0).put(q.s1()).put(q.t0());
            buffer.put(x1).put(y1).put(q.s1()).put(q.t1());
            // Triangulo 2
            buffer.put(x0).put(y0).put(q.s0()).put(q.t0());
            buffer.put(x1).put(y1).put(q.s1()).put(q.t1());
            buffer.put(x0).put(y1).put(q.s0()).put(q.t1());
        }

        q.free();
        buffer.flip();

        GL20.glUseProgram(program);
        GL20.glUniform3f(uColorLocation, r, g, b);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);

        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_DYNAMIC_DRAW);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, buffer.limit() / 4);
        GL30.glBindVertexArray(0);
    }

    public void cleanup() {
        if (cdata != null) {
            cdata.free();
        }
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
        GL20.glDeleteProgram(program);
        GL11.glDeleteTextures(texId);
    }

    public float measureTextWidth(String text, float scale) {
        if (text == null || text.isEmpty()) {
            return 0f;
        }

        float x = 0f;
        float y = 0f;

        java.nio.FloatBuffer xBuf = org.lwjgl.BufferUtils.createFloatBuffer(1);
        java.nio.FloatBuffer yBuf = org.lwjgl.BufferUtils.createFloatBuffer(1);
        org.lwjgl.stb.STBTTAlignedQuad q = org.lwjgl.stb.STBTTAlignedQuad.malloc();

        for (int i = 0; i < text.length(); i++) {
            int c = text.charAt(i);
            if (c < FIRST_CHAR || c >= FIRST_CHAR + NUM_CHARS) {
                continue;
            }
            xBuf.put(0, x);
            yBuf.put(0, y);
            org.lwjgl.stb.STBTruetype.stbtt_GetBakedQuad(cdata, BITMAP_W, BITMAP_H, c - FIRST_CHAR, xBuf, yBuf, q, true);
            x = xBuf.get(0);
            y = yBuf.get(0);
        }

        q.free();
        return x * scale;
    }

    public void drawHud(int p1, int p2, float velocidad, int screenW, int screenH) {
        String p1Text = "P1: " + p1;
        String p2Text = "P2: " + p2;
        String vText = "V: " + String.format(java.util.Locale.US, "%.2f", velocidad);

        float scale = 1.5f;
        float y = 65f;

        float p1W = measureTextWidth(p1Text, scale);
        float p2W = measureTextWidth(p2Text, scale);
        float vW = measureTextWidth(vText, scale);

        float xP1 = (screenW * 0.25f) - (p1W * 2.3f);
        float xP2 = (screenW * 0.75f) - (p2W * 2.3f);
        float xV = (screenW * 0.5f) - (vW * 1.9f);

        // Colores alineados con los jugadores.
        drawText(p1Text, xP1, y, scale, 0.98f, 0.85f, 0.20f, screenW, screenH);
        drawText(p2Text, xP2, y, scale, 0.18f, 0.70f, 0.25f, screenW, screenH);
        // Velocidad en azul.
        drawText(vText, xV, y, scale, 0.35f, 0.62f, 1.0f, screenW, screenH);
    }

    private void createShader() {
        String vertexSrc = """
            #version 330 core
            layout (location = 0) in vec2 aPos;
            layout (location = 1) in vec2 aUv;
            out vec2 vUv;
            void main() {
                vUv = aUv;
                gl_Position = vec4(aPos, 0.0, 1.0);
            }
            """;

        String fragmentSrc = """
            #version 330 core
            uniform sampler2D uTex;
            uniform vec3 uColor;
            in vec2 vUv;
            out vec4 fragColor;
            void main() {
                float a = texture(uTex, vUv).r;
                fragColor = vec4(uColor, a);
            }
            """;

        int vs = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vs, vertexSrc);
        GL20.glCompileShader(vs);

        int fs = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fs, fragmentSrc);
        GL20.glCompileShader(fs);

        program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vs);
        GL20.glAttachShader(program, fs);
        GL20.glLinkProgram(program);

        GL20.glDeleteShader(vs);
        GL20.glDeleteShader(fs);

        uColorLocation = GL20.glGetUniformLocation(program, "uColor");
    }

    private void createBuffers() {
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();

        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);

        int stride = 4 * Float.BYTES;
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, stride, 0);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, stride, 2L * Float.BYTES);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);

        GL30.glBindVertexArray(0);
    }

    private float ndcX(float xPx, int w) {
        return (xPx / w) * 2.0f - 1.0f;
    }

    private float ndcY(float yPx, int h) {
        return 1.0f - (yPx / h) * 2.0f;
    }

    private ByteBuffer readResource(String path) throws IOException {
        try (InputStream in = TextRenderer.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("No se encontro el recurso: " + path);
            }
            byte[] bytes = in.readAllBytes();
            ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
            buffer.put(bytes).flip();
            return buffer;
        }
    }
}
