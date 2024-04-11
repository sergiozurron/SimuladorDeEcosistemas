package simulator.model;

import simulator.misc.Utils;

public class DynamicSuppyRegion extends Region {

	private double food;
	private final double factor;

	public DynamicSuppyRegion(double initialFood, double growthFactor) throws IllegalArgumentException {
		if (initialFood < 0)
			throw new IllegalArgumentException("Initial food cannot be negative");
		food = initialFood;
		factor = growthFactor;
	}

	@Override
	public void update(double dt) {
		if (Utils.randomGenerator.nextDouble() > 0.5) {
			food -= dt * factor;
		}
	}

	@Override
	public double getFood(Animal a, double dt) {
		if (a.get_diet() == Diet.CARNIVORE) {
			return 0.0;
		} else {
			int n = getNumberOfAnimals(Diet.HERBIVORE);
			double eaten = Math.min(food, 60.0 * Math.exp(-Math.max(0, n - 5.0) * 2.0) * dt);
			food -= eaten;
			return eaten;
		}
	}
	
	public String toString() {
		return "Dynamic Region";
	}

}
