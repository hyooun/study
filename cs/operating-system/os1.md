### 운영체제의 목표
- 환경 관리(Envinronment management) : 프로그램을 실행하고 문제를 더 쉽게 해결하게 함
- 프로세스 관리(Process management) : 컴퓨터 시스템을 사용하기 편하게 함
- 자원 관리(Resource management) : 컴퓨터 하드웨어 자원을 효율적인 방법으로 사용하게 함
    - SW : 프로세스, 메모리 할당, 파일, 소켓 등
    - HW : 저장 장치, 그래픽 카드, 기타 주변 기기 등
 
### 컴퓨터 시스템 구조
크게 4가지 컴포넌트로 나눌 수 있음
- 하드웨어
    - 기초적인 컴퓨팅 자원 제공 (CPU, 메모리, I/O 디바이스)
- 운영체제
    - 다양한 애플리케이션과 사용자로부터 하드웨어 사용을 조정 및 제어
- 애플리케이션
    - system resources 를 사용하여 사용자의 computing problems를 해결하는 방법을 정의 (워드 프로세서, 컴파일러, 웹 브라우저, DBMS, 게임 등)
- 사용자
    - 사람, machines, 다른 컴퓨터 등

### 운영체제가 하는 일
관점에 따른 분류
- 사용자 : 편의성, 사용하기 쉽고 좋은 성능
- mainframe이나 mimicomputer같은 shared computer : 모든 사용자의 만족
- 모바일 기기 : resource가 부족하기 때문에 사용성(터치 스크린, 음성 인식)등과 배터리 수명에 최적화
- UI가 작거나 없는 몇몇 embedded 시스템 : 사용자 개입 없이 실행됨.

> ### ✔️ 운영체제에 대한 완벽한 정의는 없지만, 다음과 같이 정의해볼 수 있다.
> 운영체제는 하드웨어를 효율적으로 사용하고 사용자 프로그램의 실행을 관리하는 자원 할당자이자 제어 프로그램이다.

### Computer System Organization
- 하나 이상의 CPU와 디바이스 컨트롤러들이 shared memory에 접근을 제공하는 system bus를 통해 연결되어 있음.
- CPU와 device들이 메모리 사이클을 두고 경쟁하며 동시에 실행됨.
![image](https://github.com/user-attachments/assets/60a5ccce-a56d-43e5-8cc8-f5810fa55c0b)


### Computer System Operations
- I/O 디바이스들과 CPU는 동시에(concurrently) 실행될 수 있다.
- 각 디바이스 컨트롤러는 특정 디바이스 타입을 담당한다.
- 각 디바이스 컨트롤러는 local buffer를 지닌다.
- 각 디바이스 컨트롤러 타입은 이를 관리하기 위한 운영체제인 디바이스 드라이버가 있다.
- CPU는 main memory로부터 local buffer들로(혹은 그 반대로) 데이터를 이동시킨다.
- I/O는 디바이스로부터 컨트롤러의 로컬 버퍼까지이다.
- 디바이스 컨트롤러는 operation이 끝났음을 알리기 위해 interrupt를 발생시킨다.

### Interrupt
- 운영 체제는 interrupt를 기반으로 동작한다.
- interrupt timeline
![image](https://github.com/user-attachments/assets/4f80d1d4-e823-4c38-8dea-13932f0bde0e)
- interrupt architecture는 interrupt가 발생한 명령어의 주소를 저장해야 한다.
- 운영체제는 register와 program counter를 저장하여 CPU의 상태를 보존한다.
- 어떤 유형의 interrupt가 발생했는지 확인한다.
![image](https://github.com/user-attachments/assets/49f8f93b-41b7-4b06-a1ec-856bc742ca55)
- interrupt는 interrupt service routine으로 control을 전달한다. 이는 일반적으로 모든 service routine의 주소를 포함하는 interrupt vector를 통해 이루어진다.
- 구분된 code의 segment에 따라 각 종류의 interrupt에 대해 어떤 작업을 수행할지 결정한다.
