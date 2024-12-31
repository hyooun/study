import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

public class BOJ_7432 {

    static class Node {
        Map<String , Node> child;

        private Node() {
            child = new TreeMap<>();
        }
    }

    static class Trie {
        Node root;

        private Trie() {
            root = new Node();
        }

        private void insert(String str) {
            String[] dirs = str.split("\\\\");
            Node node = this.root;
            for (String dir : dirs) {
                node.child.putIfAbsent(dir, new Node());
                node = node.child.get(dir);
            }
        }

        private void dfs(Node node, StringBuilder sb, int depth) {
            for (Map.Entry<String, Node> entry : node.child.entrySet()) {
                for (int i = 0; i < depth; i++) {
                    sb.append(' ');
                }
                sb.append(entry.getKey()).append('\n');
                dfs(entry.getValue(), sb, depth + 1);
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            dfs(this.root, sb, 0);
            return sb.toString();
        }
    }

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int n = Integer.parseInt(br.readLine());

        Trie trie = new Trie();
        for (int i = 0; i < n; i++) {
            trie.insert(br.readLine());
        }

        System.out.println(trie);
    }
}
