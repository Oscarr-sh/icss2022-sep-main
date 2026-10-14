package nl.han.ica.datastructures;

import java.util.ArrayList;
import java.util.List;

public class HANStack <T> implements IHANStack<T> {
    private List<T> stack = new ArrayList<T>();

    @Override
    public void push(T t) {
        stack.add(t);
    }

    @Override
    public T pop() {
        return stack.remove(stack.size() - 1);
    }

    @Override
    public T peek() {
        return stack.get(stack.size() - 1);
    }
}
