package com.oddsoft.quicktranslatex.controller.history;

/**
 * Created by andycheng on 2016/2/27.
 */
import java.util.Date;
import java.util.Locale;

public class Item implements java.io.Serializable {

    // 編號、日期時間、顏色、標題、內容、檔案名稱、經緯度、修改、已選擇
    private long id;
    private long datetime;
    private String fromId;
    private String toId;
    private String fromText;
    private String toText;


    public Item() {
        fromId = "";
        toId = "";
        fromText = "";
        toText = "";
    }

    public Item(long id, long datetime, String fromId,
                String toId, String fromText, String toText) {
        this.id = id;
        this.datetime = datetime;
        this.fromId = fromId;
        this.toId = toId;
        this.fromText = fromText;
        this.toText = toText;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDatetime() {
        return datetime;
    }

    // 裝置區域的日期時間
    public String getLocaleDatetime() {
        return String.format(Locale.getDefault(), "%tF  %<tR", new Date(datetime));
    }

    // 裝置區域的日期
    public String getLocaleDate() {
        return String.format(Locale.getDefault(), "%tF", new Date(datetime));
    }

    // 裝置區域的時間
    public String getLocaleTime() {
        return String.format(Locale.getDefault(), "%tR", new Date(datetime));
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }


    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public String getFromText() {
        return fromText;
    }

    public void setFromText(String fromText) {
        this.fromText = fromText;
    }

    public String getToText() {
        return toText;
    }

    public void setToText(String toText) {
        this.toText = toText;
    }

}
