package simulator.model;

import java.util.List;

public class SelectClosest implements SelectionStrategy{

	@Override
	public Animal select(Animal a, List<Animal> as) {
		Animal closest = null;
		if (!as.isEmpty()) {
			closest = as.get(0);
			for (int i = 1; i < as.size(); i++) {
				Animal animal = as.get(i);
				if (a.get_position().distanceTo(animal.get_position()) < a.get_position().distanceTo(closest.get_position()) ) {
					closest = animal;
				}
			}
		}
		return closest;
	}
	
}
