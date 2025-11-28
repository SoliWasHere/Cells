package homework;

public class ListNode {
    public ListNode next;
    public int data;

    public ListNode(int value) {
        this.data = value;
    }

    public ListNode(int value, ListNode node) {
        this.data = value;
        this.next = node;
    }
}
