package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.*;
import com.jogamp.graph.font.Font;

import java.util.Arrays;


public class Game extends ApplicationAdapter {
    public static Game game;
    private BitmapFont font;
    private BitmapFont font2;
    private SpriteBatch batch;
    private float stateTime;
    private ShapeRenderer shapeRenderer;
    private InputHandler inputHandler;
    private Player jet;
    private Enemy enemyJet;
    private TextureAtlas atlas;
    private Stage display;
    private Stage ui;

    private Viewport viewDisplay;
    private Viewport viewUI;
    private Label killCount;

    @Override
    public void create() {

        game = this;
        stateTime = 0;
        viewDisplay = new ScreenViewport();
        display = new Stage(viewDisplay);
        viewUI = new ExtendViewport(800,600);
        ui = new Stage(viewUI);

        inputHandler = this.new InputHandler();
        Gdx.input.setInputProcessor(inputHandler);

        font = new BitmapFont(Gdx.files.internal("Fonts/dontWorry.fnt"));
        font2 = new BitmapFont(Gdx.files.internal("Fonts/font2.fnt"));
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        atlas = new TextureAtlas(Gdx.files.internal("SpriteAtlas/Sprites.atlas"));

        jet = new Player();
        jet.setSize(140, 60);
        jet.setPosition(100, 100);
        jet.setOrigin(Align.center);
        jet.setForceField(new ForceField(jet));

        Label.LabelStyle font1 = new Label.LabelStyle(font, Color.BLUE);
        killCount = new Label("Kill Count: ", font1);
        killCount.setPosition(20, ui.getViewport().getWorldHeight()-killCount.getHeight());
        ui.getActors().add(killCount);
        display.addActor(jet);

    }

    @Override
    public void resize(int width, int height) {
        // See below for what true means.
        display.getViewport().update(width, height, true);
        ui.getViewport().update(width,height,true);

    }

    @Override
    public void render() {
        stateTime += Gdx.graphics.getDeltaTime();
        Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl20.glClearColor(0, 0, 0, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);



        viewDisplay.getCamera().position.set(jet.getX() + jet.getOriginX(), jet.getY() + jet.getOriginY(), 0);
        inputHandler.handleInput();
        viewDisplay.apply();
        display.act();
        display.draw();

        batch.setProjectionMatrix(viewDisplay.getCamera().combined);
        batch.begin();
        font.draw(batch, "Welcome to JetStrike", 100, 100);
        font2.draw(batch, "JetStrike is a jet fighter game where you destroy enemy jets!", 100, 60);
        batch.end();

        viewUI.apply();
        ui.act();
        ui.draw();

    }

    @Override
    public void dispose() {

        display.dispose();
        batch.dispose();
        shapeRenderer.dispose();
    }


    private class InputHandler implements InputProcessor {
        private boolean keyPressed;
        private Ship[] ships;
        private Projectile[] projectiles;
        private int keycode;
        private boolean mousePressing;
        private float mouseX;
        private float mouseY;
        private float mouseDirection;

        @Override
        public boolean keyDown(int keycode) {
            System.out.println(keycode + " pressed");
            this.keycode = keycode;
            return keyPressed = true;
        }

        @Override
        public boolean keyUp(int keycode) {
            System.out.println(keycode + " released");
            this.keycode = keycode;
            return keyPressed = false;
        }

        @Override
        public boolean keyTyped(char character) {
            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            System.out.println("down");
            mousePressing = true;
            return true;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            System.out.println("up");
            mousePressing = false;
            return true;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {

            return false;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {

            return false;
        }

        @Override
        public boolean scrolled(float amountX, float amountY) {
            return false;
        }

        public void handleInput() {

            Vector2 mousePosition = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            mousePosition = viewDisplay.unproject(mousePosition);
            mouseX = mousePosition.x;
            mouseY = mousePosition.y;
            projectiles = getProjectiles();
            ships = getShips();
            setJetDirection();
            updateProjectiles();
            checkProjectiles();

            if (mousePressing) {
                Projectile.shootProjectile(jet);
            }

            if (keyPressed) {

                switch (keycode) {

                    case Input.Keys.W:
                        moveJet(4.5f);
                        break;
                    case Input.Keys.R:
                        enemyJet = new Enemy();
                        enemyJet.setSize(150, 60);
                        enemyJet.setPosition(MathUtils.random(1000), MathUtils.random(1000));
                        enemyJet.setOrigin(Align.center);
                        enemyJet.setForceField(new ForceField(enemyJet));

                        display.addActor(enemyJet);
                        break;
                    default:

                        break;
                }
            }
        }
        private Ship[] getShips() {
            Ship[] ships = new Ship[0];
            for (Actor actor : display.getActors()) {
                if (actor instanceof Ship) {
                    ships = Arrays.copyOf(ships, ships.length + 1);
                    ships[ships.length - 1] = (Ship) actor;
                }
            }
            return ships;
        }

        private Projectile[] getProjectiles() {
            Projectile[] projectiles = new Projectile[0];
            for (Actor actor : display.getActors()) {
                if (actor instanceof Projectile) {
                    projectiles = Arrays.copyOf(projectiles, projectiles.length + 1);
                    projectiles[projectiles.length - 1] = (Projectile) actor;
                }
            }
            return projectiles;
        }

        private void checkProjectiles() {
            for (Ship ship : ships)
                for (Projectile projectile : projectiles) {
                    if (projectile.isExploded()) {
                        display.getActors().removeValue(projectile, false);
                        continue;
                    }
                    if (ship.contains(projectile.getX(), projectile.getY())) {

                        projectile.onShipCollision(ship);
                        break;
                    }
                }

        }


        private void updateProjectiles() {

            for (Actor actor : display.getActors()) {
                if (actor instanceof Projectile) {
                    Projectile projectile = (Projectile) actor;
                    if (projectile.getAnimation() == null)
                    if (!projectile.sendProjectile())
                        display.getActors().removeValue(actor, false);

                }

            }
        }

        public void moveJet(float speed) {
            jet.setX((float) (jet.getX() + speed * Math.cos(Math.toRadians(mouseDirection))));
            jet.setY((float) (jet.getY() + speed * Math.sin(Math.toRadians(mouseDirection))));
            jet.setRotation(mouseDirection);
        }

        public void setJetDirection() {
            mouseDirection = (float) Math.toDegrees(Math.atan2(
                    mouseY - (jet.getY() + jet.getOriginY()),
                    mouseX - (jet.getX() + jet.getOriginX())
            ));
            jet.setRotation(mouseDirection);
        }

    }

    public BitmapFont getFont2() {
        return font2;
    }

    public void setFont2(BitmapFont font2) {
        this.font2 = font2;
    }

    public Enemy getEnemyJet() {
        return enemyJet;
    }

    public Stage getUi() {
        return ui;
    }

    public void setUi(Stage ui) {
        this.ui = ui;
    }

    public Viewport getViewDisplay() {
        return viewDisplay;
    }

    public void setViewDisplay(Viewport viewDisplay) {
        this.viewDisplay = viewDisplay;
    }

    public Viewport getViewUI() {
        return viewUI;
    }

    public void setViewUI(Viewport viewUI) {
        this.viewUI = viewUI;
    }

    public Label getKillCount() {
        return killCount;
    }

    public void setKillCount(Label killCount) {
        this.killCount = killCount;
    }

    public void setEnemyJet(Enemy enemyJet) {
        this.enemyJet = enemyJet;
    }

    public float getStateTime() {
        return stateTime;
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }

    public void setAtlas(TextureAtlas atlas) {
        this.atlas = atlas;
    }

    public Viewport getView() {
        return viewDisplay;
    }

    public void setView(Viewport view) {
        this.viewDisplay = view;
    }

    public BitmapFont getFont() {
        return font;
    }

    public void setFont(BitmapFont font) {
        this.font = font;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public void setBatch(SpriteBatch batch) {
        this.batch = batch;
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    public void setShapeRenderer(ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
    }

    public InputHandler getInputHandler() {
        return inputHandler;
    }

    public void setInputHandler(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    public Player getJet() {
        return jet;
    }

    public void setJet(Player jet) {
        this.jet = jet;
    }

    public Stage getDisplay() {
        return display;
    }

    public void setDisplay(Stage display) {
        this.display = display;
    }
}
