import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

abstract public class Place extends Polygon {

	private String name;
	private String category;
	private Position position;
	private String type;
	private boolean marked = false;

	public Place(double x, double y, String type, String category, Position position, String name) {
		super(x, y, x - 15, y - 30, x + 15, y - 30);
		this.name = name;
		this.category = category;
		this.position = position;
		this.type = type;
	}

	public Position getPosition() {
		return position;
	}

	public String getName() {
		return name;
	}

	public String getCategory() {
		if (category == null) {
			return ("None");
		} else {
		}
		return category;
	}

	public String getType() {
		return type;
	}

	public boolean isMarked() {
		return marked;
	}

	public void setColor(String category, Place place) {
		if (category != null) {
			if (category.equals("Bus")) {
				place.setFill(Color.RED);
			} else {
				if (category.equals("Underground")) {
					place.setFill(Color.BLUE);
				} else {
					if (category.equals("Train")) {
						place.setFill(Color.GREEN);
					} else {
						place.setFill(Color.BLACK);
					}
				}
			}
		}
	}

	public void setMarked(Place place) {
		place.setStroke(Color.BLACK);
		place.setFill(Color.YELLOW);
		place.setStrokeWidth(3);
		marked = true;
	}

	public void setUnMarked(Place place) {
		place.setColor(getCategory(), place);
		place.setStroke(null);
		marked = false;
	}

}
