public class Solution {
            String str= "good";
            char [] ch = {'a', 'b', 'c'};
            public static void main (String[] args) {
                Solution ex = new Solution();
                ex.change(ex.str, ex.ch);
                System.out.println(ex.str + "and");
                System.out.println(ex.ch);
            }

            public void change(String str, char[] ch) {
                str =  "test ok";
                ch[0] = 'g';
            }
}
