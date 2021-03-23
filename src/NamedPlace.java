public class NamedPlace extends Place {

	public NamedPlace(double x, double y, String type, String category, Position position, String name) {
		super(x, y, type, category, position, name);
	}

	public String toString() {
		return String.format(getType() + "," + getCategory() + "," + getPosition() + "," + getName());
	}
}
