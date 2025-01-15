import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class BOJ_1938 {
    static int n;
    static char[][] board;
    // true == 가로, false == 세로
    static boolean startStatus, endStatus;
    static int scx, scy, ecx, ecy;
    static int result;

    private static class Info implements Comparable<Info> {
        int cx;
        int cy;
        boolean status;
        int cnt;

        public Info(int cx, int cy, boolean status, int cnt) {
            this.cx = cx;
            this.cy = cy;
            this.status = status;
            this.cnt = cnt;
        }

        @Override
        public int compareTo(Info o) {
            return Integer.compare(this.cnt, o.cnt);
        }
    }

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        n = Integer.parseInt(br.readLine());
        board = new char[n][n];
        ArrayList<Integer> b = new ArrayList<>();
        ArrayList<Integer> e = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            String line = br.readLine();
            for (int j = 0; j < n; j++) {
                board[i][j] = line.charAt(j);
                if (board[i][j] == 'B') {
                    b.add(i*100 + j);
                } else if (board[i][j] == 'E') {
                    e.add(i*100 + j);
                }
            }
        }
        scx = b.get(1) / 100;
        scy = b.get(1) % 100;
        startStatus = b.get(0) / 100 == scx;

        ecx = e.get(1) / 100;
        ecy = e.get(1) % 100;
        endStatus = e.get(0) / 100 == ecx;
        result = Integer.MAX_VALUE;
        bfs();
        System.out.println(result == Integer.MAX_VALUE ? 0 : result);
    }

    private static void bfs() {
        boolean[][][] visited = new boolean[n][n][2];
        Queue<Info> q = new LinkedList<>();
        q.offer(new Info(scx, scy, startStatus, 0));
        if (startStatus) {
            visited[scx][scy][1] = true;
        } else {
            visited[scx][scy][0] = true;
        }

        while (!q.isEmpty()) {
            Info now = q.poll();
            if (now.cx == ecx && now.cy == ecy) {
                result = Math.min(result, now.cnt);
                continue;
            }
            int flag = 0;
            if (now.status) flag = 1;
            if (now.cx - 1 >= 0 && !visited[now.cx - 1][now.cy][flag]){
                Info u = up(now);
                if (u != null) {
                    visited[now.cx - 1][now.cy][flag] = true;
                    q.offer(u);
                }
            }
            if (now.cx + 1 < n && !visited[now.cx + 1][now.cy][flag]) {
                Info d = down(now);
                if (d != null) {
                    visited[now.cx + 1][now.cy][flag] = true;
                    q.offer(d);
                }
            }
            if (now.cy - 1 >= 0 && !visited[now.cx][now.cy - 1][flag]) {
                Info l = left(now);
                if (l != null) {
                    visited[now.cx][now.cy - 1][flag] = true;
                    q.offer(l);
                }
            }
            if (now.cy + 1 < n && !visited[now.cx][now.cy + 1][flag]) {
                Info r = right(now);
                if (r != null) {
                    visited[now.cx][now.cy + 1][flag] = true;
                    q.offer(r);
                }
            }
            if (!visited[now.cx][now.cy][now.status ? 0 : 1]) {
                Info t = turn(now);
                if (t != null) {
                    visited[now.cx][now.cy][now.status ? 0 : 1] = true;
                    q.offer(t);
                }
            }
        }
    }

    private static Info up(Info info) {
        // 가로인 경우
        if (info.status) {
            if (info.cx - 1 < 0 || info.cy - 1 < 0 || info.cy + 1 >= n) {
                return null;
            }
            if (board[info.cx - 1][info.cy - 1] != '1' &&
                    board[info.cx - 1][info.cy] != '1' &&
                    board[info.cx - 1][info.cy + 1] != '1') {
                return new Info(info.cx - 1, info.cy, info.status, info.cnt + 1);
            }
        }
        // 세로인 경우
        else {
            if (info.cx - 2 < 0) {
                return null;
            }
            if (board[info.cx - 2][info.cy] != '1') {
                return new Info(info.cx - 1, info.cy, info.status, info.cnt + 1);
            }
        }
        return null;
    }

    private static Info down(Info info) {
        // 가로인 경우
        if (info.status) {
            if (info.cx + 1 >= n || info.cy + 1 >= n || info.cy - 1 < 0) {
                return null;
            }
            if (board[info.cx + 1][info.cy - 1] != '1' &&
                    board[info.cx + 1][info.cy] != '1' &&
                    board[info.cx + 1][info.cy + 1] != '1') {
                return new Info(info.cx + 1, info.cy, info.status, info.cnt + 1);
            }
        }
        // 세로인 경우
        else {
            if (info.cx + 2 >= n) {
                return null;
            }
            if (board[info.cx + 2][info.cy] != '1') {
                return new Info(info.cx + 1, info.cy, info.status, info.cnt + 1);
            }
        }
        return null;
    }

    private static Info left(Info info) {
        // 가로인 경우
        if (info.status) {
            if (info.cy - 2 < 0) {
                return null;
            }
            if (board[info.cx][info.cy - 2] != '1') {
                return new Info(info.cx, info.cy - 1, info.status, info.cnt + 1);
            }
        }
        // 세로인 경우
        else {
            if (info.cx + 1 >= n || info.cx - 1 < 0 || info.cy - 1 < 0) {
                return null;
            }
            if (board[info.cx - 1][info.cy - 1] != '1' &&
                    board[info.cx][info.cy - 1] != '1' &&
                    board[info.cx + 1][info.cy - 1] != '1') {
                return new Info(info.cx, info.cy - 1, info.status, info.cnt + 1);
            }
        }
        return null;
    }

    private static Info right(Info info) {
        // 가로인 경우
        if (info.status) {
            if (info.cy + 2 >= n) {
                return null;
            }
            if (board[info.cx][info.cy + 2] != '1') {
                return new Info(info.cx, info.cy + 1, info.status, info.cnt + 1);
            }
        }
        // 세로인 경우
        else {
            if (info.cx + 1 >= n || info.cx - 1 < 0 || info.cy + 1 >= n) {
                return null;
            }
            if (board[info.cx - 1][info.cy + 1] != '1' &&
                    board[info.cx][info.cy + 1] != '1' &&
                    board[info.cx + 1][info.cy + 1] != '1') {
                return new Info(info.cx, info.cy + 1, info.status, info.cnt + 1);
            }
        }
        return null;
    }

    private static Info turn(Info info) {
        // 중앙 기준 비어있는지 체크
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (info.cx + i < 0 || info.cx + 1 >= n || info.cy + j < 0 || info.cy + j >= n) {
                    return null;
                }
                if (board[info.cx + i][info.cy + j] == '1') {
                    return null;
                }
            }
        }
        return new Info(info.cx, info.cy, !info.status, info.cnt + 1);
    }
}
