package simulator.factories;

import org.json.JSONObject;

import simulator.model.SelectClosest;
import simulator.model.SelectionStrategy;

public class SelectClosestBuilder extends Builder<SelectionStrategy> {
	public SelectClosestBuilder() {
		super("closest", "Build a selection strategy for selecting the closest object found to other object");
	}

	@Override
	protected SelectClosest createInstance(JSONObject data) throws IllegalArgumentException{
		if (!data.isEmpty())
			throw new IllegalArgumentException("JSONObject has to have information");
		return new SelectClosest();
	}
}
