package simulator.factories;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;
import simulator.model.Animal;
import simulator.model.SelectionStrategy;
import simulator.model.Wolf;

public class WolfBuilder extends Builder<Animal> {

    public static final JSONObject DEFAULT_SELECTION_STRATEGY = new JSONObject("""
            {
            "type": "first",
            "data": {}
            }""");
    public static final String MATE_STRATEGY_KEY = "mate_strategy";
    public static final String HUNT_STRATEGY_KEY = "danger_strategy";
    private final Factory<SelectionStrategy> selectionStrategyFactory;

    public WolfBuilder(Factory<SelectionStrategy> selectionStrategyFactory) throws IllegalArgumentException {
        super("wolf", "Builds a wolf");
        if (selectionStrategyFactory == null) {
            throw new IllegalArgumentException("Selection Strategy Factory must be initialized");
        }
        this.selectionStrategyFactory = selectionStrategyFactory;
    }


    @Override
    protected Wolf createInstance(JSONObject data) throws IllegalArgumentException {
        JSONObject mateJ = data.has(MATE_STRATEGY_KEY) ? data.getJSONObject(MATE_STRATEGY_KEY) : DEFAULT_SELECTION_STRATEGY;
        JSONObject huntJ = data.has(HUNT_STRATEGY_KEY) ? data.getJSONObject(HUNT_STRATEGY_KEY) : DEFAULT_SELECTION_STRATEGY;
        SelectionStrategy mate = selectionStrategyFactory.createInstance(mateJ);
        SelectionStrategy hunt = selectionStrategyFactory.createInstance(huntJ);
        JSONObject position = data.optJSONObject("pos");
        Vector2D pos = null;
        if (position != null) {
            try {
                JSONArray xRange = position.getJSONArray("x_range");
                JSONArray yRange = position.getJSONArray("y_range");
                if (xRange.length() != 2 || yRange.length() != 2)
                    throw new IllegalArgumentException("x_range or y_range have to have exactly two values");
                double x = Utils.randomGenerator.nextDouble(xRange.getDouble(0), xRange.getDouble(1));
                double y = Utils.randomGenerator.nextDouble(yRange.getDouble(0), yRange.getDouble(1));
                pos = new Vector2D(x, y);
            } catch (JSONException je) {
                throw new IllegalArgumentException(je);
            }
        }

        return new Wolf(mate, hunt, pos);
    }

    @Override
    protected void fillInData(JSONObject o) {
        o.put(MATE_STRATEGY_KEY, DEFAULT_SELECTION_STRATEGY);
        o.put(HUNT_STRATEGY_KEY, DEFAULT_SELECTION_STRATEGY);
        o.put("pos", new JSONObject("{ \"x\": [ 100.0, 200.0 ], \"y\":[ 100.0, 200.0 ] }"));
    }
}
