package simulator.factories;

import org.json.JSONObject;

import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;

public class SelectFirstBuilder extends Builder<SelectionStrategy> {
    public SelectFirstBuilder() {
        super("first", "Build a selection strategy for selecting the first object found");
    }

    @Override
    protected SelectFirst createInstance(JSONObject data) throws IllegalArgumentException {
        if (!data.isEmpty())
            throw new IllegalArgumentException("There has to be no data for first selection strategy");
        return new SelectFirst();
    }
}
