package simulator.factories;

import org.json.JSONObject;

import simulator.model.DefaultRegion;
import simulator.model.Region;

public class DefaultRegionBuilder extends Builder<Region> {
    public DefaultRegionBuilder() {
        super("default", "Builds a dynamic supply region");
    }

    @Override
    protected DefaultRegion createInstance(JSONObject data) {
        return new DefaultRegion();
    }
    
}
