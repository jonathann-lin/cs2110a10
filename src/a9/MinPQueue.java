package a9;

import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * A min priority queue of distinct elements of type `KeyType` associated with (extrinsic) double
 * priorities. Supports updating the priorities of elements currently in the queue, and guarantees
 * O(log N) performance for all modifying operations, where N is the queue size.
 */
public class MinPQueue<KeyType> {

    /**
     * Pairs an element `key` with its associated priority `priority`.
     */
    private record Entry<KeyType>(KeyType key, double priority) {

    }

    /**
     * ArrayList representing a binary min-heap of element-priority pairs.  Satisfies
     * `heap.get(i).priority() >= heap.get((i-1)/2).priority()` for all `i` in `[1..heap.size())`.
     */
    private final ArrayList<Entry<KeyType>> heap;

    /**
     * Associates each element in the queue with its index in `heap`.  Satisfies
     * `heap.get(index.get(e)).key().equals(e)` if `e` is an element in the queue. Only maps
     * elements that are in the queue (`index.size() == heap.size()`).
     */
    private final PacMap<KeyType, Integer> index;


    /**
     * Create an empty queue.
     */
    public MinPQueue() {
        index = new ProbingPacMap<>();
        heap = new ArrayList<>();
    }

    /**
     * Return whether this queue contains no elements.
     */
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    /**
     * Return the number of elements contained in this queue.
     */
    public int size() {
        return heap.size();
    }

    /**
     * Return an element associated with the smallest priority in this queue.  This is the same
     * element that would be removed by a call to `remove()` (assuming no mutations in between).
     * Throws a `NoSuchElementException` if this queue is empty.
     */
    public KeyType peek() {
        // Propagate exception from `List::getFirst()` if empty.
        return heap.getFirst().key();
    }

    /**
     * Return the minimum priority associated with an element in this queue.  Throws a
     * `NoSuchElementException` if this queue is empty.
     */
    public double minPriority() {
        return heap.getFirst().priority();
    }

    /**
     * Swap the `Entry`s at indices `i` and `j` in `heap`, updating `index` accordingly.  Requires
     * `0 <= i,j < heap.size()`.
     */
    private void swap(int i, int j) {
        assert i >= 0 && i < heap.size();
        assert j >= 0 && j < heap.size();

        Entry<KeyType> temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);

        index.put(heap.get(j).key, j);
        index.put(heap.get(i).key, i);


    }

    /**
     * Repeatedly swaps the element at index 'i' of 'heap' with its parent until
     * heap.get(i).priority() >= heap.get((i-1)/2).priority() or i == 0. Correctly updates
     * the index of the element in 'index'.
     * Requires that i>=0 and i<heap.size().
     */
    private void bubbleUp(int i) {
        assert i >= 0 && i < heap.size();
        if (i == 0) {
            return;
        }

        int p = parent(i);

        if (heap.get(i).priority < heap.get(p).priority) {
            swap(i, p);
            bubbleUp(p);
        }
    }

    /**
     * Returns the index of the parent of the element at index 'i' in 'heap'. The parent of the
     * element at index 0 is itself. Requires that i >= 0 and i < heap.size();
     */
    private int parent(int i) {
        assert i >= 0 && i < heap.size();
        return (i - 1) / 2;
    }

    /**
     * Repeatedly swaps the element at index 'i' of 'heap' with its child of lower priority until
     * the element at index 'i' has a lower priority than both of its children. Correctly updates
     * the index of the element in 'index'.
     *
     * Requires that i >= 0 and i < heap.size();
     */
    private void bubbleDown(int i) {
        assert i >= 0 && i < heap.size();
        int indexOfLowestChild = indexOfChildOfLowestPriority(i);

        if(indexOfLowestChild == i) {
            return; //leaf node, no more bubble down
        }

        if(heap.get(indexOfLowestChild).priority < heap.get(i).priority){
            swap(i, indexOfLowestChild);
            bubbleDown(indexOfLowestChild);
        }
    }

    /**
     * Finds the child with lower priority of the element at index 'i' in 'heap'. If the element at
     * 'i' has no child, returns i. If priorities are equal, return left child by default.
     * Requires that i >= 0 and i < heap.size();
     *
     */
    private int indexOfChildOfLowestPriority(int i) {
        assert i >= 0 && i < heap.size();
        int leftChild = i * 2 + 1;
        if (leftChild >= heap.size()){
            return i; //'i' is leaf node
        }
        int rightChild = i * 2 + 2;

        if(rightChild >= heap.size()){
            return leftChild; //only right child
        }

        if(heap.get(leftChild).priority <= heap.get(rightChild).priority){
            return leftChild;
        }
        else{
            return rightChild;
        }

    }


    /**
     * Add element `key` to this queue, associated with priority `priority`.  Requires `key` is not
     * contained in this queue.
     */
    private void add(KeyType key, double priority) {
        assert !index.containsKey(key);
        int i = heap.size(); //index of next element to be added
        heap.add(new Entry<>(key, priority));
        index.put(key, i);
        bubbleUp(i); //also updates index
    }

    /**
     * Change the priority associated with element `key` to `priority`.  Requires that `key` is
     * contained in this queue.
     */
    private void update(KeyType key, double priority) {
        assert index.containsKey(key);
        int i = index.get(key);
        double currentPriority = heap.get(i).priority;
        if (currentPriority == priority) return;
        heap.set(i, new Entry<>(key, priority));
        if (priority>currentPriority){
            bubbleDown(i);
        }
        else{
            bubbleUp(i);
        }
    }

    /**
     * If `key` is already contained in this queue, change its associated priority to `priority`.
     * Otherwise, add it to this queue with that priority.
     */
    public void addOrUpdate(KeyType key, double priority) {
        if (!index.containsKey(key)) {
            add(key, priority);
        } else {
            update(key, priority);
        }
    }

    /**
     * Remove and return the element associated with the smallest priority in this queue.  If
     * multiple elements are tied for the smallest priority, an arbitrary one will be removed.
     * Throws NoSuchElementException if this queue is empty.
     */
    public KeyType remove() {
        if(heap.isEmpty()){
            throw new NoSuchElementException();
        }

        KeyType key = heap.getFirst().key;

        swap(0, heap.size()-1);
        heap.removeLast();
        index.remove(key);

        //check for case that heap was originally only one element that is now removed
        if(!heap.isEmpty()){
            bubbleDown(0);
        }

        return key;
    }

}
