class PG_150367 {
    public int[] solution(long[] numbers) {
        int[] answer = new int[numbers.length];

        for (int i = 0; i < numbers.length; i++) {
            if (isPossible(numbers[i])) {
                answer[i] = 1;
            } else {
                answer[i] = 0;
            }
        }

        return answer;
    }

    private boolean isPossible(long num) {
        String binary = toFullBinaryString(num);
        int nodeCnt = binary.length();
        int rootIdx = nodeCnt / 2;
        if (binary.charAt(rootIdx) == '0') {
            return false;
        }
        String leftSub = binary.substring(0, rootIdx);
        String rightSub = binary.substring(rootIdx + 1);

        return subTreePossible(leftSub) && subTreePossible(rightSub);
    }

    private boolean subTreePossible(String sub) {
        int nodeCnt = sub.length();
        if (nodeCnt == 0) {
            return true;
        }
        int rootIdx = nodeCnt / 2;
        if (sub.charAt(rootIdx) == '0') {
            return isAllZero(sub);
        }
        String leftSub = sub.substring(0, rootIdx);
        String rightSub = sub.substring(rootIdx + 1);

        return subTreePossible(leftSub) && subTreePossible(rightSub);
    }

    private boolean isAllZero(String sub) {
        for (int i = 0; i < sub.length(); i++)  {
            if (sub.charAt(i) == '1') {
                return false;
            }
        }
        return true;
    }

    private String toFullBinaryString(long num) {
        String binaryString = Long.toBinaryString(num);
        int len = binaryString.length();
        int cnt = 1;
        int level = 1;
        while (len > cnt) {
            level *= 2;
            cnt += level;
        }
        return "0".repeat(cnt - len) + binaryString;
    }
}