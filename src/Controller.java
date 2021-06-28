package sample;

import com.jfoenix.controls.*;
import javafx.animation.Animation;
import javafx.animation.PathTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.net.URL;

import java.util.List;

import static java.lang.StrictMath.abs;

public class Controller implements Initializable {

    Map<String,Vertex> vertexNameToVertex = new TreeMap<>();        // vertex Name --> vertex
    Map<String,List<Edge>> vertexNameToEdgeList = new TreeMap<>();  // vertex Name --> edgeList
    List<String> PATH = new LinkedList<>();
    ObservableList<Node> edgeDragger;

    private final int scalingFactor = 1;               // <-- Change the scaling factor from here
    private int vertex_count;
    private int edge_count;
    private int events_count;
    private boolean first = true;
    private boolean isImported = false;
    private boolean wantToHighlightPath = true;
    private VGFXVertex start = null, end = null;
    private VGFXVertex selectedVertex = null;
    private final String[] shapeList = {"Default","Square","Plus","Triangle","Cross"};

    public boolean isFirst() { return first; }
    public void setFirst(boolean first) { this.first = first; }

    @FXML private ToggleGroup toggler = new ToggleGroup();
    @FXML private JFXToggleButton vertexModeTButton;
    @FXML private JFXToggleButton dragModeTButton;
    @FXML private JFXToggleButton addEdgeTButton;
    @FXML private JFXToggleButton addVertexTButton;
    @FXML private JFXToggleButton deleteModeTButton;
    @FXML private JFXToggleButton editModeTButton;
    @FXML private JFXButton clearConsoleButton;

    @FXML private JFXToggleButton sourceModeTButton;
    @FXML private JFXToggleButton destinationModeTButton;
    @FXML private JFXButton pathSearchButton;
    @FXML private JFXButton deleteEdgeButton;
    @FXML private JFXButton clearCanvasButton;
    @FXML private JFXButton deleteVertexButton;
    @FXML private JFXButton assignWeightButton;
    @FXML private JFXButton assignVertexNameButton;
    @FXML private JFXButton startAnimationButton;
    @FXML private JFXButton killAnimationsButton;
    @FXML private JFXButton deSelectorButton;
    @FXML private JFXButton clearHighLightedPaths;
    @FXML private JFXTextField weightField;
    @FXML private JFXTextField vertexNameField;
    @FXML private JFXTextArea actionHistoryConsole;
    @FXML private Label coordinatesLabel;
    @FXML private ChoiceBox<String> shapeSelector;
    @FXML private MenuItem importFile;
    @FXML private MenuItem exportFile;
    @FXML private MenuItem aboutPage;
    @FXML private Group mainGroup;
    @FXML private Pane mainPane;

    // EXTRA

    @FXML private JFXTextField Mfromvertex;
    @FXML private JFXTextField Mtovertex;
    @FXML private JFXTextField Mweight;
    @FXML private JFXButton modifyEdgeButton;

    @FXML private JFXTextField AvertexName;
    @FXML private JFXTextField Ax_coordinate;
    @FXML private JFXTextField Ay_coordinate;
    @FXML private JFXButton addvertexButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Starting values of attributes
        vertex_count = 0;
        edge_count = 0;
        events_count = 0;
        edgeDragger = mainGroup.getChildren();

        // Initially all disabled

        assignWeightButton.setDisable(true);
        assignVertexNameButton.setDisable(true);
        modifyEdgeButton.setDisable(true);
        addvertexButton.setDisable(true);

        // Setting common toggleGroup

        addVertexTButton.setToggleGroup(toggler);
        deleteModeTButton.setToggleGroup(toggler);
        addEdgeTButton.setToggleGroup(toggler);
        dragModeTButton.setToggleGroup(toggler);
        sourceModeTButton.setToggleGroup(toggler);
        destinationModeTButton.setToggleGroup(toggler);
        editModeTButton.setToggleGroup(toggler);

        // Default selected options

        addVertexTButton.setSelected(true);
        addEdgeTButton.setSelected(false);
        vertexModeTButton.setSelected(true);
        deleteModeTButton.setSelected(false);

        // Adding shapeList for animation

        shapeSelector.getItems().addAll(shapeList);
        shapeSelector.setValue("Default");

        System.out.println("Testing on progress");

        // Button Listeners
        
        clearCanvasButton.setOnAction(this::clearCanvas);
        deleteVertexButton.setOnAction(this::deleteVertex);
        deleteEdgeButton.setOnAction(this::deleteEdge);
        pathSearchButton.setOnAction(this::searchAction);
        assignWeightButton.setOnAction(this::assignWeight);
        assignVertexNameButton.setOnAction(this::assignName);
        startAnimationButton.setOnAction(this::startAnimationAction);
        killAnimationsButton.setOnAction(this::clearPane);
        clearConsoleButton.setOnAction(this::clearConsole);
        deSelectorButton.setOnAction(this::deSelecteverything);
        modifyEdgeButton.setOnAction(this::modifyEdgeAction);
        addvertexButton.setOnAction(this::addvertexAction);
        clearHighLightedPaths.setOnAction(this::clearHLPaths);

        // Menu listeners
        aboutPage.setOnAction(this::aboutOnAction);
        exportFile.setOnAction(this::exportOnAction);
        importFile.setOnAction(this::importOnAction);

    }

    // MODIFICATION

    public void alertWindow(Alert.AlertType type,String msg)      // Exception AlertBox Generator
    {
        Alert alert = new Alert(type,msg,ButtonType.OK);
        alert.setResizable(false);
        alert.setTitle("Exception");
        alert.showAndWait();
    }

    public void modifyEdgeAction(ActionEvent e)
    {
        ObservableList<Node> nodeList = mainGroup.getChildren();
        System.out.println("MODIFICATION EDGE started");
        boolean exceptionFound = false;

        try
        {
            if(vertexNameToEdgeList.containsKey(Mfromvertex.getText()))
            {
                List<Edge> edgeList = vertexNameToEdgeList.get(Mfromvertex.getText());
                for(Edge edge : edgeList)
                {
                    if(edge.getToVertex().equals(Mtovertex.getText()))
                    {
                        exceptionFound = false;
                        break;
                    }
                    exceptionFound = true;
                }
                if(exceptionFound)
                {
                    throw new Exception("Edge not found");
                }
            }
            else
            {
                throw new Exception("Edge not found");
            }
        }
        catch (Exception E)
        {
            alertWindow(Alert.AlertType.WARNING,"Edge doesn't exists for modification");
            eventOccurred("Edge doesn't exists for modification",0);
            return;
        }
        
        for(Node node : nodeList)
        {
            if(node instanceof VGFXEdge)
            {
                boolean fx =  (int)vertexNameToVertex.get(Mfromvertex.getText()).getPoint().getX() == (int)((VGFXEdge) node).getStartX();
                boolean fy =  (int)vertexNameToVertex.get(Mfromvertex.getText()).getPoint().getY() == (int)((VGFXEdge) node).getStartY();
                boolean ex =  (int)vertexNameToVertex.get(Mtovertex.getText()).getPoint().getX() ==  (int)((VGFXEdge) node).getEndX();
                boolean ey =  (int)vertexNameToVertex.get(Mtovertex.getText()).getPoint().getY() ==  (int)((VGFXEdge) node).getEndY();
                if(fx && fy && ex && ey)
                {
                    ((VGFXEdge) node).setWeight(Mweight.getText());
                    List<Edge> edgeList = vertexNameToEdgeList.get(Mfromvertex.getText());
                    for(Edge edge : edgeList)
                    {
                        if(edge.getToVertex().equals(Mtovertex.getText()))
                        {
                            edge.setWeight(Mweight.getText());
                        }
                    }
                    System.out.println("Modified edge manually");
                    eventOccurred("Modified edge manually",0);
                    break;    // Added break here
                }
            }
        }
    }

    public void addvertexAction(ActionEvent e)
    {
        if(!vertexNameToVertex.containsKey(AvertexName.getText()))
        {
            vertex_count++;
            VGFXVertex vertex = new VGFXVertex(Double.parseDouble(Ax_coordinate.getText())*5,Double.parseDouble(Ay_coordinate.getText())*5,10,Color.BLUE,vertex_count);
            vertex.setName(AvertexName.getText());
            vertex.setOnMouseClicked(vertexMouseHandler);
            vertex.setOnMouseReleased(vertexMouseHandler);
            vertex.setOnMouseDragged(vertexMouseHandler);
            vertex.setOnMousePressed(vertexMouseHandler);

            vertexNameToVertex.put(AvertexName.getText(),new Vertex(AvertexName.getText(),5*Integer.parseInt(Ax_coordinate.getText()),5*Integer.parseInt(Ay_coordinate.getText())));
            vertexNameToEdgeList.put(AvertexName.getText(),new LinkedList<>());
            System.out.println("Vertex added manually");
        }
        else
        {
            alertWindow(Alert.AlertType.WARNING,"Vertex already present, duplicated vertex names are not allowed");
            eventOccurred("Vertex already present, duplicated vertex names are not allowed",0);
        }

    }

    // MouseEvent Handlers -------------------------------------------------------------

    EventHandler<MouseEvent> vertexMouseHandler = new EventHandler<>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            System.out.println(mouseEvent.getSource());
            System.out.println("INSIDE HANDLER ");
            VGFXVertex vertex = (VGFXVertex) mouseEvent.getSource();
            if ((deleteModeTButton.isSelected()) && vertexModeTButton.isSelected()) {
                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
                    if (!vertex.isSelected()) {
                        System.out.println(" clicked ");
                        vertex.setFill(Color.RED);
                        vertex.setSelected();
                    } else {
                        System.out.println(" not selected");
                        vertex.setFill(Color.BLUE);
                        vertex.setNotSelected();
                    }
                }
            } else if (dragModeTButton.isSelected() && vertexModeTButton.isSelected()) {
                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
                    if (!vertex.isSelected()) {
                        System.out.println(" pressed");
                        vertex.setSelected();
                        vertex.setFill(Color.RED);
                    }
                }

                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                    if (vertex.isSelected()) {
                        dragEdges((int) vertex.getCenterX(), (int) vertex.getCenterY(), (int) mouseEvent.getX(), (int) mouseEvent.getY());
                        System.out.println(" dragging");
                        vertex.setCenterX(mouseEvent.getX());
                        vertex.setCenterY(mouseEvent.getY());
                        vertex.getVertexLabel().setLayoutX(mouseEvent.getX() - 15);
                        vertex.getVertexLabel().setLayoutY(mouseEvent.getY() - 27);
                        vertex.setSelected();
                        vertex.setFill(Color.GOLD);
                    }
                }
                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {
                    if (vertex.isSelected()) {
                        System.out.println(" released ");
                        vertex.setNotSelected();
                        vertex.setFill(Color.BLUE);

                        // Update internal graph vertex and edges
                        vertexNameToVertex.get(vertex.getVertexname()).getPoint().setLocation((int) vertex.getCenterX(), (int) vertex.getCenterY());
                        updateEdges((int) vertex.getPoint().getX(), (int) vertex.getPoint().getY());
                    }
                }
            } else if (addEdgeTButton.isSelected() && vertexModeTButton.isSelected()) {
                if (isFirst()) {
                    if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
                        start = (VGFXVertex) mouseEvent.getSource();
                        start.setFill(Color.RED);
                        setFirst(false);
                    }
                } else {
                    if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
                        end = (VGFXVertex) mouseEvent.getSource();
                        if (start == end) {
                            start.setFill(Color.BLUE);
                            start = null;
                            end = null;
                            setFirst(true);
                        } else {
                            end.setFill(Color.RED);
                            setFirst(true);

                            edge_count++;
                            VGFXEdge edge = new VGFXEdge(start.getPoint().getX(), start.getPoint().getY(), end.getPoint().getX(), end.getPoint().getY(), String.valueOf(edge_count));
                            edge.setStrokeWidth(4);

                            start.setFill(Color.BLUE);
                            end.setFill(Color.BLUE);

                            // Graph Edge added
                            vertexNameToEdgeList.get(start.getVertexname()).add(new Edge(start.getVertexname(), (int) start.getCenterX(), (int) start.getCenterY(), end.getVertexname(), (int) end.getCenterX(), (int) end.getCenterY(), edge.getWeight()));

                            edge.setOnMouseClicked(edgeMouseHandler);

                            start = null;
                            end = null;
                        }
                    }
                }
            } else if (sourceModeTButton.isSelected() && vertexModeTButton.isSelected()) {
                if (!vertex.isSource()) {
                    System.out.println(" clicked ");
                    vertex.setFill(Color.RED);
                    vertex.setSource(true);
                } else {
                    System.out.println(" clicked again ");
                    vertex.setFill(Color.BLUE);
                    vertex.setSource(false);
                }
            } else if (destinationModeTButton.isSelected() && vertexModeTButton.isSelected()) {
                if (!vertex.isDestination()) {
                    System.out.println(" clicked ");
                    vertex.setFill(Color.GREEN);
                    vertex.setDestination(true);
                } else {
                    System.out.println(" clicked again ");
                    vertex.setFill(Color.BLUE);
                    vertex.setDestination(false);
                }
            } else if (editModeTButton.isSelected() && vertexModeTButton.isSelected()) {
                if (!vertex.isSelected()) {
                    if (selectedVertex != null) {
                        selectedVertex.setNotSelected();
                        selectedVertex.setFill(Color.BLUE);
                    }

                    selectedVertex = vertex;
                    vertex.setFill(Color.RED);
                    vertex.setSelected();
                } else {
                    vertex.setFill(Color.BLUE);
                    vertex.setNotSelected();
                    selectedVertex = null;
                }
            }
        }
    };

    EventHandler<MouseEvent> edgeMouseHandler = new EventHandler<>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            System.out.println(mouseEvent.getSource());
            System.out.println("INSIDE EDGE HANDLER");
            VGFXEdge edge = (VGFXEdge) mouseEvent.getSource();
            if (((deleteModeTButton.isSelected()) || editModeTButton.isSelected()) && vertexModeTButton.isSelected()) {
                //System.out.println("1");
                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
                    //System.out.println("2");
                    if (!edge.isSelected()) {
                        //System.out.println("3");
                        edge.setSelected(true);
                        edge.setStroke(Color.RED);
                    } else {
                        //System.out.println("4");
                        edge.setSelected(false);
                        edge.setStroke(Color.BLACK);
                    }
                }
            }
        }
    };

    @FXML
    public void handler(MouseEvent mouseEvent)      // Pane <--
    {
        if(vertexModeTButton.isSelected()) {
            if(addVertexTButton.isSelected()) {
                if (!mouseEvent.getSource().equals(mainGroup) && mouseEvent.getButton() == MouseButton.PRIMARY) {
                    System.out.println("X = " + mouseEvent.getX() + " Y = " + mouseEvent.getY());
                    System.out.println(mouseEvent.getSource());
                    System.out.println("a mouse event");

                    vertex_count++;
                    VGFXVertex vertex = new VGFXVertex(mouseEvent.getX(), mouseEvent.getY(), 10, Color.BLUE,vertex_count);
                    eventOccurred("  Vertex  "+vertex.getVertexname()+"( "+(int)mouseEvent.getX()+" , "+(int)mouseEvent.getY()+" )"+"  added" ,0);


                    // adding in graph---------------------------------------------------------------

                    vertexNameToVertex.put(vertex.getVertexname(),new Vertex(vertex.getVertexname(),(int)vertex.getCenterX(),(int)vertex.getCenterY()));  // Graph vertex
                    vertexNameToEdgeList.put(vertex.getVertexname(),new LinkedList<>());  // Empty edgeList for this vertex
                    // ------------------------------------------------------------------------------

                    vertex.setOnMouseClicked(vertexMouseHandler);
                    vertex.setOnMouseReleased(vertexMouseHandler);
                    vertex.setOnMouseDragged(vertexMouseHandler);
                    vertex.setOnMousePressed(vertexMouseHandler);


                }
            }
        }
    }

    // Console clearing methods and handlers -------------------------------------------

    public void eventOccurred(String event,int mod)
    {
        if(mod == 0) {
            events_count++;
            actionHistoryConsole.appendText("Event "+events_count+": " + event + "\n");
        }
        else
            actionHistoryConsole.appendText("      "+"       "+event+"\n");
    }

    public void clearConsole(ActionEvent e){ actionHistoryConsole.clear();}

    public void clearPane(ActionEvent e)
    {
        mainPane.getChildren().clear();
        eventOccurred("Animations killed",0);
    }

    public void clearCanvas(ActionEvent e)
    {
        while(mainGroup.getChildren().size()!=1)
            mainGroup.getChildren().remove(mainGroup.getChildren().size()-1);

        vertex_count = 0;
        edge_count = 0;
        events_count = 0;
        isImported = false;
        actionHistoryConsole.clear();
        System.out.println("clearing canvas");
        eventOccurred("Graph cleared",0);
    }

    public void clearHLPaths(ActionEvent e)
    {
        ObservableList<Node> groupList = mainGroup.getChildren();
        for(int i=groupList.size()-1;i>=0;i--)
        {
            Node node = groupList.get(i);
            if(node instanceof Polyline)
                groupList.remove(i);
        }
        eventOccurred("Highlighted Paths cleared",0);
    }

    public void deSelecteverything (ActionEvent e)
    {
        ObservableList<Node> nodeList = mainGroup.getChildren();
        for(Node node : nodeList)
        {
            if(node instanceof VGFXVertex)
            {
                ((VGFXVertex) node).setFill(Color.BLUE);
                ((VGFXVertex) node).setSelected();
                ((VGFXVertex) node).setSource(false);
                ((VGFXVertex) node).setDestination(false);
            }
            if(node instanceof VGFXEdge)
            {
                ((VGFXEdge) node).setFill(Color.BLACK);
                ((VGFXEdge) node).setStroke(Color.BLACK);
                ((VGFXEdge) node).setSelected(false);
            }
        }
        eventOccurred("Deselected everything..",0);
    }

    // Graph Removal methods and handlers ------------------------------------------------

    public void dragEdges(int x,int y,int newX,int newY)
    {
        for(Node node : edgeDragger)
        {
            if(node instanceof VGFXEdge && ((VGFXEdge) node).getStartX()==x && ((VGFXEdge) node).getStartY()==y)
            {
                ((VGFXEdge) node).setStartX(newX);
                ((VGFXEdge) node).setStartY(newY);
                ((VGFXEdge) node).updateLabelCoordinates();
                continue;
            }

            if(node instanceof VGFXEdge && ((VGFXEdge) node).getEndX()==x && ((VGFXEdge) node).getEndY()==y)
            {
                ((VGFXEdge) node).setEndX(newX);
                ((VGFXEdge) node).setEndY(newY);
                ((VGFXEdge) node).updateLabelCoordinates();
            }
        }
    }

    public void updateEdges(int x,int y)
    {
        for(Map.Entry<String,List<Edge>> entry: vertexNameToEdgeList.entrySet())
        {
            List<Edge> edgeList = entry.getValue();
            for(Edge edge : edgeList)
            {
                if(edge.getFromPoint().getX() == x && edge.getFromPoint().getY() == y)
                    edge.getFromPoint().setLocation(x,y);
                if(edge.getToPoint().getX() == x && edge.getToPoint().getY() == y)
                    edge.getToPoint().setLocation(x,y);
            }
        }
    }

    public void deleteEdge(ActionEvent e)
    {
        ObservableList<Node> edgeList = mainGroup.getChildren();
        for(int i=edgeList.size()-1 ; i > 0 ; i--)
        {
            Node node = edgeList.get(i);
            if(node instanceof VGFXEdge)
            {
                if(((VGFXEdge) node).isSelected())
                {

                    // Graph Work======================================================================================
                    deleteThatEdge((int)((VGFXEdge) node).getStartX(),(int)((VGFXEdge) node).getStartY(),(int)((VGFXEdge) node).getEndX(),(int)((VGFXEdge) node).getEndY());

                    mainGroup.getChildren().remove(((VGFXEdge) node).getWeightLabel());   // <--- modified here
                    //=================================================================================================
                    eventOccurred("Edge deleted ",0);
                    edge_count--;
                    edgeList.remove(node);


                }
            }
        }
    }

    public int deleteEdges(int x,int y)
    {
        int c = 0;
        ObservableList<Node> edgeList = mainGroup.getChildren();
        for(int i=edgeList.size()-1 ; i > 0 ;i--)
        {
            Node node = edgeList.get(i);
            if(node instanceof VGFXEdge)
            {
                int fromX = (int)((VGFXEdge) node).getStartX();
                int fromY = (int)((VGFXEdge) node).getStartY();
                int endX = (int)((VGFXEdge) node).getEndX();
                int endY = (int)((VGFXEdge) node).getEndY();

                if(fromX == x && fromY == y) {
                    mainGroup.getChildren().remove(((VGFXEdge) node).getWeightLabel());
                    System.out.println("E1");
                    edgeList.remove(node);
                    c++;
                }

                if(endY == y && endX == x) {
                    mainGroup.getChildren().remove(((VGFXEdge) node).getWeightLabel());
                    System.out.println("E2");
                    edgeList.remove(node);
                    c++;
                }
            }
        }
        return c;
    }

    public void deleteVertex(ActionEvent e)
    {
        ObservableList<Node> vertexList= mainGroup.getChildren();
        int upperLimit = vertexList.size()-1;
        for(int i=upperLimit;i > 0;i--)
        {
            Node node = vertexList.get(i);
            if(node instanceof VGFXVertex)
            {
                if(((VGFXVertex) node).isSelected())
                {
                    int c = deleteEdges((int)((VGFXVertex) node).getPoint().getX(),(int)((VGFXVertex) node).getPoint().getY());   // not sure
                    //deleteLabel(((VGFXVertex) node).getVertexname()); // sure
                    mainGroup.getChildren().remove(((VGFXVertex) node).getVertexLabel());

                    // Graph work=================================================
                    vertexNameToVertex.remove(((VGFXVertex) node).getVertexname()); // killing graph vertex
                    vertexNameToEdgeList.remove(((VGFXVertex) node).getVertexname());  // killing vertex edge list
                    deleteRestGraphEdges(((VGFXVertex) node).getVertexname());  // killing remaining edges
                    // ===========================================================

                    vertex_count--;
                    vertexList.remove(node);                          // sure
                    System.out.println(" V removed");
                    eventOccurred("Vertex removed successfully",0);
                }
            }
        }
    }

    private void deleteThatEdge(int fx,int fy,int ex,int ey)
    {
        for(Map.Entry<String,List<Edge>> entry : vertexNameToEdgeList.entrySet())
        {
            List<Edge> edgeList = entry.getValue();
            for(int i = edgeList.size()-1 ; i>=0 ;i--)
            {
                Edge edge = edgeList.get(i);
                if(edge.getFromPoint().getX() == fx && edge.getFromPoint().getY() == fy && edge.getToPoint().getX() == ex && edge.getToPoint().getY() == ey)
                {    edgeList.remove(edge);  break;  }

            }
        }
    }

    private void deleteRestGraphEdges(String vertexName)
    {
        for(Map.Entry<String,List<Edge>> entry : vertexNameToEdgeList.entrySet())
        {
            List<Edge> tempEL = entry.getValue();
            for(int i = tempEL.size()-1 ;i>=0;i--)
            {
                Edge edge = tempEL.get(i);
                if(edge.getFromVertex().equals(vertexName) || edge.getToVertex().equals(vertexName))
                    tempEL.remove(edge);
            }

        }

    }

    // Menu ActionHandlers ----------------------------------------------------------------------

    public void importOnAction(ActionEvent e)
    {
        clearCanvasButton.fire();
        killAnimationsButton.fire();

        vertexNameToVertex = new TreeMap<>();
        vertexNameToEdgeList = new TreeMap<>();
        System.out.println("DONE till here");
        FileChooser importFileChooser = new FileChooser();
        try
        {
            importFileChooser.setInitialDirectory(new File(new File(".").getCanonicalPath()));
            importFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files (.txt)","*.txt"));
        }
        catch (IOException E)
        {
            System.out.println("Import Initial DIR error");
            E.printStackTrace();
        }
        File selectedFile = importFileChooser.showOpenDialog(null);

        if(selectedFile != null)
        {
            try {
                BufferedReader br = new BufferedReader(new FileReader(selectedFile));
                isImported = true;
                readInputFile(br);
            }
            catch(FileNotFoundException E)
            {
                System.out.println("\n  File not found");
                E.printStackTrace();
            }
            catch (IOException E)
            {
                System.out.println("\n  Buffer error");
                E.printStackTrace();
            }
        }
        else
        {
            actionHistoryConsole.appendText("\n  File not selected to import");
        }
    }

    private void readInputFile(BufferedReader in) throws IOException
    {
        int vertices = Integer.parseInt(in.readLine().trim());
        while(vertices != 0)
        {
            String[] temp_vertex = in.readLine().trim().split(" ");
            try
            {
                if(temp_vertex.length !=3)
                {
                    throw new Exception("Invalid input file format");
                }
                else
                {
                    double x = Double.parseDouble(temp_vertex[1]);
                    double y = Double.parseDouble(temp_vertex[2]);
                }
            }
            catch (Exception e)
            {
                eventOccurred("Invalid input file, rolling back",0);
                alertWindow(Alert.AlertType.WARNING,"Invalid vertex format in file");
                clearCanvasButton.fire();
                killAnimationsButton.fire();
                return;
            }

            vertex_count++;
            VGFXVertex v1 = new VGFXVertex(Double.parseDouble(temp_vertex[1])*scalingFactor,Double.parseDouble(temp_vertex[2])*scalingFactor,10, Color.BLUE,vertex_count);

            v1.setName(temp_vertex[0]);
            v1.setOnMouseClicked(vertexMouseHandler);
            v1.setOnMouseReleased(vertexMouseHandler);
            v1.setOnMouseDragged(vertexMouseHandler);
            v1.setOnMousePressed(vertexMouseHandler);

            vertexNameToVertex.put(temp_vertex[0],new Vertex(temp_vertex[0],Integer.parseInt(temp_vertex[1])*scalingFactor,Integer.parseInt(temp_vertex[2])*scalingFactor));
            vertexNameToEdgeList.put(temp_vertex[0],new LinkedList<>());

            vertices--;
        }

        int edges = Integer.parseInt(in.readLine().trim());
        while(edges != 0)
        {
            String[] temp_edge = in.readLine().trim().split(" ");
            try
            {
                if(temp_edge.length !=3)
                {
                    throw new Exception("Invalid input file format");
                }
                else
                {
                    if(!(vertexNameToVertex.containsKey(temp_edge[0]) && vertexNameToVertex.containsKey(temp_edge[1])))
                        throw new Exception("Edge not found");

                    double y = Double.parseDouble(temp_edge[2]);
                }
            }
            catch (Exception e)
            {
                eventOccurred("Invalid input file, rolling back",0);
                alertWindow(Alert.AlertType.WARNING,"Invalid edge format in file");
                clearCanvasButton.fire();
                killAnimationsButton.fire();
                return;
            }

            edge_count++;

            double fx = vertexNameToVertex.get(temp_edge[0]).getPoint().getX();
            double fy = vertexNameToVertex.get(temp_edge[0]).getPoint().getY();
            double ex = vertexNameToVertex.get(temp_edge[1]).getPoint().getX();
            double ey = vertexNameToVertex.get(temp_edge[1]).getPoint().getY();

            VGFXEdge e1 = new VGFXEdge(fx,fy,ex,ey,String.valueOf(edge_count));
            //mainGroup.getChildren().add(e1);  Already added inside constructor
            e1.setWeight(temp_edge[2]);
            e1.setStrokeWidth(4);
            e1.setOnMouseClicked(edgeMouseHandler);

            vertexNameToEdgeList.get(temp_edge[0]).add(new Edge(temp_edge[0],(int)fx,(int)fy,temp_edge[1],(int)ex,(int)ey,temp_edge[2]));
            edges--;
        }
        eventOccurred("File imported successfully",0);
    }

    public void exportOnAction(ActionEvent e)
    {
        FileChooser exportFileChooser = new FileChooser();
        try
        {
            exportFileChooser.setInitialDirectory(new File(new File(".").getCanonicalPath()));
            exportFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files (.txt)","*.txt"));
        }
        catch (IOException E)
        {
            System.out.println("Export Initial DIR error");
            E.printStackTrace();
        }

        File filetobesaved = exportFileChooser.showSaveDialog(null);

        if(filetobesaved != null)
        {
            try {
                Writer out = new FileWriter(filetobesaved);
                PrintWriter writer = new PrintWriter(out);
                writeInFile(writer);
            }
            catch (Exception E)
            {
                System.out.println("\n  Export error");
                E.printStackTrace();
            }
        }
        else
        {
            actionHistoryConsole.appendText("\n  File not specified for export");
        }

    }

    private void writeInFile(PrintWriter out)
    {
        out.println(vertex_count);
        for(Map.Entry<String,Vertex> entry: vertexNameToVertex.entrySet())
        {
            if(!isImported)
                out.println(entry.getValue());
            else
                out.println(entry.getValue().scaledOutput());
        }

        out.println(edge_count);
        List<Edge> totalEdges = new ArrayList<>();
        for(Map.Entry<String,List<Edge>> entry : vertexNameToEdgeList.entrySet()) { totalEdges.addAll(entry.getValue()); }

        totalEdges.sort((o1, o2) -> {
            if(o1.getFromVertex().compareTo(o2.getFromVertex()) == 0)
                return o1.getToVertex().compareTo(o2.getToVertex());
            return o1.getFromVertex().compareTo(o2.getFromVertex());
        });

        for(Edge edge : totalEdges) { out.println(edge); }

        eventOccurred("File exported successfully",0);
        out.close();
    }

    public void aboutOnAction(ActionEvent e)
    {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("AboutWindow.fxml"));
            Stage aboutWindow = new Stage();
            aboutWindow.setTitle("About");
            aboutWindow.initModality(Modality.APPLICATION_MODAL);
            aboutWindow.setResizable(false);
            aboutWindow.getIcons().add(new Image("/res/Wildcat.png"));       // ** Hardcoded
            aboutWindow.setScene(new Scene(root));
            aboutWindow.showAndWait();
        }
        catch (IOException E)
        {
            System.out.println("About page error");
            E.printStackTrace();
        }
    }

    // Custom Shape classes defined below-----------------------------------------------------

    class VGFXVertex extends Circle         // For Plotting
    {
        private boolean selected;
        private boolean isSource;
        private boolean isDestination;
        private Point point;
        private Label vertexLabel;
        private String name;
        private String id;

        VGFXVertex(double x, double y, double radius, Color color,int id)
        {
            super(x,y,radius,color);
            this.selected = false;
            this.isSource = false;
            this.isDestination = false;
            this.id = String.valueOf(id);
            this.point = new Point((int)x, (int)y );

            vertexLabel = new Label(String.valueOf("Dummy-" + id));
            this.name = "Dummy-"+id;
            vertexLabel.setLayoutX(x-15);
            vertexLabel.setLayoutY(y-27);
            vertexLabel.setOpacity(1.5);
            vertexLabel.setFont(new Font(17));

            mainGroup.getChildren().add(this);
            mainGroup.getChildren().add(vertexLabel);   // Adding label node

        }

        public boolean isSource() { return isSource; }
        public void setSource(boolean source) { isSource = source; }
        public boolean isDestination() { return isDestination; }
        public void setDestination(boolean destination) { isDestination = destination; }
        public Point getPoint() { return point; }
        public void setPoint(Point point) { this.point = point; }
        public Label getVertexLabel() { return vertexLabel; }
        public void setVertexLabel(Label vertexLabel) { this.vertexLabel = vertexLabel; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; this.vertexLabel.setText(name); }
        public String getVertexname() { return vertexLabel.getText(); }
        public void setSelected() { selected = true; }
        public void setNotSelected() {selected = false;}
        public boolean isSelected() { return selected; }

    }

    class VGFXEdge extends Line             // For Plotting
    {
        private boolean selected;
        private String weight,id;
        private Label weightLabel;

        VGFXEdge(double startX,double startY,double endX,double endY,String id)
        {
            super((int)startX,(int)startY,(int)endX,(int)endY);
            this.weight = weight;
            this.selected = false;


            weightLabel = new Label("NULL-"+id);
            weightLabel.setTextFill(Color.RED);
            weightLabel.setOpacity(1.5);
            weightLabel.setLayoutX((startX+endX)/2);
            weightLabel.setLayoutY((startY+endY)/2);
            weightLabel.setFont(new Font(20));

            mainGroup.getChildren().add(this);
            mainGroup.getChildren().add(weightLabel);

        }

        public boolean isSelected() { return selected; }
        public void setSelected(boolean selected) { this.selected = selected; }
        public Label getWeightLabel() { return weightLabel; }
        public void setWeightLabel(Label weightLabel) { this.weightLabel = weightLabel; }
        public String getWeight() { return weight; }
        public void setWeight(String weight) { this.weight = weight; this.weightLabel.setText(weight);}
        public void updateLabelCoordinates()
        {
            weightLabel.setLayoutX(((this.getStartX()+this.getEndX())/2));
            weightLabel.setLayoutY((this.getStartY()+this.getEndY())/2);
        }
    }

    class Vertex       // For internal graph search
    {
        private String vertexName;
        private Point point;

        Vertex(String vertexName,int x,int y) { this.vertexName = vertexName; this.point = new Point(x,y);}

        public Point getPoint() { return point; }
        public void setPoint(Point point) { this.point = point; }
        public String getVertexName() { return vertexName; }
        public void setVertexName(String vertexName) { this.vertexName = vertexName; }
        public String scaledOutput(){ return vertexName+" "+(int)(this.point.getX()/scalingFactor)+" "+(int)(this.point.getY()/scalingFactor);}

        @Override
        public String toString(){ return vertexName+" "+(int)this.point.getX()+" "+(int)this.point.getY(); }
    }

    class Edge         // For internal graph search
    {
        private String fromVertex;
        private Point fromPoint,toPoint;
        private String toVertex;
        private String weight;

        Edge(String fromVertex,int fx,int fy,String toVertex,int ex,int ey,String weight)
        {
            this.fromPoint = new Point(fx,fy);
            this.toPoint = new Point(ex,ey);
            this.fromVertex = fromVertex;
            this.toVertex = toVertex;
            this.weight = weight;
        }

        public Point getFromPoint() { return fromPoint; }
        public void setFromPoint(Point fromPoint) { this.fromPoint = fromPoint; }
        public Point getToPoint() { return toPoint; }
        public void setToPoint(Point toPoint) { this.toPoint = toPoint; }
        public String getFromVertex() { return fromVertex; }
        public void setFromVertex(String fromVertex) { this.fromVertex = fromVertex; }
        public String getToVertex() { return toVertex; }
        public void setToVertex(String toVertex) { this.toVertex = toVertex; }
        public String getWeight() { return weight; }
        public void setWeight(String weight) { this.weight = weight; }


        @Override
        public String toString(){ return fromVertex+" "+toVertex+" "+weight; }

    }

    // Graph searching and modification part here ----------------------------------------

    public void searchAction(ActionEvent e) // (H0)
    {
        String source = null;
        String destination = null;

        ObservableList<Node> groupList = mainGroup.getChildren();
        for(int i=groupList.size()-1 ; i>0 ;i--)
        {
            Node node = groupList.get(i);
            if(node instanceof VGFXVertex)
            {
                if(((VGFXVertex) node).isSource())
                {
                    source = ((VGFXVertex) node).getVertexname();
                    ((VGFXVertex) node).setSource(false);
                }

                if(((VGFXVertex) node).isDestination()) {
                    destination = ((VGFXVertex) node).getVertexname();
                    ((VGFXVertex) node).setDestination(false);
                }
            }
        }

        if(source == null)
        {
            eventOccurred("Source not selected",0);
            alertWindow(Alert.AlertType.WARNING,"Source not selected");
            return;
        }
        if(destination == null)
        {
            eventOccurred("Destination not selected",0);
            alertWindow(Alert.AlertType.WARNING,"Destination not selected");
            return;
        }

        Stack<String> path = path_search(source,destination);
        eventOccurred("Searching path from "+source+" -> "+destination+" ...",0);

        if(path.size() == 0) {
            eventOccurred("Path not found for " + source + " -> " + destination, 1);
            return;
        }

        Polyline highlightedPath = new Polyline();
        highlightedPath.setStrokeWidth(4);
        highlightedPath.setStroke(Color.YELLOWGREEN);

        StringBuilder road = new StringBuilder();
        while(path.size()!=0)
        {
            if(wantToHighlightPath)
                highlightedPath.getPoints().addAll(vertexNameToVertex.get(path.peek()).getPoint().getX(),vertexNameToVertex.get(path.peek()).getPoint().getY());

            road.append(path.peek()).append("->");
            PATH.add(path.pop());
        }

        if(wantToHighlightPath)
            mainGroup.getChildren().add(highlightedPath);


        eventOccurred(road.subSequence(0,road.length()-2).toString(),1);
    }

    public Stack<String> path_search(String source,String destination)          // Helper Class (H1)
    {
        Stack<String> path = new Stack<>();

        if(!vertexNameToVertex.containsKey(source) || !vertexNameToVertex.containsKey(destination))    // if unknown vertex is entered
        {
            path.push("Vertex unknown");
            return path;
        }

        Map<String,Double> distance = new TreeMap<>();
        Map<String,String> parent = new TreeMap<>();
        Map<String,Boolean> visited = new TreeMap<>();

        for(Map.Entry<String,Vertex> entry : vertexNameToVertex.entrySet())
        {
            distance.put(entry.getKey(),Double.MAX_VALUE);
            parent.put(entry.getKey(),"-1");
            visited.put(entry.getKey(),false);
        }

        distance.put(source,0d);
        parent.put(source,source);

        String start = source;
        while(true)
        {
            if(!vertexNameToEdgeList.containsKey(start)) continue;

            List<Edge> currentvertexList = vertexNameToEdgeList.get(start);
            visited.put(start,true);

            for (Edge road : currentvertexList) {
                System.out.println(road.getFromVertex()+" -> "+road.getToVertex());
                if (!visited.get(road.getToVertex())) {

                    if (Double.compare(distance.get(start)+ Double.parseDouble(road.getWeight()) , distance.get(road.getToVertex())) < 0) {
                        distance.put(road.getToVertex(), distance.get(start) +  Double.parseDouble(road.getWeight())) ;
                        parent.put(road.getToVertex(), start);
                    }

                    // Considering only 4 digits for equality --------------------------------------------------------
                    if (abs(distance.get(start) + Double.parseDouble(road.getWeight()) - distance.get(road.getToVertex())) < 0.0001) {
                        if (start.compareTo(parent.get(road.getToVertex())) < 0)
                            parent.put(road.getToVertex(), start);
                    }
                }
            }

            double min = Double.MAX_VALUE;
            for(Map.Entry<String,Double> entry : distance.entrySet())
            {
                if(entry.getValue() < min && !visited.get(entry.getKey()) && vertexNameToEdgeList.containsKey(entry.getKey())) {
                    start = entry.getKey();
                    min = entry.getValue();
                }
            }
            if(min == Double.MAX_VALUE)
                break;

        }

        if(distance.get(destination).equals(Double.MAX_VALUE))            // if destination is unreachable
            return new Stack<>();

        boolean result = buildPath(source,destination,parent,path);
        if(result)
            return path;
        else
            return new Stack<>();

    }

    private boolean buildPath(String source,String destination,Map<String,String> parent,Stack<String> path)     // Helper Class (H2)
    {
        if(source.equals(destination)) {
            path.push(source);
            return true;
        }
        else if(destination.equals("-1"))
            return false;
        else
        {
            path.push(destination);
            buildPath(source,parent.get(destination),parent,path);
        }
        return true;
    }

    public void assignWeight(ActionEvent e)       // Weight modification (W1)  ** change this
    {
        ObservableList<Node> nodeList = mainGroup.getChildren();
        for(Node node : nodeList)
        {
            if(node instanceof VGFXEdge && ((VGFXEdge) node).isSelected())
            {
                ((VGFXEdge) node).setWeight(weightField.getText());
                ((VGFXEdge) node).setSelected(false);
                ((VGFXEdge) node).setStroke(Color.BLACK);
                setGraphWeight((int)((VGFXEdge) node).getStartX(),(int)((VGFXEdge) node).getStartY(),(int)((VGFXEdge) node).getEndX(),(int)((VGFXEdge) node).getEndY(),weightField.getText());
            }
        }

    }

    public void assignName(ActionEvent e)
    {
        if(vertexNameToVertex.containsKey(vertexNameField.getText()))  // checking for existence in current graph
        {
            selectedVertex.setNotSelected();
            selectedVertex.setFill(Color.BLUE);
            selectedVertex = null;
            alertWindow(Alert.AlertType.WARNING,"Vertex already exists, duplicated vertex names are not allowed");
            return;
        }

        ObservableList<Node> nodeList = mainGroup.getChildren();
        for(Node node: nodeList)
        {
            if(node instanceof VGFXVertex && ((VGFXVertex) node).isSelected())
            {
                // Internal Graph
                String prevName = ((VGFXVertex) node).getVertexname();
                vertexNameToVertex.get(prevName).setVertexName(vertexNameField.getText());
                vertexNameToVertex.put(vertexNameField.getText(),vertexNameToVertex.get(prevName));
                vertexNameToVertex.remove(prevName);

                assignNameForEdges(prevName,vertexNameField.getText());

                // Visual Graph
                ((VGFXVertex) node).setName(vertexNameField.getText());
                ((VGFXVertex) node).setNotSelected();
                ((VGFXVertex) node).setFill(Color.BLUE);

            }
        }
    }

    private void assignNameForEdges(String from,String to)
    {
        vertexNameToEdgeList.put(to,vertexNameToEdgeList.get(from));
        vertexNameToEdgeList.remove(from);

        for(Map.Entry<String,List<Edge>> entry : vertexNameToEdgeList.entrySet())
        {
            List<Edge> edgeList = entry.getValue();
            for(Edge edge : edgeList)
            {
                if(edge.getToVertex().equals(from))
                    edge.setToVertex(to);

                if(edge.getFromVertex().equals(from))
                    edge.setFromVertex(to);

            }


        }

    }

    private void setGraphWeight(int fx,int fy,int ex,int ey,String newWeight)  // (W2)
    {
        for(Map.Entry<String,List<Edge>> entry : vertexNameToEdgeList.entrySet())
        {
            List<Edge> edgelist = entry.getValue();
            for(Edge road : edgelist)
            {
                if(road.getFromPoint().getX() == fx && road.getFromPoint().getY() == fy && road.getToPoint().getX() == ex && road.getToPoint().getY() == ey)
                {
                    road.setWeight(newWeight);
                    return;
                }
            }

        }
    }

    // Animation stuff-----------------------------------------------------

    public void startAnimationAction(ActionEvent e)
    {
        wantToHighlightPath = false;
        pathSearchButton.fire();
        wantToHighlightPath = true;

        eventOccurred("Animation started",0);
        Path path = new Path();
        path.getElements().add(new MoveTo(vertexNameToVertex.get(PATH.get(0)).getPoint().getX(),vertexNameToVertex.get(PATH.get(0)).getPoint().getY()));
        for(int i=1;i<PATH.size();i++)
        {
            path.getElements().add(new LineTo(vertexNameToVertex.get(PATH.get(i)).getPoint().getX(),vertexNameToVertex.get(PATH.get(i)).getPoint().getY()));
        }

        ShapeGenerator shapeGenerator = null;
        Node shape = null;
        switch(shapeSelector.getValue())
        {
            case "Default" :
                shapeGenerator = new CIRCLE(Color.GOLD,10);
                shape = ((CIRCLE)shapeGenerator).getCircle();
                break;
            case "Square" :
                shapeGenerator = new SQUARE(25,25,Color.LIGHTSEAGREEN);
                shape = ((SQUARE)shapeGenerator).getSquare();
                break;
            case "Cross" :
                shapeGenerator = new CROSS(25,25,Color.MAROON);
                shape = ((CROSS)shapeGenerator).getCross();
                break;
            case "Triangle" :
                shapeGenerator = new TRIANGLE(30,30,Color.ORANGERED);
                shape = ((TRIANGLE)shapeGenerator).getTriangle();
                break;
            case "Plus" :
                shapeGenerator = new PLUS(30,30,Color.CRIMSON);
                shape = ((PLUS)shapeGenerator).getPlus();
                break;
        }

        PathTransition VGFXTransition = new PathTransition();
        VGFXTransition.setNode(shape);
        VGFXTransition.setAutoReverse(false);
        VGFXTransition.setPath(path);
        VGFXTransition.setDuration(Duration.seconds(6));
        VGFXTransition.setCycleCount(Animation.INDEFINITE);
        VGFXTransition.play();
        mainPane.getChildren().add(shape);

        PATH = new LinkedList<>();

    }

    // Coordinates monitoring----------------------------------------------

    public void coordinatesMonitor(MouseEvent e)
    {
        if(e.getSource().equals(mainPane))
            coordinatesLabel.setText(" x = "+(int)e.getX()+", y = "+(int)e.getY());
        else {
            coordinatesLabel.setText(e.getSource().toString());
        }
    }

    // KeyPressed Listeners ------------------------------------------------

    public void AV_keypressedProperty()
    {
        String vertex = AvertexName.getText();
        String X = Ax_coordinate.getText();
        String Y = Ay_coordinate.getText();

        boolean isEmpty = (vertex.isEmpty() || vertex.trim().isEmpty()) || (X.isEmpty() || X.trim().isEmpty()) || (Y.isEmpty() || Y.trim().isEmpty());
        addvertexButton.setDisable(isEmpty);
    }

    public void ME_keypressedProperty()
    {
        String fromVertex = Mfromvertex.getText();
        String toVertex = Mtovertex.getText();
        String weight = Mweight.getText();

        boolean isEmpty = (fromVertex.isEmpty() || fromVertex.trim().isEmpty()) || (toVertex.isEmpty() || toVertex.trim().isEmpty()) || (weight.isEmpty() || weight.trim().isEmpty());
        modifyEdgeButton.setDisable(isEmpty);
    }

    public void Aname_keypressedProperty()
    {
        String fromVertex = vertexNameField.getText();
        boolean isEmpty = (fromVertex.isEmpty() || fromVertex.trim().isEmpty());

        assignVertexNameButton.setDisable(isEmpty);
    }

    public void Aweight_keypressedProperty()
    {
        String weight = weightField.getText();
        boolean isEmpty = (weight.isEmpty() || weight.trim().isEmpty());

        assignWeightButton.setDisable(isEmpty);
    }

}
