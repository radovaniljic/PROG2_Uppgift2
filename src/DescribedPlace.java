public class DescribedPlace extends Place {

	private String description;
	
	public DescribedPlace(double x, double y, String type, String category, Position position, String name, String description) {
		super(x, y, type, category, position, name);
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String toString() {
		return String.format(getType() + "," + getCategory() + "," + getPosition() + "," + getName() + "," + getDescription());
	}
}