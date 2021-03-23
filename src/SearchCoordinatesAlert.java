import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class SearchCoordinatesAlert extends Alert {
	private TextField xField = new TextField();
	private TextField yField = new TextField();

	public SearchCoordinatesAlert() {
		super(AlertType.CONFIRMATION);
		GridPane grid = new GridPane();

		Label xLabel = new Label("X:");
		xLabel.setStyle("-fx-font-weight: bold");
		grid.addRow(0, xLabel, xField);

		Label yLabel = new Label("Y:");
		yLabel.setStyle("-fx-font-weight: bold");
		grid.addRow(1, yLabel, yField);

		getDialogPane().setContent(grid);

	}

	public Position getPosition() {
		return Position.parsePosition(xField.getText(), yField.getText());
	}

}
