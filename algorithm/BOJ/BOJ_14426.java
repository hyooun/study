import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class BOJ_14426 {

    static int n, m;

    static class Node {
        Map<Character, Node> child;

        public Node() {
            this.child = new HashMap<>();
        }
    }

    static class Trie {
        Node root;

        public Trie() {
            this.root = new Node();
        }

        private void insert(String str) {
            Node node = this.root;

            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                node.child.putIfAbsent(c, new Node());
                node = node.child.get(c);
            }
        }

        private boolean search(String str) {
            Node node = this.root;

            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (node.child.containsKey(c)) {
                    node = node.child.get(c);
                } else {
                    return false;
                }
            }
            return true;
        }
    }

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());

        n = Integer.parseInt(st.nextToken());
        m = Integer.parseInt(st.nextToken());

        Trie trie = new Trie();

        for (int i = 0; i < n; i++) {
            trie.insert(br.readLine());
        }

        int cnt = 0;
        for (int i = 0; i < m; i++) {
            if (trie.search(br.readLine())) {
                cnt++;
            }
        }
        System.out.println(cnt);
    }
}
