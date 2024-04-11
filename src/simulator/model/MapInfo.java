package simulator.model;

public interface MapInfo extends JSONable, Iterable<MapInfo.RegionData> {
	
	public record RegionData(int row, int col, RegionInfo r) {
		
	}
	
	int get_cols();

	int get_rows();

	int get_width();

	int get_height();

	int get_regionWidth();

	int get_regionHeight();
}
