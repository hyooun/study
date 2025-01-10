import java.util.*;

class PG_42892 {

    static int preorderIdx;
    static int postorderIdx;

    static class Node {
        int x;
        int y;
        int num;
        Node left;
        Node right;

        public Node(int x, int y, int num) {
            this.x = x;
            this.y = y;
            this.num = num;
        }
    }

    public int[][] solution(int[][] nodeinfo) {
        int[][] answer = new int[2][nodeinfo.length];
        PriorityQueue<Node> pq = new PriorityQueue<>((o1, o2) -> {
            if (Integer.compare(o2.y, o1.y) != 0) {
                return Integer.compare(o2.y, o1.y);
            }
            return Integer.compare(o1.x, o2.x);
        });
        for (int i = 0; i < nodeinfo.length; i++) {
            pq.offer(new Node(nodeinfo[i][0], nodeinfo[i][1], i+1));
        }

        Node root = pq.poll();

        while (!pq.isEmpty()) {
            Node node = pq.poll();

            Node now = root;
            while (now.y > node.y) {
                if (node.x < now.x) {
                    if (now.left == null) {
                        break;
                    }
                    now = now.left;
                } else {
                    if (now.right == null) {
                        break;
                    }
                    now = now.right;
                }
            }
            if (node.x < now.x && now.left == null) {
                now.left = node;
            } else if (node.x > now.x && now.right == null) {
                now.right = node;
            }
        }

        preorder(root, answer);
        postorder(root, answer);

        return answer;
    }

    public static void preorder(Node node, int[][] answer) {
        if (node == null) {
            return;
        }
        answer[0][preorderIdx++] = node.num;
        preorder(node.left, answer);
        preorder(node.right, answer);
    }

    public static void postorder(Node node, int[][] answer) {
        if (node == null) {
            return;
        }
        postorder(node.left, answer);
        postorder(node.right, answer);
        answer[1][postorderIdx++] = node.num;
    }
}