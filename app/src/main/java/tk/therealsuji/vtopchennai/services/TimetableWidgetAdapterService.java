package tk.therealsuji.vtopchennai.services;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.helpers.AppDatabase;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;
import tk.therealsuji.vtopchennai.interfaces.TimetableDao;
import tk.therealsuji.vtopchennai.models.Timetable;

public class TimetableWidgetAdapterService extends RemoteViewsService{

    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory(getApplicationContext());
    }

    class RemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        private final Context context;
        AppDatabase appDatabase;
        TimetableDao timetableDao;
        private List<Timetable.AllData> timetable;

        public RemoteViewsFactory(Context context) {
            this.context = context;
            appDatabase = AppDatabase.getInstance(context);
            timetableDao = appDatabase.timetableDao();
            timetable = new ArrayList<>();
        }

        @Override
        public void onCreate() {
            updateTimetable();
        }

        @Override
        public void onDataSetChanged() {
            updateTimetable();
        }

        void updateTimetable(){
            int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;

            timetableDao
                    .get(day)
                    .subscribeOn(Schedulers.single())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<List<Timetable.AllData>>() {
                        @Override
                        public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<Timetable.AllData> todaysTimetable) {
                            if (todaysTimetable.size() == 0) {
                                return;
                            }
                            timetable=todaysTimetable;
                        }

                        @Override
                        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                        }
                    });
        }

        @Override
        public void onDestroy() {
            timetable.clear();
        }

        @Override
        public int getCount() {
            return timetable.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            Timetable.AllData data=timetable.get(position);
            RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_timetable_item);

            remoteView.setTextViewText(R.id.text_view_course_code, data.courseCode);
            String timing;
            try {
                timing = SettingsRepository.getSystemFormattedTime(this.context, data.startTime) +
                        " - " + SettingsRepository.getSystemFormattedTime(this.context, data.endTime);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            remoteView.setTextViewText(R.id.text_view_timings, timing);
            //change for this
            remoteView.setTextViewText(R.id.text_view_venue, "Venue TBD");//data.venue);


            if (data.courseType.equals("theory")) {
                remoteView.setImageViewResource(R.id.image_view_course_type,R.drawable.ic_theory);
            }
            // Log.w("Widget",data.courseCode+"\n"+data.startTime+"\n"+data.endTime+"\n"+timing);
            return remoteView;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

}