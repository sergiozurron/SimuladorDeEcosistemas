package simulator.model;

import simulator.misc.Vector2D;
import simulator.view.SimpleObjectViewer.ObjInfo;

public interface AnimalInfo extends JSONable {
	State get_state();

	Vector2D get_position();

	String get_geneticCode();

	Diet get_diet();

	double get_speed();

	double get_sightRange();

	double get_energy();

	double get_age();

	Vector2D getDestination();

	boolean isPregnant();

	ObjInfo getObjInfo();
}
