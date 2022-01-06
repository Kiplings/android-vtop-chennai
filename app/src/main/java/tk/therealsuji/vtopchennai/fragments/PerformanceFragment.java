package tk.therealsuji.vtopchennai.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.adapters.MarksAdapter;
import tk.therealsuji.vtopchennai.helpers.AppDatabase;
import tk.therealsuji.vtopchennai.interfaces.MarksDao;
import tk.therealsuji.vtopchennai.models.Course;
import tk.therealsuji.vtopchennai.models.CumulativeMark;
import tk.therealsuji.vtopchennai.widgets.PerformanceCard;

public class PerformanceFragment extends Fragment {

    public PerformanceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View performanceFragment = inflater.inflate(R.layout.fragment_performance, container, false);

        AppBarLayout appBarLayout = performanceFragment.findViewById(R.id.app_bar);
        ViewPager2 marks = performanceFragment.findViewById(R.id.view_pager_marks);

        appBarLayout.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            view.setPadding(
                    windowInsets.getSystemWindowInsetLeft(),
                    windowInsets.getSystemWindowInsetTop(),
                    windowInsets.getSystemWindowInsetRight(),
                    0
            );

            return windowInsets;
        });

        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            LinearLayout header = performanceFragment.findViewById(R.id.linear_layout_header);
            float alpha = 1 - ((float) (-1 * verticalOffset) / header.getHeight());

            header.setAlpha(alpha);
        });

        marks.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            view.setPadding(
                    windowInsets.getSystemWindowInsetLeft(),
                    0,
                    windowInsets.getSystemWindowInsetRight(),
                    0
            );

            return windowInsets;
        });

        float pixelDensity = this.getResources().getDisplayMetrics().density;

        getParentFragmentManager().setFragmentResultListener("customInsets", this, (requestKey, result) -> {
            int bottomNavigationHeight = result.getInt("bottomNavigationHeight");
            marks.setPageTransformer((page, position) -> {
                // Setting padding to the RecyclerView child inside the RelativeLayout
                ((RelativeLayout) page).getChildAt(0).setPadding(0, 0, 0, (int) (bottomNavigationHeight + 20 * pixelDensity));
            });
        });

        TabLayout courseTabs = performanceFragment.findViewById(R.id.tab_layout_courses);

        AppDatabase appDatabase = AppDatabase.getInstance(requireActivity().getApplicationContext());
        MarksDao marksDao = appDatabase.marksDao();

        marksDao
                .getCourses()
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Course>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onSuccess(@NonNull List<Course> courses) {
                        marks.setAdapter(new MarksAdapter(courses));
                        new TabLayoutMediator(courseTabs, marks, (tab, position) -> {
                            Course course = courses.get(position);

                            tab.setText(course.code);
                            TooltipCompat.setTooltipText(tab.view, course.title);
                            tab.view.setContentDescription(course.title);
                        }).attach();

                        for (int i = 0; i < courses.size(); ++i) {
                            View day = ((ViewGroup) courseTabs.getChildAt(0)).getChildAt(i);
                            ViewGroup.MarginLayoutParams tabParams = (ViewGroup.MarginLayoutParams) day.getLayoutParams();

                            if (i == 0) {
                                tabParams.setMarginStart((int) (20 * pixelDensity));
                                tabParams.setMarginEnd((int) (5 * pixelDensity));
                            } else if (i == courseTabs.getTabCount() - 1) {
                                tabParams.setMarginStart((int) (5 * pixelDensity));
                                tabParams.setMarginEnd((int) (20 * pixelDensity));
                            } else {
                                tabParams.setMarginStart((int) (5 * pixelDensity));
                                tabParams.setMarginEnd((int) (5 * pixelDensity));
                            }
                        }

                        PerformanceCard overall = performanceFragment.findViewById(R.id.performance_card_overall);
                        PerformanceCard theory = performanceFragment.findViewById(R.id.performance_card_theory);
                        PerformanceCard project = performanceFragment.findViewById(R.id.performance_card_project);
                        PerformanceCard lab = performanceFragment.findViewById(R.id.performance_card_lab);


                        marks.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                            @Override
                            public void onPageSelected(int position) {
                                super.onPageSelected(position);

                                overall.setIndeterminate(true);
                                theory.setIndeterminate(true);
                                project.setIndeterminate(true);
                                lab.setIndeterminate(true);

                                marksDao
                                        .getCumulativeMark(courses.get(position).code)
                                        .subscribeOn(Schedulers.single())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new SingleObserver<CumulativeMark>() {
                                            @Override
                                            public void onSubscribe(@NonNull Disposable d) {
                                            }

                                            @Override
                                            public void onSuccess(@NonNull CumulativeMark cumulativeMark) {
                                                if (cumulativeMark.grandTotal != null) {
                                                    overall.show();
                                                    overall.setIndeterminate(false);
                                                    overall.setScore(cumulativeMark.grandTotal, cumulativeMark.grandMax);
                                                } else {
                                                    overall.hide();
                                                }

                                                if (cumulativeMark.theoryTotal != null) {
                                                    theory.show();
                                                    theory.setIndeterminate(false);
                                                    theory.setScore(cumulativeMark.theoryTotal, cumulativeMark.theoryMax);
                                                } else {
                                                    theory.hide();
                                                }

                                                if (cumulativeMark.projectTotal != null) {
                                                    project.show();
                                                    project.setIndeterminate(false);
                                                    project.setScore(cumulativeMark.projectTotal, cumulativeMark.projectMax);
                                                } else {
                                                    project.hide();
                                                }

                                                if (cumulativeMark.labTotal != null) {
                                                    lab.show();
                                                    lab.setIndeterminate(false);
                                                    lab.setScore(cumulativeMark.labTotal, cumulativeMark.labMax);
                                                } else {
                                                    lab.hide();
                                                }
                                            }

                                            @Override
                                            public void onError(@NonNull Throwable e) {
                                            }
                                        });
                            }
                        });
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                    }
                });

        return performanceFragment;
    }
}