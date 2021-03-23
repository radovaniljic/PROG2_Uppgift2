import java.util.Objects;

public class Position {

	private double x, y;

	public Position(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Position) {
			Position position = (Position)other;
			return x == position.x && y == position.y;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	public static Position parsePosition(String first, String last) {
		int pos = first.indexOf(",");
		int ops = last.indexOf(",");
		double x = Double.parseDouble(first.substring(pos + 1));
		double y = Double.parseDouble(last.substring(ops + 1));
		return new Position(x, y);
	}

	public String toString() {
		return String.format((int) getX() + "," + (int) getY());
	}
}
