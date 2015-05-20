package com.eTilbudsavis.etasdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.model.Shoppinglist;
import com.eTilbudsavis.etasdk.model.ShoppinglistItem;
import com.eTilbudsavis.etasdk.model.interfaces.SyncState;
import com.eTilbudsavis.etasdk.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ShoppinglistItemSQLiteHelper extends DatabaseHelper {

    public static final String TAG = Constants.getTag(ShoppinglistItemSQLiteHelper.class);

    public static final String TABLE = "shoppinglistitems";

    public static final String CREATE_TABLE =
            "create table if not exists " + TABLE + "(" +
                    ID + " text not null primary key, " +
                    ERN + " text not null, " +
                    MODIFIED + " text not null, " +
                    DESCRIPTION + " text, " +
                    COUNT + " integer not null, " +
                    TICK + " integer not null, " +
                    OFFER_ID + " text, " +
                    CREATOR + " text, " +
                    SHOPPINGLIST_ID + " text not null, " +
                    STATE + " integer not null, " +
                    PREVIOUS_ID + " text, " +
                    META + " text, " +
                    USER + "  integer not null " +
                    ");";

    public ShoppinglistItemSQLiteHelper(Context context) {
        super(context);
    }

    public static void create(SQLiteDatabase db) {
        db.acquireReference();
        db.execSQL(CREATE_TABLE);
        db.releaseReference();
    }

    public static void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.acquireReference();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        db.releaseReference();
    }

    public static List<ShoppinglistItem> cursorToList(Cursor c) {
        ArrayList<ShoppinglistItem> list = new ArrayList<ShoppinglistItem>();
        for (ContentValues cv : DbUtils.cursorToContentValues(c)) {
            ShoppinglistItem sli = ShoppinglistItemSQLiteHelper.contentValuesToObject(cv);
            if (sli!=null) {
                list.add(sli);
            }
        }
        return list;
    }

    public static ShoppinglistItem contentValuesToObject(ContentValues cv) {
        ShoppinglistItem sli = new ShoppinglistItem();
        sli.setId(cv.getAsString(ID));
        sli.setErn(cv.getAsString(ERN));
        sli.setModified(Utils.stringToDate(cv.getAsString(MODIFIED)));
        sli.setDescription(cv.getAsString(DESCRIPTION));
        sli.setCount(cv.getAsInteger(COUNT));
        sli.setTick(0 < cv.getAsInteger(TICK));
        sli.setOfferId(cv.getAsString(OFFER_ID));
        sli.setCreator(cv.getAsString(CREATOR));
        sli.setShoppinglistId(cv.getAsString(SHOPPINGLIST_ID));
        sli.setState(cv.getAsInteger(STATE));
        sli.setPreviousId(cv.getAsString(PREVIOUS_ID));
        try {
            String meta = cv.getAsString(META);
            sli.setMeta(meta == null ? null : new JSONObject(meta));
        } catch (JSONException e) {
            EtaLog.e(TAG, null, e);
        }
        sli.setUserId(cv.getAsInteger(USER));
        return sli;
    }

    public static ContentValues objectToContentValues(ShoppinglistItem sli, int userId) {
        ContentValues cv = new ContentValues();
        cv.put(ID, sli.getId());
        cv.put(ERN, sli.getErn());
        cv.put(MODIFIED, Utils.dateToString(sli.getModified()));
        cv.put(DESCRIPTION, sli.getDescription());
        cv.put(COUNT, sli.getCount());
        cv.put(TICK, sli.isTicked());
        cv.put(OFFER_ID, sli.getOfferId());
        cv.put(CREATOR, sli.getCreator());
        cv.put(SHOPPINGLIST_ID, sli.getShoppinglistId());
        cv.put(STATE, sli.getState());
        cv.put(PREVIOUS_ID, sli.getPreviousId());
        cv.put(META, sli.getMeta().toString());
        cv.put(USER, userId);
        return cv;
    }

}
