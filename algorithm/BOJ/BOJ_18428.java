import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class BOJ_18428 {
    static int n;
    static char[][] board;
    static int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    static ArrayList<Integer> tList;
    static boolean flag;

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        n = Integer.parseInt(br.readLine());
        StringTokenizer st;
        board = new char[n][n];
        tList = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < n; j++) {
                board[i][j] = st.nextToken().charAt(0);
                if (board[i][j] == 'T') {
                    tList.add(i*10 + j);
                }
            }
        }
        bt(0, 0);
        if (flag) {
            System.out.println("YES");
        } else {
            System.out.println("NO");
        }
    }

    public static void bt(int depth, int start) {
        if (flag) {
            return;
        }
        if (depth == 3) {
            check();
            return;
        }
        for (int i = start; i < n * n; i++) {
            int x = i / n;
            int y = i % n;
            if (board[x][y] != 'X') {
                continue;
            }
            board[x][y] = 'O';
            bt(depth+1, start+1);
            board[x][y] = 'X';
        }
    }

    public static void check() {
        for (int t : tList) {
            int x = t / 10;
            int y = t % 10;
            for (int[] dir : dirs) {
                for (int i = 0; i < 5; i++) {
                    int nx = x + dir[0] * i;
                    int ny = y + dir[1] * i;
                    if (nx < 0 || ny < 0 || nx >= n || ny >= n || board[nx][ny] == 'O') {
                        break;
                    }
                    if (board[nx][ny] == 'S') {
                        return;
                    }
                }
            }
        }
        flag = true;
    }
}
