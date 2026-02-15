import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;

//  コンストラクタでパスとサイズを指定して好みのフォントを設定できるクラス

public class FontLoader {
    public static Font loadFont(String path, float size) {
        try {
            java.io.InputStream fontFile = FontLoader.class.getResourceAsStream(path);
            if (fontFile == null) {
                throw new IOException("Font file not found: " + path);      //  ファイルが見つからなかった場合
            }
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(baseFont);
            
            return baseFont.deriveFont(Font.BOLD, size); 

        } catch (FontFormatException | IOException e) {
            System.err.println("フォント読み込みエラー: " + path);
            e.printStackTrace();
            // エラー時はデフォルトのフォントを返す
            return new Font("Monospaced", Font.BOLD, (int)size); 
        }
    }
}