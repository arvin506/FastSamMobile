package com.example.pytochmobiledemo;

import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;

public class Solution {


    public int[] reversePrint(ListNode head) {
        ListNode h1 = new ListNode(-1);
        int cnt = 0;
        while (head != null) {
            ListNode tmp = head.next;
            head.next = h1.next;
            h1.next = head;
            head = tmp;
            cnt++;
        }
        int[] res = new int[cnt];
        head = h1.next;
        int index = 0;
        while (head!=null){
            res[index++] = head.val;
            head = head.next;
        }
        return res;
    }


}
