/*
 * Author Konstantin Zaharov
 * Copyright (c) 2020. Simple sunMe implementation. Important! -device  system version should be non less then 1.5.3
 * If you need to change something - do it!
 */

package com.nts.sunmetester.myPrinter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;

import com.nts.sunmetester.R;
import com.sunmi.peripheral.printer.SunmiPrinterService;

import java.util.List;


public class MyPrinter implements Runnable {

    private SunmiPrinterService sunmiPrinterService;
    private Handler handler;

    // define z-report example
    private final String REPORT;

    // define printer info const
    private final String PRINTER_NOT_READY;
    private final String PRINTER_INFO;
    private final String PRINTER_STATUS_PRN;
    private final String PRINTER_CUTTER;
    private final String PRINTER_NUMBER;
    private final String PRINTER_FW_VER;
    private final String PRINTER_MODEL;
    private final String PRINTER_SW_VER;
    private final String PRINTER_STATUS_CD;
    private final String PRINTER_PAPER;
    private final String PRINTER_FIRMWARE;

    private final String TEXT_LINE;
    private final String QR_TEXT;
    private final int MSG;
    private final int ALIGN_LEFT;
    private final int ALIGN_CENTER;
    private final int QR_CODE_SIZE;
    private final int QR_CODE_CORRECTION_LEVEL;

    private final List taskList;

    private final Context context;


    public MyPrinter(String s, SunmiPrinterService sunmiPrinterService, Handler handler, List<Task> taskList, Context context) {
        this.sunmiPrinterService = sunmiPrinterService;
        this.handler = handler;
        this.TEXT_LINE = s;
        this.taskList = taskList;

        this.PRINTER_NOT_READY = context.getResources().getString(R.string.printerNotReady);
        this.QR_TEXT = context.getResources().getString(R.string.url);

        this.MSG = Integer.parseInt(context.getResources().getString(R.string.msg));
        this.ALIGN_LEFT = Integer.parseInt(context.getResources().getString(R.string.alignLeft));
        this.ALIGN_CENTER = Integer.parseInt(context.getResources().getString(R.string.alignCenter));
        this.QR_CODE_SIZE = Integer.parseInt(context.getResources().getString(R.string.qrCodeSize));
        this.QR_CODE_CORRECTION_LEVEL = Integer.parseInt(context.getResources().getString(R.string.qrCodeCorrectionLevel));

        this.PRINTER_INFO = context.getResources().getString(R.string.printerInfo);
        this.PRINTER_FIRMWARE = context.getResources().getString(R.string.printerFirmware);
        this.PRINTER_NUMBER = context.getResources().getString(R.string.printerNumber);
        this.PRINTER_MODEL = context.getResources().getString(R.string.printerModel);
        this.PRINTER_FW_VER = context.getResources().getString(R.string.printerFwVersion);
        this.PRINTER_SW_VER = context.getResources().getString(R.string.printerSwVersionr);
        this.PRINTER_CUTTER = context.getResources().getString(R.string.printerCutter);
        this.PRINTER_STATUS_CD = context.getResources().getString(R.string.printerStatusCd);
        this.PRINTER_PAPER = context.getResources().getString(R.string.printerPaper);
        this.PRINTER_STATUS_PRN = context.getResources().getString(R.string.printerStatus);

        this.REPORT = context.getResources().getString(R.string.report);

        this.context = context;

    }

    @Override
    public void run() {
        if (sunmiPrinterService != null) {
            try {
                /*
                  check printer via call status before printout.
                  result
                  1 → ready
                  2 → Preparing printer, not ready
                  3 → Abnormal communication, not ready
                  4 → Out of paper, not ready
                  5 → Overheated, not ready
                  6 → Open the lid, not ready
                  7 → The paper cutter is abnormal, not ready
                  8 → The paper cutter has been recovered, not ready
                  9 → No black mark has been detected
                  505 →No printer has been detected
                  507 →Failed to upgrade the printer firmware

                 */

                int i = sunmiPrinterService.updatePrinterState();
                if (i != 1) {
                    sendMsg(PRINTER_NOT_READY);
                    return;
                }


                // next steps uses for fill printer task buffer
                // open buffer
                sunmiPrinterService.enterPrinterBuffer(true);
                for (Object o : taskList) {
                    switch ((Task) o) {
                        // printout task, don't forget \n at the line end (obligate)
                        case PRINT: {
                            sunmiPrinterService.printText(TEXT_LINE.toUpperCase() + '\n', null);
                            break;
                        }
                        // qr code data
                        case QR: {
                            sunmiPrinterService.setAlignment(ALIGN_CENTER, null);
                            sunmiPrinterService.printQRCode(QR_TEXT, QR_CODE_SIZE, QR_CODE_CORRECTION_LEVEL, null);
                            sunmiPrinterService.setAlignment(ALIGN_LEFT, null);
                            break;
                        }
                        // use cutter
                        case CUT: {
                            sunmiPrinterService.cutPaper(null);

                        }
                        // open cash draw
                        case CD: {
                            sunmiPrinterService.openDrawer(null);
                            break;
                        }
                        // info
                        case INFO: {
                            String info = "";
                            info += PRINTER_INFO + '\n';
                            info += PRINTER_FIRMWARE + sunmiPrinterService.getFirmwareStatus() + '\n';
                            info += PRINTER_NUMBER + sunmiPrinterService.getPrinterSerialNo() + '\n';
                            info += PRINTER_MODEL + sunmiPrinterService.getPrinterModal();
                            info += PRINTER_FW_VER + sunmiPrinterService.getPrinterVersion();
                            info += PRINTER_SW_VER + sunmiPrinterService.getServiceVersion() + '\n';
                            info += PRINTER_CUTTER + sunmiPrinterService.getCutPaperTimes() + '\n';
                            info += PRINTER_STATUS_CD + sunmiPrinterService.getDrawerStatus() + '\n';
                            info += PRINTER_PAPER + sunmiPrinterService.getPrinterPaper() + '\n';
                            info += PRINTER_STATUS_PRN + sunmiPrinterService.updatePrinterState() + '\n';
                            sunmiPrinterService.printText(info.toUpperCase() + '\n', null);
                            break;
                        }
                        // print test report
                        case REPORT: {
                            sunmiPrinterService.printText(REPORT.toUpperCase() + '\n', null);
                            break;
                        }
                    }
                }
                // check result of buffer transaction operation via call back
                sunmiPrinterService.commitPrinterBufferWithCallback(new MyPrinterCallBack(handler, context));
                // close buffer and finally recheck error again, method will close buffer in case if
                // commitPrinterBufferWithCallback execute without error
                sunmiPrinterService.exitPrinterBufferWithCallback(true, new MyPrinterCallBack(handler, context));
            } catch (RemoteException e) {
                sendMsg(e.toString());
            }
        }
    }

    private void sendMsg(String s) {
        Message message = handler.obtainMessage(MSG, s);
        handler.sendMessage(message);
    }
}
