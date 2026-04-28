package ${packageName};

import de.gurkenlabs.litiengine.*;
import de.gurkenlabs.litiengine.resources.Resources;

/**
 *
 * @see <a href="https://litiengine.com/docs/">LITIENGINE Documentation</a>
 *
 */

public class Main {
    public static void main(String[] args) {
        // set meta information about the game
        Game.info().setName("${projectName}");
        Game.info().setSubTitle("");
        Game.info().setVersion("v0.0.1");
        Game.info().setWebsite("link to game");
        Game.info().setDescription("A 2D Game made in the LITIENGINE");

        // init the game infrastructure
        Game.init(args);

        // set the icon for the game (this has to be done after initialization because the ScreenManager will not be present otherwise)
        // Game.window().setIcon(Resources.images().get("path/to/icon/image"));
        Game.graphics().setBaseRenderScale(4f);

        // load data from the utiLITI game file
        Resources.load("game.litidata");

        // load the first level (resources for the map were implicitly loaded from the game file)
        // Game.world().loadEnvironment("path/to/level");
        Game.start();
    }
}
