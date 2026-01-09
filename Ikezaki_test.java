
import java.io.*;

public class Ikezaki_test {
    public static void main(String[] args) {
        try (FileOutputStream fos = new FileOutputStream("example.bin");
                DataOutputStream dos = new DataOutputStream(fos)) {

            // 整数値を書き込み
            dos.writeInt(31);
            dos.writeInt(42);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (FileInputStream fis = new FileInputStream("example.bin");
                DataInputStream dis = new DataInputStream(fis)) {

            // 整数値を読み取り
            int value = dis.readInt();
            value = dis.readInt();
            System.out.println("Read integer: " + value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}