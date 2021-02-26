package com.vikingelectronics.softphone.schedules;


import android.app.Dialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.linphone.LinphoneActivity;
import org.linphone.LinphoneApp;
import org.linphone.LinphoneService;
import org.linphone.R;
import org.linphone.ScheduleInterval;
import org.linphone.ScheduleObject;
import org.linphone.fragments.FragmentsAvailable;
import org.linphone.utils.DividerItemDecorator;
import org.linphone.utils.SelectableHelper;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dagger.hilt.android.qualifiers.ApplicationContext;

@AndroidEntryPoint
public class LegacyScheduleFragment extends Fragment implements
    OnClickListener,
    OnItemClickListener,
    LegacyScheduleAdapter.ViewHolder.ClickListener,
    SelectableHelper.DeleteListener
{

    private RecyclerView scheduleList;
    private TextView noSchedules;
    private ImageView edit;
    private Switch snoozeSwitch;
    private LegacyScheduleAdapter mScheduleAdapter;
    private SelectableHelper mSelectionHelper;
    private CountDownTimer timer;

    @Inject
    @ApplicationContext
    public Context mContext;
    @Inject
    public ScheduleManager manager;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.schedule, container, false);
        mSelectionHelper = new SelectableHelper(view, this);

        noSchedules = view.findViewById(R.id.no_call_history);

        scheduleList = view.findViewById(R.id.schedule_list);
        scheduleList.setItemAnimator(null);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        scheduleList.setLayoutManager(mLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecorator(scheduleList.getContext(), mLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(mContext, R.drawable.divider)));
        scheduleList.addItemDecoration(dividerItemDecoration);

        ImageView addSchedule = view.findViewById(R.id.add_schedule);
        addSchedule.setEnabled(true);
        addSchedule.setOnClickListener(this);

        snoozeSwitch = view.findViewById(R.id.snooze_switch);
        SharedPreferences prefs = getActivity().getSharedPreferences("snooze",Context.MODE_PRIVATE);
        snoozeSwitch.setChecked(prefs.getBoolean("inSnooze", false));
        if(snoozeSwitch.isChecked()){
            long time = prefs.getLong("finishTime",0);
            if(time <= DateTime.now().getMillis()){
                manager.stopSnooze();
            }
            else{
                timer = new CountDownTimer(time-DateTime.now().getMillis(), 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        updateSnoozeTimer(millisUntilFinished);
                    }

                    @Override
                    public void onFinish() {
                        snoozeSwitch.setText("Snooze");
                        snoozeSwitch.setChecked(false);
                    }
                }.start();
            }
        }
        snoozeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences prefs1 = getActivity().getSharedPreferences("snooze",Context.MODE_PRIVATE);
            if(isChecked){
                manager.startSnooze();
                long time = prefs1.getLong("finishTime",0);
                if(time <= DateTime.now().getMillis()){
                    manager.stopSnooze();
                }
                else{
                    timer = new CountDownTimer(time-DateTime.now().getMillis(), 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            updateSnoozeTimer(millisUntilFinished);
                        }

                        @Override
                        public void onFinish() {
                            snoozeSwitch.setText("Snooze");
                            snoozeSwitch.setChecked(false);
                        }
                    }.start();
                }
            }
            else{
                manager.stopSnooze();
                try{
                    timer.cancel();
                    snoozeSwitch.setText("Snooze");
                }
                catch(Exception e){

                }
            }
        });
        edit = view.findViewById(R.id.edit);

        return view;
    }

    public void updateSnoozeTimer(long remaining){
        int minutes = (int) remaining/60000;
        int seconds = (int) remaining % 60000 /1000;
        snoozeSwitch.setText(String.format(Locale.ENGLISH, "Snooze \n %d:%02d",minutes, seconds));
    }

    public void refresh() {
        mScheduleAdapter = new LegacyScheduleAdapter(mContext, manager.getSchedules(), this, mSelectionHelper);
        scheduleList.setAdapter(mScheduleAdapter);
        mSelectionHelper.setAdapter(mScheduleAdapter);
        mSelectionHelper.setDialogMessage(R.string.schedule_delete_dialog);
        mScheduleAdapter.notifyDataSetChanged();
    }

    private boolean hideScheduleListAndDisplayMessageIfEmpty() {
        if (manager.getSchedules().isEmpty()) {
            noSchedules.setVisibility(View.VISIBLE);
            scheduleList.setVisibility(View.GONE);
            edit.setEnabled(false);
            return true;
        } else {
            noSchedules.setVisibility(View.GONE);
            scheduleList.setVisibility(View.VISIBLE);
            edit.setEnabled(true);
            return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

//        if (LinphoneActivity.isInstantiated()) {
//            LinphoneActivity.instance().selectMenu(FragmentsAvailable.SCHEDULE_LIST);
//            LinphoneActivity.instance().hideTabBar(false);
//        }

//        mSchedules = ((LinphoneApp) this.getActivity().getApplication()).getSchedule();
        if (!hideScheduleListAndDisplayMessageIfEmpty()) {
            mScheduleAdapter = new LegacyScheduleAdapter(mContext, manager.getSchedules(), this, mSelectionHelper);
            scheduleList.setAdapter(mScheduleAdapter);
            mSelectionHelper.setAdapter(mScheduleAdapter);
            mSelectionHelper.setDialogMessage(R.string.schedule_delete_dialog);
        }
    }

    @Override
    public void onPause() { super.onPause(); }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.add_schedule) {
            showScheduleDialog(v);
            refresh();
        }
        if (!hideScheduleListAndDisplayMessageIfEmpty()) {
            mScheduleAdapter = new LegacyScheduleAdapter(mContext, manager.getSchedules(), this, mSelectionHelper);
            scheduleList.setAdapter(mScheduleAdapter);
            mSelectionHelper.setAdapter(mScheduleAdapter);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        if (mScheduleAdapter.isEditable()) {
//            mSchedules = ((LinphoneApp) this.getActivity().getApplication()).getSchedule();
        }
    }

    @Override
    public void onDeleteSelection(Object[] objectsToDelete) {
        int size = mScheduleAdapter.getSelectedItemCount();
        for (int i = 0; i < size; i++) {
            try {
                ((LinphoneApp) getActivity().getApplication()).removeSchedule(objectsToDelete[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            onResume();
        }
//        mSchedules = ((LinphoneApp) getActivity().getApplication()).getSchedule();
        mScheduleAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClicked(int position) {

        //Toggles Schedule selection if in edit mode
        if (mScheduleAdapter.isEditable()) {
            mScheduleAdapter.toggleSelection(position);
        }
        //Toggles schedule activation
        else {
//            if (LinphoneActivity.isInstantiated()) {
//                try {
//                    mSchedules.get(position).toggleActive();
//                    ((LinphoneApp) getActivity().getApplication()).serializeSchedule();
//
//                    if(LinphoneService.instance().shouldAcceptCalls()){
//                        LinphoneActivity.instance().showSnoozeIndicator(false);
//                    }
//                    else if(!LinphoneService.instance().shouldAcceptCalls()){
//                        LinphoneActivity.instance().showSnoozeIndicator(true);
//                    }
//
//                    mScheduleAdapter.notifyItemChanged(position);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }

    //Toggles edit mode
    @Override
    public boolean onItemLongClicked(int position) {
        if (!mScheduleAdapter.isEditable()) {
            mSelectionHelper.enterEditMode();
        }
        mScheduleAdapter.toggleSelection(position);
        return true;
    }

    //Brings up a dialog box to create a schedule object
    public void showScheduleDialog(final View v) {
        final Dialog d = new Dialog(v.getContext());
        d.setContentView(R.layout.schedule_maker);
        final TimePicker picker = d.findViewById(R.id.picker);
        final LocalTime[] t1 = new LocalTime[1];
        final boolean[] isSecond = {false};

        //Sets the text for each day button
        int i = 0;
        for (final String day : getResources().getStringArray(R.array.days)) {
            final ToggleButton view = (ToggleButton) (((LinearLayout) d.findViewById(R.id.day_buttons)).getChildAt(i));
            if(day.equalsIgnoreCase(new DateFormatSymbols().getWeekdays()[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)])){
                view.setChecked(true);
            }
            view.setOnClickListener(
                v12 -> ((Button) v12).setText(day.substring(0, 3)));
            view.setText(day.substring(0, 3));
            i++;
        }

        (d.findViewById(R.id.next_button))
                .setOnClickListener(
                    v1 -> {
                        String toastMessage = getString(R.string.invalid_schedule_time);
                        //Choose interval start
                        if(!isSecond[0]){
                            int beginningHour = picker.getHour();
                            int beginningMinute = picker.getMinute();

                            t1[0]=new LocalTime(beginningHour, beginningMinute);
                            ((Button) v1).setText(getString(R.string.set));
                            ((TextView)d.findViewById(R.id.info_text)).setText(String.format("Block calls from %s to", LinphoneApp.parseTime(t1[0])));
                            isSecond[0] = true;

                            picker.setHour(0);
                            picker.setMinute(0);

                            d.findViewById(R.id.day_buttons).setVisibility(View.VISIBLE);
                        }
                        //Choose interval end
                        else{
                            try{
                                int endingHour = picker.getHour();
                                int endingMinute = picker.getMinute();

                                LocalTime t2 = new LocalTime(endingHour,endingMinute);
                                ScheduleInterval interval = new ScheduleInterval(t1[0], t2);

                                boolean[] days = new boolean[7];
                                boolean empty = true;
                                for (int i1 = 0; i1 < 7; i1++) {
                                    String name = getResources().getStringArray(R.array.day_ids)[i1];
                                    int id = getResources().getIdentifier(name, "id", getActivity().getPackageName());
                                    ToggleButton tb = d.findViewById(id);
                                    days[i1] = tb.isChecked();
                                    if (tb.isChecked()) {
                                        days[i1] = true;
                                        empty = false;
                                    }
                                }
                                if (empty) {
                                    toastMessage = getString(R.string.no_days_selected);
                                    throw new Exception();
                                }

                                //Check that the schedule does not overlap with an existing schedule object
                                ScheduleObject scheduleObject = new ScheduleObject(days, interval);
                                for (ScheduleObject s : ((LinphoneApp) getActivity().getApplication()).getSchedule()) {
                                    if (scheduleObject.overlap(s)) {
                                        toastMessage = getString(R.string.overlaps_with_existing);
                                        throw new Exception();
                                    }
                                }
                                ((LinphoneApp) getActivity().getApplication()).addSchedule(scheduleObject);

                                //Toast says "Ignoring calls between <interval_start> and <interval_end>"
                                toastMessage =
                                        getResources().getString(R.string.ignoring_calls_between) +
                                        LinphoneApp.parseTime(t1[0]) +
                                        getResources().getString(R.string.and) +
                                        LinphoneApp.parseTime(t2);
                                Toast toast = Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.BOTTOM, 0, 100);
                                toast.show();

                                onResume();
                                d.dismiss();
                            }
                            catch (Exception e){
                                Toast toast = Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.BOTTOM, 0, 100);
                                toast.show();
                            }
                        }
                    });
        (d.findViewById(R.id.back_button)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //Change interval start
                if(isSecond[0]){
                    ((Button)d.findViewById(R.id.next_button)).setText(getString(R.string.next));
                    ((TextView)d.findViewById(R.id.info_text)).setText(getString(R.string.Block_calls_from));
                    isSecond[0] = false;

                    picker.setHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
                    picker.setMinute(Calendar.getInstance().get(Calendar.MINUTE));

                    t1[0]=null;
                    d.findViewById(R.id.day_buttons).setVisibility(View.GONE);
                }
                else{
                    d.dismiss();
                }
            }
        });
        d.show();
    }
}
