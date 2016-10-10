package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.GameActivity;
import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.locale.Level;
import com.linguaculturalists.phoenicia.locale.tour.Stop;
import com.linguaculturalists.phoenicia.models.DefaultTile;
import com.linguaculturalists.phoenicia.models.GameTile;
import com.linguaculturalists.phoenicia.models.WordTile;
import com.linguaculturalists.phoenicia.tour.TourOverlay;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.Entity;
import org.andengine.entity.IEntity;
import org.andengine.input.touch.TouchEvent;
import org.andengine.util.debug.Debug;

import java.util.EmptyStackException;
import java.util.Stack;

/**
 * Actual AndEngine HUD class used by the game scene
 *
 * Manages a stack of PhoeniciaHUD instances, with only the top one being displayed
 */
public class HUDManager extends HUD {

    public PhoeniciaHUD currentHUD; /**< The current top-most HUD on the stack */
    public Stack<PhoeniciaHUD> hudStack; /**< stack of PhoeniciaHUD instances */
    private PhoeniciaGame game;
    private PhoeniciaHUD nextHUD;
    private float transitionWait = 0;

    private Entity hudLayer;
    private TourOverlay tourLayer;


    /**
     * Create a new instance for the given PhoeniciaGame
     * @param game game to attach HUDs to
     */
    public HUDManager(final PhoeniciaGame game) {
        this.game = game;
        this.hudStack = new Stack<PhoeniciaHUD>();
        this.hudLayer = new Entity(GameActivity.CAMERA_WIDTH/2, GameActivity.CAMERA_HEIGHT/2, GameActivity.CAMERA_WIDTH, GameActivity.CAMERA_HEIGHT);
        this.attachChild(this.hudLayer);
    }

    public void startTour(Stop stop) {
        this.tourLayer = new TourOverlay(game, stop);
        this.setChildScene(this.tourLayer);
        this.tourLayer.show();
    }

    public void endTour() {
        this.clearChildScene();
        this.tourLayer = null;
    }

    /**
     * Create a new instance of the default game hud and display it
     */
    public void showDefault() {
        this.push(new DefaultHUD(this.game));
    }

    /**
     * Create a new instance of the new level items HUD and display it
     * @param level
     */
    public void showNewLevel(final Level level) {
        this.clear();
        // Place the intro HUD just below the new level HUD so the player sees it next
        if (level.intro.size() > 0) {
            if (this.currentHUD != null) {
                this.hudStack.push(this.currentHUD);
                this.currentHUD.hide();
            }
            this.currentHUD = new LevelIntroHUD(this.game, level);
            this.currentHUD.open();
        }
        this.push(new NewLevelHUD(this.game, level));
    }

    /**
     * Create a new instance of the inter-level introduction HUD and display it
     * @param level
     */
    public void showLevelIntro(final Level level) {
        this.set(new LevelIntroHUD(this.game, level));
    }

    public void showNextLevelReq(final Level level) {
        this.set(new NextLevelRequirementsHUD(this.game, level));
    }
    /**
     * Create a new instance of the inventory management HUD and display it
     */
    public void showInventory() {
        InventoryHUD inventoryHUD = new InventoryHUD(this.game);
        this.push(inventoryHUD);
    }

    /**
     * Create a new instance of the marketplace HUD and display it
     */
    public void showMarket() {
        MarketHUD marketHUD = new MarketHUD(this.game);
        this.push(marketHUD);
    }

    /**
     * Create a new instance of the workshop HUD and display it
     */
    public void showWorkshop(final DefaultTile tile) {
        WorkshopHUD workshopHUD = new WorkshopHUD (this.game, this.game.locale.level_map.get(this.game.current_level), tile);
        this.push(workshopHUD);
    }

    /**
     * Create a new instance of the game placement HUD for the specified game level
     */
    public void showDecorationPlacement() {
        this.push(new DecorationPlacementHUD(this.game));
    }

    /**
     * Create a new instance of the letter placement HUD for the current game level
     */
    public void showLetterPlacement() {
        this.showLetterPlacement(this.game.locale.level_map.get(this.game.current_level));
    }
    /**
     * Create a new instance of the letter placement HUD for the specified game level
     */
    public void showLetterPlacement(final Level level) {
        LetterPlacementHUD letterPlacementHUD = new LetterPlacementHUD(this.game, level);
        this.push(letterPlacementHUD);
    }

    /**
     * Create a new instance of the word placement HUD for the current game level
     */
    public void showWordPlacement() {
        this.showWordPlacement(this.game.locale.level_map.get(this.game.current_level));
    }
    /**
     * Create a new instance of the word placement HUD for the specified game level
     */
    public void showWordPlacement(final Level level) {
        this.push(new WordPlacementHUD(this.game, level));
    }

    /**
     * Create a new instance of the word building HUD for the specified word
     */
    public void showWordBuilder(final Level level, final WordTile tile) {
        WordBuilderHUD hud = new WordBuilderHUD(this.game, level, tile);
        this.push(hud);
    }

    /**
     * Create a new instance of the game placement HUD for the specified game level
     */
    public void showGamePlacement() {
        this.push(new GamePlacementHUD(this.game));
    }

    /**
     * Create a new instance of the game building HUD for the specified game
     */
    public void showGame(final Level level, final GameTile tile) {
        //TODO: Show appropriate game hud
        Debug.d("Opening game of type: " + tile.game.type);
        if (tile.game.type.equals("wordmatch")) {
            this.push(new WordMatchGameHUD(this.game, level, tile));
        } else if (tile.game.type.equals("imagematch")) {
                this.push(new ImageMatchGameHUD(this.game, level, tile));
        } else {
            Debug.w("Unknown game type: " + tile.game.type);
        }
    }

    public void showDebugMode() {
        DebugHUD hud = new DebugHUD(this.game);
        this.push(hud);
    }
    /**
     * Remove everything except the DefaultHUD from the stack
     */
    public synchronized void clear() {
        while (this.hudStack.size() >= 1) {
            this.pop();
        }
    }

    /**
     * Hide the currently displayed HUD, and show the one below it on the stack
     */
    public synchronized void pop() {
        Debug.d("Popping one off the HUD stack");
        try {
            PhoeniciaHUD previousHUD = this.hudStack.pop();
            if (previousHUD != null) {
                this.currentHUD.hide();
                this.currentHUD.close();
                this.setHudLayer(previousHUD);
                this.currentHUD = previousHUD;
                previousHUD.show();
            }
        } catch (EmptyStackException e) {
            Debug.d("Nothing to pop off the stack");
            return;
        }
    }

    /**
     * Remove everything except the DefaultHUD and push newHUD on top of that
     * @param newHUD
     */
    public void set(PhoeniciaHUD newHUD) {
        this.clear();
        this.push(newHUD);
    }

    /**
     * Hide the current displayed HUD, and add the given HUD to the top of the stack and display it
     * @param newHUD new HUD to add to the top of the stack
     */
    public void push(PhoeniciaHUD newHUD) {
        if (this.currentHUD != null) {
            this.hudStack.push(this.currentHUD);
            this.currentHUD.hide();
        }
        newHUD.open();
        this.setHudLayer(newHUD);
        this.currentHUD = newHUD;
        newHUD.show();
    }

    private void setHudLayer(PhoeniciaHUD hud) {
        this.hudLayer.detachChildren();
        this.hudLayer.attachChild(hud);
    }

    public boolean isHudLayerVisible() {
        return this.hudLayer.isVisible();
    }
    public void setHudLayerVisible(boolean visible) {
        this.hudLayer.setVisible(visible);
    }
    @Override
    public boolean onSceneTouchEvent(TouchEvent pSceneTouchEvent) {
        final boolean handled = super.onSceneTouchEvent(pSceneTouchEvent);
        return handled || this.currentHUD.onSceneTouchEvent(pSceneTouchEvent);
    }
}
