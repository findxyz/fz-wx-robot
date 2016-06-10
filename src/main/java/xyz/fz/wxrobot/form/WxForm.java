package xyz.fz.wxrobot.form;

import org.apache.commons.lang.StringUtils;
import xyz.fz.wxrobot.WxFun;
import xyz.fz.wxrobot.WxRobotApp;
import xyz.fz.wxrobot.util.BaseUtil;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.Map;
import java.util.Vector;

import static xyz.fz.wxrobot.WxRobotApp.xiaoIceId;

/**
 * Created by fz on 2016/5/31.
 */
public class WxForm {

    private JTabbedPane tabbedPanel;
    private JPanel mainPanel;
    private JPanel tipPanel;
    private JLabel tipLabel;
    private JPanel qrCodePanel;
    private JList contactList;
    private JScrollPane contactScP;
    private JList messageList;
    private JLabel status;
    private JButton msgSendBtn;
    private JTextArea msgContent;
    private JLabel curCustomer;
    private JList choseList;
    private JList confirmList;
    private JTabbedPane copyTab;
    private JButton cancelCopyBtn;
    private JButton copyXiaoIceBtn;
    private JLabel copyStatus;
    private WxRobotApp wxRobotApp;

    private FormContext formContext = FormContext.getInstance();

    public WxForm() {
        initContext();
        initListeners();
    }

    private void initContext() {
        formContext.putComponent("mainPanel", mainPanel);
        formContext.putComponent("tabbedPanel", tabbedPanel);
        formContext.putComponent("tipPanel", tipPanel);
        formContext.putComponent("tipLabel", tipLabel);
        formContext.putComponent("qrCodePanel", qrCodePanel);
        formContext.putComponent("contactList", contactList);
        formContext.putComponent("contactScP", contactScP);
        formContext.putComponent("messageList", messageList);
        formContext.putComponent("status", status);
        formContext.putComponent("msgSendBtn", msgSendBtn);
        formContext.putComponent("msgContent", msgContent);
        formContext.putComponent("curCustomer", curCustomer);
        formContext.putComponent("choseList", choseList);
        formContext.putComponent("confirmList", confirmList);
        formContext.putComponent("copyTab", copyTab);
        formContext.putComponent("cancelCopyBtn", cancelCopyBtn);
        formContext.putComponent("copyXiaoIceBtn", copyXiaoIceBtn);
    }

    private void initListeners() {
        contactList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                String customer = (String) contactList.getModel().getElementAt(contactList.locationToIndex(e.getPoint()));
                String[] customerInfo = customer.split(WxRobotApp.splitFlag);
                wxRobotApp.putThing("curCustomerName", customerInfo[0]);
                wxRobotApp.putThing("curCustomerId", customerInfo[1]);
                SwingUtilities.invokeLater(() -> {
                    curCustomer.setText("当前联系人：" + customerInfo[0]);
                    curCustomer.updateUI();
                });
            }
        });

        choseList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (wxRobotApp.getThing("copyRole") == null || StringUtils.isBlank(wxRobotApp.getThing("copyRole").toString())) {
                    SwingUtilities.invokeLater(() -> {
                        String customer = (String) choseList.getModel().getElementAt(choseList.locationToIndex(e.getPoint()));
                        if (!formContext.getConfirmSet().contains(customer)) {
                            formContext.getConfirmSet().add(customer);
                            Vector confirmVector = formContext.getConfirmVector();
                            confirmVector.add(customer);
                            confirmList.setListData(confirmVector);
                            confirmList.updateUI();
                        }
                    });
                }
            }
        });

        confirmList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (wxRobotApp.getThing("copyRole") == null || StringUtils.isBlank(wxRobotApp.getThing("copyRole").toString())) {
                    SwingUtilities.invokeLater(() -> {
                        if (confirmList.getModel().getSize() > 0) {
                            String customer = (String) confirmList.getModel().getElementAt(confirmList.locationToIndex(e.getPoint()));
                            Vector confirmVector = formContext.getConfirmVector();
                            if (confirmVector.size() > 0) {
                                formContext.getConfirmSet().remove(customer);
                                confirmVector.remove(confirmList.locationToIndex(e.getPoint()));
                                confirmList.setListData(confirmVector);
                                confirmList.updateUI();
                            }
                        }
                    });
                }
            }
        });

        msgContent.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMsg();
                }
            }
        });

        msgSendBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                sendMsg();
            }
        });

        copyXiaoIceBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                SwingUtilities.invokeLater(() -> {
                    wxRobotApp.putThing("copyRole", xiaoIceId);
                    copyStatus.setText("当前化身：小冰");
                    Vector confirmVector = formContext.getConfirmVector();
                    for (Object target : confirmVector) {
                        String targetId = target.toString().split(WxRobotApp.splitFlag)[1];
                        wxRobotApp.getTargetSet().add(targetId);
                    }
                });
            }
        });

        cancelCopyBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                SwingUtilities.invokeLater(() -> {
                    wxRobotApp.putThing("copyRole", "");
                    copyStatus.setText("当前化身：无");
                    wxRobotApp.getTargetSet().clear();
                });
            }
        });
    }

    private void sendMsg() {
        msgContent.setEnabled(false);
        Object curCustomerId = wxRobotApp.getThing("curCustomerId");
        if (curCustomerId != null) {
            String userId = wxRobotApp.getThing("userId").toString();
            String curUserName = wxRobotApp.getThing("curUserName").toString();
            String curCustomerName = wxRobotApp.getThing("curCustomerName").toString();
            boolean sendFlag = WxFun.sendMsg(userId, curCustomerId.toString(), msgContent.getText(), (Map<String, Object>) wxRobotApp.getThing("baseRequest"), wxRobotApp.getThing("pass_ticket").toString());
            if (sendFlag) {
                SwingUtilities.invokeLater(() -> {
                    Vector messageVector = formContext.getMessageVector();
                    String content = curUserName + " 在 " + BaseUtil.ymdHmsDateFormat(new Date()) + " 对 " + curCustomerName + " 说：" + msgContent.getText();
                    messageVector.add(0, content);
                    messageList.setListData(messageVector);
                    messageList.updateUI();
                    msgContent.setText("");
                    msgContent.updateUI();
                });
            }
        }
        msgContent.setEnabled(true);
        msgContent.requestFocus();
    }

    public FormContext getFormContext() {
        return formContext;
    }

    public void setWxRobotApp(WxRobotApp wxRobotApp) {
        this.wxRobotApp = wxRobotApp;
    }
}
