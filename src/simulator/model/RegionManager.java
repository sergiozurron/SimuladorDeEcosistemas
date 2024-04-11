package simulator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class RegionManager implements AnimalMapView {
	// Constants
	public static final int MIN_WIDTH = 10;
	public static final int MIN_HEIGHT = 10;
	public static final int MIN_COLS = 1;
	public static final int MIN_ROWS = 1;

	// Fields
	private final int _cols;
	private final int _rows;
	private final int _width;
	private final int _height;
	private final int _regionWidth;
	private final int _regionHeight;
	private final Region[][] _regions;
	private final Map<Animal, Region> _animalRegion;

	public RegionManager(int cols, int rows, int width, int height) throws IllegalArgumentException {
		if (width < MIN_WIDTH)
			throw new IllegalArgumentException("Width must be at least " + MIN_WIDTH);
		if (height < MIN_HEIGHT)
			throw new IllegalArgumentException("Height must be at least " + MIN_HEIGHT);

		// So that there are minimum and maximum columns and rows
		cols = Utils.constrainValueInRange(cols, MIN_COLS, width);
		rows = Utils.constrainValueInRange(rows, MIN_ROWS, height);

		this._cols = cols;
		this._rows = rows;
		this._width = width;
		this._height = height;
		this._regionWidth = width / cols + (width % cols != 0 ? 1 : 0);
		this._regionHeight = height / rows + (height % rows != 0 ? 1 : 0);
		_regions = new Region[rows][cols];

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				_regions[i][j] = new DefaultRegion();
			}
		}

		_animalRegion = new HashMap<>();
	}

	@Override
	public double getFood(Animal a, double dt) {
		return _animalRegion.get(a).getFood(a, dt);
	}

	@Override
	public int get_cols() {
		return _cols;
	}

	@Override
	public int get_rows() {
		return _rows;
	}

	@Override
	public int get_width() {
		return _width;
	}

	@Override
	public int get_height() {
		return _height;
	}

	@Override
	public int get_regionWidth() {
		return _regionWidth;
	}

	@Override
	public int get_regionHeight() {
		return _regionHeight;
	}

	@Override
	public List<Animal> getAnimalsInRange(Animal a, Predicate<Animal> filter) {
		List<Animal> inSight = new ArrayList<>();

		Vector2D topLeft = minPositionSightRange(a);
		Vector2D bottomRight = maxPositionSightRange(a);

		int minRow = (int) (topLeft.getY() / _regionHeight);
		int maxRow = (int) (bottomRight.getY() / _regionHeight);
		int minCol = (int) (topLeft.getX() / _regionWidth);
		int maxCol = (int) (bottomRight.getX() / _regionWidth);

		for (int i = minRow; i < maxRow; i++) {
			for (int j = minCol; j < maxCol; j++) {
				List<Animal> animalsInRegion = _regions[i][j].getAnimals().stream().filter(filter).toList();
				inSight.addAll(animalsInRegion);
			}
		}

		return inSight;
	}

	@Override
	public JSONObject asJSON() {
		JSONArray regionsJA = new JSONArray();
		for (int r = 0; r < _rows; r++)
			for (int c = 0; c < _cols; c++)
				regionsJA.put(new JSONObject().put("row", r).put("col", c).put("data", this._regions[r][c].asJSON()));
		return new JSONObject().put("regiones", regionsJA);
	}
	
	// ------------------------- //

	public void setRegion(int row, int col, Region r) {
		if ((row >= 0) && (row < get_regionHeight()) && (col >= 0) && (col < get_regionWidth())) {
			_regions[row][col] = r;
		}
	}

	public void registerAnimal(Animal a) {
		a.init(this);
		Region r = calcAnimalRegion(a);
		r.addAnimal(a);
		_animalRegion.put(a, r);
	}

	public void unregisterAnimal(Animal a) {
		_animalRegion.get(a).removeAnimal(a);
		_animalRegion.remove(a);
	}

	public void updateAnimalRegion(Animal a) {
		Region next = calcAnimalRegion(a);
		Region current = _animalRegion.get(a);
		if (!next.equals(current)) {
			next.addAnimal(a);
			current.removeAnimal(a);
			_animalRegion.replace(a, current, next);
		}
	}

	public void updateAllRegions(double dt) {
		for (Region[] regionsRow : _regions) {
			for (Region region : regionsRow) {
				region.update(dt);
			}
		}
	}

	public void removeDeadAnimals() {
		List<Animal> deadAnimals = new ArrayList<>();
		_animalRegion.forEach((a, r) -> {
			if (!a.isAlive())
				deadAnimals.add(a);
		});
		deadAnimals.forEach(this::unregisterAnimal);
	}

	// Auxiliary methods
	private Vector2D minPositionSightRange(Animal a) {
		double x = a.get_position().getX() - a.get_sightRange();
		double y = a.get_position().getY() - a.get_sightRange();
		x = Math.max(x, 0);
		y = Math.max(y, 0);
		return new Vector2D(x, y);
	}

	private Vector2D maxPositionSightRange(Animal a) {
		double x = a.get_position().getX() + a.get_sightRange();
		double y = a.get_position().getY() + a.get_sightRange();
		x = Math.min(x, _width);
		y = Math.min(y, _height);
		return new Vector2D(x, y);
	}

	private Region calcAnimalRegion(Animal a) {
		int x = (int) Math.floor(a.get_position().getX() / get_regionWidth());
		int y = (int) Math.floor(a.get_position().getY() / get_regionHeight());
		return _regions[y][x];
	}

	@Override
	public Iterator<RegionData> iterator() {
		List<RegionData> l = new ArrayList<RegionData>();
		for(int i = 0; i < _rows; i++) {
			for(int j = 0; j < _cols; j++) {
				l.add(new RegionData(i, j, _regions[i][j]));
			}
		}
		return l.iterator();
	}

}
