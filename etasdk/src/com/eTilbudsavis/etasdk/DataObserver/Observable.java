package com.eTilbudsavis.etasdk.DataObserver;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class Observable<T> {

	protected final Set<T> mObservers = newSetFromMap(new WeakHashMap<T, Boolean>());
	
    /**
     * Adds an observer to the list. The observer cannot be null and it must not already
     * be registered.
     * @param observer the observer to register
     * @throws IllegalArgumentException the observer is null
     * @throws IllegalStateException the observer is already registered
     */
    public void registerObserver(T observer) {
        if (observer == null) {
            throw new IllegalArgumentException("The observer is null.");
        }
        synchronized(mObservers) {
            if (mObservers.contains(observer)) {
                throw new IllegalStateException("Observer " + observer + " is already registered.");
            }
            mObservers.add(observer);
        }
        
    }

    /**
     * Removes a previously registered observer. The observer must not be null and it
     * must already have been registered.
     * @param observer the observer to unregister
     * @throws IllegalArgumentException the observer is  null
     * @throws IllegalStateException the observer is not yet registered
     */
    public void unregisterObserver(T observer) {
        if (observer == null) {
            throw new IllegalArgumentException("The observer is null.");
        }
        synchronized(mObservers) {
            if (!mObservers.contains(observer)) {
                throw new IllegalStateException("Observer " + observer + " was not registered.");
            }
            mObservers.remove(observer);
        }
    }
    
    /**
     * Remove all registered observers.
     */
    public void unregisterAll() {
        synchronized(mObservers) {
            mObservers.clear();
        }
    }
    
	public static <E> Set<E> newSetFromMap(Map<E, Boolean> map) {
		return new SetFromMap<E>(map);
	}

	private static class SetFromMap<E> extends AbstractSet<E> implements Set<E>, Serializable {
		private final Map<E, Boolean> m;  // The backing map
		private transient Set<E> s;       // Its keySet

		SetFromMap(Map<E, Boolean> map) {
			if (!map.isEmpty())
				throw new IllegalArgumentException("Map is non-empty");
			m = map;
			s = map.keySet();
		}
		
		public void clear()               {        m.clear(); }
		public int size()                 { return m.size(); }
		public boolean isEmpty()          { return m.isEmpty(); }
		public boolean contains(Object o) { return m.containsKey(o); }
		public boolean remove(Object o)   { return m.remove(o) != null; }
		public boolean add(E e) { return m.put(e, Boolean.TRUE) == null; }
		public Iterator<E> iterator()     { return s.iterator(); }
		public Object[] toArray()         { return s.toArray(); }
		public <T> T[] toArray(T[] a)     { return s.toArray(a); }
		public String toString()          { return s.toString(); }
		public int hashCode()             { return s.hashCode(); }
		public boolean equals(Object o)   { return o == this || s.equals(o); }
		public boolean containsAll(Collection<?> c) {return s.containsAll(c);}
		public boolean removeAll(Collection<?> c)   {return s.removeAll(c);}
		public boolean retainAll(Collection<?> c)   {return s.retainAll(c);}
		// addAll is the only inherited implementation

		private static final long serialVersionUID = 2454657854757543876L;

		private void readObject(java.io.ObjectInputStream stream)
				throws IOException, ClassNotFoundException
		{
			stream.defaultReadObject();
			s = m.keySet();
		}
	}
}
