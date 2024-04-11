package simulator.model;

import java.util.List;
import java.util.function.Predicate;

public interface AnimalMapView extends MapInfo, FoodSupplier {
	List<Animal> getAnimalsInRange(Animal e, Predicate<Animal> filter);
}
