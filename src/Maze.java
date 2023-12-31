import java.awt.Color;
import java.util.*;

import javalib.impworld.WorldScene;
import javalib.worldimages.*;
import tester.*;

//represents a node in the graph maze
class Node {
  int row;
  int col;
  int size;
  ArrayList<Node> pathTo = new ArrayList<Node>();
  
  boolean seen;
  
  boolean topConnected = false;
  boolean bottomConnected = false;
  boolean leftConnected = false;
  boolean rightConnected = false;
  
  Node top;
  Node bottom;
  Node left;
  Node right;
  
  // 1 is solved, 2 is explored, 3 is start, 4 is end
  int state;
  
  Node(int row, int col, int size) {
    this.row = row;
    this.col = col;
    this.state = 0;
    this.size = size;
    this.seen = false;
  }
  
  // default size is 50 for most tests
  Node(int row, int col) {
    this(row, col, 50);
  }
  
  // compute the hashcode based on x and y
  public int hashCode() {
    return 1000 * this.row + 10000000 * this.col;
  }
  
  // does this node have the same location as the other node
  public boolean equals(Object other) {
    return other instanceof Node
        && ((Node) other).col == this.col
        && ((Node) other).row == this.row;
  }
  
  // set the state of the current node
  void setState(int state) {
    this.state = state;
  }
  
  // TODO test
  // does this node only have explored neighbors
  boolean onlySeenNeighbors() {
    for (Node n : this.pathTo) {
      if (!n.seen) {
        return false;
      }
    }
    
    return true;
  }
  
  // draws a square representing this node and any appropriate walls
  WorldImage drawNode() {
    Color stateColor;
    
    if (this.seen) {
      stateColor = Color.getHSBColor(0, 100, 76);
    } else {
      stateColor = Color.lightGray;
    }
    
    switch (this.state) {
      case 0:
        stateColor = Color.lightGray;
        break;
      case 1:
        stateColor = Color.getHSBColor(0, 100, 76);
        break;
      case 2:
        stateColor = Color.getHSBColor(0, 78, 29);
        break;
      case 3:
        stateColor = Color.green;
        break;
      case 4:
        stateColor = Color.blue;
        break;
      default:
        stateColor = Color.lightGray;
    }
 
    
    int s = this.size;
    
    WorldImage base = new RectangleImage(s, s, OutlineMode.SOLID, stateColor);
    if (!this.bottomConnected) {
      base = new OverlayImage((new RectangleImage(s, 2,
                              OutlineMode.SOLID, Color.black)).movePinhole(0,
                                  -(((double) s) / 2) + 1), base);
    }
    if (!this.topConnected) {
      base = new OverlayImage((new RectangleImage(s, 2,
                              OutlineMode.SOLID, Color.black)).movePinhole(0,
                                  (((double) s) / 2) - 1), base);
    }
    if (!this.rightConnected) {
      base = new OverlayImage((new RectangleImage(2, s,
                              OutlineMode.SOLID, Color.black)).movePinhole(-(((double) s) / 2) + 1,
          0), base);
    }
    if (!this.leftConnected) {
      base = new OverlayImage((new RectangleImage(2, s,
                              OutlineMode.SOLID, Color.black)).movePinhole((((double) s) / 2) - 1,
          0), base);
    }
    return base.movePinhole(-s * (col + 0.5), -s * (row + 0.5));
  }
  
  // Adds all nodes connected to this by an edge in the given edge
  // list to the pathTo field
  void connect(ArrayList<Edge> edgeList) {
    for (Edge e : edgeList) {
      if (e.contains(this)) {
        this.pathTo.add(e.getOther(this));
      }
    }
    
    for (Node n : this.pathTo) {
      if (n.row > this.row) {
        this.bottomConnected = true;
        this.bottom = n;
      } else if (n.row < this.row) {
        this.topConnected = true;
        this.top = n;
      }
      if (n.col > this.col) {
        this.rightConnected = true;
        this.right = n;
      } else if (n.col < this.col) {
        this.leftConnected = true;
        this.left = n;
      }
    }
  }
  
  // get the node connected to this one in the given direction if it exists
  // otherwise return this node
  // EFFECT: set the new node to explored
  Node getNext(String key) {
    switch (key) {
      case "up":
        if (this.topConnected) {
          this.state = 2;
          this.top.setState(1);
          return this.top;
        }
        break;
      case "down":
        if (this.bottomConnected) {
          this.bottom.setState(1);
          this.state = 2;
          return this.bottom;
        }
        break;
      case "left":
        if (this.leftConnected) {
          this.state = 2;
          this.left.setState(1);
          return this.left;
        }
        break;
      case "right":
        if (this.rightConnected) {
          this.state = 2;
          this.right.setState(1);
          return this.right;
        }
        break;
      default:
        return this;
    }
    return this;
  }
  
  // get the Manhattan distance to another node
  int distanceTo(Node other) {
    return Math.abs(this.row - other.row) + Math.abs(this.col - other.col);
  }
}

//represents an edge in the graph
class Edge {
  Node one;
  Node two;
  int weight;
  
  Edge(Node one, Node two, int weight) {
    this.one = one;
    this.two = two;
    this.weight = weight;
  }
  
  // do the nodes of this edge have the same value in the map
  boolean sameRepresentatives(HashMap<Node,Node> map) {
    return map.get(this.one).equals(map.get(this.two));
  }
  
  // Determines if this edge connects to the given node
  boolean contains(Node n) {
    return this.one.equals(n) || this.two.equals(n);
  }
  
  // Given one side of the edge, returns the other
  Node getOther(Node n) {
    if (this.one.equals(n)) {
      return this.two;
    } else if (this.two.equals(n)) {
      return this.one;
    } else {
      throw new IllegalArgumentException("This node is not in this edge.");
    }
  }
}

//union/find data structure for edge lists
class Kruskal {
  //hashmap<node, representative>
  HashMap<Node,Node> nodes;
  ArrayList<Edge> worklist;
  ArrayList<Edge> treeEdges;
  
  Kruskal(ArrayList<Node> nodes, ArrayList<Edge> edges) {
    this.treeEdges = new ArrayList<Edge>();
    
    this.nodes = new HashMap<Node,Node>();
    for (Node n : nodes) {
      this.nodes.put(n, n);
    }
    
    edges.sort((Edge e1, Edge e2) -> (Integer.compare(e1.weight, e2.weight)));
    
    this.worklist = edges;
    
    if (!this.verifyConnected()) {
      throw new IllegalArgumentException("Must have edges that connect all nodes");
    }
  }
  
  //apply kruskal's algorithm by changing the representative of nodes
  ArrayList<Edge> run() {
    while (this.multipleTrees()) {
      Edge e = this.worklist.get(0);
      if (this.getRep(e.one).equals(this.getRep(e.two))) {
        this.worklist.remove(0);
      } else {
        this.nodes.put(this.getRep(e.one),
                       this.getRep(e.two));
        this.treeEdges.add(this.worklist.remove(0));
      }
    }
    
    return this.treeEdges;
  }
  
  // verify that every node has a corresponding edge
  boolean verifyConnected() {
    // one cell is always connected
    if (this.nodes.values().size() == 1) {
      return true;
    }
    
    boolean inEdges;
    for (Node n : this.nodes.values()) {
      inEdges = false;
      for (Edge e : this.worklist) {
        if (e.contains(n)) {
          inEdges = true;
          break;
        }
      }
      if (!inEdges) {
        return false;
      }
    }
    return true;
  }
  
  // get the representative of this node
  Node getRep(Node n) {
    Node prevNode = n;
    Node returnNode = this.nodes.get(n);
    // terminates because there are no loops in the hashmap (a->b->c->a)
    while (true) {
      if (returnNode.equals(prevNode)) {
        return returnNode;
      } else {
        prevNode = this.nodes.get(prevNode);
        returnNode = this.nodes.get(prevNode);
      }
    }
  }
  
  // does the hashmap have multiple trees?
  // (do any nodes have different representatives?)
  boolean multipleTrees() {
    ArrayList<Node> nodesAsList = new ArrayList<Node>();
    for (Node i : this.nodes.values()) {
      nodesAsList.add(i);
    }
    Node firstNodeRep = this.getRep(nodesAsList.remove(0));
    
    for (Node n : nodesAsList) {
      if (!this.getRep(n).equals(firstNodeRep)) {
        return true;
      }
    }
    
    return false;
  }
}

// Examples
class ExamplesKruskal {
  Node n1;
  Node n1Again;
  Node n2;
  Node n3;
  Node n4;
  Node n5;
  Node n6;
  Node n7;
  Node n8;
  Node n9;
  
  ArrayList<Node> nodes;
  
  HashMap<Node,Node> testMap;
  
  Edge e1;
  Edge e2;
  Edge e3;
  Edge e4;
  Edge e5;
  Edge e6;
  Edge e7;
  Edge e8;
  Edge e9;
  Edge e10;
  Edge e11;
  Edge e12;
  
  ArrayList<Edge> edgesSorted;
  ArrayList<Edge> edgesUnsorted;
  ArrayList<Edge> minimumSpanningTree;
  
  Kruskal testMaze1;
  Kruskal testMaze2;
  
  Bfs breadthFirstTest;
  Dfs depthFirstTest;
  AStar astarTest;
  
  MazeGenerator testWorld;
  
  void initData() {
    this.n1 = new Node(1, 1);
    this.n1Again = new Node(1, 1);
    this.n2 = new Node(1, 2);
    this.n3 = new Node(1, 3);
    this.n4 = new Node(2, 1);
    this.n5 = new Node(2, 2);
    this.n6 = new Node(2, 3);
    this.n7 = new Node(3, 1);
    this.n8 = new Node(3, 2);
    this.n9 = new Node(3, 3);
    
    this.testMap = new HashMap<Node,Node>();
    this.testMap.put(this.n1, this.n1);
    this.testMap.put(this.n2, this.n1);
    this.testMap.put(this.n3, this.n1);
    this.testMap.put(this.n4, this.n4);
    
    this.nodes = new ArrayList<Node>(Arrays.asList(n1, n2, n3, n4, n5, n6, n7, n8, n9));
    
    this.e1 = new Edge(this.n1, this.n2, 0);
    this.e2 = new Edge(this.n2, this.n3, 1);
    this.e3 = new Edge(this.n1, this.n4, 2);
    this.e4 = new Edge(this.n4, this.n5, 3);
    this.e5 = new Edge(this.n2, this.n5, 4);
    this.e6 = new Edge(this.n5, this.n6, 5);
    this.e7 = new Edge(this.n3, this.n6, 6);
    this.e8 = new Edge(this.n4, this.n7, 7);
    this.e9 = new Edge(this.n7, this.n8, 8);
    this.e10 = new Edge(this.n5, this.n8, 9);
    this.e11 = new Edge(this.n8, this.n9, 10);
    this.e12 = new Edge(this.n6, this.n9, 11);
    
    this.edgesSorted =
        new ArrayList<Edge>(Arrays.asList(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12));
    this.edgesUnsorted =
        new ArrayList<Edge>(Arrays.asList(e2, e1, e4, e6, e5, e3, e10, e8, e7, e12, e9, e11));
    
    this.minimumSpanningTree = new ArrayList<Edge>(Arrays.asList(e1, e2, e3, e4, e6, e8, e9, e11));
    
    this.testMaze1 = new Kruskal(this.nodes, this.edgesSorted);
    this.testMaze2 = new Kruskal(this.nodes, this.edgesUnsorted);
    
    this.breadthFirstTest = new Bfs(n1, n9);
    this.depthFirstTest = new Dfs(n1, n9);
    this.astarTest = new AStar(n1, n9);
    
    this.testWorld = new MazeGenerator(3, 3, new Random(0));
  }
  
  // test node equals
  @SuppressWarnings("unlikely-arg-type")
  void testEquals(Tester t) {
    this.initData();
    t.checkExpect(this.n1.equals(this.n2), false);
    t.checkExpect(this.n1.equals(this.n1Again), true);
    t.checkExpect(this.n1.equals(5), false);
  }
  
  // test node hashcode
  void testHashCode(Tester t) {
    this.initData();
    t.checkExpect(this.n1.hashCode(), 10001000);
    t.checkExpect(this.n1.hashCode(), this.n1Again.hashCode());
    t.checkFail(this.n1.hashCode(), this.n2.hashCode());
  }
  
  // test sameRepresentatives
  void testSameRep(Tester t) {
    this.initData();
    t.checkExpect(this.e1.sameRepresentatives(this.testMap), true);
    t.checkExpect(this.e2.sameRepresentatives(this.testMap), true);
    t.checkExpect(this.e3.sameRepresentatives(this.testMap), false);
  }
  
  // test kruskal constructor
  void testKruskal(Tester t) {
    this.initData();
    t.checkExpect(this.testMaze1.worklist, this.testMaze2.worklist);
    
    this.edgesSorted.remove(0);
    this.edgesSorted.remove(0);
    this.edgesSorted.remove(0);
    this.edgesSorted.remove(0);
    this.edgesSorted.remove(0);
    
    // tests verifyConnect method because when the method returns false we throw this error
    t.checkConstructorException(
        new IllegalArgumentException("Must have edges that connect all nodes"),
        "Kruskal", this.nodes, this.edgesSorted);
  }
  
  // test kruskal run method
  void testRun(Tester t) {
    this.initData();
    t.checkExpect(this.testMaze1.run(), this.minimumSpanningTree);
  }
  
  // test kruskal multiple tree method
  void testMultiTree(Tester t) {
    this.initData();
    t.checkExpect(this.testMaze1.multipleTrees(), true);
    this.testMap.put(this.n4, n1);
    this.testMaze1.nodes = this.testMap;
    t.checkExpect(this.testMaze1.multipleTrees(), false);
  }
  
  // test the connect method
  void testConnect(Tester t) {
    this.initData();
    n1.connect(this.minimumSpanningTree);
    t.checkExpect(n1.pathTo, new ArrayList<Node>(Arrays.asList(this.n2, this.n4)));
  }
  
  // test the contains method
  void testContains(Tester t) {
    this.initData();
    t.checkExpect(this.e1.contains(this.n1), true);
    t.checkExpect(this.e12.contains(this.n1), false);
  }
  
  // test the getOther method
  void testGetOther(Tester t) {
    this.initData();
    t.checkExpect(this.e1.getOther(this.n1), this.n2);
    t.checkExpect(this.e1.getOther(this.n2), this.n1);
    t.checkException(new IllegalArgumentException("This node is not in this edge."),
                     this.e5, "getOther", this.n3);
  }
  
  // test the drawNode method
  void testDrawNode(Tester t) {
    WorldImage grayBase = new RectangleImage(50, 50, OutlineMode.SOLID, Color.lightGray);
    this.initData();
    
    
    grayBase = new OverlayImage((new RectangleImage(50, 2,
        OutlineMode.SOLID, Color.black)).movePinhole(0, -24), grayBase);
    grayBase = new OverlayImage((new RectangleImage(50, 2,
        OutlineMode.SOLID, Color.black)).movePinhole(0, 24), grayBase);
    grayBase = new OverlayImage((new RectangleImage(2, 50,
        OutlineMode.SOLID, Color.black)).movePinhole(-24, 0), grayBase);
    grayBase = new OverlayImage((new RectangleImage(2, 50,
        OutlineMode.SOLID, Color.black)).movePinhole(24, 0), grayBase);
    t.checkExpect(this.n1.drawNode(), grayBase.movePinhole(-75, -75));
    
    grayBase = new RectangleImage(50, 50, OutlineMode.SOLID, Color.lightGray);
    grayBase = new OverlayImage((new RectangleImage(50, 2,
        OutlineMode.SOLID, Color.black)).movePinhole(0, 24), grayBase);
    grayBase = new OverlayImage((new RectangleImage(2, 50,
        OutlineMode.SOLID, Color.black)).movePinhole(24, 0), grayBase);
    this.n1.connect(this.minimumSpanningTree);
    t.checkExpect(this.n1.drawNode(), grayBase.movePinhole(-75, -75));
    
    this.n5.state = 1;
    this.n5.topConnected = true;
    this.n5.leftConnected = true;
    this.n5.rightConnected = true;
    this.n5.bottomConnected = true;
    grayBase = new RectangleImage(50, 50, OutlineMode.SOLID, Color.getHSBColor(0, 100, 76));
    t.checkExpect(this.n5.drawNode(), grayBase.movePinhole(-125, -125));
  }
  
  // test get representative method
  void testGetRep(Tester t) {
    this.initData();
    
    t.checkExpect(this.testMaze1.getRep(n1), n1);
    this.testMaze1.nodes.put(n2, n1);
    this.testMaze1.nodes.put(n3, n2);
    t.checkExpect(this.testMaze1.getRep(n2), n1);
    t.checkExpect(this.testMaze1.getRep(n3), n1);
  }
  
  // test make scene
  void testMakeScene(Tester t) {
    this.initData();
    WorldScene scene = new WorldScene(1500, 800);
    for (Node n: this.nodes) {
      n.connect(this.minimumSpanningTree);
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
    scene.placeImageXY(new TextImage(Integer.toString(0),
        30, Color.black), 1415, 740);
    t.checkExpect(new MazeGenerator(3, 3, this.minimumSpanningTree, this.nodes).makeScene(),
        scene);
  }
  
  // test bfs, dfs, and A*
  void testSearchAlgs(Tester t) {
    this.initData();
    for (Node n : nodes) {
      n.connect(this.edgesSorted);
    }
    t.checkExpect(this.breadthFirstTest.next(), this.n1);
    t.checkExpect(this.breadthFirstTest.worklist, new ArrayDeque<Node>(Arrays.asList(n2, n4)));
    t.checkExpect(this.breadthFirstTest.next(), this.n2);
    t.checkExpect(this.breadthFirstTest.worklist, new ArrayDeque<Node>(Arrays.asList(n4, n3, n5)));
    this.initData();
    for (Node n : nodes) {
      n.connect(this.edgesSorted);
    }
    ArrayDeque<Node> temp = new ArrayDeque<Node>();
    temp.push(n2);
    temp.push(n4);
    t.checkExpect(this.depthFirstTest.next(), this.n1);
    t.checkExpect(this.depthFirstTest.worklist, temp);
    temp.pop();
    temp.push(n5);
    temp.push(n7);
    t.checkExpect(this.depthFirstTest.next(), this.n4);
    t.checkExpect(this.depthFirstTest.worklist, temp);
    
    this.initData();
    for (Node n : nodes) {
      n.connect(this.edgesSorted);
    }
    t.checkExpect(this.astarTest.next(), this.n1);
    t.checkExpect(this.astarTest.next(), this.n2);
    t.checkExpect(this.astarTest.next(), this.n4);
  }
  
  // test worklist methods for search algs
  void testWorklist(Tester t) {
    this.initData();
    t.checkExpect(this.breadthFirstTest.removeFromWorklist(), this.n1);
    t.checkExpect(this.depthFirstTest.removeFromWorklist(), this.n1);
    t.checkExpect(this.astarTest.removeFromWorklist(), this.n1);
    this.breadthFirstTest.addToWorklist(n2);
    this.breadthFirstTest.addToWorklist(n3);
    this.breadthFirstTest.addToWorklist(n4);
    t.checkExpect(this.breadthFirstTest.removeFromWorklist(), this.n2);
    t.checkExpect(this.breadthFirstTest.removeFromWorklist(), this.n3);
    this.depthFirstTest.addToWorklist(n2);
    this.depthFirstTest.addToWorklist(n3);
    this.depthFirstTest.addToWorklist(n4);
    t.checkExpect(this.depthFirstTest.removeFromWorklist(), this.n4);
    t.checkExpect(this.depthFirstTest.removeFromWorklist(), this.n3);
    this.initData();
    this.astarTest.cameFromNode.put(n3, n1);
    this.astarTest.addToWorklist(n3);
    t.checkExpect(this.astarTest.removeFromWorklist(), this.n3);
    t.checkExpect(this.astarTest.removeFromWorklist(), this.n1);
  }
  
  // test getNext node method
  void testGetNext(Tester t) {
    this.initData();
    for (Node n : this.nodes) {
      n.connect(this.edgesSorted);
    }
    
    t.checkExpect(this.n1.getNext("right"), this.n2);
    t.checkExpect(this.n1.getNext("left"), this.n1);
    t.checkExpect(this.n1.getNext("top"), this.n1);
    t.checkExpect(this.n1.getNext("bottom"), this.n1);
  }
  
  // test the distance to method
  void testDistanceTo(Tester t) {
    this.initData();
    t.checkExpect(this.n1.distanceTo(n1), 0);
    t.checkExpect(this.n1.distanceTo(n9), 4);
    t.checkExpect(this.n9.distanceTo(n1), 4);
    t.checkExpect(this.n1.distanceTo(n2), 1);
  }
  
  // test maze reset
  void testReset(Tester t) {
    initData();
    MazeGenerator temp = new MazeGenerator(this.testWorld);
    this.testWorld.onKeyEvent("a");
    t.checkFail(this.testWorld, temp);
    this.testWorld.reset();
    t.checkExpect(this.testWorld, temp);
    this.testWorld.reset();
    t.checkExpect(this.testWorld, temp);
  }
  
  // test new maze
  void testNewMaze(Tester t) {
    initData();
    this.testWorld.newMaze();
    MazeGenerator temp = new MazeGenerator(this.testWorld);
    initData();
    // test that the new random maze is deterministic
    this.testWorld.newMaze();
    t.checkExpect(this.testWorld, temp);
  }
  
  // test on tick
  void testOnTick(Tester t) {
    initData();
    MazeGenerator temp = new MazeGenerator(this.testWorld);
    this.testWorld.onTick();
    t.checkExpect(this.testWorld, temp);
    
    this.testWorld.onKeyEvent("d");
    this.testWorld.onTick();
    t.checkExpect(this.testWorld.numberOfMoves, 1);
    Node tempNode = this.testWorld.nodes.get(0);
    t.checkExpect(this.testWorld.currentPos, tempNode);
    this.testWorld.onTick();
    t.checkExpect(this.testWorld.numberOfMoves, 2);
    tempNode = this.testWorld.nodes.get(1);
    t.checkExpect(this.testWorld.currentPos, tempNode);
  }
  
  // test key event
  void testOnKeyEvent(Tester t) {
    initData();
    MazeGenerator temp = new MazeGenerator(this.testWorld);
    this.testWorld.onKeyEvent("1");
    t.checkExpect(this.testWorld, temp);
    initData();
    temp = new MazeGenerator(this.testWorld);
    this.testWorld.onKeyEvent("a");
    temp.lockKeyInputs = true;
    temp.autoSolve = true;
    temp.algType = new AStar(temp.start, temp.target);
    t.checkExpect(this.testWorld, temp);
    initData();
    temp = new MazeGenerator(this.testWorld);
    this.testWorld.onKeyEvent("d");
    temp.lockKeyInputs = true;
    temp.autoSolve = true;
    temp.algType = new Dfs(temp.start, temp.target);
    t.checkExpect(this.testWorld, temp);
    initData();
    temp = new MazeGenerator(this.testWorld);
    this.testWorld.onKeyEvent("b");
    temp.lockKeyInputs = true;
    temp.autoSolve = true;
    temp.algType = new Bfs(temp.start, temp.target);
    t.checkExpect(this.testWorld, temp);
    initData();
    temp = new MazeGenerator(this.testWorld);
    this.testWorld.onKeyEvent("n");
    temp.newMaze();
    initData();
    temp = new MazeGenerator(this.testWorld);
    this.testWorld.onKeyEvent("r");
    temp.reset();
    t.checkExpect(this.testWorld, temp);
    this.initData();
    this.testWorld.onKeyEvent("down");
    t.checkExpect(this.testWorld.autoSolve, false);
    t.checkExpect(this.testWorld.lockKeyInputs, true);
    initData();
    this.testWorld.onKeyEvent("up");
    t.checkExpect(this.testWorld.autoSolve, false);
    t.checkExpect(this.testWorld.lockKeyInputs, true);
    initData();
    this.testWorld.onKeyEvent("left");
    t.checkExpect(this.testWorld.autoSolve, false);
    t.checkExpect(this.testWorld.lockKeyInputs, true);
    initData();
    this.testWorld.onKeyEvent("right");
    t.checkExpect(this.testWorld.autoSolve, false);
    t.checkExpect(this.testWorld.lockKeyInputs, true);
  }
  
  // play the full maze game
  void testPlayGame(Tester t) {
    initData();
    
    new MazeGenerator(100, 60, new Random()).bigBang(1500, 800, 0.01);
  }
}