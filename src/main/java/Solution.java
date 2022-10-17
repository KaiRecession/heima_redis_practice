public class Solution {
    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            ThreadLocal<Integer> tl1 = set(1);
            ThreadLocal<Integer> tl2 = set(2);
            System.out.println(tl1.get());
            System.out.println(tl2.get());
        });
        t1.start();
        Thread t2 = new Thread(() -> {
            ThreadLocal<Integer> tl1 = set(3);
            ThreadLocal<Integer> tl2 = set(4);
            System.out.println(tl1.get());
            System.out.println(tl2.get());
        });
        t2.start();
    }

    public static ThreadLocal<Integer> set(Integer num) {
        ThreadLocal<Integer> threadLocal = new ThreadLocal();
        threadLocal.set(num);
        return threadLocal;
    }
}
