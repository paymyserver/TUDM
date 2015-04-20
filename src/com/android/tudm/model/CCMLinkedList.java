package com.android.tudm.model;


/**
 * Representation of a Generic Linked list of type T. This linked list holds the
 * node of type {@link Node} .
 * 
 * @author Mahesh Chauhan
 * 
 * @param <T>
 */
public class CCMLinkedList<T> {
    public Node<T> mHead = null;
    private int mSize = 0;

    public CCMLinkedList() {
        // this is an empty list, so the reference to the head node
        // is set to a new node with no data
        mHead = new Node<T>(null);
        mSize = 0;
    }
    
    public boolean isEmpty() {
        return (mSize == 0);
    }

    /**
     * Appends the specified element to the end of this list.
     * 
     * @param data
     */
    public void add(T data)
    {
        Node<T> temp = new Node<T>(data);
        Node<T> current = mHead;
        // starting at the head node, move to the end of the list
        while (current.getNextNode() != null) {
            current = current.getNextNode();
        }
        // the last node's "next" reference set to our new node
        current.setNextNode(temp);
        mSize++;// increment the number of elements variable
    }

    /**
     * Inserts the specified element at the specified position in this list
     * @param data
     * @param index
     */
    public void add(T data, int index) {
        Node<T> temp = new Node<T>(data);
        Node<T> current = mHead;
        // Move to the requested index or the last element in the list,
        // whichever comes first
        for (int i = 1; i < index && current.getNextNode() != null; i++) {
            current = current.getNextNode();
        }
        // set the new node's next-node reference to this node's next-node
        // reference
        temp.setNextNode(current.getNextNode());
        // now set this node's next-node reference to the new node
        current.setNextNode(temp);
        mSize++;// increment the number of elements variable
    }

    /**
     * Returns the element at the specified position in this list.
     * @param index
     * @return
     */
    public T get(int index) {
        // index must be 1 or higher
        if (index <= 0)
            return null;

        Node<T> current = mHead.getNextNode();
        for (int i = 1; i < index; i++) {
            if (current.getNextNode() == null)
                return null;
            current = current.getNextNode();
        }
        return current.getData();
    }

    /**
     * Removes the element at the specified position in this list.
     * @param index
     * @return
     */
    public boolean remove(int index) {
        // if the index is out of range, exit
        if (index < 1 || index > size())
            return false;

        Node<T> current = mHead;
        for (int i = 1; i < index; i++) {
            if (current.getNextNode() == null)
                return false;

            current = current.getNextNode();
        }
        current.setNextNode(current.getNextNode().getNextNode());
        mSize--; // decrement the number of elements variable
        return true;
    }

    /**
     * Returns the number of elements in this list.
     * @return
     */
    public int size() {
        return mSize;
    }

    public String toString() {
        Node<T> current = mHead.getNextNode();
        String output = "";
        while (current != null) {
            output += "[" + current.getData().toString() + "]";
            current = current.getNextNode();
        }
        return output;
    }
}
