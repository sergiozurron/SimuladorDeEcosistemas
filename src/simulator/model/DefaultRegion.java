package simulator.model;

public class DefaultRegion extends Region {
	
	@Override
	public void update(double dt) {
	}

	@Override
	public double getFood(Animal a, double dt) {
		if (a.get_diet() == Diet.CARNIVORE) {
			return 0.0;
		} else {
			int n = getNumberOfAnimals(Diet.HERBIVORE);
			return 60.0 * Math.exp(-Math.max(0, n - 5.0) * 2.0) * dt;
		}
	}
	
	public String toString() {
		return "Default Region";
	}

}
