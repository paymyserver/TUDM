package com.android.cloudcovermusic.model;

import java.io.Serializable;

/**
 * Represents a node of the {@link CCMLinkedList} class. The Node<T> is
 * also a container, and can be thought of as instrumentation to determine the
 * location of the type T in the CircularLinkedList<T>.
 * 
 * @author Mahesh Chauhan
 */
public class Node<T> implements Serializable {

    public T data;
    public Node<T> next;

    /**
     * Default ctor.
     */
    public Node() {
        super();
    }

    public Node(T data, Node<T> next) {
        this.next = next;
        this.data = data;
    }
    /**
     * Convenience creator to create a Node<T> with an instance of T.
     * 
     * @param data
     *            an instance of T.
     */
    public Node(T data) {
        this();
        setData(data);
    }

    /**
     * Return the next Node<T>.
     * 
     * @return the next node to the current Node<T>
     */
    public Node<T> getNextNode() {
        return this.next;
    }

    public void setNextNode(Node<T> next) {
        this.next = next;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
