package homework;

// Name: Timothy Lopez
// Period: 6
// Version: 1.0

/* 
 * Name: LinkedIntList 
 * Parameters: front node of a list (listNode) 
 * Use: It is a manager for a LinkedList full of only ints. It allows you to  
 *      add or delete nodes wherever you want, get the range, check if it is 
 *      sorted or empty, get number of nodes in list, and print it in a 
 *      clean format. 
 */


//Imports
import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.IntUnaryOperator;
import java.util.function.IntPredicate;
import java.util.function.IntBinaryOperator;

public class LinkedIntList implements Iterable<Integer> {

    //Set isLooping to true manually when manipulating LinkedIntList
    private ListNode front;
    public boolean isLooping;

    /*     
     * Name: LinkedIntList     
     * Parameters: None     
     * Output: Initializes the LinkedIntList     
     */
    public LinkedIntList() {
        front = null;
    }

    /*     
     * Name: LinkedIntList     
     * Parameters: The node (ListNode) that is at the front of the LinkedList.     
     * Output: Initializes the LinkedIntList     
     */
    public LinkedIntList(ListNode frontNode) {
        front = frontNode;
    }

    /*     
     * Name: LinkedIntList     
     * Parameters: An array list of integers (from frontmost as index 0)  
     * Output: Initializes the LinkedIntList with the array list 
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
    }

    /*     
     * Name: LinkedIntList     
     * Parameters: A list of integers (from frontmost as index 0)  
     * Output: Initializes the LinkedIntList with the list 
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
    }


    /*
     * Name: hashCode
     * Parameters: None
     * Output: Returns the hash code for the LinkedIntList.
     * Exceptions: Linked Int List cannot be looping.
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


    /*     
     * Name: map     
     * Parameters: An IntUnaryOperator function that takes an int and returns an int.     
     * Output: Returns a new LinkedIntList where each element is the result of applying     
     *          the function to the corresponding element in the original list.     
     * Exceptions: Linked Int List cannot be looping.  
     */
    public LinkedIntList map(IntUnaryOperator f) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (isEmpty()) {
            return new LinkedIntList();
        }

        LinkedIntList result = new LinkedIntList(new ListNode(f.applyAsInt(front.data)));
        ListNode current = front.next;
        ListNode resultCurrent = result.front;
        while (current != null) {
            resultCurrent.next = new ListNode(f.applyAsInt(current.data));
            resultCurrent = resultCurrent.next;
            current = current.next;
        }
        
        return result;
    }

    /*
     * Name: filter
     * Parameters: An IntPredicate function that takes an int and returns a boolean.
     * Output: Returns a new LinkedIntList containing only the elements that match the predicate.
     * Exceptions: Linked Int List cannot be looping.
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
            }
            current = current.next;
        }

        return result;
    }

    /*     
     * Name: reduce     
     * Parameters: An identity value and an IntBinaryOperator function that takes two ints     
     *              and returns an int.     
     * Output: Returns the result of applying the binary operator cumulatively to the elements     
     *          of the list, starting with the identity value.     
     * Exceptions: Linked Int List cannot be looping.  
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

    /*     
     * Name: isEmpty     
     * Parameters: None     
     * Output: Returns whether the Linked Int List is empty.     
     */
    public boolean isEmpty() {
        return front == null;
    }

    /*     
     * Name: size     
     * Parameters: None     
     * Output: Returns the amount of nodes in the Linked Int List.     
     * Exceptions: Linked Int List cannot be looping.  
     */ 
    public int size() {
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

    /*     
     * Name: contains     
     * Parameters: Value you want to check that is contained at least once within     
     *        the entire LinkedList of integers.     
     * Output: Returns whether the value is inside the Linked Int List.
     * Exceptions: Linked Int List cannot be looping.  
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

    /*     
     * Name: isSorted     
     * Parameters: None     
     * Output: Returns whether the Linked Int List is sorted by increasing     
     *         order.     
     * Exceptions: Linked Int List cannot be looping.  
     */
    public boolean isSorted() {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (isEmpty()) {
            return true;
        }
        ListNode current = front;
        int currentInt = front.data;
        while (current.next != null) {
            //get next node            
            current = current.next;
            //if last node is greater than current, false             
            if (currentInt > current.data) {
                return false;
            }
            currentInt = current.data;
        }
        return true;
    }

    /*     
     * Name: isLooping
     * Parameters: None     
     * Output: Returns whether the Linked Int List is looping.     
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

    /*     
     * Name: getBiggest     
     * Parameters: None     
     * Output: Returns the biggest number.     
     * Exceptions: Linked Int List cannot be empty. 
     *             Linked Int List cannot be looping.      
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

    /*     
     * Name: getSmallest     
     * Parameters: None     
     * Output: Returns the smallest number.     
     * Exceptions: Linked Int List cannot be empty.     
     *             Linked Int List cannot be looping.  
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

    /*     
     * Name: getRange     
     * Parameters: None     
     * Output: Returns the difference between the smallest and biggest number     
     *         in the Linked Int List.     
     * Exceptions: Linked Int List cannot be empty.     
     *             Linked Int List cannot be looping.  
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

    /*     
     * Name: getTotalSum     
     * Parameters: None     
     * Output: Returns the sum of all node's data.  
     * Exceptions: Linked Int List cannot be looping.     
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

    /*     
     * Name: getNode     
     * Parameters: Index of node you want to receive.     
     * Output: Returns node at index.     
     * Exceptions: Linked Int List cannot be empty.     
     *             Index has to be greater than -1
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

    /*     
     * Name: getFront     
     * Parameters: None     
     * Output: Returns node at front     
     */
    public ListNode getFront() {
        return front;
    }

    /*     
     * Name: getLast
     * Parameters: None     
     * Output: Returns node at end     
     * Exceptions: It doesn't work if Linked Int List is empty.  
     *             Linked Int List cannot be looping.     
     */
    public ListNode getLast() {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (front == null) {
            throw new IllegalStateException("Linked Int List can't be empty.");
        }
        ListNode current = front;
        while (current.next != null) {
            current = current.next;
        }
        return current;
    }

    /*     
     * Name: getMiddle     
     * Parameters: None     
     * Output: Returns node at middle     
     * Exceptions: It doesn't work if Linked Int List is empty.   
     *             Linked Int List cannot be looping.    
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

    /*     
     * Name: getMiddleTwo     
     * Parameters: None     
     * Output: Returns the two nodes in the middle     
     * Exceptions: It doesn't work if Linked Int List is empty.     
     *             Can't get middle two of an odd numbered list.    
     *             Linked Int List cannot be looping.  
     */
    public ListNode[] getMiddleTwo() {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (isEmpty()) {
            throw new IllegalStateException("Linked Int List cannot be empty.");
        }
        if (size() % 2 == 1) { 
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

    /*
     * Name: getSubList
     * Parameters: Two indexes (start and end)
     * Output: Returns a new LinkedIntList from index1 to index2 inclusive.
     * Exceptions: index1 has to be less than or equal to index2.
     *             index1 has to be greater than or equal to 0.
     *             index1 and index2 have to be less than the size of the list.
     */
    public LinkedIntList getSubList(int index1, int index2) {
        if (index1 > index2) {
            throw new IllegalArgumentException("index1 has to be less than or equal to index2");
        }
        if (index1 < 0) {
            throw new IllegalArgumentException("index1 has to be greater than or equal to 0");
        }
        int size = size();
        if (index1 >= size || index2 >= size) {
            throw new IllegalArgumentException("index1 and index2 have to be less than the size of the list");
        }
        ListNode start = getNode(index1);
        LinkedIntList subList = new LinkedIntList(new ListNode(start.data));
        int currentIndex = index1;
        ListNode current = start.next;
        ListNode currentOther = subList.front;
        while (currentIndex < index2) {
            currentOther.next = new ListNode(current.data);
            current = current.next;
            currentIndex++;
            currentOther = currentOther.next;
        }
        return subList;
    }

    /*
     * Name: rotate
     * Parameters: An integer x
     * Output: Rotates the entire list to the right by x positions.
     */
    public void rotate(int x) {
        if (front == null || front.next == null) {
            return;
        }
        if (x==0) {
            return;
        }
        int size = 1;
        ListNode current = front;
        while (current.next != null) {
            current = current.next;
            size++;
        }
        current.next = front;
        int times = x%size;
        if (x < 0) {
        times += size;
        }
        size = size - times;

        int index = 0;
        current = front; //it starts at front
        while (index != times-1) {
            index++;
            current = current.next;
        }
        front = current.next;
        current.next = null;
    }

    /*     
     * Name: removeDuplicates     
     * Parameters: None     
     * Output: Removes all duplicate values from the Linked Int List.     
     *         Returns the number of removed nodes.     
     * Exceptions: Linked Int List cannot be looping.  
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
        return removedCount;
    }

    /*     
     * Name: setLooping     
     * Parameters: None     
     * Output: Sets the Linked Int List to be looping by connecting the last     
     *         node to the front node.     
     */
    public void setLooping() {
        isLooping = true;
        getLast().next = front;
    }

    public LinkedIntList[] splitAt(int index) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (index < 0 || index >= size()) {
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

    /*     
     * Name: reverse     
     * Parameters: None     
     * Output: Reverse order of Linked Int List.     
     * Exceptions: Linked Int List cannot be looping.  
     */
    public void reverse() {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        switch (size()) {
            case 0:
                return;
            case 1:
                return;
            case 2:
                front.next.next = front;
                ListNode hold = front;
                front = front.next;
                hold.next = null;
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
    }

    /*
     * Name: clone
     * Parameters: None
     * Output: Returns a full copy of the Linked Int List.
     * Exceptions: Linked Int List cannot be looping.
     */
    public LinkedIntList clone() {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (front == null) return new LinkedIntList();

        LinkedIntList list = new LinkedIntList(new ListNode(front.data));
        ListNode currentOther = front.next;
        ListNode current = list.front;
        while (currentOther!=null) {
            current.next = new ListNode(currentOther.data);
            currentOther = currentOther.next;
            current = current.next;
        }
        return list;
    }

    /*     
     * Name: addSorted     
     * Parameters: Value of the node you want to insert     
     * Output: Inserts a node in the place it needs to be if sorted.     
     * Exceptions: List has to be sorted in the first place.     
     *             Linked Int List cannot be looping.  
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
            return;
        }
        if (value < front.data) {
            front = new ListNode(value, front);
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
        }
    }

    /*
     * Name: addAll
     * Parameters: Other linkedIntList you want to add all values of
     * Output: Copies other Linked Int List to end of the current list.
     * Exceptions: Other List cannot be null.
     *             Cannot add a looping list.
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
            ListNode last = getLast();
            ListNode current = other.front;
            while (current != null) {
                last.next = new ListNode(current.data);
                last = last.next;
                current = current.next;
            }
        } else {
            front = new ListNode(other.front.data);
            ListNode current = this.front;
            ListNode currentOther = other.front.next;
            while (currentOther != null) {
                current.next = new ListNode(currentOther.data);
                current = current.next;
                currentOther = currentOther.next;
            }
        }
    }

    /*     
     * Name: swap    
     * Parameters: Two indexes of the nodes you want to switch.
     * Output: Switches the positions of the nodes 
     * Exceptions: Node indexes have to be within range of the Linked Int List.
     *             Linked Int List cannot be looping.  
     */
    public void swap(int index1, int index2) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        int end = size();
        if (
            (
                (index1 < 0) || (index1 >= end)
            ) || (
                (index2 < 0) || (index2 >= end)
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
            // Case 1: Swapping adjacent nodes where one is at index 0
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
            // Case 2: Swapping non-adjacent nodes where one is at index 0
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
            // Case 3: Swapping adjacent nodes (neither at index 0)
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
            // Case 4: Swapping non-adjacent nodes (neither at index 0)
            prev1.next = node2;
            prev2.next = node1;
            ListNode temp = node1.next;
            node1.next = node2.next;
            node2.next = temp;
        }
    }

    /*     
     * Name: merge     
     * Parameters: A LinkedIntList     
     * Output: Appends the other list to the end of the current one     
     * Exceptions: Other List cannot be null. 
     *             Neither Linked Int List can be looping.    
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
            return;
        }
        getLast().next = other.front;
    }

    /*     
     * Name: mergeSorted     
     * Parameters: A LinkedIntList     
     * Output: Merges both lists together in sorted order. 
     * Exceptions: Other List cannot be null.     
     *             Both lists have to be sorted.     
     *             Neither List can be looping.   
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
            return;  // nothing to merge
        }
        if (front == null) {
            front = other.front;
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
    }

    /*     
     * Name: indexOf    
     * Parameters: Value you are searching for.
     * Output: Returns the first index of the value
     * Exceptions: Linked Int List cannot be looping 
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

    /*     
     * Name: listIndexesOf   
     * Parameters: Value you are searching for.
     * Output: Returns all indexes of the value
     * Exceptions: Linked Int List cannot be looping 
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

    /*     
     * Name: lastIndexOf    
     * Parameters: Value you are searching for.
     * Output: Returns the last index of the value
     * Exceptions: Linked Int List cannot be looping 
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

    /*     
     * Name: get  
     * Parameters: Index of node you want to receive.     
     * Output: Returns node at index.     
     * Exceptions: Linked Int List cannot be empty.     
     *             Index has to be greater than -1
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

    /*     
     * Name: set  
     * Parameters: Index and value of node you want to set  
     * Output: Returns node at index.     
     * Exceptions: Linked Int List cannot be empty.     
     *             Index has to be greater than -1
     *             Index has to be reachable (non-looping)
     */
    public void set(int index, int value) {
        getNode(index).data = value;
    }

    /*     
     * Name: addLast     
     * Parameters: Value of the node you want to insert     
     * Output: Attachs a new node at the end of the Linked Int List with specified     
     *         value to the previously last node. 
     * Exceptions: Linked Int List cannot be looping.       
     */
    public void addLast(int value) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        ListNode current = front;
        if (isEmpty()) {
            front = new ListNode(value);
            return;
        }
        while (current.next != null) {
            current = current.next;
        }
        current.next = new ListNode(value);
    }

    /*     
     * Name: addFront     
     * Parameters: Value of the node you want to insert     
     * Output: Adds a new node at the start of the Linked Int List with specified     
     *         value and attaches the previously first node to the new first.     
     */
    public void addFront(int value) {
        front = new ListNode(value, front);
    }

    /*     
     * Name: addPenultimate
     * Parameters: Value of the node you want to insert
     * Output: Adds in the position of the second to last node.
     * Exceptions: Linked Int List has to have at least 2 nodes.  
     *             Linked Int List cannot be looping.   
     */
    public void addPenultimate(int value) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (size() < 2) {
            throw new IllegalStateException("List has to be at least 2 nodes long.");
        }
        
        ListNode current = front;
        while (current.next.next != null) {
            current = current.next;
        }
        current.next = new ListNode(value,current.next);
    }

    /*     
     * Name: addIndex     
     * Parameters: Index of the node you want to add ( 0 = first)     
     *        Value of the node you want to insert     
     * Output: Connects the node at the index with specificed value specificed      
     *         and connects it between the node before and after. If the index      
     *         is 0 or the size of the Linked Int List, use premade functions.     
     * Exceptions: Index has to be within range of the Linked Int List. 
     *             Linked Int List cannot be looping.       
     */
    public void addIndex(int index, int value) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (index > size() || index < 0) {
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
    }
    
    /*     
     * Name: removeFront     
     * Parameters: None     
     * Output: Disconnects the first node and attach the second one. Returns 
     *         the data of the lost node.  
     * Exceptions: List cannot be empty when removing.     
     */
    public int removeFront() {
        if (isEmpty()) {
            throw new NoSuchElementException("List cannot be removed from further.");
        }
        int data = front.data;
        front = front.next;
        return data;
    }

    /*     
     * Name: removeLast     
     * Parameters: None     
     * Output: Disconnects the last node. Returns the data of the lost node. 
     * Exceptions: List cannot be empty when removing.  
     *             Linked Int List cannot be looping.      
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
        return data;
    }

    /*     
     * Name: remove   
     * Parameters: None  
     * Output: Disconnects the node at the front
     */
    public int remove() {
        return removeFront();
    }

    /*     
     * Name: remove   
     * Parameters: Index of the node you want to remove ( 0 = first)  
     * Output: Disconnects the node at the index specificed and connects the gap     
     *         between the node before and after. If the index is 0 or the      
     *         size of the Linked Int List, use premade functions. Returns the
     *         data of the lost node.    
     * Exceptions: Index has to be within range of the Linked Int List.     
     *             Linked Int List cannot be looping.   
     */
    public int remove(int index) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (index >= size() || index < 0) {
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
        return data;
    }

    /*     
     * Name: removePenultimate
     * Parameters: None
     * Output: Removes the second to last node.
     * Exceptions: Linked Int List has to have at least 2 nodes.  
     *             Linked Int List cannot be looping.   
     */
    public int removePenultimate() {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (size()-2 < 0) {
            throw new IllegalStateException("List has to be at least 2 nodes long.");
        }
        return remove(size()-2);
    }

    /*     
     * Name: removeAll
     * Parameters: Value of the nodes you want to remove
     * Output: Removes all instances of nodes who's data is equal to the
     *         parameter given. Returns amount of nodes removed.
     * Exceptions: Linked Int List cannot be empty.   
     *             Linked Int List cannot be looping.   
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
        }

        if (front == null) {
            return count;
        }

        ListNode current = front;
        while (current.next != null) {
            if (current.next.data == value) {
                current.next = current.next.next;
                count++;
            } else {
                current = current.next;
            }
        }

        return count;
    }

    /*     
     * Name: removeFirstValue
     * Parameters: Value of the node you want to remove
     * Output: Removes the first instance of a node who's data is equal to the
     *         parameter given.
     * Exceptions: Linked Int List cannot be empty.   
     *             Linked Int List cannot be looping.   
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
            return;
        }

        ListNode current = front;
        while (current.next != null) {
            if (current.next.data == value) {
                current.next = current.next.next;
                return;
            } else {
                current = current.next;
            }
        }
    }

    /*
     * Name: removeRange
     * Parameters: Start and end index of the range you want to remove
     * Output: Removes all nodes in the specified range (inclusive).
     * Exceptions: Linked Int List cannot be empty.
     *             Linked Int List cannot be looping.
     */
    public void removeRange(int start, int end) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        if (start < 0 || end < start || end >= size()) {
            throw new IllegalArgumentException("Invalid start or end index.");
        }

        if (start == 0) {
            ListNode current = front;
            for (int i = 0; i <= end; i++) {
                current = current.next;
            }
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
    }

    /*     
     * Name: clear
     * Parameters: None
     * Output: Makes list empty.
     */
    public void clear() {
        front = null;
    }

    /*
     * Name: toArray
     * Parameters: None
     * Output: Returns the Linked Int List in the form of an array.
     * Exceptions: Linked Int List cannot be looping.
     */
    public int[] toArray() {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        int[] array = new int[size()];
        int index = 0;
        ListNode current = front;
        while(current != null) {
            array[index] = current.data;
            index++;
        }
        return array;
    }

    /*     
     * Name: countAmount  
     * Parameters: Value you want to check for 
     * Output: Returns the amount of nodes who's values are equal to your value.
     * Exceptions: Linked Int List cannot be looping.   
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

    /*     
     * Name: countIf  
     * Parameters: IntPredicate you want to check for 
     * Output: Returns the amount of nodes that satisfy the predicate.
     * Exceptions: Linked Int List cannot be looping.   
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

    /*
     * Name: findFirst
     * Parameters: IntPredicate you want to check for
     * Output: Returns the first node that satisfies the predicate.
     * Exceptions: Linked Int List cannot be looping.
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

    /*     
     * Name: allMatch  
     * Parameters: IntPredicate you want to check for 
     * Output: Returns true if all nodes satisfy the predicate, false otherwise.
     * Exceptions: Linked Int List cannot be looping.   
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

    /*     
     * Name: anyMatch  
     * Parameters: IntPredicate you want to check for 
     * Output: Returns true if any node satisfies the predicate, false otherwise.
     * Exceptions: Linked Int List cannot be looping.   
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

    /*     
     * Name: stutter    
     * Parameters: None  
     * Output: Copies list and stutters it by one.
     * Exceptions: Linked Int List cannot be looping.   
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
    }

    /*     
     * Name: sort   
     * Parameters: None     
     * Output: Sorts the list by lowest to highest  
     * Exceptions: Linked Int List cannot be looping.   
     */
    public void sort() {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }
        this.front = sort(size(), front).front;
    }

    /*     
     * Name: sort (private recursion-based helper)  
     * Parameters: size of chunk, front of chunk  
     * Output: First divides chunk into single bits, and reorganizes those bits.  
     * Exceptions: size cannot be 0
     *             tip cannot be null  
     */
    private LinkedIntList sort(int size, ListNode tip) {
        if (size == 0) {
            throw new IllegalStateException("Chunk size cannot be less than 1.");
        }
        if (tip == null) {
            throw new IllegalStateException("Chunk front cannot be null.");
        }
        if (size == 1) {
            tip.next = null;  // isolate this single node
            return new LinkedIntList(tip);
        }
        
        ListNode a = tip;
        ListNode current = tip;
        
        // Stop one node BEFORE the midpoint
        for (int i = 0; i < size/2 - 1; i++) {
            current = current.next;
        }
        
        ListNode b = current.next;  // second half starts here
        current.next = null;        // SEVER the link between halves
        
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

    /*     
     * Name: iterator
     * Parameters: None     
     * Output: Used for for-each loops and such.
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

    /*
    * Name: retainAll
    * Parameters: IntPredicate you want to check for
    * Output: Returns true if all nodes satisfy the predicate, false otherwise.
    * Exceptions: Linked Int List cannot be looping.
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
               // Remove current node
               if (prev == null) {
                   front = current.next;
               } else {
                   prev.next = current.next;
               }
               changed = true;
           } else {
               prev = current;
           }
           current = current.next;
       }
       return changed;
   }

   /*
    * Name: mergeCommon
    * Parameters: A LinkedIntList
    * Output: Modifies the current list to retain only elements present in both lists.
    * Exceptions: Other List cannot be null.
    *             Neither Linked Int List can be looping.
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
        }

        ListNode current = front;
        while (current.next != null) {
            if (!otherValues.contains(current.next.data)) {
                current.next = current.next.next;
            } else {
                current = current.next;
            }
        }
    }

    /*
    * Name: removeAnd
    * Parameters: A LinkedIntList
    * Output: Modifies the current list to remove elements present in the other list.
    * Exceptions: Other List cannot be null.
    *             Neither Linked Int List can be looping.
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
        }

        ListNode current = front;
        while (current.next != null) {
            if (otherValues.contains(current.next.data)) {
                current.next = current.next.next;
            } else {
                current = current.next;
            }
        }
    }

    /*
    * Name: intersection
    * Parameters: A LinkedIntList
    * Output: Returns a new LinkedIntList containing elements common to both lists.
    * Exceptions: Other List cannot be null.
    *             Neither Linked Int List can be looping.
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
            }
            current = current.next;
        }
        result.front = hold.next;
        return result;
    }

    /*     
     * Name: partition  
     * Parameters: IntPredicate
     * Output: Partitions the list into two lists based on the predicate.
     * Exceptions: Linked Int List cannot be looping.
     *             Output lists cannot be null.   
     */
    public void partition(IntPredicate predicate) {
        if (isLooping) {
            throw new IllegalStateException("Linked Int List cannot be looping.");
        }

        //First part of list does not satisfy predicate
        LinkedIntList list1 = new LinkedIntList();
        //Second part of list satisfies predicate
        LinkedIntList list2 = new LinkedIntList();
        ListNode current = front;       
        while (current != null) {
            if (predicate.test(current.data)) {
                list2.addLast(current.data);
            } else {
                list1.addLast(current.data);
            }
            current = current.next;
        }
        list1.merge(list2);
        front = list1.front;
    }

    /*     
    * Name: equals
    * Parameters: Object obj - the object to compare with this LinkedIntList     
    * Output: Returns true if obj is a LinkedIntList with the same value sequence,
    *         false otherwise. For looping lists, checks that loops occur at 
    *         corresponding positions in the value sequence.
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
        
        // Initialize pointers to traverse both lists
        ListNode current = front;
        ListNode currentOther = other.front;
        
        // Handle looping lists
        if (isLooping && other.isLooping) {
            // Track visit counts for each node to detect when we complete one cycle
            HashMap<ListNode, Integer> visited = new HashMap<>();
            HashMap<ListNode, Integer> visitedOther = new HashMap<>();
            
            while (true) {
                // Record visits to current nodes
                visited.put(current, visited.getOrDefault(current, 0) + 1);
                visitedOther.put(currentOther, visitedOther.getOrDefault(currentOther, 0) + 1);
                
                // Check if both lists loop back at the same relative position
                if (visited.get(current) != visitedOther.get(currentOther)) {
                    return false; // Loops occur at different points in the sequence
                }
                
                // Check if values match at current position
                if (current.data != currentOther.data) {
                    return false;
                }
                
                // Stop after completing one full cycle in both lists
                if (visited.get(current) == 2) {
                    break;
                }
                
                // Advance to next nodes
                current = current.next;
                currentOther = currentOther.next;
            }
        } else {
            // Handle non-looping lists
            // Traverse both lists simultaneously, comparing values
            while (current != null && currentOther != null) {
                if (current.data != currentOther.data) {
                    return false;
                }
                current = current.next;   
                currentOther = currentOther.next;
            }
            
            // Ensure both lists ended at the same time (same length)
            if (current != null || currentOther != null) {
                return false;
            }
        }
        
        return true;
    }

    /*     
     * Name: toString     
     * Parameters: None     
     * Output: Returns an easy to understand format of the contents of the     
     *         Linked Int List.  
     * Exceptions: Linked Int List cannot be looping.   
     */
    @Override public String toString() {
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