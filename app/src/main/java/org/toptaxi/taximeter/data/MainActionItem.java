package org.toptaxi.taximeter.data;

public class MainActionItem {
    int Action;
    String ActionName;

    public MainActionItem(int action, String actionName) {
        Action = action;
        ActionName = actionName;
    }

    public String getActionName() {
        return ActionName;
    }

    public int getAction() {
        return Action;
    }
}
