package org.toptaxi.taximeter.data;

import java.util.Comparator;

public class MessageComp implements Comparator {
    public int compare(Object obj1, Object obj2){
        Message message1 = (Message)obj1;
        Message message2 = (Message)obj2;
        if (message1.ID < message2.ID)return -1;
        else if (message1.ID > message2.ID)return 1;
        return 0;
    }
}
