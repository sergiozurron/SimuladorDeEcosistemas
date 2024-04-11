package simulator.factories;

import org.json.JSONObject;

import simulator.model.SelectYoungest;
import simulator.model.SelectionStrategy;

public class SelectYoungestBuilder extends Builder<SelectionStrategy> {
    public SelectYoungestBuilder() {
        super("youngest", "Build a selection strategy for selecting the youngest object found");
    }

    @Override
    protected SelectYoungest createInstance(JSONObject data) throws IllegalArgumentException{
        if (!data.isEmpty())
            throw new IllegalArgumentException("JSONObject has to have information");
        return new SelectYoungest();
    }
}
