package simulator.model;

import java.util.List;

public class SelectYoungest implements SelectionStrategy {
	@Override
	public Animal select(Animal a, List<Animal> as) {
		Animal youngest = null;
		if (!as.isEmpty()) {
			youngest = as.get(0);
			for (Animal animal : as) {
				if (animal.get_age() < youngest.get_age()) {
					youngest = animal;
				}
			}
		}
		return youngest;
	}
}
