package top.vchar;

import top.vchar.util.StringUtils;
import top.vchar.util.TikTokHttpUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;

/**
 * <p> 界面 </p>
 *
 * @author vchar fred
 * @version 1.0
 * @create_date 2020/11/23
 */
public class AppUI {

    private JPanel panel1;
    private JTextField shareUrlInput;
    private JButton clearBtn;
    private JButton downloadBtn;
    private JTextField fileDirInput;
    private JTextField fileName;
    private JTextArea logArea;

    public static boolean downloadBtnIsEnable = true;

    private static TikTokHttpUtil tikTokHttp;

    private static String separator;

    public AppUI() {
        tikTokHttp = TikTokHttpUtil.getInstance(this);
        fileDirInput.setText(System.getProperty("user.dir"));
        separator = String.valueOf(File.separatorChar);

        clearBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shareUrlInput.setText("");
                logArea.setText("");
            }
        });
        downloadBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (AppUI.downloadBtnIsEnable) {
                    AppUI.downloadBtnIsEnable = false;

                    addLog("=============start==================");

                    String url = shareUrlInput.getText();
                    if (isDefaultUrl(url)) {
                        addLog("请输入抖音视频分享链接\n=============end==================");
                        AppUI.downloadBtnIsEnable = true;
                        return;
                    }

                    String dir = fileDirInput.getText();
                    String videoName = fileName.getText();
                    if (StringUtils.isBlank(videoName)) {
                        videoName = System.currentTimeMillis() + ".mp4";
                    } else {
                        videoName = videoName.trim();
                        if (!videoName.endsWith(".mp4")) {
                            videoName += ".mp4";
                        }
                    }
                    if (StringUtils.isNotBlank(dir)) {
                        dir = dir.trim();
                        if (!dir.endsWith(separator)) {
                            dir += separator;
                        }
                        if (StringUtils.isNotBlank(url)) {
                            try {
                                String videoUrl = tikTokHttp.extractVideoUrl(url);
                                tikTokHttp.downloadVideo(videoUrl, dir, videoName);
                                shareUrlInput.setText("");
                                fileName.setText("");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    } else {
                        addLog("请设置文件保存路径.");
                    }
                    addLog("=============end==================");
                    AppUI.downloadBtnIsEnable = true;
                } else {
                    addLog("请输入抖音视频分享链接");
                }
            }
        });

        shareUrlInput.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                String val = shareUrlInput.getText();
                if (isDefaultUrl(val)) {
                    shareUrlInput.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                String val = shareUrlInput.getText();
                if (StringUtils.isBlank(val)) {
                    shareUrlInput.setText("抖音视频分享链接");
                }
            }
        });
    }

    public JPanel panel() {
        return this.panel1;
    }

    public void addLog(String message) {
        this.logArea.append("\n" + message);
    }

    public void updateUrl(String url) {
        shareUrlInput.setText(url);
    }

    private boolean isDefaultUrl(String val) {
        return "抖音视频分享链接".equals(val);
    }

}
