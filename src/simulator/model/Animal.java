package simulator.model;

import org.json.JSONArray;
import org.json.JSONObject;
import simulator.misc.Utils;
import simulator.misc.Vector2D;
import simulator.view.SimpleObjectViewer.ObjInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Animal implements Entity, AnimalInfo {
    public static final double PROBABILITY_TO_INHERIT_WHICH_MATE_STRATEGY = 0.5;
    private static final String THE_ANIMAL_MUST_HAVE_A_MATING_STRATEGY = "The animal must have a mating strategy";
    private static final String SIGHT_RANGE_AND_THE_INITIAL_SPEED_MUST_BE_POSITIVE = "Sight range and the initial speed must be positive";
    private static final String THERE_HAS_TO_BE_A_GENETIC_CODE = "There has to be a genetic code";
    protected String _geneticCode;
    protected Diet _diet;
    protected State _state;
    protected Vector2D _pos;
    protected Vector2D _dest;
    protected double _energy;
    protected double _speed;
    protected double _age;
    protected double _desire;
    protected double _sightRange;
    protected Animal _mateTarget;
    protected Animal _baby;
    protected AnimalMapView _regionManager;
    protected SelectionStrategy _mateStrategy;

    protected Animal(String geneticCode, Diet diet, double sightRange, double initSpeed,
                     SelectionStrategy mateStrategy, Vector2D pos) throws IllegalArgumentException {
        if (geneticCode.isBlank())
            throw new IllegalArgumentException(THERE_HAS_TO_BE_A_GENETIC_CODE);
        if (sightRange <= 0 || initSpeed < 0)
            throw new IllegalArgumentException(SIGHT_RANGE_AND_THE_INITIAL_SPEED_MUST_BE_POSITIVE);
        if (mateStrategy == null)
            throw new IllegalArgumentException(THE_ANIMAL_MUST_HAVE_A_MATING_STRATEGY);
        if (diet == null)
            throw new IllegalArgumentException("The animal must have a diet");

        this._geneticCode = geneticCode;
        this._diet = diet;
        this._sightRange = sightRange;
        this._pos = pos;
        this._mateStrategy = mateStrategy;
        this._speed = Utils.getRandomizedParameter(initSpeed, 0.1);
        this._age = 0.0;
        this._desire = 0.0;
        this._energy = 100.0;
        this._dest = null;
        this._baby = null;
        this._mateTarget = null;
        this._regionManager = null;
        this._state = State.NORMAL;
    }

    protected Animal(Animal p1, Animal p2) throws IllegalArgumentException {
        if (p1 == null || p2 == null)
            throw new IllegalArgumentException("One of the parents does not exist");
        if (p1.equals(p2))
            throw new IllegalArgumentException("An animal can not become pregnant with itself");

        _dest = null;
        _baby = null;
        _mateTarget = null;
        _regionManager = null;

        _state = State.NORMAL;

        _age = 0.0;
        _desire = 0.0;

        _geneticCode = p1._geneticCode;
        _diet = p1._diet;
        _mateStrategy = Utils.randomGenerator.nextDouble() < PROBABILITY_TO_INHERIT_WHICH_MATE_STRATEGY ? p1._mateStrategy : p2._mateStrategy;
        _energy = (p1._energy + p2._energy) / 2.0;

        _pos = p1.get_position().plus(Vector2D.getRandomVector(-1, 1).scale(60.0 * (Utils.randomGenerator.nextGaussian() + 1)));
        _sightRange = Utils.getRandomizedParameter((p1.get_sightRange() + p2.get_sightRange()) / 2, 0.2);
        _speed = Utils.getRandomizedParameter((p1.get_speed() + p2.get_speed()) / 2, 0.2);
    }

    // --------- Getters y Setters --------- //
    @Override
    public double get_speed() {
        return _speed;
    }

    @Override
    public double get_sightRange() {
        return _sightRange;
    }

    @Override
    public Vector2D get_position() {
        return _pos;
    }

    @Override
    public State get_state() {
        return _state;
    }

    @Override
    public String get_geneticCode() {
        return _geneticCode;
    }

    @Override
    public Diet get_diet() {
        return _diet;
    }

    @Override
    public double get_energy() {
        return _energy;
    }

    @Override
    public double get_age() {
        return _age;
    }

    @Override
    public Vector2D getDestination() {
        return _dest;
    }

    public double get_desire() {
        return _desire;
    }

    protected Animal get_mateTarget() {
        return _mateTarget;
    }

    protected AnimalMapView get_regionManager() {
        return _regionManager;
    }

    @Override
    public boolean isPregnant() {
        return _baby != null;
    }

    public Animal deliverBaby() {
        Animal a = _baby;
        _baby = null;
        return a;
    }

    @Override
    public JSONObject asJSON() {
        Map<String, Object> map = new HashMap<>();
        map.put("pos", new JSONArray(Arrays.asList(this._pos.getX(), this._pos.getY())));
        map.put("gcode", _geneticCode);
        map.put("diet", _diet.toString());
        map.put("state", _state.toString());
        return new JSONObject(map);
    }

    @Override
    public ObjInfo getObjInfo() {
        return new ObjInfo(_geneticCode, (int) _pos.getX(), (int) _pos.getY(), (int) Math.round(get_age()) + 2);
    }

    public void init(AnimalMapView regionManager) {
        this._regionManager = regionManager;

        int width = regionManager.get_width();
        int height = regionManager.get_height();

        if (_pos == null)
            _pos = Utils.getRandomPosition(width, height);
        else
            _pos = _pos.adjust(width, height);

        _dest = Utils.getRandomPosition(width, height);
    }

    protected void move(double speed) {
        _pos = _pos.plus(_dest.minus(_pos).direction().scale(speed)).adjust(_regionManager.get_width(), _regionManager.get_height());
    }

    public boolean isAlive() {
        return _state != State.DEAD;
    }

    protected void die() {
        _state = State.DEAD;
    }

    protected boolean isOutOfBounds(int maxWidth, int maxHeight) {
        return (_pos.getX() < 0 || _pos.getX() > maxWidth) ||
                (_pos.getY() < 0 || _pos.getY() > maxHeight);
    }

    protected boolean outOfSight(Animal other) {
        return _pos.distanceTo(other.get_position()) >= _sightRange;
    }

    protected void mateDiedOrWentFar() {
        if (_mateTarget != null && (!isAlive() || outOfSight(_mateTarget))) {
            _mateTarget = null;
        }
        if (_mateTarget == null)
            findMateTarget();
    }

    protected void findMateTarget() {
        List<Animal> odds = _regionManager.getAnimalsInRange(this, a -> _geneticCode.equals(a._geneticCode) && a != this);
        _mateTarget = _mateStrategy.select(_baby, odds);
    }

    protected double calcBaseMoveSpeed(double dt) {
        return _speed * dt * Math.exp((_energy - 100.0) * 0.007);
    }
}
