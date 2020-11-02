/*
 * Author Konstantin Zaharov
 * Copyright (c) 2020. Simple sunMe implementation. Important! -device  system version should be non less then 1.5.3
 * If you need to change something - do it!
 */

package com.nts.sunmetester.lcd;

import android.content.Context;

import com.nts.sunmetester.R;

class LcdUtils {

    private final int[] LCD_FONT_SIZE;
    private final int MAX_INPUT_STRING_LEN;
    private final int MAX_LINE_LENGTH;
    private final String[] LCD_LINES;
    private String inputString;



    LcdUtils(String inputString, Context context) {

        // set free lcd data
        this.LCD_LINES = new String[(Integer.parseInt(context.getResources().getString(R.string.lcdLines)))];
        this.LCD_LINES[0] = "";
        this.LCD_LINES[1] = "";
        this.LCD_LINES[2] = "";

        // set font for 3 lcd lines
        int i = Integer.parseInt(context.getResources().getString(R.string.lcdFont));
        this.LCD_FONT_SIZE = new int[]{i, i, i};

        // define lcd line
        this.MAX_INPUT_STRING_LEN = Integer.parseInt(context.getResources().getString(R.string.lcdMaxInputStringLength));
        this.MAX_LINE_LENGTH = Integer.parseInt(context.getResources().getString(R.string.lcdMaxLineLength));

        // set input text dataStr
        this.inputString = inputString;
        getLcdLines();
    }

    /**
     * method create 3 strs * 20chars array
     */
    private void getLcdLines() {
        int k = 0;
        if (inputString.length() > MAX_INPUT_STRING_LEN) {
            inputString = inputString.substring(0, MAX_INPUT_STRING_LEN);
        }
        for (int i = 0; i < inputString.length(); i++) {
            LCD_LINES[k] += inputString.charAt(i);
            if (LCD_LINES[k].length() == MAX_LINE_LENGTH) {
                if (k < LCD_LINES.length - 1) {
                    k++;
                }
            }
        }
    }

    int[] getLCD_FONT_SIZE() {
        return LCD_FONT_SIZE;
    }

    String[] getLCD_LINES() {
        return LCD_LINES;
    }
}
