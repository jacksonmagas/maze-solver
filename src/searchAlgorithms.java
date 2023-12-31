import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.PriorityQueue;

// represents an abstract sorting algorithm which can step through the sort.
abstract class SearchAlg {
  ArrayDeque<Node> worklist;
  HashMap<Node, Node> cameFromNode;
  Node from;
  Node to;
  boolean solved = false;
  
  SearchAlg(Node from, Node to) {
    this.to = to;
    this.from = from;
    this.worklist = new ArrayDeque<Node>();
    this.worklist.add(from);
    this.cameFromNode = new HashMap<Node, Node>();
    this.cameFromNode.put(from, from);
  }
  
  // add the node to the worklist
  abstract void addToWorklist(Node n);
  
  //remove the node from the worklist
  abstract Node removeFromWorklist();
  
  // is the worklist empty
  boolean emptyWorklist() {
    return this.worklist.isEmpty();
  }
 
  // determines whether the next node is the solution
  // EFFECT: progress the search by one step
  Node next() {
    if (this.emptyWorklist()) {
      return this.to;
    }
    Node next = this.removeFromWorklist();
    
    if (next.equals(this.to)) {
      this.trace(next);
      return next;
    } else if (next.seen) {
      return next;
    } else {
      for (Node n : next.pathTo) {
        if (!n.seen) {
          this.cameFromNode.put(n, next);
          this.addToWorklist(n);
        }
      }
      next.seen = true;
      return next;
    }
  }
  
  // EFFECT: change the state of all nodes in the path to solved
  void trace(Node last) {
    this.solved = true;
    last.setState(1);
    while (!last.equals(this.cameFromNode.get(last))) {
      last = this.cameFromNode.get(last);
      last.setState(1);
    }
  }
}

// a breadth first search algorithm
class Bfs extends SearchAlg {
  
  Bfs(Node from, Node to) {
    super(from, to);
  }
  
  @Override
  // add to the end of the deque
  void addToWorklist(Node n) {
    this.worklist.addLast(n);
  }

  @Override
  // remove the first element in the deque
  Node removeFromWorklist() {
    return this.worklist.remove();
  }
}

//a depth first search algorithm
class Dfs extends SearchAlg {
  Dfs(Node from, Node to) {
    super(from, to);
  }

  @Override
  // add the element to the top of the stack
  void addToWorklist(Node n) {
    this.worklist.push(n);
  }

  @Override
  // remove the element from the top of the stack
  Node removeFromWorklist() {
    return this.worklist.pop();
  }
}

//A* search algorithm using Manhattan distance from the target as the heuristic
class AStar extends SearchAlg {
  // shadow worklist
  PriorityQueue<Node> worklist;
  HashMap<Node, Integer> distances;
  
  AStar(Node from, Node to) {
    super(from, to);
    this.distances = new HashMap<Node, Integer>();
    this.distances.put(from, 0);
    this.worklist = new PriorityQueue<Node>((Node n1, Node n2) -> 
        (Integer.compare(this.distances.get(n1) + n1.distanceTo(this.to),
        this.distances.get(n2) + n2.distanceTo(this.to))));
    this.worklist.add(from);
  }

  // EFFECT: add the given node to the worklist
  //         and add its new distance if it is smaller
  @Override
  void addToWorklist(Node n) {
    int dist = this.distances.get(this.cameFromNode.get(n)) + 1;
    if (this.distances.get(n) == null
        || dist < this.distances.get(n)) {
      this.distances.put(n, dist);
    }
    this.worklist.add(n);
  }

  @Override
  // return the first
  // EFFECT: remove the first node from the worklist
  Node removeFromWorklist() {
    return this.worklist.remove();
  }
  
  @Override
  // is the worklist empty?
  boolean emptyWorklist() {
    return this.worklist.isEmpty();
  }
}
