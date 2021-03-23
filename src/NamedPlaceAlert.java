import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class NamedPlaceAlert extends Alert {
	private TextField name = new TextField();

	public NamedPlaceAlert() {
		super(AlertType.CONFIRMATION);
		GridPane grid = new GridPane();

		Label nameLabel = new Label("Name");
		nameLabel.setStyle("-fx-font-weight: bold");
		grid.addRow(0, nameLabel, name);

		getDialogPane().setContent(grid);

	}

	public String getName() {
		return name.getText();
	}
}