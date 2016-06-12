package xyz.fz.wxrobot.form;

import java.awt.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fz on 2016/5/31.
 */
public class FormContext {

    public static int messageSize = 1000;

    private ExecutorService threadPool = Executors.newFixedThreadPool(3);

    private FormContext() {}

    private Map<String, Component> componentMap = new HashMap<>();

    private Vector messageVector = new Vector(messageSize);

    private Vector confirmVector = new Vector();

    private Set confirmSet = new HashSet<>();

    private static FormContext formContext = new FormContext();

    public static FormContext getInstance() {
        return formContext;
    }

    public Component getComponent(String key) {
        return componentMap.get(key);
    }

    public void putComponent(String key, Component component) {
        this.componentMap.put(key, component);
    }

    public Vector getMessageVector() {
        return messageVector;
    }

    public void setMessageVector(Vector messageVector) {
        this.messageVector = messageVector;
    }

    public Vector getConfirmVector() {
        return confirmVector;
    }

    public Set getConfirmSet() {
        return confirmSet;
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }
}
