package nl.han.ica.datastructures;

public class HANLinkedList<T> implements IHANLinkedList<T> {
    private Node<T> header;
    private int size;

    public HANLinkedList() {
        header = new Node<>(null);
        size = 0;
    }

    @Override
    public void addFirst(T value) {
        Node<T> newNode = new Node<>(value);
        newNode.next = header.next;
        header.next = newNode;
        size++;
    }

    @Override
    public void clear() {
        if (size > 0) {
            header.next = null;
            size = 0;
        }
    }

    @Override
    public void insert(int index, T value) {
        if (index >= 0 && index <= size) {
            Node<T> previous = header;
            for (int i = 0; i < index; i++) {
                previous = previous.next;
            }
            Node<T> newNode = new Node<>(value);
            newNode.next = previous.next;
            previous.next = newNode;
            size++;
        }
    }

    @Override
    public void delete(int pos) {
        if (pos >= 0 && pos < size) {
            Node<T> previous = header;
            for (int i = 0; i < pos; i++) {
                previous = previous.next;
            }
            previous.next = previous.next.next;
            size--;
        }
    }

    @Override
    public T get(int pos) {
        if (pos < 0 || pos >= size) {
            return null;
        }
        Node<T> current = header.next;
        for (int i = 0; i < pos; i++) {
            current = current.next;
        }
        return current.value;
    }

    @Override
    public void removeFirst() {
        if (header.next != null) {
            header.next = header.next.next;
            size--;
        }
    }

    @Override
    public T getFirst() {
        if (header.next != null) {
            return header.next.value;
        }
        return null;
    }

    @Override
    public int getSize() {
        return size;
    }
}
