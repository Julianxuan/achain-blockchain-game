package com.achain.blockchain.game.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * @author qiangkz on 2017/8/9.
 */
public class DateUtils {

    /**加8时区*/
    public static Date getTimeoneEight(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR_OF_DAY, 8);//因为是数据是0区
        return cal.getTime();
    }


}
