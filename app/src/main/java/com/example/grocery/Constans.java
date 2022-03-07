package com.example.grocery;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.view.Display;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Constans  {

    public static final String FCM_KEY = "AAAA35lUq-U:APA91bFSF1cN8rFAjxgR5rVZF1lkP_i2raQbotwTyqp9j9anG1WKgGPERGt18dUva75cXSDkYi_h-PRYTx0-HqUttUieyWssIn6N0fsXNuXhjf0QQvF6sYNXHtrBewJNgFQBiu3ByS-D";
    public static final String FCM_TOPIC = "PUSH_NOTIFICATIONS";

    //product catagory
    public static final String[] productCategories ={
            "Beverages",
            "Beaut & Personnal Care",
            "baby Kids",
            "Biscuits Snacks & Chocolates",
            "Breakfast & Dairy",
            "Cooking Needs",
            "Frozen Food",
            "Fruites",
            "Pet Cate",
            "Pharmacy",
            "Vagetable",
            "Others"
    };

    public static final String[] productCategories1 ={
            "All",
            "Beverages",
            "Beaut & Personnal Care",
            "baby Kids",
            "Biscuits Snacks & Chocolates",
            "Breakfast & Dairy",
            "Cooking Needs",
            "Frozen Food",
            "Fruites",
            "Pet Cate",
            "Pharmacy",
            "Vagetable",
            "Others"
    };


}
