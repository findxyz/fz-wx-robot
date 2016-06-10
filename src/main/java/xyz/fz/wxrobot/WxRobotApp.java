package xyz.fz.wxrobot;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.fz.wxrobot.form.QRCodePanel;
import xyz.fz.wxrobot.form.WxForm;
import xyz.fz.wxrobot.util.BaseUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static xyz.fz.wxrobot.form.FormContext.messageSize;

/**
 * Created by fz on 2016/5/29.
 */
public class WxRobotApp {

    private static final String appTitle = "说出来你可能不信，其实我是个机器人 By: xiayudashan【natureorz@foxmail.com】";

    public static String xiaoIceId = "";

    private static String xiaoIceName = "小冰";

    private Queue<String> blockingQueue = new LinkedBlockingDeque<>();

    public static final String splitFlag = "                                                                                                   ";

    private static Pattern groupMemberPattern = Pattern.compile("@(\\S+):<br/>");

    private static Logger logger = LoggerFactory.getLogger(WxRobotApp.class);

    private WxForm wxForm;

    private final Map<String, Object> store = new HashMap<>();

    public void putThing(String key, Object obj) {
        store.put(key, obj);
    }

    public Object getThing(String key) {
        return store.get(key);
    }

    private final Set<String> targetSet = new HashSet<>();

    public Set<String> getTargetSet() {
        return targetSet;
    }

    public WxRobotApp() {
        this.wxForm = new WxForm();
        wxForm.setWxRobotApp(this);
    }

    public static void main(String[] args) {

        WxRobotApp wxRobotApp = new WxRobotApp();

        wxRobotApp.putThing("deviceId", "e" + System.currentTimeMillis());

        wxRobotApp.showFrame();

        // 1、2、3步骤合并，获取到最新可用二维码
        wxRobotApp.prepareQRCode();

        // 4.确认后登陆，获取后续请求必要参数
        Map<String, Object> cookieResult = WxFun.getCookie(wxRobotApp.getThing("redirect").toString());
        Map<String, Object> baseRequest = new HashMap<>();
        Map<String, Object> bodyInnerParams = new HashMap<>();
        bodyInnerParams.put("Uin", cookieResult.get("wxuin"));
        bodyInnerParams.put("Sid", cookieResult.get("wxsid"));
        bodyInnerParams.put("Skey", cookieResult.get("skey"));
        bodyInnerParams.put("DeviceId", wxRobotApp.getThing("deviceId"));
        baseRequest.put("BaseRequest", bodyInnerParams);
        wxRobotApp.putThing("baseRequest", baseRequest);
        wxRobotApp.putThing("pass_ticket", cookieResult.get("pass_ticket"));
        // 5.初始化微信
        Map<String, Object> initData = WxFun.init((Map<String, Object>) wxRobotApp.getThing("baseRequest"));
        Map user = (Map) initData.get("User");
        Map syncKey = (Map) initData.get("SyncKey");
        wxRobotApp.putThing("user", user);
        wxRobotApp.putThing("userId", user.get("UserName"));
        wxRobotApp.putThing("syncKey", syncKey);
        logger.debug("[欢迎] " + user.get("NickName").toString());
        wxRobotApp.putThing("curUserName", user.get("NickName").toString());
        // 6 done 获取联系人，并将联系人信息填入联系人列表
        Vector vector = WxFun.getContactList((Map<String, Object>) wxRobotApp.getThing("baseRequest"));
        wxRobotApp.initContactList(vector);
        for (Object uStr : vector) {
            String[] uInfo = uStr.toString().split(splitFlag);
            wxRobotApp.putThing("u_" + uInfo[1], uInfo[0]);
            if (uInfo[0].equals(xiaoIceName)) {
                xiaoIceId = uInfo[1];
            }
        }
        wxRobotApp.putThing("u_" + user.get("UserName").toString(), user.get("NickName").toString());
        // 7 启动消息监听
        wxRobotApp.messageListener();
    }

    // 准备二维码，合并1、2、3步
    private void prepareQRCode() {
        // 1.获取uuid以准备获得qrCode的参数
        Map<String, Object> uuidResult;
        while (true) {
            uuidResult = WxFun.queryUUID();
            if (uuidResult.get("error") == null) {
                break;
            }
            logger.warn("无法获取uuid，请检查网络");
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                // do nothing
            }
        }
        this.putThing("uuid", uuidResult.get("uuid").toString());
        // 2.根据uuid获取二维码
        try {
            BufferedImage bufferedImage = WxFun.queryQRCode(this.getThing("uuid").toString());
            SwingUtilities.invokeLater(() -> this.showQRCode(bufferedImage));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(BaseUtil.getExceptionStackTrace(e));
        }
        // 3.等待扫码
        boolean reTryFlag = false;
        while (true) {
            Map<String, Object> checkResult = WxFun.checkConfirm(this.getThing("uuid").toString());
            if (checkResult.get("code") == null) {
                reTryFlag = true;
                break;
            }
            if (checkResult.get("code").toString().equals("201")) {
                switchTab();
            }
            if (checkResult.get("code").toString().equals("200")) {
                this.putThing("redirect", checkResult.get("redirect"));
                break;
            }
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                // do nothing
            }
        }
        // 重新尝试生成二维码
        if (reTryFlag) {
            this.prepareQRCode();
        }
    }

    private void showFrame() {

        JFrame frame = new JFrame();
        frame.setContentPane((Container) wxForm.getFormContext().getComponent("mainPanel"));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(200, 50);
        frame.setResizable(false);
        frame.pack();
        frame.setVisible(true);
        frame.setTitle(appTitle);
    }

    private void showQRCode(Image image) {

        JPanel qrCodePanel = (JPanel) wxForm.getFormContext().getComponent("qrCodePanel");
        qrCodePanel.removeAll();
        qrCodePanel.setLayout(new BoxLayout(qrCodePanel, BoxLayout.PAGE_AXIS));
        qrCodePanel.add(new QRCodePanel(image));
        ((JPanel) wxForm.getFormContext().getComponent("tipPanel")).updateUI();
    }

    private void switchTab() {
        SwingUtilities.invokeLater(() -> {
            JTabbedPane tabbedPanel = (JTabbedPane) wxForm.getFormContext().getComponent("tabbedPanel");
            if (tabbedPanel.isEnabledAt(0)) {
                tabbedPanel.setEnabledAt(0, false);
                tabbedPanel.setSelectedIndex(1);
                tabbedPanel.updateUI();
                JLabel statusLabel = (JLabel) wxForm.getFormContext().getComponent("status");
                statusLabel.setText("当前状态：正在获取联系人...");
            }
        });
    }

    private void initContactList(Vector contactList) {

        SwingUtilities.invokeLater(() -> {
            JList cList = (JList) wxForm.getFormContext().getComponent("contactList");
            cList.setListData(contactList);
            cList.updateUI();
            JList choseList = ((JList) wxForm.getFormContext().getComponent("choseList"));
            choseList.setListData(contactList);
            choseList.updateUI();
            JLabel statusLabel = (JLabel) wxForm.getFormContext().getComponent("status");
            statusLabel.setText("当前状态：联系人获取完毕");
        });
    }

    private void messageListener() {

        Runnable messageListenerRunner = () -> {
            while (true) {
                try {
                    String selector = checkNewMsg();
                    logger.warn(selector);
                    syncNewMsg();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    logger.error(BaseUtil.getExceptionStackTrace(e));
                }
            }
        };
        Thread mThread = new Thread(messageListenerRunner);
        mThread.start();
    }

    private String checkNewMsg() {
        return WxFun.checkNewMsg((Map<String, Object>) this.getThing("baseRequest"), (Map) this.getThing("syncKey"));
    }

    private void syncNewMsg() {
        Map<String, Object> result = WxFun.syncNewMsg((Map<String, Object>) this.getThing("baseRequest"), this.getThing("pass_ticket").toString(), (Map) this.getThing("syncKey"));
        this.putThing("syncKey", result.get("SyncKey"));
        java.util.List msgList = (java.util.List) result.get("AddMsgList");
        if (msgList == null) {
            return;
        }
        JList messageList = ((JList) wxForm.getFormContext().getComponent("messageList"));
        Vector messageVector = wxForm.getFormContext().getMessageVector();
        if (messageVector.size() >= messageSize - 1) {
            messageVector = new Vector(messageSize);
            wxForm.getFormContext().setMessageVector(messageVector);
        }
        for (Object msg : msgList) {
            Map m = (Map) msg;
            if (m.get("MsgType").toString().equals("1")) {
                String fromName = this.getThing("u_" + m.get("FromUserName").toString()).toString();
                String toName = this.getThing("u_" + m.get("ToUserName").toString()).toString();
                String content = fromName + " 在 " + BaseUtil.ymdHmsDateFormat(new Date()) + " 对 " + toName + " 说：" + m.get("Content").toString();
                // !!重要 化身处理消息
                processCopyRole(m.get("FromUserName").toString(), m.get("ToUserName").toString(), m.get("Content").toString());
                // 处理群消息
                Matcher matcher = groupMemberPattern.matcher(content);
                if (matcher.find()) {
                    String groupMemberId = "@" + matcher.group(1);
                    // 如果群消息中当前发言人已在缓存则直接取出使用
                    // 如果不在缓存中则将本次收到的消息体内的群用户列表进行缓存
                    if (this.getThing("u_" + groupMemberId) == null) {
                        java.util.List groupList = (java.util.List) result.get("ModContactList");
                        if (groupList != null && groupList.size() > 0) {
                            java.util.List memberList = (java.util.List) ((Map) groupList.get(0)).get("MemberList");
                            for (Object member : memberList) {
                                Map memberMap = (Map) member;
                                String memberId = memberMap.get("UserName").toString();
                                if (this.getThing(memberId) == null) {
                                    this.putThing("u_" + memberId, StringUtils.isNotBlank(memberMap.get("DisplayName").toString()) ? memberMap.get("DisplayName") : memberMap.get("NickName").toString());
                                }
                            }
                        }
                    }
                    String memberName = this.getThing("u_" + groupMemberId) != null ? this.getThing("u_" + groupMemberId).toString() : "未知";
                    content = content.replace(groupMemberId + ":<br/>", "【" + memberName + "，说：】");
                }
                messageVector.add(0, content);
            }

        }
        Vector finalMessageVector = messageVector;
        SwingUtilities.invokeLater(() -> {
            messageList.setListData(finalMessageVector);
            messageList.updateUI();
            JLabel statusLabel = (JLabel) wxForm.getFormContext().getComponent("status");
            statusLabel.setText("当前状态：接收新消息");
            logger.debug(result.toString());
        });
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void processCopyRole(String fromId, String toId, String content) {
        // 1.fromId 是目标人物，说明目标用户发来的消息，此时需要给其消息回复
        // 1.1 发送消息给小冰，并记下发送的序号unionSeqNum
        String copyRole = "";
        if (this.getThing("copyRole") != null && StringUtils.isNotBlank(this.getThing("copyRole").toString())) {
            copyRole = this.getThing("copyRole").toString();
        }
        if (targetSet.contains(fromId) && StringUtils.isNotBlank(copyRole)) {
            String finalCopyRole = copyRole;
            blockingQueue.offer(fromId);
            String userId = this.getThing("userId").toString();
            WxFun.sendMsg(userId, finalCopyRole, content, (Map<String, Object>) this.getThing("baseRequest"), this.getThing("pass_ticket").toString());
            SwingUtilities.invokeLater(() -> {
                Vector messageVector = wxForm.getFormContext().getMessageVector();
                String c = this.getThing("u_" + userId) + " 在 " + BaseUtil.ymdHmsDateFormat(new Date()) + " 对 " + this.getThing("u_" + finalCopyRole) + " 说：" + content;
                messageVector.add(0, c);
                ((JList) wxForm.getFormContext().getComponent("messageList")).setListData(messageVector);
                ((JList) wxForm.getFormContext().getComponent("messageList")).updateUI();
            });
        }
        // 2.fromId 是小冰，说明小冰将消息返回，此时可以根据unionSeqNum回复消息给目标人物
        if (fromId.equals(copyRole)) {
            String userId = this.getThing("userId").toString();
            if (blockingQueue.size() > 0) {
                String backId = blockingQueue.poll().toString();
                WxFun.sendMsg(userId, backId, content, (Map<String, Object>) this.getThing("baseRequest"), this.getThing("pass_ticket").toString());
                SwingUtilities.invokeLater(() -> {
                    Vector messageVector = wxForm.getFormContext().getMessageVector();
                    String c = this.getThing("u_" + userId) + " 在 " + BaseUtil.ymdHmsDateFormat(new Date()) + " 对 " + this.getThing("u_" + backId) + " 说：" + content;
                    messageVector.add(0, c);
                    ((JList) wxForm.getFormContext().getComponent("messageList")).setListData(messageVector);
                    ((JList) wxForm.getFormContext().getComponent("messageList")).updateUI();
                });
            }
        }
    }

}
