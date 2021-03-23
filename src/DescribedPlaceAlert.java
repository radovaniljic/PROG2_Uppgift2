import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class DescribedPlaceAlert extends Alert {
	private TextField name = new TextField();
	private TextField description = new TextField();

	public DescribedPlaceAlert() {
		super(AlertType.CONFIRMATION);
		GridPane grid = new GridPane();

		Label nameLabel = new Label("Name");
		nameLabel.setStyle("-fx-font-weight: bold");
		grid.addRow(0, nameLabel, name);
		
		Label descriptionLabel = new Label("Description");
		descriptionLabel.setStyle("-fx-font-weight: bold");
		grid.addRow(1, descriptionLabel, description);

		getDialogPane().setContent(grid);

	}

	public String getName() {
		return name.getText();
	}
	
	public String getDescription() {
		return description.getText();
	}
}