package xyz.fz.wxrobot.form;

import javax.swing.*;
import java.awt.*;

/**
 * Created by fz on 2016/6/1.
 */
public class QRCodePanel extends JPanel {

    private Image image;

    public QRCodePanel(Image image) {
        super();
        this.image = image;
    }

    @Override
    public void paintComponent(Graphics g) {
        ImageIcon icon = new ImageIcon(image);
        g.drawImage(icon.getImage(), 350, 50, 300, 300, this);
        g.setFont(new Font("宋体", Font.ITALIC, 21));
        g.setColor(Color.BLUE);
        g.drawString("☆☆ 扫描二维码登录微信 ☆☆", 350, 420);
    }
}
