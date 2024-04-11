package simulator.model;

import org.json.JSONObject;

public interface JSONable {
	default JSONObject asJSON() {
		return new JSONObject();
	}
}
