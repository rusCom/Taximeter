package org.toptaxi.taximeter.tools.cardview;

public interface IMainCardViewData {
    String getMainText();
    default String getNoteText(){
        return null;
    }
    default Integer getImageResourceID(){
        return null;
    }


}
