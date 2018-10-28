package com.ettud.is74task3.Presenters;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;

import java.lang.reflect.InvocationTargetException;

public class BatteryPresenter {
    private BatteryManager mBatteryManager;
    private Integer mTemp;
    private Integer mCurrentCurrency;
    private Integer mAverageCurrency;
    private Double mMaxCapacity;
    private Double mCurrentCapacity;
    private Integer mChargeStatus;
    private Double mBatteryLevel;

    public BatteryPresenter(Context context, Intent intent) {
        mTemp = null;
        mCurrentCurrency = null;
        mAverageCurrency = null;
        mMaxCapacity = null;
        mCurrentCapacity = null;
        mChargeStatus = null;
        mBatteryLevel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBatteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            getInformation(context, intent);
        }
    }

    public Boolean isCharging() {
        if(mChargeStatus == null){
            return null;
        }
        return mChargeStatus == BatteryManager.BATTERY_PLUGGED_AC ||
                mChargeStatus == BatteryManager.BATTERY_PLUGGED_USB ||
                mChargeStatus == BatteryManager.BATTERY_PLUGGED_WIRELESS;
    }

    public Boolean isChargingAc() {
        if(mChargeStatus == null){
            return null;
        }
        return mChargeStatus == BatteryManager.BATTERY_PLUGGED_AC;
    }

    public Boolean isChargingUsb() {
        if(mChargeStatus == null){
            return null;
        }
        return mChargeStatus == BatteryManager.BATTERY_PLUGGED_USB;
    }

    public Boolean isChargingWireless() {
        if(mChargeStatus == null){
            return null;
        }
        return mChargeStatus == BatteryManager.BATTERY_PLUGGED_WIRELESS;
    }

    public Double getCelciusTemp() {
        if (mTemp == null)
            return null;
        return mTemp / (double) 10;
    }

    public Integer getCurrentCurrency() {
        return mCurrentCurrency;
    }

    public Integer getAverageCurrency() {
        return mAverageCurrency;
    }

    public Double getMaxCapacity() {
        return mMaxCapacity;
    }

    public Double getCurrentCapacity() {
        return mCurrentCapacity;
    }

    public Double getBatteryLevel() { return mBatteryLevel; }

    @SuppressLint({"NewApi", "PrivateApi"})
    private void getInformation(Context context, Intent intent) {
        mTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Integer.MIN_VALUE);
        mChargeStatus = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        if(mChargeStatus == -1){
            mChargeStatus = null;
        }
        if (mBatteryManager != null) {
            mAverageCurrency = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);
            if (mAverageCurrency == Integer.MIN_VALUE ||
                    mAverageCurrency == 0 && (isCharging())) {
                mAverageCurrency = null;
            }
            mCurrentCurrency = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
            if (mCurrentCurrency == Integer.MIN_VALUE ||
                    mCurrentCurrency == 0 && (isCharging())) {
                mCurrentCurrency = null;
            }

            {
                final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                if (level != -1 &&
                        level != 0 ) {
                    mBatteryLevel = new Double(level);
                }
                if (mBatteryLevel != null &&
                        mMaxCapacity != null) {
                    mCurrentCapacity = mMaxCapacity * mBatteryLevel / 100;
                }
            }

            {
                int chargeCounter = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
                int capacity = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                if (chargeCounter != Integer.MIN_VALUE &&
                        chargeCounter != 0 &&
                        capacity != Integer.MIN_VALUE) {
                    mMaxCapacity = new Double(chargeCounter * 100) / capacity;
                }
            }
            if (mMaxCapacity == null ||
                    mCurrentCapacity == null) {
                Object powerProfile;
                try {
                    powerProfile = Class.forName("com.android.internal.os.PowerProfile").getConstructor(Context.class).newInstance(context);
                } catch (ClassNotFoundException e) {
                    powerProfile = null;
                } catch (IllegalAccessException e) {
                    powerProfile = null;
                } catch (InstantiationException e) {
                    powerProfile = null;
                } catch (NoSuchMethodException e) {
                    powerProfile = null;
                } catch (InvocationTargetException e) {
                    powerProfile = null;
                }
                if (powerProfile != null) {
                    if (mMaxCapacity == null){
                        try {
                            Object obj = powerProfile.getClass().getMethod("getBatteryCapacity").invoke(powerProfile);
                            if (obj != null) {
                                if (obj instanceof Integer ||
                                        obj instanceof Double ||
                                        obj instanceof Float) {
                                    mMaxCapacity = (Double) obj;
                                }
                            }
                        } catch (NoSuchMethodException e) {
                            mMaxCapacity = null;
                        } catch (IllegalAccessException e) {
                            mMaxCapacity = null;
                        } catch (InvocationTargetException e) {
                            mMaxCapacity = null;
                        }
                    }
                    if (mCurrentCapacity == null) {
                        try {
                            Object obj = powerProfile.getClass().getMethod("getAveragePower").invoke(powerProfile, "battery.capacity");
                            if (obj != null) {
                                if (obj instanceof Integer ||
                                        obj instanceof Double ||
                                        obj instanceof Float) {
                                    mCurrentCapacity = (Double) obj;
                                }
                            }
                        } catch (NoSuchMethodException e) {
                            mCurrentCapacity = null;
                        } catch (IllegalAccessException e) {
                            mCurrentCapacity = null;
                        } catch (InvocationTargetException e) {
                            mCurrentCapacity = null;
                        }
                    }
                }
            }
            if (mMaxCapacity == null &&
                    mCurrentCapacity != null &&
                    mBatteryLevel != null){
                mMaxCapacity = new Double(mCurrentCapacity*100)/mBatteryLevel;
            } else if (mMaxCapacity != null &&
                    mCurrentCapacity == null &&
                    mBatteryLevel != null){
                mCurrentCapacity = mMaxCapacity*mBatteryLevel/100;
            } else if (mMaxCapacity != null &&
                    mCurrentCapacity != null &&
                    mBatteryLevel == null){
                mBatteryLevel = new Double(mCurrentCapacity)*100/mMaxCapacity;
            }
        }
    }
}
