package com.shopgun.android.sdk.utils;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class FIFOMaxSizeCollections {

    /**
     * Removal of elements will be determined of the underlying iterators
     */
    public static <T> Collection getFIFOMaxSizeCollection(Collection<T> collection, int maxSize) {
        return new FIFOMaxSizeCollection<>(collection, maxSize);
    }

    /**
     * Removal of elements will be determined of the underlying iterators
     */
    public static <T> Collection getFIFOMaxSizeList(List<T> list, int maxSize) {
        return new FIFOMaxSizeList<>(list, maxSize);
    }

    static class FIFOMaxSizeCollection<E> implements Collection<E> {

        int mMaxSize = 0;
        Collection<E> mCollection;

        public FIFOMaxSizeCollection(Collection<E> collection, int maxSize) {
            mCollection = collection;
            mMaxSize = maxSize;
        }

        public int getMaxSize() {
            return mMaxSize;
        }

        public void setMaxSize(int maxSize) {
            mMaxSize = maxSize;
            trim();
        }

        @Override
        public boolean add(E object) {
            try {
                return mCollection.add(object);
            } finally {
                trim();
            }
        }

        @Override
        public boolean addAll(Collection<? extends E> collection) {
            try {
                return mCollection.addAll(collection);
            } finally {
                trim();
            }
        }

        protected void trim() {
            Iterator<E> it = mCollection.iterator();
            while (it.hasNext() && size() > mMaxSize) {
                it.next();
                it.remove();
            }
        }

        @Override
        public void clear() {
            mCollection.clear();
        }

        @Override
        public boolean contains(Object object) {
            return mCollection.contains(object);
        }

        @Override
        public boolean containsAll(Collection<?> collection) {
            return mCollection.containsAll(collection);
        }

        @Override
        public boolean isEmpty() {
            return mCollection.isEmpty();
        }

        @NonNull
        @Override
        public Iterator<E> iterator() {
            return mCollection.iterator();
        }

        @Override
        public boolean remove(Object object) {
            return mCollection.remove(object);
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            return mCollection.removeAll(collection);
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            return mCollection.retainAll(collection);
        }

        @Override
        public int size() {
            return mCollection.size();
        }

        @NonNull
        @Override
        public Object[] toArray() {
            return mCollection.toArray();
        }

        @NonNull
        @Override
        public <T1> T1[] toArray(T1[] array) {
            return mCollection.toArray(array);
        }
    }

    static class FIFOMaxSizeList<E> extends FIFOMaxSizeCollection<E> implements List<E> {

        List<E> mList;

        public FIFOMaxSizeList(List<E> list, int maxSize) {
            super(list, maxSize);
            mList = list;
        }

        @Override
        public void add(int location, E object) {
            try {
                mList.add(location, object);
            } finally {
                trim();
            }
        }

        @Override
        public boolean addAll(int location, Collection<? extends E> collection) {
            try {
                return mList.addAll(location, collection);
            } finally {
                trim();
            }
        }

        @Override
        public E get(int location) {
            return mList.get(location);
        }

        @Override
        public int indexOf(Object object) {
            return mList.indexOf(object);
        }

        @Override
        public int lastIndexOf(Object object) {
            return mList.lastIndexOf(object);
        }

        @Override
        public ListIterator<E> listIterator() {
            return mList.listIterator();
        }

        @NonNull
        @Override
        public ListIterator<E> listIterator(int location) {
            return mList.listIterator(location);
        }

        @Override
        public E remove(int location) {
            return mList.remove(location);
        }

        @Override
        public E set(int location, E object) {
            return mList.set(location, object);
        }

        @NonNull
        @Override
        public List<E> subList(int start, int end) {
            return mList.subList(start, end);
        }
    }

//    public static class ReverseIterator

}
