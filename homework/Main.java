package homework;

public class Main {
    public static void main(String[] args) {
        ListNode node1 = new ListNode(6);
        ListNode node2 = new ListNode(2);
        ListNode node3 = new ListNode(3);
        ListNode node4 = new ListNode(4);
        ListNode node5 = new ListNode(6);
        ListNode node6 = new ListNode(6);
        ListNode node7 = new ListNode(3);
        ListNode node8 = new ListNode(6);

        node1.next = node2;
        node2.next = node3;
        node3.next = node4;
        node4.next = node5;
        node5.next = node6;
        node6.next = node7;
        node7.next = node8; 

        LinkedIntList list = new LinkedIntList(node1);
        System.out.println(list);
        //list.sort();
        list.removeAll(6);
        System.out.println(list);
        list.swap(2,3);
        System.out.println(list);

        for (int s : list) {
            System.out.println(s);
        }
    }
}
