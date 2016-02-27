package com.oddsoft.quicktranslatex.controller.history;

/**
 * Created by andycheng on 2016/2/27.
 */
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class HistoryDAO {

    // 表格名稱
    public static final String TABLE_NAME = "item";

    // 編號表格欄位名稱，固定不變
    public static final String KEY_ID = "_id";

    // 其它表格欄位名稱

    public static final String FROM_ID = "fromid";
    public static final String TO_ID = "toid";
    public static final String FROM_TEXT = "fromtext";
    public static final String TO_TEXT = "totext";
    public static final String DATETIME = "datetime";


    // 使用上面宣告的變數建立表格的SQL指令
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    FROM_ID + " TEXT NOT NULL, " +
                    TO_ID + " TEXT NOT NULL, " +
                    FROM_TEXT + " TEXT NOT NULL, " +
                    TO_TEXT + " TEXT NOT NULL, " +
                    DATETIME + " INTEGER NOT NULL)";

    // 資料庫物件
    private SQLiteDatabase db;

    // 建構子，一般的應用都不需要修改
    public HistoryDAO(Context context) {
        db = HistoryDBHelper.getDatabase(context);
    }

    // 關閉資料庫，一般的應用都不需要修改
    public void close() {
        db.close();
    }

    // 新增參數指定的物件
    public Item insert(Item item) {
        // 建立準備新增資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的新增資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(FROM_ID, item.getFromId());
        cv.put(TO_ID, item.getToId());
        cv.put(FROM_TEXT, item.getFromText());
        cv.put(TO_TEXT, item.getToText());
        cv.put(DATETIME, item.getDatetime());

        // 新增一筆資料並取得編號
        // 第一個參數是表格名稱
        // 第二個參數是沒有指定欄位值的預設值
        // 第三個參數是包裝新增資料的ContentValues物件
        long id = db.insert(TABLE_NAME, null, cv);

        // 設定編號
        item.setId(id);
        // 回傳結果
        return item;
    }

    // 修改參數指定的物件
    public boolean update(Item item) {
        // 建立準備修改資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的修改資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(FROM_ID, item.getFromId());
        cv.put(TO_ID, item.getToId());
        cv.put(FROM_TEXT, item.getFromText());
        cv.put(TO_TEXT, item.getToText());
        cv.put(DATETIME, item.getDatetime());

        // 設定修改資料的條件為編號
        // 格式為「欄位名稱＝資料」
        String where = KEY_ID + "=" + item.getId();

        // 執行修改資料並回傳修改的資料數量是否成功
        return db.update(TABLE_NAME, cv, where, null) > 0;
    }

    // 刪除參數指定編號的資料
    public boolean delete(long id){
        // 設定條件為編號，格式為「欄位名稱=資料」
        String where = KEY_ID + "=" + id;
        // 刪除指定編號資料並回傳刪除是否成功
        return db.delete(TABLE_NAME, where , null) > 0;
    }

    // 讀取所有記事資料
    public List<Item> getAll() {
        List<Item> result = new ArrayList<>();

        String orderBy = KEY_ID + " DESC";

        Cursor cursor = db.query(
                TABLE_NAME, null, null, null, null, null, orderBy, null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    // 取得指定編號的資料物件
    public Item get(long id) {
        // 準備回傳結果用的物件
        Item item = null;
        // 使用編號為查詢條件
        String where = KEY_ID + "=" + id;
        // 執行查詢
        Cursor result = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);

        // 如果有查詢結果
        if (result.moveToFirst()) {
            // 讀取包裝一筆資料的物件
            item = getRecord(result);
        }

        // 關閉Cursor物件
        result.close();
        // 回傳結果
        return item;
    }

    // 把Cursor目前的資料包裝為物件
    public Item getRecord(Cursor cursor) {
        // 準備回傳結果用的物件
        Item result = new Item();

        result.setId(cursor.getLong(0));
        result.setFromId(cursor.getString(1));
        result.setToId(cursor.getString(2));
        result.setFromText(cursor.getString(3));
        result.setToText(cursor.getString(4));
        result.setDatetime(cursor.getLong(5));

        // 回傳結果
        return result;
    }

    // 取得資料數量
    public int getCount() {
        int result = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);

        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }

        return result;
    }

    // 建立範例資料
    public void sample() {
        /*
        Item item = new Item(0, new Date().getTime(), Colors.RED, "關於Android Tutorial的事情.", "Hello content", "", 0, 0, 0);
        Item item2 = new Item(0, new Date().getTime(), Colors.BLUE, "一隻非常可愛的小狗狗!", "她的名字叫「大熱狗」，又叫\n作「奶嘴」，是一隻非常可愛\n的小狗。", "", 25.04719, 121.516981, 0);
        Item item3 = new Item(0, new Date().getTime(), Colors.GREEN, "一首非常好聽的音樂！", "Hello content", "", 0, 0, 0);
        Item item4 = new Item(0, new Date().getTime(), Colors.ORANGE, "儲存在資料庫的資料", "Hello content", "", 0, 0, 0);

        insert(item);
        insert(item2);
        insert(item3);
        insert(item4);*/
    }

}
