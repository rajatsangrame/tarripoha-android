package com.tarripoha.android.data.db;

import androidx.room.TypeConverter;

import java.io.Serializable;
import java.sql.Date;

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */
public class DbTypeConverter implements Serializable {

    @TypeConverter
    public static Date toDate(Long dateLong) {
        return dateLong == null ? null : new Date(dateLong);
    }

    @TypeConverter
    public static Long fromDate(Date date) {
        return date == null ? null : date.getTime();
    }
}

