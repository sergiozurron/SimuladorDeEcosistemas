package simulator.control;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.model.AnimalInfo;
import simulator.model.MapInfo;
import simulator.model.Simulator;
import simulator.view.SimpleObjectViewer;
import simulator.view.SimpleObjectViewer.ObjInfo;

public class Controller {
	private final Simulator sim;

	public Controller(Simulator sim) {
		this.sim = sim;
	}

	public void loadData(JSONObject data) {
		JSONArray regionsArray = data.optJSONArray("regions");

		if (regionsArray != null) {
			for (int i = 0; i < regionsArray.length(); i++) {
				JSONObject obj = regionsArray.getJSONObject(i);

				JSONArray row = obj.getJSONArray("row");
				JSONArray col = obj.getJSONArray("col");
				JSONObject spec = obj.getJSONObject("spec");

				for (int r = row.getInt(0); r <= row.getInt(1); r++) {
					for (int c = col.getInt(0); c <= col.getInt(1); c++) {
						sim.setRegion(r, c, spec);
					}
				}
			}
		}

		JSONArray animalsArray = data.getJSONArray("animals");
		for (int i = 0; i < animalsArray.length(); i++) {
			JSONObject animal = animalsArray.getJSONObject(i);

			int amount = animal.getInt("amount");
			JSONObject spec = animal.getJSONObject("spec");

			for (int j = 0; j < amount ; j++) {
				sim.addAnimal(spec);
			}
		}
	}

	public void run(double t, double dt, boolean sv, OutputStream out) {
		// Initialize viewer
		SimpleObjectViewer view = null;
		if (sv) {
			MapInfo m = sim.getMapInfo();
			view = new SimpleObjectViewer("[ECOSYSTEM]", m.get_width(), m.get_height(), m.get_cols(), m.get_rows());
			view.update(toAnimalsInfo(sim.getAnimals()), sim.getSimulationTime(), dt);
		}
		// store initial and final simulation state
		JSONObject jo = new JSONObject();
		jo.put("in", sim.asJSON());

		// Main loop
		while (sim.getSimulationTime() <= t) {
			sim.advance(dt);
			if (sv)
				view.update(toAnimalsInfo(sim.getAnimals()), sim.getSimulationTime(), dt);
		}

		jo.put("out", sim.asJSON());

		// Write out the json
		PrintStream printStream = new PrintStream(out);
		printStream.println(jo);

		// Close view if viewer active
		if (sv)
			view.close();
	}

	private static List<ObjInfo> toAnimalsInfo(List<? extends AnimalInfo> animals) {
		// Map each object of the list with the method getObjInfo implemented in Animal
		// Then stored in a list
		return animals.stream().map(AnimalInfo::getObjInfo).toList();
	}
}
