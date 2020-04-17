package org.wulin.jvm.jdk.console;

/**
 * 测试通过,必须使用命令行运行
 * @author wulin
 *
 */
public class TerminalTest{


    public static void main(String[] args) {

        for (int i = 0; i <= 100; i++) {
            printSchedule(i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 进度条总长度
     */
    private static int TOTLE_LENGTH = 30;
    public static void printSchedule(int percent){
        for (int i = 0; i < TOTLE_LENGTH + 10; i++) {
            System.out.print("\b");
        }
        //
        int now = TOTLE_LENGTH * percent / 100;
        for (int i = 0; i < now; i++) {
            System.out.print(">");
        }
        for (int i = 0; i < TOTLE_LENGTH - now; i++) {
            System.out.print("-");
        }
//        System.out.println("---------------------");
        System.out.print("等于==" + percent + "%");
    }

}
