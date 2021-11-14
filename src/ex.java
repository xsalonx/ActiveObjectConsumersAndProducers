import java.util.LinkedList;

public class ex {

    static private void print(LinkedList<Integer> llist) {
        while (!llist.isEmpty()){
            System.out.print(llist.pop() + " ");
        }
        System.out.print("");
    }

    static public void main(String[] args) {
        LinkedList<Integer> llist = new LinkedList<>();
        for (int i=0; i<5; i++) {
            llist.addLast(i);
        }

        print(llist);
    }
}
