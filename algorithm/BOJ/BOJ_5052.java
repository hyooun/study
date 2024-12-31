import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BOJ_5052 {

    static class Node {
        Map<Character, Node> child;

        public Node() {
            child = new HashMap<>();
        }
    }

    static class Trie {
        Node root;

        public Trie() {
            root = new Node();
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
        StringBuilder sb = new StringBuilder();
        int t = Integer.parseInt(br.readLine());

        for (int tc = 1; tc <= t; tc++) {
            int n = Integer.parseInt(br.readLine());
            Trie trie = new Trie();
            ArrayList<String> pnList = new ArrayList<>();

            for (int i = 0; i < n; i++) {
                pnList.add(br.readLine());
            }

            pnList.sort((o1, o2) -> {
                return o2.length() - o1.length();
            });

            boolean isDuplicated = false;
            for (String pn : pnList) {
                if (!trie.search(pn)) {
                    trie.insert(pn);
                } else {
                    isDuplicated = true;
                    break;
                }
            }
            if (isDuplicated) {
                sb.append("NO").append('\n');
            } else {
                sb.append("YES").append('\n');
            }
        }
        System.out.println(sb);
    }
}
