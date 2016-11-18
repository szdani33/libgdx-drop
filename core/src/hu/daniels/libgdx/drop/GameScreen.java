package hu.daniels.libgdx.drop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

/**
 * Created by Dani on 2016.11.18..
 */
public class GameScreen implements Screen {
    private final Drop game;

	private Texture droplet;
	private Texture bucketImage;

	private Rectangle bucket;
    private Array<Rectangle> raindrops;

    private long lastDropTime;

    private Sound dropSound;
	private Music rainMusic;
    private Vector3 touchPos;

    private OrthographicCamera camera;

    private int dropsGathered;

    public GameScreen(Drop game) {
        this.game = game;

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

		droplet = new Texture(Gdx.files.internal("droplet.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));

        touchPos = new Vector3();

		bucket = new Rectangle();
		bucket.x = 800 / 2 - 64 / 2;
		bucket.y = 20;
		bucket.width = 64;
		bucket.height = 64;

        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
        rainMusic.setLooping(true);

        raindrops = new Array<Rectangle>();
        spawnRaindrop();
    }

    @Override
    public void show() {
        rainMusic.play();
    }

    @Override
    public void render(float delta) {
        handleUserInput();
        updateGame();
        updateDisplay();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        droplet.dispose();
        bucketImage.dispose();
        dropSound.dispose();
        rainMusic.dispose();
    }

	private void spawnRaindrop() {
        Rectangle raindrop = new Rectangle();
        raindrop.x = MathUtils.random(0, 800 - 64);
        raindrop.y = 480;
        raindrop.width = 64;
        raindrop.height = 64;
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
    }

    private void updateGame() {
        if(TimeUtils.nanoTime() - lastDropTime > 1000000000) {
            spawnRaindrop();
        }

        Iterator<Rectangle> iterator = raindrops.iterator();
        while (iterator.hasNext()) {
            Rectangle raindrop = iterator.next();
            raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
            if(raindrop.y + 64 < 0) {
                iterator.remove();
            }
            if(raindrop.overlaps(bucket)) {
                dropSound.play();
                iterator.remove();
                dropsGathered++;
            }
        }
    }

    private void updateDisplay() {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        game.font.draw(game.batch, "Drops caught: " + dropsGathered, 10, 470);
        game.batch.draw(bucketImage, bucket.x, bucket.y);
        for(Rectangle raindrop : raindrops) {
            game.batch.draw(droplet, raindrop.x, raindrop.y);
        }
        game.batch.end();
    }

    private void handleUserInput() {
        if(Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = touchPos.x - 64 / 2;
        }

        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bucket.x -= 200 * Gdx.graphics.getDeltaTime();
        }

        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            bucket.x += 200 * Gdx.graphics.getDeltaTime();
        }

        if(bucket.x < 0) {
            bucket.x = 0;
        }

        if(bucket.x > 800 - 64) {
            bucket.x = 800 - 64;
        }
    }
}
