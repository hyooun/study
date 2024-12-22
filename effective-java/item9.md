# 아이템 9. try-finally보다는 try-with-resources를 사용하라
자바 라이브러리에는 InputStream, OutputStream, java.sql.Connection 처럼 close 메서드를 호출해 직접 닫아줘야 하는 자원이 많다.
close를 클라이언트가 놓칠 경우 예측할 수 없는 성능 문제로 이어지기도 한다.
이런 자원 중 상당수가 finalizer를 안전망으로 사용하고는 있지만 그리 믿음직하지 않다. ([아이템 8](item8.md))

## try-finally
```java
static String firstLineOfFile(String path) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(path));
    try {
        return br.readLine();
    } finally {
        br.close();
    }
}
```
전통적으로 자원에 대한 close를 보장하는 방식으로 try-finally 방식이 많이 쓰였다.
하지만 이는 자원이 늘어나는 경우 코드가 난잡해진다. 
```java
static void copy(String src, String dst) throws IOException {
    InputStream in = new FileInputStream(src);
    try {
        OutputStream out = new FileOutputStream(dst);
        try {
            byte[] buf = new byte[BUFFER_SIZE];
            int n;
            while((n = in.read(buf)) >= 0)
                out.write(buf, 0, n);
        } finally {
            out.close();
        }
    } finally {
        in.close();
    }
}
```
이 두 코드에서 예외는 try와 finally 블록 모두에서 발생할 수 있는데, 기기에 물리적인 문제가 생기는 경우엔 firstLineOfFile 메서드 안의 readLine 메서드가 예외를 던지고 같은 이유로 close도 실패한다.
이런 상황이라면 두 번째 예외가 첫 번째 예외를 집어삼켜 버리므로 stacktrace에 첫 번째 예외에 대한 정보가 남지 않아 디버깅이 어려워진다.

자바 7부터 try-with-resources라는 구조로 해결하였는데, 이를 사용하기 위해서는 해당 자원이 AutoCloseable 인터페이스를 구현해야 한다.
단순히 void를 반환하는 close 메서드 하나만 덩그러니 정의한 인터페이스이다. 
자바 라이브러리와 서드파티 라이브러리들의 수많은 클래스와 인터페이스가 이미 AutoCloseable을 구현하거나 확장하고 있고 새롭게 구현할 때에도 닫아야 하는 자원이라면 AutoCloseable을 구현해야 한다.

## try-with-resources
```java
static String firstLineOfFile(String path) throws IOException {
    try (BufferedReader br = new BufferedReader(
            new FileReader(path))) {
        return br.readLine();
    }
}
```
```java
static void copy(String src, String dst) throws IOException {
    try (InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst)) {
        byte[] buf = new byte[BUFFER_SIZE];
        int n;
        while ((n = in.read(buf)) >= 0)
            out.write(buf, 0, n);
    }
}
```
이 방식으로 작성된 코드들은 짧고 읽기 수월할 뿐만 아니라 문제를 진단하기에도 훨씬 좋다.
firstLineOfFile 메서드에서도 readLine과 close 호출 양쪽에서 예외가 발생하면 close에서 발생한 예외는 숨겨지고 readLine에서 발생한 예외가 기록된다.
이렇게 숨겨진 예외들도 버려지지는 않고 stacktrace에 suppressed를 달고 출력된다.
또한 자바 7에서 Throwable에 추가된 getSupressed 메서드를 이용하면 프로그램 코드에서 가져올 수도 있다.

보통의 try-finally처럼 try-with-resources에서도 catch 절을 쓸 수 있다.
```java
static String firstLineOfFile(String path, String defaultVal) {
    try (BufferedReader br = new BufferedReader(
            new FileReader(path))) {
        return br.readLine();
    } catch (IOException e) {
        return defaultVal;
    }
}
```
