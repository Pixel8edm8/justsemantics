package com.theethancorporation.util;

import java.util.ArrayList;

public class EthansHashSet<T> // implements Iterable
{
    /**
     * A custom implementation of a HashSet for my SemanticSimilarity project. 
     * @param size : Quantity of buckets for HashSet
     */
    private int quantityOfBuckets;
    private int numberOfElements;
    private ArrayList<ArrayList<T>> buckets = new ArrayList<>();
    private final double loadFactor;
    public int hashElement(T elementToHash) {
        return Math.abs(elementToHash.hashCode() % quantityOfBuckets);
    }

    /**
     * Checks the load factor. If it's too high, then resize the HashSet, and go again.
     */
    private void checkLoadFactor() {
        if ((numberOfElements / quantityOfBuckets) >= loadFactor) {
            resize();
        }
    }
    public EthansHashSet(int size, final double loadFactor) {
        this.quantityOfBuckets = size;
        this.loadFactor = loadFactor;
        for (int i = 0; i < size; i++) {
            ArrayList<T> newList = new ArrayList<>();
            buckets.add(i, newList);
        }
    }

    /**
     * Returns a TRUE if such an element is contained in HashSet, and FALSE otherwise.
     * @param thingToSearch
     * @return
     */
    public boolean contains(T thingToSearch) {
        for(T currentElement : buckets.get(hashElement(thingToSearch))) {
            if (thingToSearch.equals(currentElement)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add a member to your HashSet
     * @param thingToAdd
     */
    public void add(T thingToAdd) {
        if (!contains(thingToAdd)) {
            buckets.get(hashElement(thingToAdd)).add(thingToAdd);
            numberOfElements++;
            checkLoadFactor();   
        }
    }

    /**
     * A void function that prints every element in the HashSet.
     */
    public void printAll() {
        for (ArrayList<T> arrayList : buckets) {
            System.out.println(arrayList.toString());
        }
    }
    /**
     * A void function that removes all elements from a HashSet.
     */
    public void clear() {
        for (ArrayList<T> arrayList : buckets) {
            if (!arrayList.isEmpty()) {
                arrayList.removeAll(arrayList);
            }
        }
    }
    /**
     * Removes an element, if it is in the HashSet.
     * @param thingToRemove
     */
    public void remove(T thingToRemove) {
        buckets.get(hashElement(thingToRemove)).remove(thingToRemove);
        numberOfElements--;
        checkLoadFactor();
    }

    /**
     * Resizes and rehashes everything in the HashSet when the load factor is hit.
     */
    private void resize() {
        // Create a new ArrayList<ArrayList<T>> of the doubled size.
        quantityOfBuckets *= 2;
        ArrayList<ArrayList<T>> newArrayList = new ArrayList<>();
        for (int i = 0; i < quantityOfBuckets; i++) {
            ArrayList<T> subArrayList = new ArrayList<>();
            newArrayList.add(subArrayList);
        }
        // FOR each element in each bucket of old ArrayList<ArrayList<T>>:
        for (ArrayList<T> subArray : buckets) {
            for (T object : subArray) {
                newArrayList.get(hashElement(object)).add(object);
            }
        }
        buckets = newArrayList;
    }

    public ArrayList<ArrayList<T>> getBuckets() {
        return buckets;
    }

    // TODO: Do this later, to replace getBuckets. This is a lot cleaner and less prone to screw-ups than returning the raw buckets.
    // @Override
    // public Iterator<T> iterator() {
    //     return new CustomIterator();
    // }

    // private class CustomIterator implements Iterator<T> {
    //     private int bucketIndex = 0;
    //     private int itemIndex = 0;
        
    //     @Override
    //     public boolean hasNext() {
    //         return bucketIndex < buckets.size() && itemIndex < buckets.get(bucketIndex).size();
    //     }

    //     @Override
    //     public T next() {
    //         throw new UnsupportedOperationException("Not supported yet.");
    //     }
        
    // }
}
