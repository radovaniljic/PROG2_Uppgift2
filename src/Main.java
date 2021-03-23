import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

	private Stage primaryStage;
	private BorderPane rootPane;

	private ImageView imageView = new ImageView();
	private ObservableList<String> categories = FXCollections.observableArrayList("Bus", "Underground", "Train");
	private ListView<String> categoryList = new ListView<>();

	private Button newButton;
	private RadioButton namedRadioButton;
	private RadioButton describedRadioButton;
	private TextField searchField = new TextField();

	private MenuItem save;
	private MenuItem loadPlaces;

	private ClickHandler clickHandler = new ClickHandler();

	private Map<Position, Place> newPlacesMap = new HashMap<>();
	private Map<Position, Place> loadPlacesMap = new HashMap<>();
	private Map<Position, Place> markedPlacesMap = new HashMap<>();
	private Map<Position, Place> allPlacesMap = new HashMap<>();
	private Map<String, List<Place>> placesPerNameMap = new HashMap<>();
	private Map<String, List<Place>> placesPerCategoryMap = new HashMap<>();
	private List<Place> savedPlaces = new ArrayList<>();

	private boolean changed = false;

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;

		MenuItem loadMap = new MenuItem("Load map");
		loadMap.setOnAction(new LoadMapHandler());

		loadPlaces = new MenuItem("Load places");
		loadPlaces.setOnAction(new LoadPlacesHandler());
		loadPlaces.setDisable(true);

		save = new MenuItem("Save");
		save.setOnAction(new SaveHandler());

		MenuItem exit = new MenuItem("Exit");
		exit.setOnAction(new ExitHandler());

		newButton = new Button("New");

		namedRadioButton = new RadioButton("Named");
		describedRadioButton = new RadioButton("Described");

		ToggleGroup group = new ToggleGroup();
		namedRadioButton.setToggleGroup(group);
		namedRadioButton.setPadding(new Insets(2.5));
		namedRadioButton.setSelected(true);
		describedRadioButton.setToggleGroup(group);
		describedRadioButton.setPadding(new Insets(2.5));

		newButton.setDisable(true);
		newButton.setOnAction(new NewPlaceHandler());

		searchField = new TextField("Search");
		searchField.setOnMouseClicked(new SearchHandler());

		Button searchButton = new Button("Search");
		searchButton.setOnAction(new SearchButtonHandler());

		Button hideButton = new Button("Hide");
		hideButton.setOnAction(new HideButtonHandler());

		Button removeButton = new Button("Remove");
		removeButton.setOnAction(new RemoveButtonHandler());

		Button coordinatesButton = new Button("Coordinates");
		coordinatesButton.setOnAction(new CoordinatesButtonHandler());

		FlowPane top = new FlowPane();
		top.setHgap(5);

		VBox topVBox = new VBox();
		topVBox.getChildren().addAll(namedRadioButton, describedRadioButton);
		topVBox.setPadding(new Insets(5));

		top.getChildren().addAll(newButton, topVBox, searchField, searchButton, hideButton, removeButton,
				coordinatesButton);
		top.setAlignment(Pos.CENTER);

		Label categoriesLabel = new Label("Categories");
		categoriesLabel.setStyle("-fx-font-weight: bold");

		Button hideCategoryButton = new Button("Hide Category");
		hideCategoryButton.setOnAction(new HideCategoryHandler());

		VBox rightVBox = new VBox();
		rightVBox.setPrefWidth(200);
		rightVBox.setPadding(new Insets(5));
		rightVBox.getChildren().addAll(categoriesLabel, categoryList, hideCategoryButton);

		categoryList.setPrefSize(100, 100);
		categoryList.setItems(categories);
		categoryList.setOnMouseClicked(new ShowCategoryHandler());

		Menu fileMenu = new Menu("File");
		fileMenu.getItems().addAll(loadMap, loadPlaces, save, exit);

		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().add(fileMenu);

		VBox menuBarVBox = new VBox();
		menuBarVBox.getChildren().add(menuBar);
		menuBarVBox.getChildren().add(top);

		rootPane = new BorderPane();
		rootPane.setStyle("-fx-font-size: 14");
		rootPane.setCenter(imageView);
		rootPane.setRight(rightVBox);
		rootPane.setTop(menuBarVBox);

		Scene scene = new Scene(rootPane, 1154, 856);
		scene.setOnMouseClicked(new MarkingHandler());
		primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.setTitle("Karta");
		primaryStage.setOnCloseRequest(new CloseHandler());

	}

	private class NewPlaceHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			rootPane.addEventHandler(MouseEvent.MOUSE_CLICKED, clickHandler);
			rootPane.setCursor(Cursor.CROSSHAIR);
			newButton.setDisable(true);
		}
	}

	private class ClickHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event) {
			double x = event.getX();
			double y = event.getY();

			Position position = new Position(x, y);
			if (allPlacesMap.containsKey(position)) {
				Alert alert = new Alert(AlertType.ERROR, "There is already a place on those coordinates");
				alert.setHeaderText(null);
				alert.showAndWait();
			} else {

				rootPane.removeEventHandler(MouseEvent.MOUSE_CLICKED, clickHandler);
				rootPane.setCursor(Cursor.DEFAULT);
				newButton.setDisable(false);
				if (event.getButton() != MouseButton.SECONDARY) {
					String category = categoryList.getSelectionModel().getSelectedItem();
					if (namedRadioButton.isSelected()) {
						createNamedPlace(x, y, category);
					} else {
						createDescribedPlace(x, y, category);
					}
					categoryList.getSelectionModel().clearSelection();
				}
			}
		}

		private void createNamedPlace(double x, double y, String category) {
			NamedPlaceAlert namedPlaceAlert = new NamedPlaceAlert();
			namedPlaceAlert.setTitle("Create new place");
			namedPlaceAlert.setHeaderText(null);

			Optional<ButtonType> result = namedPlaceAlert.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.OK) {
				String name = namedPlaceAlert.getName();
				Position position = new Position(x, y);
				String type = "Named";

				if (name.trim().isEmpty() || isNameValid(name) == false) {
					nameError();

				} else {
					NamedPlace namedPlace = new NamedPlace(x, y, type, category, position, name);
					rootPane.getChildren().add(namedPlace);
					newPlacesMap.put(position, namedPlace);

					namedPlaceToList(name, namedPlace, category);

					namedPlace.setColor(category, namedPlace);
					changed = true;
					namedPlace.setOnMouseClicked(new PlaceClickHandler());
				}
			}
		}

		private void createDescribedPlace(double x, double y, String category) {
			DescribedPlaceAlert describedPlaceAlert = new DescribedPlaceAlert();
			describedPlaceAlert.setTitle("Create new place");
			describedPlaceAlert.setHeaderText(null);

			Optional<ButtonType> result = describedPlaceAlert.showAndWait();
			if (result.isPresent() && result.get() == ButtonType.OK) {
				String name = describedPlaceAlert.getName();
				String description = describedPlaceAlert.getDescription();
				Position position = new Position(x, y);
				String type = "Named";

				if (name.trim().isEmpty() || isNameValid(name) == false) {
					nameError();
				}

				if (description.trim().isEmpty() || isDescriptionValid(description) == false) {
					Alert descriptionError = new Alert(AlertType.ERROR,
							"The description can't be empty and can only conist of the letters A-ö");
					descriptionError.setHeaderText(null);
					descriptionError.showAndWait();
				} else {
					if (describedRadioButton.isSelected()) {
						type = "Described";
						DescribedPlace describedPlace = new DescribedPlace(x, y, type, category, position, name,
								description);
						rootPane.getChildren().add(describedPlace);
						newPlacesMap.put(position, describedPlace);

						describedPlaceToList(name, describedPlace, category);

						describedPlace.setColor(category, describedPlace);
						changed = true;
						describedPlace.setOnMouseClicked(new PlaceClickHandler());
					}
				}
			}
		}

		private boolean isDescriptionValid(String description) {
			String category = categoryList.getSelectionModel().getSelectedItem();
			if (category == null || category.equals("Bus")) {
				return true;
			} else {
				return description.matches("[a-öA-Ö]+");
			}
		}

		private boolean isNameValid(String name) {
			String category = categoryList.getSelectionModel().getSelectedItem();
			if (category == null || category.equals("Bus")) {
				return true;
			} else {
				return name.matches("[a-öA-Ö]+");
			}
		}

		private void nameError() {
			Alert nameError = new Alert(AlertType.ERROR,
					"The name can't be empty and can only consist of the letters A-Ö");
			nameError.setHeaderText(null);
			nameError.showAndWait();
		}
	}

	private class PlaceClickHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event) {
			if (event.getButton() == MouseButton.SECONDARY) {
				Object object = event.getSource();
				if (object instanceof DescribedPlace) {
					Alert describedPlaceAlert = new Alert(AlertType.INFORMATION);
					String name = ((DescribedPlace) object).getName();
					Position position = ((DescribedPlace) object).getPosition();
					String description = ((DescribedPlace) object).getDescription();
					describedPlaceAlert.setTitle("Message");
					describedPlaceAlert.setHeaderText(name + "[" + position + "]");
					describedPlaceAlert.setContentText(description);
					describedPlaceAlert.showAndWait();

				} else {
					if (object instanceof NamedPlace) {
						Alert namedPlaceAlert = new Alert(AlertType.INFORMATION);
						String name = ((NamedPlace) object).getName();
						Position position = ((NamedPlace) object).getPosition();
						namedPlaceAlert.setTitle("Message");
						namedPlaceAlert.setContentText(name + "[" + position + "]");
						namedPlaceAlert.setHeaderText(null);
						namedPlaceAlert.showAndWait();
					}
				}
			} else {
				if (event.getButton() == MouseButton.PRIMARY) {
					Object object = event.getSource();
					if (object instanceof Place) {
						Place place = ((Place) object);
						if (place.isMarked() == false) {
							place.setMarked(place);
							markedPlacesMap.put(place.getPosition(), place);
						} else {
							place.setUnMarked(place);
							markedPlacesMap.remove(place.getPosition(), place);
						}
					}
				}
			}
		}
	}

	private class LoadMapHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			if (changed == true) {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setContentText("You have unsaved changes. Do you want to proceed anyway?");
				alert.setHeaderText(null);
				Optional<ButtonType> result = alert.showAndWait();
				if (result.isPresent() && result.get() == ButtonType.OK) {
					loadMap();
				}
			} else {
				loadMap();
				newButton.setDisable(false);
			}
		}

		private void loadMap() {
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Bildfiler", "*.jpg", "*.png"),
					new FileChooser.ExtensionFilter("Alla filer", "*.*"));
			File file = fileChooser.showOpenDialog(primaryStage);

			for (Map.Entry<Position, Place> entry : loadPlacesMap.entrySet()) {
				rootPane.getChildren().remove(entry.getValue());
			}

			for (Map.Entry<Position, Place> entry : newPlacesMap.entrySet()) {
				rootPane.getChildren().remove(entry.getValue());
			}

			for (Place p : savedPlaces) {
				rootPane.getChildren().remove(p);
			}

			if (file != null) {
				newPlacesMap.clear();
				loadPlacesMap.clear();
				markedPlacesMap.clear();
				allPlacesMap.clear();
				placesPerNameMap.clear();
				placesPerCategoryMap.clear();
				savedPlaces.clear();
				imageView.setImage(null);

				String name = file.getAbsolutePath();
				Image image = new Image("file:" + name);
				imageView.setImage(image);
				primaryStage.sizeToScene();
				loadPlaces.setDisable(false);
			}
		}
	}

	private class MarkingHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event) {
			categoryList.getSelectionModel().clearSelection();
			searchField.setText("Search");
			rootPane.requestFocus();
		}
	}

	private class LoadPlacesHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {

			if (changed == true) {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setContentText("You have unsaved changes. Do you want to proceed anyway?");
				alert.setHeaderText(null);
				Optional<ButtonType> result = alert.showAndWait();
				if (result.isPresent() && result.get() == ButtonType.OK) {
					loadPlaces();
				}
			} else {
				loadPlaces();
			}
		}

		private void loadPlaces() {
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Placesfiler", "*.places"),
					new FileChooser.ExtensionFilter("Alla filer", "*.*"));
			File file = fileChooser.showOpenDialog(primaryStage);

			try {
				if (file != null) {
					for (Map.Entry<Position, Place> entry : loadPlacesMap.entrySet()) {
						rootPane.getChildren().remove(entry.getValue());
					}

					for (Map.Entry<Position, Place> entry : newPlacesMap.entrySet()) {
						rootPane.getChildren().remove(entry.getValue());
					}

					for (Place p : savedPlaces) {
						rootPane.getChildren().remove(p);
					}

					newPlacesMap.clear();
					loadPlacesMap.clear();
					markedPlacesMap.clear();
					allPlacesMap.clear();
					placesPerNameMap.clear();
					savedPlaces.clear();
					placesPerCategoryMap.clear();

					BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
					String line;
					while ((line = bufferedReader.readLine()) != null) {
						String[] tokens = line.split(",");
						String type = tokens[0];
						String category = tokens[1];
						double x = Double.parseDouble(tokens[2]);
						double y = Double.parseDouble(tokens[3]);
						Position position = Position.parsePosition(tokens[2], tokens[3]);
						String name = tokens[4];

						if (tokens.length == 6) {
							String description = (tokens[5]);
							DescribedPlace describedPlace = new DescribedPlace(x, y, type, category, position, name,
									description);
							loadPlacesMap.put(describedPlace.getPosition(), describedPlace);

							describedPlaceToList(name, describedPlace, category);

						} else {
							NamedPlace namedPlace = new NamedPlace(x, y, type, category, position, name);
							namedPlace.relocate(namedPlace.getPosition().getX() - 10,
									namedPlace.getPosition().getY() - 20);
							loadPlacesMap.put(namedPlace.getPosition(), namedPlace);

							namedPlaceToList(name, namedPlace, category);
						}
					}
					printPlaces();
					bufferedReader.close();
				}

			} catch (IOException e) {
				Alert alert = new Alert(AlertType.ERROR, "IOException");
				alert.setHeaderText(null);
				alert.showAndWait();

			}
		}

		private void printPlaces() {
			for (Place p : loadPlacesMap.values()) {
				rootPane.getChildren().add(p);
				p.setOnMouseClicked(new PlaceClickHandler());
				p.setColor(p.getCategory(), p);

			}
		}
	}

	private class SaveHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			try {
				FileChooser fileChooser = new FileChooser();
				File file = fileChooser.showOpenDialog(primaryStage);
				if (file != null) {
					PrintWriter pw = new PrintWriter(new FileWriter(file, true));
					for (Place p : newPlacesMap.values()) {
						pw.print(p + "\n");
						changed = false;
					}
					pw.close();
				}
				for (Map.Entry<Position, Place> entry : newPlacesMap.entrySet()) {
					savedPlaces.add(entry.getValue());
				}
				newPlacesMap.clear();

			} catch (FileNotFoundException e) {
				Alert alert = new Alert(AlertType.ERROR, e.getMessage());
				alert.showAndWait();
			} catch (IOException e) {
				Alert alert = new Alert(AlertType.ERROR, e.getMessage());
				alert.showAndWait();
			}
		}
	}

	private class SearchHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event) {
			searchField.clear();
		}
	}

	private class SearchButtonHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			String name = searchField.getText();
			clearMarkedPlacesMap();

			List<Place> nameList = placesPerNameMap.get(name);
			if (nameList != null) {
				for (Place p : nameList) {
					p.setVisible(true);
					p.setMarked(p);
					markedPlacesMap.put(p.getPosition(), p);
				}
			}

		}
	}

	private class CloseHandler implements EventHandler<WindowEvent> {
		@Override
		public void handle(WindowEvent event) {
			if (changed) {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setContentText("Do you want to exit without saving?");
				alert.setHeaderText(null);
				Optional<ButtonType> result = alert.showAndWait();
				if (result.isPresent() && result.get() == ButtonType.CANCEL) {
					event.consume();
				}
			}
		}
	}

	private class ExitHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			if (changed) {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setContentText("Do you want to exit without saving?");
				alert.setHeaderText(null);
				Optional<ButtonType> res = alert.showAndWait();
				if (res.isPresent() && res.get() == ButtonType.CANCEL) {
					event.consume();
				}
			}
		}
	}

	private class ShowCategoryHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event) {
			String category = categoryList.getSelectionModel().getSelectedItem();

			List<Place> categoryList = placesPerCategoryMap.get(category);
			if (categoryList != null) {
				for (Place p : categoryList) {
					p.setVisible(true);
				}
			}
		}
	}

	private class HideCategoryHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			String category = categoryList.getSelectionModel().getSelectedItem();

			List<Place> categoryList = placesPerCategoryMap.get(category);
			if (categoryList != null) {
				for (Place p : categoryList) {
					p.setUnMarked(p);
					p.setVisible(false);
					markedPlacesMap.remove(p.getPosition(), p);
				}
			}
		}

	}

	private class HideButtonHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			Iterator<Map.Entry<Position, Place>> iterator = markedPlacesMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Position, Place> entry = iterator.next();
				Place place = entry.getValue();
				if (entry != null) {
					place.setVisible(false);
					place.setUnMarked(place);
					iterator.remove();
				}
			}

		}
	}

	private class RemoveButtonHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			Iterator<Map.Entry<Position, Place>> iterator = markedPlacesMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Position, Place> entry = iterator.next();
				Position position = entry.getKey();
				Place place = entry.getValue();
				if (entry != null) {
					rootPane.getChildren().remove(place);
					loadPlacesMap.remove(position, place);
					newPlacesMap.remove(position, place);
					allPlacesMap.remove(position, place);
					placesPerNameMap.get(place.getName()).remove(place);
					placesPerCategoryMap.get(place.getCategory()).remove(place);
					iterator.remove();
				}
			}
		}
	}

	private class CoordinatesButtonHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			try {
				SearchCoordinatesAlert alert = new SearchCoordinatesAlert();
				alert.setTitle("Input Coordinates");
				alert.setHeaderText(null);
				allPlacesMap.putAll(loadPlacesMap);
				allPlacesMap.putAll(newPlacesMap);

				Optional<ButtonType> result = alert.showAndWait();
				if (result.isPresent() && result.get() == ButtonType.OK) {
					Position position = alert.getPosition();
					Place place = allPlacesMap.get(position);
					place.setVisible(true);
					place.setMarked(place);
					clearMarkedPlacesMap();
					markedPlacesMap.put(position, place);
				}
				allPlacesMap.clear();

			} catch (NumberFormatException e) {
				Alert alert = new Alert(AlertType.ERROR, "The input can't be empty and can only consist of digits");
				alert.setHeaderText(null);
				alert.showAndWait();
			} catch (NullPointerException e) {
				Alert alert = new Alert(AlertType.ERROR, "There is no place with those coordinates");
				alert.setHeaderText(null);
				alert.showAndWait();
			}
		}

	}

	private void clearMarkedPlacesMap() {
		for (Place p : markedPlacesMap.values()) {
			p.setUnMarked(p);
		}
		markedPlacesMap.clear();
	}

	private void describedPlaceToList(String name, DescribedPlace describedPlace, String category) {
		List<Place> nameList = placesPerNameMap.get(name);
		if (nameList == null) {
			nameList = new ArrayList<Place>();
			placesPerNameMap.put(name, nameList);
		}
		nameList.add(describedPlace);

		List<Place> categoryList = placesPerNameMap.get(category);
		if (categoryList == null) {
			categoryList = new ArrayList<Place>();
			placesPerCategoryMap.put(category, categoryList);
		}
		categoryList.add(describedPlace);
	}

	private void namedPlaceToList(String name, NamedPlace namedPlace, String category) {
		List<Place> nameList = placesPerNameMap.get(name);
		if (nameList == null) {
			nameList = new ArrayList<Place>();
			placesPerNameMap.put(name, nameList);
		}
		nameList.add(namedPlace);

		List<Place> categoryList = placesPerCategoryMap.get(category);
		if (categoryList == null) {
			categoryList = new ArrayList<Place>();
			placesPerCategoryMap.put(category, categoryList);
		}
		categoryList.add(namedPlace);
	}

	public static void main(String[] args) {
		launch(args);
	}
}
