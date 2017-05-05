package com.dexter.requestmanagement;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by jack2 on 5/5/2017.
 */

public class NavigationHelper {

    public static void showMainActivity(Context context, int id) {
        Intent intent = new Intent(context, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("display", id);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void setBackBundle(Activity activity, int navID) {

        Intent data = new Intent();
        Bundle b = new Bundle();
        b.putInt("display", navID);
        data.putExtras(b);
        activity.setResult(Activity.RESULT_OK, data);
    }
}
