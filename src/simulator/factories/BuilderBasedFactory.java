package simulator.factories;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;


public class BuilderBasedFactory<T> implements Factory<T> {
    private final Map<String, Builder<T>> builders;
    private final List<JSONObject> buildersInfo;

    public BuilderBasedFactory() {
        this.builders = new HashMap<>();
        this.buildersInfo = new LinkedList<>();
    }

    public BuilderBasedFactory(List<Builder<T>> builders) {
        this();
        for (Builder<T> builder : builders) {
            addBuilder(builder);
        }
    }

    public void addBuilder(Builder<T> b) {
        builders.put(b.getTypeTag(), b);
        buildersInfo.add(b.getInfo());
    }

    @Override
    public T createInstance(JSONObject info) throws IllegalArgumentException {
        if (info == null)
            throw new IllegalArgumentException("’info’ cannot be null");

        Builder<T> b = builders.get(info.getString("type"));
        if (b == null)
            throw new IllegalArgumentException("Unrecognized ‘info’:" + info);

        JSONObject jo = info.has("data") ? info.getJSONObject("data") : new JSONObject();
        return b.createInstance(jo);
    }

    @Override
    public List<JSONObject> getInfo() {
        return Collections.unmodifiableList(buildersInfo);
    }
}
