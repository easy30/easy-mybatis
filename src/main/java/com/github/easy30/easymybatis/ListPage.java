package com.github.easy30.easymybatis;

/*import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;*/

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * query page result
 * @param <E>
 */
public class ListPage<E> extends Page<E>  implements List<E> {

    private static final long serialVersionUID = 7395507780937350288L;

    public ListPage() {
        super();
    }

    public ListPage(int pageIndex,int pageSize) {
        super(pageIndex,pageSize);
    }
    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return data.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return data.iterator();
    }

    @Override
    public Object[] toArray() {
        return data.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return data.toArray(a);
    }

    @Override
    public boolean add(E e) {
        return data.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return data.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return data.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return data.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return data.addAll(index,c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return data.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return data.retainAll(c);
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public E get(int index) {
        return data.get(index);
    }

    @Override
    public E set(int index, E element) {
        return data.set(index,element);
    }

    @Override
    public void add(int index, E element) {
        data.add(index,element);
    }

    @Override
    public E remove(int index) {
        return data.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return data.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return data.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return data.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return data.listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return data.subList(fromIndex,toIndex);
    }

    /*
       预留对ListPage序列化处理
       public static class ListSerializer extends JsonSerializer<ListPage> {
        @Override
        public void serialize(ListPage listPage, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            // 在这里处理ListPage对象的序列化，只处理属性而不处理元素
            jsonGenerator.writeStartObject();
            // 处理属性
            jsonGenerator.writeObjectField("pageSize", listPage.getPageSize());
            jsonGenerator.writeObjectField("data", listPage.getData() );
            // ...
            jsonGenerator.writeEndObject();
        }
    }*/
}