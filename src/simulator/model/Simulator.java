package simulator.model;

import org.json.JSONObject;
import simulator.factories.Factory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Simulator implements JSONable, Observable<EcoSysObserver> {
	
    private final Factory<Animal> animalsFactory;
    private final Factory<Region> regionsFactory;
    private RegionManager regionManager;
    private List<Animal> animals;
    private double simulationTime;
    private List<EcoSysObserver> observers;

    public Simulator(int cols, int rows, int width, int height, Factory<Animal> animalsFactory,
                     Factory<Region> regionsFactory) {
        this.animalsFactory = animalsFactory;
        this.regionsFactory = regionsFactory;
        this.regionManager = new RegionManager(cols, rows, width, height);
        this.animals = new ArrayList<>();
        this.simulationTime = 0.0;
        this.observers = new ArrayList<>();
    }

    private void setRegion(int row, int col, Region r) {
        regionManager.setRegion(row, col, r);
    }

    public void setRegion(int row, int col, JSONObject rJson) {
    Region r = regionsFactory.createInstance(rJson);
    	setRegion(row, col, r);
        Iterator<EcoSysObserver> it = observers.iterator();
        while(it.hasNext())
        	it.next().onRegionSet(row, col, regionManager, r);
        // Más elegante
        // observers.forEach(o -> o.onRegionSet(row, col, regionManager, r);
    }

    private void addAnimal(Animal a) {
        this.animals.add(a);
        this.regionManager.registerAnimal(a);
        Iterator<EcoSysObserver> it = observers.iterator();
        while(it.hasNext())
        	it.next().onAnimalAdded(simulationTime, regionManager, new ArrayList<>(animals), a);
    }

    public void addAnimal(JSONObject aJson) {
        addAnimal(animalsFactory.createInstance(aJson));
    }

    public MapInfo getMapInfo() {
        return regionManager;
    }

    public List<? extends AnimalInfo> getAnimals() {
        return animals;
    }

    public double getSimulationTime() {
        return simulationTime;
    }

    public void advance(double dt) {
        simulationTime += dt;
        regionManager.removeDeadAnimals();

        animals = animals.stream().filter(Animal::isAlive).collect(Collectors.toList());

        List<Animal> born = new ArrayList<>();
        for (Animal a : animals) {
            a.update(dt);
            regionManager.updateAnimalRegion(a);
            regionManager.updateAllRegions(dt);
            if (a.isPregnant()) {
                born.add(a.deliverBaby());
            }
        }

        born.forEach(this::addAnimal);
        observers.forEach(o -> o.onAdvanced(simulationTime, regionManager, new ArrayList<>(animals), dt));
    }

    @Override
    public JSONObject asJSON() {
        return new JSONObject().put("time", simulationTime).put("state", regionManager.asJSON());
    }
    
public void reset(int cols, int rows, int width, int height) {
    	animals = new ArrayList<Animal>();
    	regionManager = new RegionManager(cols, rows, width, height);
    	simulationTime = 0.0;
    	Iterator<EcoSysObserver> it = observers.iterator();
    	while(it.hasNext())
    		it.next().onReset(simulationTime, regionManager, new ArrayList<>(animals));
    }

	@Override
	public void addObserver(EcoSysObserver o) {
		if(!observers.contains(o)) {
			observers.add(o);
			o.onRegister(simulationTime, regionManager, new ArrayList<>(animals));
		}
	}

	@Override
	public void removeObserver(EcoSysObserver o) {
		observers.remove(o);
	}
}
