import java.awt.Color;
import java.util.*;
import javalib.impworld.*;
import javalib.worldimages.TextImage;

// representation of a random maze
class MazeGenerator extends World {
  ArrayList<Edge> edges;
  ArrayList<Node> nodes;
  
  Node currentPos;
  Node start;
  Node target;
  Random rand;
  int numberOfMoves;
  
  int width;
  int height;
  
  boolean autoSolve;
  boolean lockKeyInputs;
  
  // controls which algorithm to use to solve the maze
  SearchAlg algType;
  
  
  
  MazeGenerator(int width, int height, ArrayList<Edge> edges, ArrayList<Node> nodes) {
    this.edges = edges;
    this.nodes = nodes;
    this.width = width;
    this.height = height;
    this.autoSolve = false;
    this.lockKeyInputs = false;
    this.numberOfMoves = 0;
  }
  
  // constructor for making copy
  MazeGenerator(MazeGenerator m) {
    this.edges = m.edges;
    this.nodes = m.nodes;
    this.width = m.width;
    this.height = m.height;
    this.autoSolve = m.autoSolve;
    this.lockKeyInputs = m.lockKeyInputs;
    this.numberOfMoves = m.numberOfMoves;
    this.currentPos = m.currentPos;
    this.start = m.start;
    this.target = m.target;
    this.rand = m.rand;
    this.algType = m.algType;
  }
  
  MazeGenerator(int width, int height, Random rand) {
    this(width, height, new ArrayList<Edge>(), new ArrayList<Node>());
    this.rand = rand;
    this.newMaze();
  }
  
  // progresses the state of the world each tick
  public void onTick() {
    if (this.autoSolve) {
      this.currentPos = this.algType.next();
      this.currentPos.setState(2);
      this.numberOfMoves ++;
    } 
    if (this.currentPos.equals(this.target)) {
      while (!this.algType.next().equals(this.target)) {
        // loop algType.next() until the target finished
      }
      this.algType.trace(currentPos);
      this.target.setState(1);
      this.autoSolve = false;
    }
  }
  
  // reset the maze
  void reset() {
    this.currentPos = this.start;
    this.autoSolve = false;
    this.lockKeyInputs = false;
    for (Node n : this.nodes) {
      n.seen = false;
      n.setState(0);
    }
    this.start.setState(3);
    this.target.setState(4);
    this.numberOfMoves = 0;
  }
  
  // generate new maze
  void newMaze() {
    this.edges = new ArrayList<Edge>();
    this.nodes = new ArrayList<Node>();
    ArrayList<Integer> ints = new ArrayList<Integer>();
    for (int i = 0; i < 2 * height * width - width - height; i++) {
      ints.add(i);
    }
    
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        this.nodes.add(new Node(i, j,
            Math.min(1500 / width,
                     800 / height)));
      }
    }
    
    // set the start and end nodes
    this.nodes.get(0).setState(3);
    this.nodes.get(this.nodes.size() - 1).setState(4);
    
    // add horizontal edges
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width - 1; j++) {
        this.edges.add(new Edge(this.nodes.get(j + width * i), this.nodes.get(j + width * i + 1),
                 ints.remove(this.rand.nextInt(ints.size()))));
      }
    }
    
    // add vertical edges
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height - 1; j++) {
        this.edges.add(new Edge(this.nodes.get(i + width * j),
            this.nodes.get(i + width * j + width),
                 ints.remove(this.rand.nextInt(ints.size()))));
      } 
    }
    this.edges = new Kruskal(this.nodes, this.edges).run();
    
    for (Node n : this.nodes) {
      n.connect(this.edges);
    }
    
    this.start = this.nodes.get(0);
    this.currentPos = this.start;
    this.target = this.nodes.get(this.nodes.size() - 1);
    this.algType = new AStar(this.start, this.target);
    this.reset();
  }
  
  // handles key presses
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      this.reset();
    }
    if (key.equals("n")) {
      this.newMaze();
    }
    if (!this.lockKeyInputs) {
      switch (key) {
        case "b":
          this.algType = new Bfs(this.start, this.target);
          this.lockKeyInputs = true;
          this.autoSolve = true;
          break;
        case "d":
          this.algType = new Dfs(this.start, this.target);
          this.lockKeyInputs = true;
          this.autoSolve = true;
          break;
        case "a":
          this.algType = new AStar(this.nodes.get(0), this.nodes.get(this.nodes.size() - 1));
          this.lockKeyInputs = true;
          this.autoSolve = true;
          break;
        case "left":
        case "right":
        case "up":
        case "down":
          this.autoSolve = false;
          this.lockKeyInputs = true;
          this.algType = new AStar(this.start, this.target);
          break;
        default:
          // do nothing for other keys
      }
    }
    if (!this.autoSolve && this.lockKeyInputs) {
      this.currentPos =  this.currentPos.getNext(key);
      this.numberOfMoves ++;
    }
  }

  // draws the current state of the maze
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(1500, 800);
    for (Node n: nodes) {
      scene.placeImageXY(n.drawNode(), 10, 10);
    }
    scene.placeImageXY(new TextImage("Controls:", 30, Color.black), 1415, 100);
    scene.placeImageXY(new TextImage("New maze: n", 25, Color.black), 1415, 150);
    scene.placeImageXY(new TextImage("Reset: r", 25, Color.black), 1415, 200);
    scene.placeImageXY(new TextImage("Breadth-first:", 25, Color.black), 1415, 300);
    scene.placeImageXY(new TextImage("b", 25, Color.black), 1415, 330);
    scene.placeImageXY(new TextImage("Depth-first:", 25, Color.black), 1415, 380);
    scene.placeImageXY(new TextImage("d", 25, Color.black), 1415, 410);
    scene.placeImageXY(new TextImage("A*: a", 25, Color.black), 1415, 460);
    scene.placeImageXY(new TextImage("Manual solve:", 25, Color.black), 1415, 600);
    scene.placeImageXY(new TextImage("Arrow Keys", 25, Color.black), 1415, 630);
    scene.placeImageXY(new TextImage("Moves:", 30, Color.black), 1415, 700);
    scene.placeImageXY(new TextImage(Integer.toString(this.numberOfMoves),
        30, Color.black), 1415, 740);
    return scene;
  }
}