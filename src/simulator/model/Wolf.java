package simulator.model;

import java.util.List;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Wolf extends Animal {
	// ---------------------------- //
	// Constants //
	// ---------------------------- //
	// ranges
	public static final double MIN_ENERGY = 0.0;
	public static final double MAX_ENERGY = 100.0;
	public static final double MIN_DESIRE = 0.0;
	public static final double MAX_DESIRE = 100.0;
	public static final double MIN_DESIRE_TO_MATE = 65.0;
	public static final double MAX_ENERGY_TO_HUNGER = 50.0;

	public static final double DEATH_AGE = 14.0;
	public static final double DISTANCE_TO_OBJECTIVE = 8.0;
	public static final double BECOME_PREGNANT_PROBABILITY = 0.9;
	// factors
	public static final double BASE_INIT_SPEED = 60.0;
	public static final double SPEED_INCREASE_FACTOR = 3.0;
	public static final double BASE_ENERGY_DECREASE_FACTOR = 18.0;
	public static final double BASE_DESIRE_INCREASE_FACTOR = 30.0;
	public static final double ALTERED_STATE_ENERGY_INCREASE_FACTOR = 1.2;
	public static final double BASE_SIGHT_RANGE = 50.0;

	// ---------------------------- //
	// Attributes //
	// ---------------------------- //
	private Animal _huntTarget;
	private final SelectionStrategy _huntingStrategy;

	// ---------------------------- //
	// Constructors //
	// ---------------------------- //
	public Wolf(SelectionStrategy mateStrategy, SelectionStrategy huntingStrategy, Vector2D pos)
			throws IllegalArgumentException {
		super("Wolf", Diet.CARNIVORE, BASE_SIGHT_RANGE, BASE_INIT_SPEED, mateStrategy, pos);
		this._huntingStrategy = huntingStrategy;
		this._huntTarget = null;
	}

	protected Wolf(Wolf p1, Animal p2) {
		super(p1, p2);
		_huntingStrategy = p1._huntingStrategy;
		_huntTarget = null;
	}

	// --------- other methods ---------//

	@Override
	public void update(double dt) {
		if (isAlive()) {
			switch (_state) {
			case NORMAL -> {
				moveNormal(dt);
				changeStateNormal();
			}
			case HUNGER -> updateHungerState(dt);
			case MATE -> updateMateState(dt);
			default -> System.out.println(" ");
			}

			// 3
			if (isOutOfBounds(get_regionManager().get_width(), get_regionManager().get_height())) {
				_pos = _pos.adjust(get_regionManager().get_width(), get_regionManager().get_height());
				_state = State.NORMAL;
			}

			// 4
			if (get_energy() == MIN_ENERGY || get_age() > DEATH_AGE)
				die();

			// 5
			if (isAlive())
				alterEnergy(get_energy() + get_regionManager().getFood(this, dt));
		}
	}

	private void updateHungerState(double dt) {
		if (huntingTargetDiedOrWentFar())
			findHuntingTarget();
		if (_huntTarget == null)
			moveNormal(dt);
		else
			moveForHunt(dt);
		changeStateHunger();
	}

	private void updateMateState(double dt) {
		mateDiedOrWentFar();
		if (get_mateTarget() == null)
			moveNormal(dt);
		else {
			moveToMate(get_mateTarget(), dt);
			reproduce();
		}
		changeStateMate();
	}

	private boolean huntingTargetDiedOrWentFar() {
		return _huntTarget == null || !_huntTarget.isAlive() || outOfSight(_huntTarget);
	}

	private void findHuntingTarget() {
		List<Animal> odds = get_regionManager().getAnimalsInRange(this, a -> a.get_diet().equals(Diet.HERBIVORE));
		_huntTarget = _huntingStrategy.select(this, odds);
	}

	// ---------------------------- //
	// Movements //
	// ---------------------------- //
	private void moveNormal(double dt) {
		if (_pos.distanceTo(_dest) < DISTANCE_TO_OBJECTIVE) {
			_dest = Utils.getRandomPosition(get_regionManager().get_width(), get_regionManager().get_height());
		}
		move(calcBaseMoveSpeed(dt));
		_age += dt;
		alterEnergy(get_energy() - BASE_ENERGY_DECREASE_FACTOR * dt);
		alterDesire(get_desire() + BASE_DESIRE_INCREASE_FACTOR * dt);
	}

	private void moveForHunt(double dt) {
		_dest = _huntTarget.get_position();

		move(SPEED_INCREASE_FACTOR * calcBaseMoveSpeed(dt));
		alterEnergy(get_energy() - BASE_ENERGY_DECREASE_FACTOR * dt);
		alterDesire(get_desire() + BASE_DESIRE_INCREASE_FACTOR * dt);
		if (_pos.distanceTo(_huntTarget.get_position()) < DISTANCE_TO_OBJECTIVE) {
			_huntTarget.die();
			_huntTarget = null;
			_dest = Utils.getRandomPosition(get_regionManager().get_width(), get_regionManager().get_height());
			_energy = Utils.constrainValueInRange(get_energy() + 50.0, MIN_ENERGY, MAX_ENERGY);
		}
	}

	private void moveToMate(Animal mateTarget, double dt) {
		_dest = mateTarget.get_position();
		move(SPEED_INCREASE_FACTOR * calcBaseMoveSpeed(dt));
		_age += dt;
		alterEnergy(get_energy() - BASE_ENERGY_DECREASE_FACTOR * ALTERED_STATE_ENERGY_INCREASE_FACTOR * dt);
		alterDesire(get_desire() + BASE_DESIRE_INCREASE_FACTOR * dt);
	}

	private void reproduce() {
		Animal mate = get_mateTarget();
		if (get_position().distanceTo(mate.get_position()) < DISTANCE_TO_OBJECTIVE) {
			_desire = MIN_DESIRE;
			mate._desire = MIN_DESIRE;
			if (Utils.randomGenerator.nextDouble() < BECOME_PREGNANT_PROBABILITY) {
				_baby = new Wolf(this, mate);
				_energy = Utils.constrainValueInRange(get_energy() - 10.0, MIN_ENERGY, MAX_ENERGY);
			}
			_mateTarget = null;
		}
	}
	// ---------------------------- //
	// change State //
	// ---------------------------- //

	private void changeStateNormal() {
		if (_energy < MAX_ENERGY_TO_HUNGER) {
			_mateTarget = null;
			_state = State.HUNGER;
		} else if (_desire > MIN_DESIRE_TO_MATE) {
			_huntTarget = null;
			_state = State.MATE;
		}
	}

	private void changeStateHunger() {
		if (_energy >= MAX_ENERGY_TO_HUNGER) {
			if (_desire < MIN_DESIRE_TO_MATE) {
				_state = State.NORMAL;
				_mateTarget = null;
			} else
				_state = State.MATE;
			_huntTarget = null;
		}
	}

	private void changeStateMate() {
		if (_energy < MAX_ENERGY_TO_HUNGER) {
			_state = State.HUNGER;
			_mateTarget = null;
		} else if (_desire < MIN_DESIRE_TO_MATE) {
			_state = State.NORMAL;
			_mateTarget = null;
			_huntTarget = null;
		}
	}

	private void alterEnergy(double addition) {
		_energy = Utils.constrainValueInRange(addition, MIN_ENERGY, MAX_ENERGY);
	}

	private void alterDesire(double addition) {
		_desire = Utils.constrainValueInRange(addition, MIN_DESIRE, MAX_DESIRE);
	}
}
