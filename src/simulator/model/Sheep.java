package simulator.model;

import java.util.List;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Sheep extends Animal {
	// ---------------------------- //
	// Constants
	// ---------------------------- //

	public static final double MIN_DESIRE = 0.0;
	public static final double MAX_DESIRE = 100.0;
	public static final double MIN_ENERGY = 0.0;
	public static final double MAX_ENERGY = 100.0;
	public static final double DEATH_AGE = 8.0;
	public static final double DISTANCE_TO_OBJECTIVE = 8.0;
	public static final double BECOME_PREGNANT_PROBABILITY = 0.9;
	public static final double IN_DANGER_SPEED_FACTOR = 2.0;
	public static final double MIN_DESIRE_TO_MATE = 65.0;
	public static final double SPEED_FACTOR_TO_MATE = 3.0;
	public static final double BASE_ENERGY_DECREASE_FACTOR = 20.0;
	public static final double BASE_DESIRE_INCREASE_FACTOR = 40.0;
	public static final double ALTERED_STATE_ENERGY_DECREASE_FACTOR = 1.2;
	public static final double BASE_SIGHT_RANGE = 40.0;
	public static final double BASE_INIT_SPEED = 35.0;

	// ---------------------------- //
	// Attributes //
	// ---------------------------- //

	private final SelectionStrategy dangerStrategy;
	private Animal dangerSource;

	// ---------------------------- //
	// Constructors
	// ---------------------------- //

	public Sheep(SelectionStrategy mateStrategy, SelectionStrategy dangerStrategy, Vector2D pos)
			throws IllegalArgumentException {
		super("Sheep", Diet.HERBIVORE, BASE_SIGHT_RANGE, BASE_INIT_SPEED, mateStrategy, pos);
		this.dangerStrategy = dangerStrategy;
		this.dangerSource = null;
		this._age = 1.0;
	}

	protected Sheep(Sheep p1, Animal p2) {
		super(p1, p2);
		dangerStrategy = p1.dangerStrategy;
		dangerSource = null;
	}

	@Override
	public void update(double dt) {
		if (isAlive()) {
			switch (_state) {
			case NORMAL -> {
				moveNormal(dt);
				stateChangeNormal();
			}
			case DANGER -> updateDangerState(dt);
			case MATE -> updateMateState(dt);
			default -> System.out.println(" ");
			}

			if (isOutOfBounds(_regionManager.get_width(), _regionManager.get_height())) {
				_pos = _pos.adjust(_regionManager.get_width(), _regionManager.get_height());
				_state = State.NORMAL;
			}

			if (_energy == MIN_ENERGY || _age > DEATH_AGE)
				die();

			if (isAlive()) {
				double food = _regionManager.getFood(this, dt);
				alterEnergy(_energy + food);
			}
		}
	}

	private void updateDangerState(double dt) {
		if (dangerSource != null && !dangerSource.isAlive() || outOfSight(dangerSource))
			dangerSource = null;
		if (dangerSource == null)
			moveNormal(dt);
		else if (!outOfSight(dangerSource))
			moveWithDangerSource(dt);
		stateChangeDanger();
	}

	private void updateMateState(double dt) {
		mateDiedOrWentFar();

		if (_mateTarget == null)
			moveNormal(dt);
		else {
			_dest = _mateTarget.get_position();
			move(SPEED_FACTOR_TO_MATE * calcBaseMoveSpeed(dt));
			_age += dt;
			alterEnergy(_energy - BASE_ENERGY_DECREASE_FACTOR * ALTERED_STATE_ENERGY_DECREASE_FACTOR * dt);
			alterDesire(_desire + BASE_DESIRE_INCREASE_FACTOR * dt);
			reproduce();
		}

		findDangerSource();
		stateChangeMate();
	}

	// ---------------------------- //
	// Movements
	// ---------------------------- //

	private void moveNormal(double dt) {
		if (_pos.distanceTo(_dest) < DISTANCE_TO_OBJECTIVE) {
			_dest = Utils.getRandomPosition(_regionManager.get_width(), _regionManager.get_cols());
		}
		move(calcBaseMoveSpeed(dt));
		_age += dt;
		alterEnergy(_energy - BASE_ENERGY_DECREASE_FACTOR * dt);
		alterDesire(_desire + BASE_DESIRE_INCREASE_FACTOR * dt);
	}

	private void moveWithDangerSource(double dt) {
		_pos = _pos.plus(_pos.minus(dangerSource.get_position()).direction());
		move(IN_DANGER_SPEED_FACTOR * calcBaseMoveSpeed(dt));
		_age += dt;
		alterEnergy(_energy - BASE_ENERGY_DECREASE_FACTOR * ALTERED_STATE_ENERGY_DECREASE_FACTOR * dt);
		alterDesire(_desire + BASE_DESIRE_INCREASE_FACTOR * dt);
	}

	// ---------------------------- //
	// Change States
	// ---------------------------- //

	private void stateChangeNormal() {
		findDangerSource();
		if (dangerSource != null) {
			_state = State.DANGER;
			_mateTarget = null;
		} else if (_desire >= MIN_DESIRE_TO_MATE)
			_state = State.MATE;
	}

	private void stateChangeDanger() {
		if (dangerSource == null || outOfSight(dangerSource)) {
			findDangerSource();
			if (dangerSource == null) {
				if (_desire < MIN_DESIRE_TO_MATE) {
					_state = State.NORMAL;
					_mateTarget = null;
				} else
					_state = State.MATE;
			}
		}
	}

	private void stateChangeMate() {
		if (dangerSource != null) {
			_state = State.DANGER;
			_mateTarget = null;
		} else if (_desire < MIN_DESIRE_TO_MATE) {
			_state = State.NORMAL;
			_mateTarget = null;
		}
	}

	// ---------------------------- //
	// Auxiliary methods
	// ---------------------------- //
	private void reproduce() {
		if (_pos.distanceTo(_mateTarget._pos) < DISTANCE_TO_OBJECTIVE) {
			_desire = MIN_DESIRE;
			_mateTarget._desire = MIN_DESIRE;

			if (_baby == null && Utils.randomGenerator.nextDouble() < BECOME_PREGNANT_PROBABILITY)
				_baby = new Sheep(this, _mateTarget);

			_mateTarget = null;
		}
	}

	private void findDangerSource() {
		if (dangerSource == null) {
			List<Animal> carnivoresInRange = _regionManager.getAnimalsInRange(this, a -> a._diet.equals(Diet.CARNIVORE));
			dangerSource = dangerStrategy.select(this, carnivoresInRange);
		}
	}

	private void alterEnergy(double addition) {
		_energy = Utils.constrainValueInRange(addition, MIN_ENERGY, MAX_ENERGY);
	}

	private void alterDesire(double addition) {
		_desire = Utils.constrainValueInRange(addition, MIN_DESIRE, MAX_DESIRE);
	}
}
