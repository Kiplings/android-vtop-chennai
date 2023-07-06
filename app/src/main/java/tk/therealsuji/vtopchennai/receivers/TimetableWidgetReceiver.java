package tk.therealsuji.vtopchennai.receivers;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.services.TimetableWidgetAdapterService;

public class TimetableWidgetReceiver extends AppWidgetProvider{

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_timetable);
        Intent intent = new Intent(context, TimetableWidgetAdapterService.class);
        views.setRemoteAdapter(R.id.listview, intent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

}