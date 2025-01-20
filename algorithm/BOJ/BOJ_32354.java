import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class BOJ_32354 {
    static Node[] nodes;
    static Node now;

    private static class Node {
        long sum;
        int top;
        Node parent;
        Node child;

        public Node(long sum, int top, Node parent) {
            this.sum = sum;
            this.top = top;
            this.parent = parent;
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        StringBuilder sb = new StringBuilder();

        int n = Integer.parseInt(br.readLine());
        nodes = new Node[n + 1];
        nodes[0] = new Node(0, 0, null);

        for (int i = 1; i <= n; i++) {
            st = new StringTokenizer(br.readLine());
            String s = st.nextToken();
            int k = 0;
            if (st.hasMoreTokens()) {
                k = Integer.parseInt(st.nextToken());
            }
            switch (s) {
                case "push":
                    if (now == null) {
                        now = new Node(k, k, null);
                        nodes[0].child = now;
                    } else {
                        Node node = new Node(now.sum + k, k, now);
                        now.child = node;
                        now = node;
                    }
                    break;
                case "restore":
                    now = nodes[k];
                    break;
                case "pop":
                    now = now.parent;
                    break;
                case "print":
                    if (now == null) {
                        sb.append(0).append('\n');
                    } else {
                        sb.append(now.sum).append('\n');
                    }
                    break;
            }
            nodes[i] = now;
        }
        System.out.print(sb);
    }
}
