package org.example.gestioninscripciones;

import org.example.gestioninscripciones.exceptions.*;
import org.example.gestioninscripciones.model.*;
import org.example.gestioninscripciones.service.InscripcionService;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Optional;

public class App extends Application {

    private final InscripcionService service = new InscripcionService();
    private final ObservableList<Inscripcion> tablaObs = FXCollections.observableArrayList();
    private final ObservableList<Inscripcion> esperaObs = FXCollections.observableArrayList();
    private final ObservableList<Carrera> carrerasObs = FXCollections.observableArrayList();

    private final Label lblInfoCupo = new Label();
    private ComboBox<Carrera> cbCarrera;

    @Override
    public void start(Stage stage) {
        // Carga inicial de carreras
        Carrera c1 = new Carrera("INF-301", "Análisis de Sistemas", 2);
        Carrera c2 = new Carrera("INF-302", "Desarrollo Web", 3);
        service.registrarCarrera(c1);
        service.registrarCarrera(c2);
        carrerasObs.addAll(service.getCarreras());

        VBox topContainer = new VBox(10);
        topContainer.setPadding(new Insets(10));
        topContainer.setStyle("-fx-background-color: #e9ecef; -fx-background-radius: 5;");

        // --- SECCIÓN 1: CREAR / ELIMINAR CARRERAS ---
        HBox rowCarreraCrud = new HBox(10);
        rowCarreraCrud.setAlignment(Pos.CENTER_LEFT);

        TextField txtCodCarrera = new TextField();
        txtCodCarrera.setPromptText("Código");
        txtCodCarrera.setPrefWidth(80);

        TextField txtNombreCarrera = new TextField();
        txtNombreCarrera.setPromptText("Nombre Carrera");

        TextField txtCupoCarrera = new TextField();
        txtCupoCarrera.setPromptText("Cupo");
        txtCupoCarrera.setPrefWidth(60);

        Button btnCrearCarrera = new Button("+ Crear");
        btnCrearCarrera.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");

        Button btnEliminarCarrera = new Button("Eliminar Carrera Activa");
        btnEliminarCarrera.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");

        rowCarreraCrud.getChildren().addAll(new Label("Carrera:"), txtCodCarrera, txtNombreCarrera, txtCupoCarrera, btnCrearCarrera, btnEliminarCarrera);

        // --- SECCIÓN 2: SELECCIÓN Y CONTADOR ---
        HBox rowCarreraSel = new HBox(15);
        rowCarreraSel.setAlignment(Pos.CENTER_LEFT);

        cbCarrera = new ComboBox<>(carrerasObs);
        if (!carrerasObs.isEmpty()) cbCarrera.setValue(carrerasObs.get(0));

        lblInfoCupo.setStyle("-fx-font-weight: bold; -fx-text-fill: #1d3d6e; -fx-font-size: 13px;");
        rowCarreraSel.getChildren().addAll(new Label("Carrera Activa:"), cbCarrera, lblInfoCupo);

        // --- SECCIÓN 3: INSCRIPCIÓN DE ALUMNOS ---
        HBox rowInscripcion = new HBox(10);
        rowInscripcion.setAlignment(Pos.CENTER_LEFT);

        TextField txtEstudiante = new TextField();
        txtEstudiante.setPromptText("Nombre Estudiante");

        ComboBox<TipoInscripcion> cbTipo = new ComboBox<>(FXCollections.observableArrayList(TipoInscripcion.values()));
        cbTipo.setValue(TipoInscripcion.REGULAR);

        Button btnInscribir = new Button("Inscribir Estudiante");
        btnInscribir.setStyle("-fx-background-color: #2b579a; -fx-text-fill: white; -fx-font-weight: bold;");

        rowInscripcion.getChildren().addAll(new Label("Estudiante:"), txtEstudiante, new Label("Tipo:"), cbTipo, btnInscribir);

        topContainer.getChildren().addAll(rowCarreraCrud, new Separator(), rowCarreraSel, rowInscripcion);

        // --- TABLA DE CONFIRMADOS (TableView) ---
        TableView<Inscripcion> tabla = new TableView<>(tablaObs);

        TableColumn<Inscripcion, String> colEstudiante = new TableColumn<>("Estudiante");
        colEstudiante.setCellValueFactory(new PropertyValueFactory<>("estudiante"));

        TableColumn<Inscripcion, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));

        TableColumn<Inscripcion, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        tabla.getColumns().addAll(colEstudiante, colTipo, colEstado);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // --- LISTA DE ESPERA (ListView) ---
        VBox rightBox = new VBox(5);
        Label lblEsperaHeader = new Label("Lista de Espera:");
        lblEsperaHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #c0392b;");

        ListView<Inscripcion> listEspera = new ListView<>(esperaObs);
        listEspera.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Inscripcion item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getEstudiante() + " (" + item.getTipo() + ")");
            }
        });

        rightBox.getChildren().addAll(lblEsperaHeader, listEspera);

        SplitPane centerSplit = new SplitPane(tabla, rightBox);
        centerSplit.setDividerPositions(0.65);

        // --- BARRA INFERIOR CRUD PARA INSCRIPCIONES ---
        HBox bottomActions = new HBox(10);
        bottomActions.setPadding(new Insets(10, 0, 0, 0));

        Button btnEliminarInscripcion = new Button("Cancelar / Dar de Baja Inscripción");
        btnEliminarInscripcion.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");

        Button btnModificarTipo = new Button("Cambiar Tipo de Inscripción");
        btnModificarTipo.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;");

        bottomActions.getChildren().addAll(btnEliminarInscripcion, btnModificarTipo);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));
        root.setTop(topContainer);
        root.setCenter(centerSplit);
        root.setBottom(bottomActions);

        // EVENTOS Y ACCIONES

        cbCarrera.setOnAction(e -> {
            if (cbCarrera.getValue() != null) actualizarVistas(cbCarrera.getValue());
        });

        // C - Crear Carrera
        btnCrearCarrera.setOnAction(e -> {
            try {
                String cod = txtCodCarrera.getText().trim();
                String nom = txtNombreCarrera.getText().trim();
                int cupo = Integer.parseInt(txtCupoCarrera.getText().trim());

                if (cod.isEmpty() || nom.isEmpty()) return;

                Carrera nueva = new Carrera(cod, nom, cupo);
                service.registrarCarrera(nueva);
                carrerasObs.add(nueva);
                cbCarrera.setValue(nueva);

                txtCodCarrera.clear();
                txtNombreCarrera.clear();
                txtCupoCarrera.clear();
            } catch (NumberFormatException ex) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "El cupo debe ser un número.");
            }
        });

        // D - Eliminar Carrera
        btnEliminarCarrera.setOnAction(e -> {
            Carrera sel = cbCarrera.getValue();
            if (sel == null) return;

            try {
                service.eliminarCarrera(sel.getCodigo());
                carrerasObs.remove(sel);
                if (!carrerasObs.isEmpty()) {
                    cbCarrera.setValue(carrerasObs.get(0));
                } else {
                    tablaObs.clear();
                    esperaObs.clear();
                }
            } catch (CarreraNoEncontradaException ex) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", ex.getMessage());
            }
        });

        // C - Crear Inscripción
        btnInscribir.setOnAction(e -> {
            String nombre = txtEstudiante.getText().trim();
            Carrera carreraSel = cbCarrera.getValue();

            if (nombre.isEmpty() || carreraSel == null) return;

            try {
                service.inscribir(nombre, carreraSel.getCodigo(), cbTipo.getValue());
                actualizarVistas(carreraSel);
                txtEstudiante.clear();
            } catch (CupoLlenoException ex) {
                mostrarAlerta(Alert.AlertType.WARNING, "Cupo Lleno - Lista de Espera", ex.getMessage());
                actualizarVistas(carreraSel);
                txtEstudiante.clear();
            } catch (InscripcionDuplicadaException | CarreraNoEncontradaException ex) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", ex.getMessage());
            }
        });

        // D - Eliminar / Cancelar Inscripción
        btnEliminarInscripcion.setOnAction(e -> {
            Inscripcion selTabla = tabla.getSelectionModel().getSelectedItem();
            Inscripcion selEspera = listEspera.getSelectionModel().getSelectedItem();
            Inscripcion aEliminar = (selTabla != null) ? selTabla : selEspera;

            if (aEliminar != null && cbCarrera.getValue() != null) {
                service.cancelarInscripcion(cbCarrera.getValue().getCodigo(), aEliminar);
                actualizarVistas(cbCarrera.getValue());
            }
        });

        // U - Modificar Tipo de Inscripción
        btnModificarTipo.setOnAction(e -> {
            Inscripcion sel = tabla.getSelectionModel().getSelectedItem();
            if (sel != null) {
                ChoiceDialog<TipoInscripcion> dialog = new ChoiceDialog<>(sel.getTipo(), TipoInscripcion.values());
                dialog.setTitle("Editar Inscripción");
                dialog.setHeaderText("Cambiar tipo para " + sel.getEstudiante());
                dialog.setContentText("Nuevo Tipo:");

                Optional<TipoInscripcion> result = dialog.showAndWait();
                result.ifPresent(nuevoTipo -> {
                    service.actualizarTipoInscripcion(sel, nuevoTipo);
                    actualizarVistas(cbCarrera.getValue());
                });
            }
        });

        if (!carrerasObs.isEmpty()) actualizarVistas(carrerasObs.get(0));

        Scene scene = new Scene(root, 900, 580);
        stage.setTitle("Gestión de Inscripciones y Cupos - CRUD Completo (JavaFX & JPA)");
        stage.setScene(scene);
        stage.show();
    }

    private void actualizarVistas(Carrera carrera) {
        int confirmados = service.getInscriptos(carrera.getCodigo()).size();
        int espera = service.getListaEspera(carrera.getCodigo()).size();
        int cupoMax = carrera.getCupo();

        lblInfoCupo.setText("Cupo Máximo: " + cupoMax + " | Ocupados: " + confirmados + "/" + cupoMax + " | En Espera: " + espera);

        tablaObs.setAll(service.getInscriptos(carrera.getCodigo()));
        esperaObs.setAll(service.getListaEspera(carrera.getCodigo()));
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String msj) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msj);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}