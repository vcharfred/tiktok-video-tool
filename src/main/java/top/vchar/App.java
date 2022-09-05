package top.vchar;

import javax.swing.*;
import java.awt.*;

/**
 * <p> 开始类 </p>
 *
 * @author vchar fred
 * @version 1.0
 * @create_date 2020/11/23
 */
public class App {

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            JFrame frame = new JFrame("抖音视频下载");
            frame.setSize(800, 500);

            //获取系统窗口大小
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = toolkit.getScreenSize();
            int screenWidth = screenSize.width;
            int screenHeight = screenSize.height;
            frame.setLocation(screenWidth / 3, screenHeight / 3);
            frame.setContentPane(new AppUI().panel());

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }

}