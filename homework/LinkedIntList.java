package homework;

/**
 * A singly linked list implementation that stores integers.
 * Provides comprehensive list manipulation operations including adding, removing,
 * sorting, merging, and various utility methods.
 * 
 * @author Timothy Lopez
 * @version 2.0
 * // I added the addition of last and size tracking to various methods.
 */

// Imports
import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.IntUnaryOperator;
import java.util.function.IntPredicate;
import java.util.function.IntBinaryOperator;

public class LinkedIntList implements Iterable<Integer> {

    private ListNode last;
    private ListNode front;
    private int size;
    
    /** Indicates whether the list contains a cycle */
    public boolean isLooping;

    /**
     * Constructs an empty LinkedIntList.
     */
    public LinkedIntList() {
        front = null;
    }

    /**
     * Constructs a LinkedIntList with the specified front node.
     * 
     * @param frontNode the node to use as the front of the list
     */
    public LinkedIntList(ListNode frontNode) {
        front = frontNode;
    }

    /**
     * Constructs a LinkedIntList from an ArrayList of integers.
     * 
     * @param list the ArrayList to convert to a linked list
     */
    public LinkedIntList(ArrayList<Integer> list) {
        if (list == null || list.isEmpty()) {
            front = null;
            return;
        }

        front = new ListNode(list.get(0));
        ListNode current = front;
        for (int i = 1; i < list.size(); i++) {
            current.next = new ListNode(list.get(i));
            current = current.next;
        }
        last = current;
        size = list.size();
    }

    /**
     * Constructs a LinkedIntList from an array of integers.
     * 
     * @param list the array to convert to a linked list
     */
    public LinkedIntList(int[] list) {
        if (list == null || list.length == 0) {
            front = null;
            return;
        }

        front = new ListNode(list[0]);
        ListNode current = front;

        for (int i = 1; i < list.length; i++) {
            current.next = new ListNode(list[i]);
            current = current.next;        
        }
        last = current;
        size = list.length;
    }

    /**
     * Returns a hash code value for this list.
     * 
     * @return the hash code value
     * @throws IllegalStateException if the list is looping
     */
    @Override
    public int hashCode() {
        if (isLooping) {
            throw new IllegalStateException("Cannot hash a looping list.");
        }

        int hash = 1;
        ListNode current = front;
        while (current != null) {
            hash = 31 * hash + current.data; // 31 is a common multiplier in Java
            current = current.next;
        }
        return hash;
    }

    /**
     * Returns a new LinkedIntList where each element is the result of applying
     * the given function to the corresponding element in this list.
     * 
     * @param f the function to apply to each element
     * @return a new LinkedIntList with transformed elements
     * @throws IllegalStateException if the list is looping
     */
    public LinkedIntList map(IntUnaryOperator f) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (isEmpty()) {
            return new LinkedIntList();
        }

        LinkedIntList result = new LinkedIntList(new ListNode(f.applyAsInt(front.data)));
        result.size = 1;
        ListNode current = front.next;
        ListNode resultCurrent = result.front;
        while (current != null) {
            resultCurrent.next = new ListNode(f.applyAsInt(current.data));
            resultCurrent = resultCurrent.next;
            current = current.next;
            result.size++;
        }
        result.last = resultCurrent;
        return result;
    }

    /**
     * Returns a new LinkedIntList containing only elements that match the given predicate.
     * 
     * @param predicate the predicate to test each element
     * @return a new LinkedIntList with filtered elements
     * @throws IllegalStateException if the list is looping
     */
    public LinkedIntList filter(IntPredicate predicate) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        LinkedIntList result = new LinkedIntList();
        ListNode current = front;
        ListNode resultCurrent = null;

        while (current != null) {
            if (predicate.test(current.data)) {
                if (result.front == null) {
                    result.front = new ListNode(current.data);
                    resultCurrent = result.front;
                } else {
                    resultCurrent.next = new ListNode(current.data);
                    resultCurrent = resultCurrent.next;
                }
                result.size++;
            }
            current = current.next;
        }
        result.last = resultCurrent;
        return result;
    }

    /**
     * Returns the result of applying the binary operator cumulatively to the elements
     * of the list, starting with the identity value.
     * 
     * @param identity the initial value
     * @param operator the binary operator to apply
     * @return the accumulated result
     * @throws IllegalStateException if the list is looping
     */
    public int reduce(int identity, IntBinaryOperator operator) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        int result = identity;
        ListNode current = front;

        while (current != null) {
            result = operator.applyAsInt(result, current.data);
            current = current.next;
        }

        return result;
    }

    /**
     * Returns whether this list is empty.
     * 
     * @return true if the list is empty, false otherwise
     */
    public boolean isEmpty() {
        return front == null;
    }

    /**
     * Returns the number of nodes in this list.
     * 
     * @return the size of the list
     * @throws IllegalStateException if the list is looping
     */
    public int checkSize() {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        int count = 0;
        ListNode current = front;
        while (current != null) {
            count++;
            current = current.next;
        }
        return count;
    }

    public int size() {
        return size;
    }

    /**
     * Returns whether this list contains the specified value.
     * 
     * @param value the value to search for
     * @return true if the value is found, false otherwise
     * @throws IllegalStateException if the list is looping
     */
    public boolean contains(int value) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }        
        ListNode current = front;
        while (current != null) {
            if (value == current.data) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    /**
     * Returns whether this list is sorted in ascending order.
     * 
     * @return true if sorted, false otherwise
     * @throws IllegalStateException if the list is looping
     */
    public boolean isSorted() {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (isEmpty()) {
            return true;
        }
        ListNode current = front;
        while (current.next != null) {
            if (current.data > current.next.data) {
                return false;
            }
            current = current.next;
        }
        return true;
    }

    /**
     * Checks whether this list contains a cycle and updates the isLooping field.
     * Uses Floyd's cycle detection algorithm.
     * 
     * @return true if the list is looping, false otherwise
     */
    public boolean checkLooping() {
        ListNode slow = front;
        ListNode fast = front;
        while ( (fast != null) && (fast.next != null) ) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) {
                isLooping = true;
                return true;
            }
        }
        isLooping = false;
        return false;
    }

    /**
     * Returns the largest value in this list.
     * 
     * @return the maximum value
     * @throws IllegalStateException if the list is empty or looping
     */
    public int getBiggest() {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (isEmpty()) {
            throw new IllegalStateException("Linked Int List cannot be empty.");
        }
        ListNode current = front;
        int biggest = front.data;
        while (current.next != null) {
            current = current.next;
            if (current.data > biggest) {
                biggest = current.data;
            }
        }
        return biggest;
    }

    /**
     * Returns the smallest value in this list.
     * 
     * @return the minimum value
     * @throws IllegalStateException if the list is empty or looping
     */
    public int getSmallest() {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (isEmpty()) {
            throw new IllegalStateException("Linked Int List cannot be empty.");
        }
        ListNode current = front;
        int smallest = front.data;
        while (current.next != null) {
            current = current.next;
            if (current.data < smallest) {
                smallest = current.data;
            }
        }
        return smallest;
    }

    /**
     * Returns the difference between the largest and smallest values in this list.
     * 
     * @return the range of values
     * @throws IllegalStateException if the list is empty or looping
     */
    public int getRange() {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (isEmpty()) {
            throw new IllegalStateException("Linked Int List cannot be empty.");
        }
        ListNode current = front;
        int smallest = front.data;
        int biggest = front.data;
        while (current.next != null) {
            current = current.next;
            if (current.data < smallest) {
                smallest = current.data;
            }
            if (current.data > biggest) {
                biggest = current.data;
            }
        }
        return biggest-smallest;
    }

    /**
     * Returns the sum of all values in this list.
     * 
     * @return the total sum
     * @throws IllegalStateException if the list is looping
     */
    public int getTotalSum() {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        ListNode current = front;
        int sum = 0;
        while (current != null) {
            sum += current.data;
            current = current.next;
        }
        return sum;
    }

    /**
     * Returns the node at the specified index.
     * 
     * @param index the index of the node to retrieve
     * @return the node at the specified index
     * @throws IllegalStateException if the list is empty
     * @throws IllegalArgumentException if index is negative
     * @throws IndexOutOfBoundsException if index exceeds list size
     */
    public ListNode getNode(int index) {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot get node of empty Linked Int List.");
        }
        if (index < 0) {
            throw new IllegalArgumentException("Index cannot be lower than 0");
        }
        int currentIndex = 0;
        ListNode current = front;
        while (currentIndex != index && current != null) {
            current = current.next;
            currentIndex++;
        }
        if (current == null) {
            throw new IndexOutOfBoundsException("Index exceeds list size.");
        }

        return current;
    }

    /**
     * Returns the first node in this list.
     * 
     * @return the front node
     */
    public ListNode getFront() {
        return front;
    }

    /**
     * Returns the last node in this list.
     * 
     * @return the last node
     */
    public ListNode getLast() {
        return last;
    }

    /**
     * Returns the middle node in this list.
     * Uses the slow/fast pointer technique.
     * 
     * @return the middle node
     * @throws IllegalStateException if the list is empty or looping
     */
    public ListNode getMiddle() {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (front == null) {
            throw new IllegalStateException("Linked Int List can't be empty.");
        }
        ListNode slow = front;
        ListNode fast = front;
        while (true) {
            if (fast == null) {
                break;
            } else if (fast.next == null) {
                break;
            } else if (fast.next.next == null) {
                break;
            }
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow;
    }

    /**
     * Returns the two middle nodes in this list.
     * 
     * @return an array containing the two middle nodes
     * @throws IllegalStateException if the list is empty, has odd size, or is looping
     */
    public ListNode[] getMiddleTwo() {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (isEmpty()) {
            throw new IllegalStateException("Linked Int List cannot be empty.");
        }
        if (size % 2 == 1) { 
            throw new IllegalStateException("It has to be an even numbered list."); 
        }
        ListNode slow = front;
        ListNode fast = front;
        while (true) {
            if (fast == null) {
                break;
            } else if (fast.next == null) {
                break;
            } else if (fast.next.next == null) {
                break;
            }
            slow = slow.next;
            fast = fast.next.next;
        }
        return new ListNode[] {
            slow,
            slow.next
        };
    }

    /**
     * Returns a new LinkedIntList containing elements from the specified range.
     * 
     * @param index1 the starting index (inclusive)
     * @param index2 the ending index (inclusive)
     * @return a new sublist
     * @throws IllegalArgumentException if indices are invalid
     */
    public LinkedIntList getSubList(int index1, int index2) {
        if (index1 > index2) {
            throw new IllegalArgumentException("index1 has to be less than or equal to index2");
        }
        if (index1 < 0) {
            throw new IllegalArgumentException("index1 has to be greater than or equal to 0");
        }
        if (index1 >= size || index2 >= size) {
            throw new IllegalArgumentException("index1 and index2 have to be less than the size of the list");
        }
        
        LinkedIntList subList = new LinkedIntList();
        ListNode current = getNode(index1);
        
        for (int i = index1; i <= index2; i++) {
            subList.addLast(current.data);
            current = current.next;
        }
        
        return subList;
    }

    /**
     * Rotates the list to the right by the specified number of positions.
     * 
     * @param x the number of positions to rotate (positive for right, negative for left)
     */
    public void rotate(int x) {
        if (front == null || front.next == null) {
            return;
        }
        
        x = x % size;
        if (x < 0) {
            x += size;
        }
        if (x == 0) {
            return;
        }

        int stepsToNewTail = size - x - 1;

        int index = 0;
        ListNode current = front;
        while (index != stepsToNewTail) {
            index++;
            current = current.next;
        }
        front = current.next;
        current.next = null;
        last = current;
    }

    /**
     * Removes all duplicate values from this list.
     * 
     * @return the number of nodes removed
     * @throws IllegalStateException if the list is looping
     */
    public int removeDuplicates() {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        HashMap<Integer, Boolean> seen = new HashMap<>();
        ListNode current = front;
        ListNode prev = null;
        int removedCount = 0;

        while (current != null) {
            if (seen.containsKey(current.data)) {
                prev.next = current.next;
                removedCount++;
            } else {
                seen.put(current.data, true);
                prev = current;
            }
            current = current.next;
        }
        size -= removedCount;
        last = prev;
        return removedCount;
    }

    /**
     * Makes this list circular by connecting the last node to the front node.
     */
    public void setLooping() {
        isLooping = true;
        getLast().next = front;
    }

    /**
     * Splits this list at the specified index into two separate lists.
     * 
     * @param index the index at which to split
     * @return an array containing the two resulting lists
     * @throws IllegalStateException if the list is looping
     * @throws IllegalArgumentException if index is out of bounds
     */
    public LinkedIntList[] splitAt(int index) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (index < 0 || index >= size) {
            throw new IllegalArgumentException("Index out of bounds.");
        }
        LinkedIntList list1 = new LinkedIntList();
        LinkedIntList list2 = new LinkedIntList();
        ListNode current = front;
        for (int i = 0; i < index; i++) {
            list1.addLast(current.data);
            current = current.next;
        }
        while (current != null) {
            list2.addLast(current.data);
            current = current.next;
        }
        return new LinkedIntList[]{list1, list2};
    }

    /**
     * Reverses the order of elements in this list.
     * 
     * @throws IllegalStateException if the list is looping
     */
    public void reverse() {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        switch (size) {
            case 0:
                return;
            case 1:
                return;
            case 2:
                front.next.next = front;
                ListNode hold = front;
                front = front.next;
                hold.next = null;
                last = hold;
                return;
            default:
                ListNode temp0 = front;
                ListNode temp1 = front.next;
                ListNode temp2 = front.next.next;
                temp0.next = null;
                while (temp2.next != null) {
                    temp1.next = temp0;
                    temp0 = temp1;
                    temp1 = temp2;
                    temp2 = temp2.next;
                }
                temp1.next = temp0;
                temp2.next = temp1;
                front = temp2;
        }
        ListNode temp = front;
        front = last;
        last = temp;
    }

    /**
     * Returns a deep copy of this list.
     * 
     * @return a new LinkedIntList with the same elements
     * @throws IllegalStateException if the list is looping
     */
    public LinkedIntList clone() {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (front == null) return new LinkedIntList();

        LinkedIntList list = new LinkedIntList();
        ListNode current = front;
        while (current != null) {
            list.addLast(current.data);
            current = current.next;
        }
        return list;
    }

    /**
     * Inserts a value into its correct position in this sorted list.
     * 
     * @param value the value to insert
     * @throws IllegalStateException if the list is not sorted or is looping
     */
    public void addSorted(int value) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (!isSorted()) {
            throw new IllegalStateException("List has to be sorted in the first place.");
        }
        if (isEmpty()) {
            front = new ListNode(value);
            last = front;
            size++;
            return;
        }
        if (value < front.data) {
            front = new ListNode(value, front);
            if (size == 0) {
                last = front;
            }
            size++;
            return;
        }
        boolean inserted = false;
        ListNode current = front;
        while (current.next != null) {
            if (value < current.next.data) {
                current.next = new ListNode(value, current.next);
                inserted = true;
                break;
            }
            current = current.next;
        }
        if (!inserted) {
            current.next = new ListNode(value);
            last = current.next;
        }
        size++;
    }

    /**
     * Appends all elements from another list to the end of this list.
     * 
     * @param other the list to append
     * @throws IllegalArgumentException if other is null
     * @throws UnsupportedOperationException if other is looping
     */
    public void addAll(LinkedIntList other) {
        if (other == null) {
            throw new IllegalArgumentException("Other list cannot be null");
        }
        if (other.isLooping) {
            throw new UnsupportedOperationException("Cannot add a looping list.");
        }

        if (other.front == null) {
            return;
        }

        if (front != null) {
            ListNode current = other.front;
            while (current != null) {
                last.next = new ListNode(current.data);
                last = last.next;
                current = current.next;
                size++;
            }
        } else {
            front = new ListNode(other.front.data);
            size = 1;
            ListNode current = this.front;
            ListNode currentOther = other.front.next;
            while (currentOther != null) {
                current.next = new ListNode(currentOther.data);
                current = current.next;
                currentOther = currentOther.next;
                size++;
            }
            last = current;
        }
    }

    /**
     * Swaps the nodes at the two specified indices.
     * 
     * @param index1 the first index
     * @param index2 the second index
     * @throws IllegalStateException if the list is looping
     * @throws IllegalArgumentException if indices are out of bounds
     */
    public void swap(int index1, int index2) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (
            (
                (index1 < 0) || (index1 >= size)
            ) || (
                (index2 < 0) || (index2 >= size)
            )
        ) {
            throw new IllegalArgumentException("Node indexes have to be within range of list.");
        }

        if (index1 == index2) {
            return;
        }

        boolean zero1 = index1 == 0;
        boolean zero2 = index2 == 0;

        ListNode prev1 = null, node1 = null, next1 = null;
        ListNode prev2 = null, node2 = null, next2 = null;

        ListNode current = front;
        int currentIndex = 0;
        while (current != null) {
            if (currentIndex == index1 - 1 && !zero1) {
                prev1 = current;
            }
            if (currentIndex == index2 - 1 && !zero2) {
                prev2 = current;
            }
            if (currentIndex == index1) {
                node1 = current;
                next1 = current.next;
            }
            if (currentIndex == index2) {
                node2 = current;
                next2 = current.next;
            }

            current = current.next;
            currentIndex++;
        }

        boolean eitherZero = zero1 || zero2;
        boolean touching = Math.abs(index1 - index2) == 1;

        if (eitherZero && touching) {
            if (zero2) {
                ListNode tempNode = node1, tempNext = next1;
                node1 = node2;
                next1 = next2;

                node2 = tempNode;
                next2 = tempNext;
            }

            node1.next = next2;
            node2.next = next1;

            front = node2;
        } else if (eitherZero) {
            if (zero2) {
                ListNode tempNode = node1, tempNext = next1;
                node1 = node2;
                next1 = next2;

                node2 = tempNode;
                next2 = tempNext;
            }
            
            node1.next = next2;
            prev2.next = node1;
            node2.next = next1;

            front = node2;
        } else if (touching) {
            if (index2 < index1) {
                ListNode tempNode = node1, tempNext = next1, tempPrev = prev1;
                node1 = node2;
                next1 = next2;
                prev1 = prev2;

                node2 = tempNode;
                next2 = tempNext;
                prev2 = tempPrev;
            }
            node1.next = next2;
            node2.next = node1;
            prev1.next = node2;
        } else {
            prev1.next = node2;
            prev2.next = node1;
            ListNode temp = node1.next;
            node1.next = node2.next;
            node2.next = temp;
        }

        if (node1.next == null) {
            last = node1;
        } else if (node2.next == null) {
            last = node2;
        }
    }

    /**
     * Appends another list to the end of this list by reference.
     * 
     * @param other the list to merge
     * @throws IllegalArgumentException if other is null
     * @throws IllegalStateException if either list is looping
     */
    public void merge(LinkedIntList other) {
        if (other == null) {
            throw new IllegalArgumentException("Other Linked Int List cannot be null.");
        }
        if (isLooping || other.isLooping) {
            throw new IllegalStateException("Neither List can be looping.");
        }
        if (isEmpty()) {
            front = other.front;
            last = other.last;
            size = other.size;
            return;
        }
        getLast().next = other.front;
        size += other.size;
        last = other.last;
    }

    /**
     * Merges another sorted list into this sorted list while maintaining sort order.
     * 
     * @param other the sorted list to merge
     * @throws IllegalArgumentException if other is null
     * @throws IllegalStateException if either list is looping or not sorted
     */
    public void mergeSorted(LinkedIntList other) {
        if (other == null) {
            throw new IllegalArgumentException("Other Linked Int List cannot be null.");
        }
        if (isLooping || other.isLooping) {
            throw new IllegalStateException("Neither List can be looping.");
        }
        if ((!isSorted()) || (!other.isSorted())) {
            throw new IllegalStateException("Both lists have to be sorted in the first place.");
        }

        if (other.front == null) {
            return;
        }
        if (front == null) {
            front = other.front;
            last = other.last;
            size = other.size;
            return;
        }

        ListNode current1 = front;
        ListNode current2 = other.front;
        ListNode hold;
        if (current1.data > current2.data) {
            hold = current2;
            current2 = current2.next;
        } else {
            hold = current1;
            current1 = current1.next;
        }
        ListNode start = hold;
        while ((!(current1 == null)) && (!(current2 == null))) {
            if (current1.data > current2.data) {
                hold.next = current2;
                hold = hold.next;
                current2 = current2.next;
            } else {
                hold.next = current1;
                hold = hold.next;
                current1 = current1.next;
            }
        }
        if (current1 == null) {
            hold.next = current2;
        }
        if (current2 == null) {
            hold.next = current1;
        }
        front = start;
        size += other.size;

        while (hold.next != null) {
            hold = hold.next;
        }
        last = hold;
    }

    /**
     * Returns the index of the first occurrence of the specified value.
     * 
     * @param value the value to search for
     * @return the index, or -1 if not found
     * @throws UnsupportedOperationException if the list is looping
     */
    public int indexOf(int value) {
        if (isLooping) {
            throw new UnsupportedOperationException("Cannot search in a looping list");
        }
        ListNode current = front;
        int index = 0;
        while (current != null) {
            if (current.data == value) {
                return index;
            }
            index++;
            current = current.next;
        }
        return -1;
    }

    /**
     * Returns a list of all indices where the specified value occurs.
     * 
     * @param value the value to search for
     * @return an ArrayList of indices
     * @throws UnsupportedOperationException if the list is looping
     */
    public ArrayList<Integer> listIndexesOf(int value) {
        if (isLooping) {
            throw new UnsupportedOperationException("Cannot search in a looping list");
        }
        ListNode current = front;
        ArrayList<Integer> indexes = new ArrayList<>();
        int index = 0;
        while (current != null) {
            if (current.data == value) {
                indexes.add(index);
            }
            index++;
            current = current.next;
        }
        return indexes;
    }

    /**
     * Returns the index of the last occurrence of the specified value.
     * 
     * @param value the value to search for
     * @return the last index, or -1 if not found
     * @throws UnsupportedOperationException if the list is looping
     */
    public int lastIndexOf(int value) {
        if (isLooping) {
            throw new UnsupportedOperationException("Cannot search in a looping list");
        }
        ListNode current = front;
        int lastNodeIndex = -1;
        int index = 0;
        while (current != null) {
            if (current.data == value) {
                lastNodeIndex = index;
            }
            index++;
            current = current.next;
        }
        return lastNodeIndex;
    }

/**
     * Returns the node at the specified index.
     * 
     * @param index the index of the node to retrieve
     * @return the node at the specified index
     * @throws IllegalStateException if the list is looping
     * @throws IllegalArgumentException if index is negative
     */
    public ListNode get(int index) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (index < 0) {
            throw new IllegalArgumentException("Index cannot be lower than 0");
        }
        return getNode(index);
    }

    /**
     * Sets the value of the node at the specified index.
     * 
     * @param index the index of the node to modify
     * @param value the new value
     * @throws IllegalStateException if the list is empty
     * @throws IllegalArgumentException if index is negative
     * @throws IndexOutOfBoundsException if index exceeds list size
     */
    public void set(int index, int value) {
        getNode(index).data = value;
    }

    /**
     * Adds a new node with the specified value to the end of the list.
     * 
     * @param value the value to add
     * @throws IllegalStateException if the list is looping
     */
    public void addLast(int value) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (isEmpty()) {
            front = new ListNode(value);
            last = front;
            return;
        } else {
            last.next = new ListNode(value);
            last = last.next;
        }
        size++;
    }

    /**
     * Adds a new node with the specified value to the front of the list.
     * 
     * @param value the value to add
     */
    public void addFront(int value) {
       if (isEmpty()) {
           front = new ListNode(value);
           last = front;
       } else {
           front = new ListNode(value, front);
       }
       size++;
    }

    /**
     * Adds a new node with the specified value at the second-to-last position.
     * 
     * @param value the value to add
     * @throws IllegalStateException if the list has fewer than 2 nodes or is looping
     */
    public void addPenultimate(int value) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (size < 2) {
            throw new IllegalStateException("List has to be at least 2 nodes long.");
        }
        
        ListNode current = front;
        while (current.next.next != null) {
            current = current.next;
        }
        current.next = new ListNode(value,current.next);
        size++;
    }

    /**
     * Inserts a new node with the specified value at the given index.
     * 
     * @param index the index at which to insert (0 for front)
     * @param value the value to insert
     * @throws IllegalStateException if the list is looping
     * @throws IllegalArgumentException if index is out of bounds
     */
    public void addIndex(int index, int value) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (index > size || index < 0) {
            throw new IllegalArgumentException("Index has to be in range of the Linked Int List");
        }
        if (index == 0) {
            addFront(value);
            return;
        }
        ListNode current = front;
        int currentIndex = 0;
        while (currentIndex != index - 1) {
            current = current.next;
            currentIndex++;
        }
        current.next = new ListNode(value, current.next);
        size++;
        if (current.next.next == null) {
            last = current.next;
        }
    }
    
    /**
     * Removes and returns the first node's value.
     * 
     * @return the value of the removed node
     * @throws NoSuchElementException if the list is empty
     */
    public int removeFront() {
        if (isEmpty()) {
            throw new NoSuchElementException("List cannot be removed from further.");
        }
        int data = front.data;
        front = front.next;
        size--;
        if (isEmpty()) {
            last = null;
        }
        return data;
    }

    /**
     * Removes and returns the last node's value.
     * 
     * @return the value of the removed node
     * @throws NoSuchElementException if the list is empty
     * @throws IllegalStateException if the list is looping
     */
    public int removeLast() {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (isEmpty()) {
            throw new NoSuchElementException("List cannot be removed from further.");
        }
        if (front.next == null) {
            return removeFront();
        }
        ListNode current = front;
        while (current.next.next != null) {
            current = current.next;
        }
        int data = current.next.data;
        current.next = null;
        size--;
        last = current;
        return data;
    }

    /**
     * Removes and returns the first node's value.
     * Alias for removeFront().
     * 
     * @return the value of the removed node
     */
    public int remove() {
        return removeFront();
    }

    /**
     * Removes and returns the node at the specified index.
     * 
     * @param index the index of the node to remove
     * @return the value of the removed node
     * @throws IllegalStateException if the list is looping
     * @throws IllegalArgumentException if index is out of bounds
     */
    public int remove(int index) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (index >= size || index < 0) {
            throw new IllegalArgumentException("Index has to be in range of the Linked Int List");
        }
        if (index == 0) {
            return removeFront();
        }
        ListNode current = front;
        int currentIndex = 0;
        while (currentIndex != index - 1) {
            current = current.next;
            currentIndex++;
        }
        int data = current.next.data;
        current.next = current.next.next;
        if (current.next == null) {
            last = current;
        }
        size--;
        return data;
    }

    /**
     * Removes and returns the second-to-last node's value.
     * 
     * @return the value of the removed node
     * @throws IllegalStateException if the list has fewer than 2 nodes or is looping
     */
    public int removePenultimate() {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (size < 2) {
            throw new IllegalStateException("List has to be at least 2 nodes long.");
        }
        return remove(size - 2);
    }

    /**
     * Removes all nodes with the specified value.
     * 
     * @param value the value to remove
     * @return the number of nodes removed
     * @throws IllegalStateException if the list is empty or looping
     */
    public int removeAll(int value) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (isEmpty()) {
            throw new IllegalStateException("Cannot remove from an empty Linked Int List.");
        }

        int count = 0;
        
        while (front!= null && front.data == value) {
            front = front.next;
            count++;
            size--;
        }

        if (front == null) {
            return count;
        }

        ListNode current = front;
        while (current.next != null) {
            if (current.next.data == value) {
                current.next = current.next.next;
                count++;
                size--;
            } else {
                current = current.next;
            }
        }
        last = current;
        return count;
    }

    /**
     * Removes the first occurrence of the specified value.
     * 
     * @param value the value to remove
     * @throws IllegalStateException if the list is empty or looping
     */
    public void removeFirstValue(int value) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (isEmpty()) {
            throw new IllegalStateException("Cannot remove from an empty Linked Int List.");
        }

        if (front.data == value) {
            front = front.next;
            if (front == null) {
                last = null;
            }
            size--;
            return;
        }

        ListNode current = front;
        while (current.next != null) {
            if (current.next.data == value) {
                current.next = current.next.next;
                if (current.next == null) {
                    last = current;
                }
                size--;
                return;
            } else {
                current = current.next;
            }
        }
    }

    /**
     * Removes all nodes in the specified range (inclusive).
     * 
     * @param start the starting index
     * @param end the ending index
     * @throws IllegalStateException if the list is looping
     * @throws IllegalArgumentException if indices are invalid
     */
    public void removeRange(int start, int end) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (start < 0 || end < start || end >= size) {
            throw new IllegalArgumentException("Invalid start or end index.");
        }

        if (start == 0) {
            ListNode current = front;
            for (int i = 0; i <= end; i++) {
                current = current.next;
            }
            front = current;
            if (front == null) {
                last = null;
            }
            size -= (end - start + 1);
            front = current;
            return;
        }

        ListNode current = front;
        for (int i = 0; i < start - 1; i++) {
            current = current.next;
        }

        ListNode temp = current;
        for (int i = start; i <= end; i++) {
            temp = temp.next;
        }
        current.next = temp;
        last = current;
        size -= (end - start + 1);
    }

    /**
     * Removes all nodes from the list.
     */
    public void clear() {
        front = null;
        last = null;
        size = 0;
        isLooping = false;
    }

    /**
     * Converts this list to an array.
     * 
     * @return an array containing all values in this list
     * @throws IllegalStateException if the list is looping
     */
    public int[] toArray() {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        int[] array = new int[size];
        int index = 0;
        ListNode current = front;
        while(current != null) {
            array[index] = current.data;
            index++;
        }
        return array;
    }

    /**
     * Counts the number of occurrences of the specified value.
     * 
     * @param value the value to count
     * @return the number of occurrences
     * @throws IllegalStateException if the list is looping
     */
    public int countAmount(int value) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        int count = 0;
        ListNode current = front;
        while (current != null) {
            if (current.data == value) {
                count++;
            }
            current = current.next;
        }
        return count;
    }

    /**
     * Counts the number of nodes that satisfy the given predicate.
     * 
     * @param predicate the predicate to test
     * @return the count of matching nodes
     * @throws IllegalStateException if the list is looping
     */
    public int countIf(IntPredicate predicate) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        int count = 0;
        ListNode current = front;
        while (current != null) {
            if (predicate.test(current.data)) {
                count++;
            }
            current = current.next;
        }
        return count;
    }

    /**
     * Finds and returns the first node that satisfies the given predicate.
     * 
     * @param predicate the predicate to test
     * @return the first matching node, or null if none found
     * @throws IllegalStateException if the list is looping
     */
    public ListNode findFirst(IntPredicate predicate) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        ListNode current = front;
        while (current != null) {
            if (predicate.test(current.data)) {
                return current;
            }
            current = current.next;
        }
        return null;
    }

    /**
     * Returns whether all nodes satisfy the given predicate.
     * 
     * @param predicate the predicate to test
     * @return true if all nodes match, false otherwise
     * @throws IllegalStateException if the list is looping
     */
    public boolean allMatch(IntPredicate predicate) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        ListNode current = front;
        while (current != null) {
            if (!predicate.test(current.data)) {
                return false;
            }
            current = current.next;
        }
        return true;
    }

    /**
     * Returns whether any node satisfies the given predicate.
     * 
     * @param predicate the predicate to test
     * @return true if any node matches, false otherwise
     * @throws IllegalStateException if the list is looping
     */
    public boolean anyMatch(IntPredicate predicate) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        ListNode current = front;
        while (current != null) {
            if (predicate.test(current.data)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    /**
     * Duplicates each node in the list.
     * After calling this method, each value appears twice consecutively.
     * 
     * @throws IllegalStateException if the list is looping
     */
    public void stutter() {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        ListNode current = front;
        while (current != null) {
            current.next = new ListNode(current.data, current.next);
            current = current.next.next;
        }
        size *= 2;
        last = current;
    }

    /**
     * Sorts this list in ascending order using merge sort.
     * 
     * @throws IllegalStateException if the list is looping
     */
    public void sort() {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        LinkedIntList sorted = sort(size, front);
        this.front = sorted.front;
        this.last = sorted.last;
        this.size = sorted.size;
    }

    /**
     * Helper method for merge sort. Recursively sorts a portion of the list.
     * 
     * @param size the size of the portion to sort
     * @param tip the first node of the portion
     * @return a new sorted LinkedIntList
     * @throws IllegalStateException if size is 0 or tip is null
     */
    private LinkedIntList sort(int size, ListNode tip) {
        if (size == 0) {
            throw new IllegalStateException("Chunk size cannot be less than 1.");
        }
        if (tip == null) {
            throw new IllegalStateException("Chunk front cannot be null.");
        }
        if (size == 1) {
            tip.next = null;
            return new LinkedIntList(tip);
        }
        
        ListNode a = tip;
        ListNode current = tip;
        
        for (int i = 0; i < size/2 - 1; i++) {
            current = current.next;
        }
        
        ListNode b = current.next;
        current.next = null;
        
        if (size % 2 == 0) {
            LinkedIntList a1 = sort(size/2, a);
            LinkedIntList b1 = sort(size/2, b);
            a1.mergeSorted(b1);
            return a1;
        } else {
            LinkedIntList a1 = sort(size/2, a);
            LinkedIntList b1 = sort(size/2 + 1, b);    
            a1.mergeSorted(b1);
            return a1; 
        }
    }

    /**
     * Returns an iterator over the elements in this list.
     * 
     * @return an Iterator for this list
     */
    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            private ListNode current = front;
            @Override public boolean hasNext() { return current != null; }
            @Override public Integer next() {
                if (current == null) throw new NoSuchElementException();
                int val = current.data;
                current = current.next;
                return val;
            }
        };
    }

    /**
     * Retains only the nodes that satisfy the given predicate.
     * 
     * @param predicate the predicate to test
     * @return true if the list was modified, false otherwise
     * @throws IllegalStateException if the list is looping
     */
    public boolean retainAll(IntPredicate predicate) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        boolean changed = false;
        ListNode current = front;
        ListNode prev = null;
        while (current != null) {
            if (!predicate.test(current.data)) {
                if (prev == null) {
                    front = current.next;
                } else {
                    prev.next = current.next;
                }
                size--;
                changed = true;
            } else {
                prev = current;
            }
            current = current.next;
        }
        last = prev;
        return changed;
    }

    /**
     * Modifies this list to retain only elements present in both lists.
     * 
     * @param other the list to compare with
     * @throws IllegalArgumentException if other is null
     * @throws IllegalStateException if either list is looping
     */
    public void mergeCommon(LinkedIntList other) {
        if (other == null) {
            throw new IllegalArgumentException("Other list cannot be null.");
        }
        if (isLooping || other.isLooping) {
            throw new IllegalStateException("Neither List can be looping.");
        }     
        if (isEmpty()) {
            return;
        }
       
        HashSet<Integer> otherValues = new HashSet<>();
        ListNode currentOther = other.front;
        while (currentOther != null) {
            otherValues.add(currentOther.data);
            currentOther = currentOther.next;
        }

        while (front != null && !otherValues.contains(front.data)) {
            front = front.next;
            size--;
        }

        if (front == null) {  // Add this check
            last = null;
            return;
        }

        ListNode current = front;
        while (current.next != null) {
            if (!otherValues.contains(current.next.data)) {
                size--;
                current.next = current.next.next;
            } else {
                current = current.next;
            }
        }
        last = current;
    }

    /**
     * Removes all elements from this list that are present in the other list.
     * 
     * @param other the list containing values to remove
     * @throws IllegalArgumentException if other is null
     * @throws IllegalStateException if either list is looping
     */
    public void removeCommon(LinkedIntList other) {
        if (other == null) {
            throw new IllegalArgumentException("Other list cannot be null.");
        }
        if (isLooping || other.isLooping) {
            throw new IllegalStateException("Neither List can be looping.");
        }     
        if (isEmpty()) {
            return;
        }
       
        HashSet<Integer> otherValues = new HashSet<>();
        ListNode currentOther = other.front;
        while (currentOther != null) {
            otherValues.add(currentOther.data);
            currentOther = currentOther.next;
        }

        while (front != null && otherValues.contains(front.data)) {
            front = front.next;
            size--;
        }

        if (front == null) {
            last = null;
            return;
        }
    
        ListNode current = front;
        while (current.next != null) {
            if (otherValues.contains(current.next.data)) {
                current.next = current.next.next;
                size--;
            } else {
                current = current.next;
            }
        }
        last = current;
    }

    /**
     * Returns a new list containing elements common to both lists.
     * 
     * @param other the list to intersect with
     * @return a new LinkedIntList containing the intersection
     * @throws IllegalArgumentException if other is null
     * @throws IllegalStateException if either list is looping
     */
    public LinkedIntList intersection(LinkedIntList other) {
        if (other == null) {
            throw new IllegalArgumentException("Other list cannot be null.");
        }
        if (isLooping || other.isLooping) {
            throw new IllegalStateException("Neither List can be looping.");
        }     
        LinkedIntList result = new LinkedIntList();
        if (isEmpty() || other.isEmpty()) {
            return result;
        }

        HashSet<Integer> otherValues = new HashSet<>();
        ListNode currentOther = other.front;
        while (currentOther != null) {
            otherValues.add(currentOther.data);
            currentOther = currentOther.next;
        }

        ListNode hold = new ListNode(0);
        ListNode tail = hold;
        ListNode current = front;

        while (current != null) {
            if (otherValues.contains(current.data)) {
                tail.next = new ListNode(current.data);
                tail = tail.next;
                result.size++;
            }
            current = current.next;
        }
        result.front = hold.next;
        result.last = tail;
        return result;
    }

    /**
     * Partitions this list so that nodes not satisfying the predicate come first,
     * followed by nodes that do satisfy the predicate.
     * 
     * @param predicate the predicate used to partition
     * @throws IllegalStateException if the list is looping
     */
    public void partition(IntPredicate predicate) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }

        LinkedIntList list1 = new LinkedIntList();
        LinkedIntList list2 = new LinkedIntList();
        ListNode current = front;       
        while (current != null) {
            if (predicate.test(current.data)) {
                list2.addLast(current.data);
            } else {
                list1.addLast(current.data);
            }
            current = current.next;
        };
        list1.merge(list2);
        front = list1.front;
        last = list1.last;
    }

    /**
     * Compares this list with another object for equality.
     * Two lists are equal if they contain the same sequence of values.
     * For looping lists, loops must occur at corresponding positions.
     * 
     * @param obj the object to compare with
     * @return true if the lists are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        LinkedIntList other = (LinkedIntList) obj;

        if (front == null && front == other.front) {
            return true;
        }

        if (front == null || other.front == null) {
            return false;
        }
        
        if (isLooping != other.isLooping) {
            return false;
        }
        
        ListNode current = front;
        ListNode currentOther = other.front;
        
        if (isLooping && other.isLooping) {
            HashMap<ListNode, Integer> visited = new HashMap<>();
            HashMap<ListNode, Integer> visitedOther = new HashMap<>();
            
            while (true) {
                visited.put(current, visited.getOrDefault(current, 0) + 1);
                visitedOther.put(currentOther, visitedOther.getOrDefault(currentOther, 0) + 1);
                
                if (visited.get(current) != visitedOther.get(currentOther)) {
                    return false;
                }
                
                if (current.data != currentOther.data) {
                    return false;
                }
                
                if (visited.get(current) == 2) {
                    break;
                }
                
                current = current.next;
                currentOther = currentOther.next;
            }
        } else {
            while (current != null && currentOther != null) {
                if (current.data != currentOther.data) {
                    return false;
                }
                current = current.next;   
                currentOther = currentOther.next;
            }
            
            if (current != null || currentOther != null) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Returns a string representation of this list.
     * Format: "value1 -> value2 -> ... -> null" or "empty" if empty.
     * 
     * @return a string representation of the list
     * @throws IllegalStateException if the list is looping
     */
    @Override 
    public String toString() {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        String result = "";
        if (!isEmpty()) {
            result = front.data + "";
        } else {
            return "empty";
        }
        ListNode current = front.next;
        while (current != null) {
            result = result + " -> " + current.data;
            current = current.next;
        }
        return result + " -> null";
    }
}