import java.util.*;
 class ListNode {
      int val;
     ListNode next;
      ListNode() {}
      ListNode(int val) { this.val = val; }
      ListNode(int val, ListNode next) { this.val = val; this.next = next; }
  }
class Solution {
    public static void main(String[] args) {
        String s = "123";
        System.out.println(Integer.toString(123));
        System.out.println();
        Solution solution = new Solution();
        ListNode head = new ListNode(4);
        ListNode temp = head;
        temp.next =  new ListNode(2);
        temp = temp.next;
        temp.next =  new ListNode(1);
        temp = temp.next;
        temp.next =  new ListNode(3);
        solution.sortList(head);

    }
    public ListNode sortList(ListNode head) {
        return sortList(head, null);

    }

    private ListNode sortList(ListNode head, ListNode tail) {
        if (head == tail) {
            return head;
        }
        if (head.next == tail) {
            return head;
        }

        ListNode fast = head, slow = head;
        System.out.println(head.val + "" + tail);
        // 当链表节点为偶数时，slow为靠后那个重点，取不到tail
        while (fast != tail && fast.next != tail) {
            slow = slow.next;
            fast = fast.next.next;
        }
        ListNode listNode1 = sortList(head, slow);
        ListNode listNode2 = sortList(slow, tail);
        ListNode result = merge(listNode1, listNode2);
        return result;

    }

    private ListNode merge(ListNode node1, ListNode node2) {
        ListNode dumpNode = new ListNode(0);
        ListNode temp = dumpNode, temp1 = node1, temp2 = node2;
        while (temp1 != null && temp2 != null) {
            if (temp1.val >= temp2.val) {
                temp.next = temp2;
                temp2 = temp2.next;
            } else {
                temp.next = temp1;
                temp1 = temp1.next;
            }
            temp = temp.next;
        }
        temp.next = temp1 != null ? temp1 : temp2;
        return dumpNode.next;
    }
}
