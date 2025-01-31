import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class BOJ_6051 {
    static int n;
    static Node[] nodes;
    private static class Node {
        int num;
        Node parent;

        public Node(int num, Node parent) {
            this.parent = parent;
            this.num = num;
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        StringBuilder sb = new StringBuilder();

        n = Integer.parseInt(br.readLine());
        nodes = new Node[n + 1];

        Node now = null;

        for (int i = 1; i <= n; i++) {
            nodes[i] = now;
            st = new StringTokenizer(br.readLine());
            char c = st.nextToken().charAt(0);
            if (c == 's') {
                if (now != null) {
                    now = now.parent;
                }
            }
            else {
                int k = Integer.parseInt(st.nextToken());
                if (c == 'a') {
                    now = new Node(k, now);
                } else if (c == 't') {
                    now = nodes[k];
                }
            }
            if (now == null) {
                sb.append(-1).append('\n');
            } else {
                sb.append(now.num).append('\n');
            }
        }
        System.out.print(sb);
    }
}
