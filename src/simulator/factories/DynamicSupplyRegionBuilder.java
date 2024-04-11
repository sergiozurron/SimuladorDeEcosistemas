package simulator.factories;

import org.json.JSONObject;

import simulator.model.DynamicSuppyRegion;
import simulator.model.Region;

public class DynamicSupplyRegionBuilder extends Builder<Region> {

    public static final String FACTOR_KEY = "factor";
    public static final String FOOD_KEY = "food";
    public static final double DEFAULT_FACTOR = 2.0;
    public static final double DEFAULT_FOOD = 100.0;

    public DynamicSupplyRegionBuilder() {
        super("dynamic", "Builds a dynamic supply region");
    }


    @Override
    protected DynamicSuppyRegion createInstance(JSONObject data) {
        double factor = data.has(FACTOR_KEY) ? data.getDouble(FACTOR_KEY) : DEFAULT_FACTOR;
        double food = data.has(FOOD_KEY) ? data.getDouble(FOOD_KEY) : DEFAULT_FOOD;
        return new DynamicSuppyRegion(food, factor);
    }

    @Override
    protected void fillInData(JSONObject o) {
        o.put(FACTOR_KEY, 2.5);
        o.put(FOOD_KEY,1250.0);
    }
}
