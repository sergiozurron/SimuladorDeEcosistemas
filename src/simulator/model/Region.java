package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class Region implements Entity, FoodSupplier, RegionInfo {
    protected List<Animal> animals;

    protected Region() {
        animals = new ArrayList<>();
    }

    public final void addAnimal(Animal a) {
        animals.add(a);
    }

    public final void removeAnimal(Animal a) {
        animals.remove(a);
    }

    public final List<Animal> getAnimals() {
        return Collections.unmodifiableList(animals);
    }

    public final int getNumberOfAnimals(Diet diet) {
        int n = 0;
        for (Animal animal : animals)
            if (animal.get_diet() == diet) n++;
        return n;
    }

    @Override
    public JSONObject asJSON() {
        return new JSONObject().put("animals", new JSONArray(this.animals));
    }
    
    public List<AnimalInfo> getAnimalsInfo(){
    	return new ArrayList<>(animals);
    }
    
}
