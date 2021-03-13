package com.applandeo.materialcalendarsampleapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.annimon.stream.Stream;
import com.applandeo.materialcalendarsampleapp.models.CalendarModel;
import com.applandeo.materialcalendarsampleapp.utils.DrawableUtils;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.DatePicker;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.builders.DatePickerBuilder;
import com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException;
import com.applandeo.materialcalendarview.listeners.OnSelectDateListener;


import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;


import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Mateusz Kornakiewicz on 26.05.2017.
 */
public class CalendarActivity extends AppCompatActivity implements OnSelectDateListener {
    private SharedPreferences sharedPreferences;
    String savedPeriodLength, savedCycleLength;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    EditText periodLength, cycleLength;
    Button done, periodStartedBtn, intimacyAdderBtn;
    View signingView, periodStartedView, intimacySetterView;
    CalendarView calendarView;
    EditText lastPeriodDay, intimacyEditText;
    Realm realm;
    List<EventDay> events, eventsIntimacy;
    private int year, month, dayOfMonth;
    Calendar calendarObject, calendarintimacyObject;
    View viewObject;
    boolean createSigningDiaContinue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_activity);

        realm = Realm.getDefaultInstance();
        Log.i("burası", isSetup() + " is setup");
        if (isSetup()) {//Kurulum mu hayır değilse kurulum diyalogu calıssın
            createSigningDiaContinue = true;
            Log.i("burası", "oncreate_if");
            createSigningDiaolog();
        }

        periodStartedBtn = findViewById(R.id.getDateButton);
        periodStartedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!createSigningDiaContinue) {
                    createPeriodStartedDiaolog();
                }
            }
        });

        intimacyAdderBtn = findViewById(R.id.intimacyAdderBtn);
        intimacyAdderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!createSigningDiaContinue) {
                    createIntimacyDiaolog();
                }
            }
        });


        setCalendar();

    }


    private boolean isSetup() {
        sharedPreferences = this.getSharedPreferences(" com.applandeo.materialcalendarsampleapp",
                Context.MODE_PRIVATE);

        String controlValue = sharedPreferences.getString("periodLength", "");
        int value = 0;
        if (controlValue.equals("")) {
            return true;
        } else {
            return false;
        }
    }

    private Calendar dateToCalendar(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;

    }

    private void tablodanCekveSetet(String tipi) {
        realm.beginTransaction();
        RealmResults<CalendarModel> sonuclar = realm.where(CalendarModel.class)
                .equalTo("type", tipi)
                .findAll();
        Log.i("burası", "tablodanCekveSetet");
      //  Calendar calendar = Calendar.getInstance();
        //events.add(new EventDay(calendar, R.drawable.circle)); bugunu işaretliyor
        for (CalendarModel l : sonuclar) {


            Calendar cal = dateToCalendar(l.getConvertedCalendertoDate());
            Log.i("tip ", l.getType());
            if (l.getType().equals("period")) {
                events.add(new EventDay(cal, DrawableUtils.getCircleDrawableWithText(this, "P")));
            }
            if (l.getType().equals("ovulation")) {
                events.add(new EventDay(cal, R.drawable.ovulation));
            }
            if (l.getType().equals("fertile")) {
                events.add(new EventDay(cal, R.drawable.fertile));
            }
            if (l.getType().equals("intimacy")) {
                events.add(new EventDay(cal, R.drawable.kalp));
            }


            //  events.add(new EventDay(calendar4, DrawableUtils.getThreeDots(this)));
            // events.add(new EventDay(calendar2, R.drawable.sample_icon_3, Color.parseColor("#228B22")));
            //events.add(new EventDay(calendar, DrawableUtils.getCircleDrawableWithText(this, "M")));
        }

        realm.commitTransaction();
    }

    private void goster() {
        realm.beginTransaction();
        RealmResults<CalendarModel> sonuc = realm.where(CalendarModel.class).findAll();

        for (CalendarModel l : sonuc) {

            Log.i("cıktı", l.toString());
        }

        realm.commitTransaction();
    }

    private void tabloyaekle(Calendar calendarPar, String tipi) {

        realm.beginTransaction();

        CalendarModel calendarModel01 = realm.createObject(CalendarModel.class, UUID.randomUUID().toString()); //nesne olusturduk

        Date date = calendarPar.getTime();//Date formatına ceviriyoruz
        calendarModel01.setConvertedCalendertoDate(date);
        calendarModel01.setType(tipi);
        Log.i("burası", "tabloyaekle");
        realm.commitTransaction();
    }

    private void checkSavedSharedPreference() {
        sharedPreferences = this.getSharedPreferences(" com.applandeo.materialcalendarsampleapp",
                Context.MODE_PRIVATE);
        //iki parametre istiyor 1-> sharedpreferences xml imizin ismi genelde tavsiye paket ismi verilir 2-> modu private
        savedPeriodLength = sharedPreferences.getString("periodLength", "");
        savedCycleLength = sharedPreferences.getString("cycleLength", "");
        //ilki key, ikincisi oyle bir anahtarda veri yoksa ne yapılacak,
        if (savedPeriodLength != null && savedCycleLength != null) {
            Log.i("çekilen kayıt1 ", savedPeriodLength);
            Log.i("çekilen kayıt2 ", savedCycleLength);

        } else return;

    }

    private void kaydet(View view) { //sadece period ve cycle uzunluğunu kayıt eder
        sharedPreferences = this.getSharedPreferences(" com.applandeo.materialcalendarsampleapp",
                Context.MODE_PRIVATE);
        String stringPeriodLength = periodLength.getText().toString();
        String stringCycleLength = cycleLength.getText().toString();
        String stringLastPeriodDay = lastPeriodDay.getText().toString();
        int value = 0;
        if (!"".equals(stringCycleLength)) {
            value = Integer.parseInt(stringCycleLength);
        }
        if (value < 23 || value > 35) {
            Toast.makeText(getApplicationContext(), "23-35 must be!", Toast.LENGTH_LONG).show();
            return;
        }
        if (stringCycleLength == "" && stringPeriodLength == "") {
            Toast.makeText(this, "Lütfen bir değer giriniz!", Toast.LENGTH_LONG).show();
            return;
        }
        if (stringPeriodLength == null) {
            Toast.makeText(this, "Lütfen bir tarih seçiniz!", Toast.LENGTH_LONG).show();
            return;
        } else {
            sharedPreferences.edit().putString("periodLength", stringPeriodLength).apply();
            sharedPreferences.edit().putString("cycleLength", stringCycleLength).apply();

            Log.i("kayıt olan periodLength", stringPeriodLength);
            Log.i("kayıt olan cycleLength", stringCycleLength);


        }


    }

    /*  private void sil(View view) {
          alinanKullaniciAdi = sharedPreferences.getString("kullanici", "")
          if (alinanKullaniciAdi!=null) {
              textView.text="Kaydedilen Kullanıcı Adı"
              sharedPreferences.edit().remove("kullanici").apply()

          }
      }*/
    private void createSigningDiaolog() { //sadece dialog ekranı olusturur
        dialogBuilder = new AlertDialog.Builder(this);
        signingView = getLayoutInflater().inflate(R.layout.popup, null);

        periodLength = signingView.findViewById(R.id.periodLength);

        cycleLength = signingView.findViewById(R.id.cycleLength);
        cycleLength.setFilters(new InputFilter[]{new InputFilterMinMax(2, 35)});


        lastPeriodDay = signingView.findViewById(R.id.lastPeriodDay);


        Button openOneDayPickerDialog2 = (Button) signingView.findViewById(R.id.lastPeriodDayBtn);
        openOneDayPickerDialog2.setOnClickListener(v -> openOneDayPicker());

        done = signingView.findViewById(R.id.done);

        dialogBuilder.setView(signingView);
        dialog = dialogBuilder.create();
        dialog.show();
        //dialog.dismiss();

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //define Save button
                viewObject = v;
                kaydet(v);
                dialog.dismiss();
                calculate();

                createSigningDiaContinue = false;

            }
        });

    }

    private void createPeriodStartedDiaolog() { //sadece dialog ekranı olusturur
        dialogBuilder = new AlertDialog.Builder(this);
        periodStartedView = getLayoutInflater().inflate(R.layout.period_started, null);


        lastPeriodDay = periodStartedView.findViewById(R.id.lastPeriodDay);


        Button openOneDayPickerDialog = (Button) periodStartedView.findViewById(R.id.lastPeriodDayBtn);
        openOneDayPickerDialog.setOnClickListener(v -> openOneDayPicker());

        done = periodStartedView.findViewById(R.id.done);

        dialogBuilder.setView(periodStartedView);
        dialog = dialogBuilder.create();
        dialog.show();
        //dialog.dismiss();

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //define Save button
                viewObject = v;

                dialog.dismiss();


                    deleteTableAfterToday();
                    calculate();
                    setCalendar();



            }
        });

    }
    private void createIntimacyDiaolog() { //sadece dialog ekranı olusturur
        dialogBuilder = new AlertDialog.Builder(this);
        intimacySetterView = getLayoutInflater().inflate(R.layout.intimacy_setter, null);


        intimacyEditText = intimacySetterView.findViewById(R.id.intimacyDay);


        Button openOneDayPickerDialog = (Button) intimacySetterView.findViewById(R.id.intimacyBtn);
        openOneDayPickerDialog.setOnClickListener(v -> openOneDayPicker());

        done = intimacySetterView.findViewById(R.id.done);

        dialogBuilder.setView(intimacySetterView);
        dialog = dialogBuilder.create();
        dialog.show();
        //dialog.dismiss();
        calendarintimacyObject = calendarObject;

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //define Save button
                viewObject = v;

                dialog.dismiss();
              //  deleteTableAfterToday();
               tabloyaekle(calendarintimacyObject, "intimacy");
                setCalendar();

                Log.i("burası", "intimacy eklendi");

            }
        });

    }

    private void deleteTableAfterToday() {
        realm.beginTransaction();
        RealmResults<CalendarModel> sonuclar = realm.where(CalendarModel.class)
                .findAll();
        for (CalendarModel l : sonuclar) {
            Calendar calendar = Calendar.getInstance();
            //events.add(new EventDay(calendar, DrawableUtils.getCircleDrawableWithText(this, "M")));

            //Calendar cal= dateToCalendar(l.getConvertedCalendertoDate());
            if (calendar.getTime().compareTo(l.getConvertedCalendertoDate()) < 0 || calendar.getTime().compareTo(l.getConvertedCalendertoDate()) == 0) {
             l.deleteFromRealm(); //returns boolean
            }


/*// remove single match
        results.deleteFirstFromRealm();
        results.deleteLastFromRealm();

        // remove a single object
        Dog dog = results.get(5);
        dog.deleteFromRealm();

        // Delete all matches
        results.deleteAllFromRealm();

 */

            //  events.add(new EventDay(calendar4, DrawableUtils.getThreeDots(this)));
            // events.add(new EventDay(calendar2, R.drawable.sample_icon_3, Color.parseColor("#228B22")));
            //events.add(new EventDay(calendar, DrawableUtils.getCircleDrawableWithText(this, "M")));
        }

        realm.commitTransaction();
    }

    private void calculate() {
        Calendar calendarlocal = calendarObject;
        createSigningDiaContinue = false;

        int periodLength = Integer.parseInt(readPeriodLength());
        Log.i("readPeriodLength()", readPeriodLength());
        int cycleLength = Integer.parseInt(readCycleLength());
        Log.i("readCycleLength()", readCycleLength());

        int ovulationtoPeriod = cycleLength - 14;// 28-14=14 -1 =13
        int preFertileDaytoPeriod = ovulationtoPeriod - 5 - 1;//-4,-3,-2,-1, +1     14-5=9 -1=8
        int firstFDPtoPerLastDay = preFertileDaytoPeriod - periodLength; //8-4(periodlen)=4. günden başla PFD basmaya 5 tekrar sonra ovalasyon sonra +1 PFD daha bas

        //periodu ekrana basar
        for (int i = 0; i < periodLength; i++) {
            tabloyaekle(calendarlocal, "period");
            setCalendar();
           // createSigningDiaContinue = false;
            calendarlocal.add(Calendar.DAY_OF_MONTH, 1);
        }


        //FertileDays
        calendarlocal.add(Calendar.DAY_OF_MONTH, firstFDPtoPerLastDay);
        for (int i = 0; i < 5; i++) {
            tabloyaekle(calendarlocal, "fertile");
            setCalendar();
            calendarlocal.add(Calendar.DAY_OF_MONTH, 1);
        }


        //ovulationı ekrana basar
        tabloyaekle(calendarlocal, "ovulation");
        setCalendar();
        calendarlocal.add(Calendar.DAY_OF_MONTH, 1);

        //sonraki fertile bir gün
        tabloyaekle(calendarlocal, "fertile");
        setCalendar();


        //predictedperiods
        int distance = cycleLength - periodLength - firstFDPtoPerLastDay - 5 - 1;

        calendarlocal.add(Calendar.DAY_OF_MONTH, distance);
        for (int i = 0; i < periodLength; i++) {

            tabloyaekle(calendarlocal, "period");
            setCalendar();
            calendarlocal.add(Calendar.DAY_OF_MONTH, 1);
        }


        goster();//log icin


        //tabloyaekle(new EventDay(calendar, R.drawable.sample_icon_2, "period", true));
    }

    private void setCalendar() { //Sadece Database verilerine göre calendar ekranını olusturacak
        events = new ArrayList<>();

        //Show All Saved Periods on CalenderView (first pull Date format enum from model object, than convert date to calendar)

        tablodanCekveSetet("period");
        tablodanCekveSetet("fertile");
        tablodanCekveSetet("ovulation");
        tablodanCekveSetet("intimacy");


        /*
        java.util.Calendar calendar0 = java.util.Calendar.getInstance();
        events.add(new EventDay(calendar0, DrawableUtils.getCircleDrawableWithText(this, "T")));

        java.util.Calendar calendar1 = java.util.Calendar.getInstance();
        calendar1.add(java.util.Calendar.DAY_OF_MONTH, 10);
        events.add(new EventDay(calendar1, R.drawable.sample_icon_2));

         */

        CalendarView calendarView = (CalendarView) findViewById(R.id.calendarView);

        java.util.Calendar min = java.util.Calendar.getInstance();
        min.add(java.util.Calendar.MONTH, -6);

        java.util.Calendar max = java.util.Calendar.getInstance();
        max.add(java.util.Calendar.MONTH, 6);

        calendarView.setMinimumDate(min);
        calendarView.setMaximumDate(max);

        calendarView.setEvents(events); //eventsleri tablodan cekiyor ekrana basıyo
        Log.i("burası", "setCalendar_eventsset");

    }

    private void anaekranbutondinle() {

        View onedaypickerView = getLayoutInflater().inflate(R.layout.one_day_picker_activity, null);
        Button openOneDayPickerDialog = (Button) onedaypickerView.findViewById(R.id.getDateButton);
        openOneDayPickerDialog.setOnClickListener(v -> openOneDayPicker());
        Log.i("burası", "setCalendar_onedaybutonlistener");

        CalendarView calendarView = (CalendarView) onedaypickerView.findViewById(R.id.calendarView);


        // calendarView.setEvents(events);


        calendarView.setOnDayClickListener(eventDay ->
                Toast.makeText(getApplicationContext(),
                        eventDay.getCalendar().getTime().toString() + " "
                                + eventDay.isEnabled(),
                        Toast.LENGTH_SHORT).show());

        Button getDateButton = (Button) findViewById(R.id.getDateButton);
        getDateButton.setOnClickListener(v -> {
            viewObject = v;
            calculate();


        });

    }

    private void openOneDayPicker() {
        java.util.Calendar min = java.util.Calendar.getInstance();
        min.add(Calendar.MONTH, -6);
        Calendar calendartoday=Calendar.getInstance();
        java.util.Calendar max = java.util.Calendar.getInstance();
        max.add(java.util.Calendar.MONTH, 6);

        DatePickerBuilder oneDayBuilder = new DatePickerBuilder(this, this)
                .pickerType(CalendarView.ONE_DAY_PICKER)
                .date(calendartoday)
                .headerColor(R.color.colorPrimaryDark)
                .headerLabelColor(R.color.currentMonthDayColor)
                .selectionColor(R.color.daysLabelColor)
                .todayLabelColor(R.color.colorAccent)
                .dialogButtonsColor(android.R.color.holo_green_dark)
                .disabledDaysLabelsColor(android.R.color.holo_purple)
                .previousButtonSrc(R.drawable.ic_chevron_left_black_24dp)
                .forwardButtonSrc(R.drawable.ic_chevron_right_black_24dp)
                .typefaceSrc(R.font.sample_custom_font)
                .todayTypefaceSrc(R.font.sample_custom_font_bold)
                .minimumDate(min)
                .maximumDate(max)
                .todayColor(R.color.sampleLighter)
                .headerVisibility(View.VISIBLE);

        DatePicker oneDayPicker = oneDayBuilder.build();
        oneDayPicker.show();
    }

    @Override
    public void onSelect(@NotNull List<java.util.Calendar> calendars) {
        Stream.of(calendars).forEach(calendar -> {



            if (lastPeriodDay != null) {
                calendarObject = calendar;
                year = calendar.get(calendarObject.YEAR);
                month = calendar.get(calendarObject.MONTH);
                dayOfMonth = calendar.get(calendarObject.DAY_OF_MONTH);
                lastPeriodDay.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
            }
            if (intimacyEditText != null) {
                calendarintimacyObject=calendar;
                year = calendar.get(calendarintimacyObject.YEAR);
                month = calendar.get(calendarintimacyObject.MONTH);
                dayOfMonth = calendar.get(calendarintimacyObject.DAY_OF_MONTH);
                intimacyEditText.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
            }


        });
    }

    private String readCycleLength() {
        sharedPreferences = this.getSharedPreferences(" com.applandeo.materialcalendarsampleapp",
                Context.MODE_PRIVATE);
        //iki parametre istiyor 1-> sharedpreferences xml imizin ismi genelde tavsiye paket ismi verilir 2-> modu private
        savedCycleLength = sharedPreferences.getString("cycleLength", "");
        //ilki key, ikincisi oyle bir anahtarda veri yoksa ne yapılacak,
        if (savedCycleLength != null) {
            Log.i("çekilen kayıt2 ", savedCycleLength);
            return savedCycleLength;

        } else return "";
    }

    private String readPeriodLength() {
        sharedPreferences = this.getSharedPreferences(" com.applandeo.materialcalendarsampleapp",
                Context.MODE_PRIVATE);
        //iki parametre istiyor 1-> sharedpreferences xml imizin ismi genelde tavsiye paket ismi verilir 2-> modu private
        savedPeriodLength = sharedPreferences.getString("periodLength", "");

        //ilki key, ikincisi oyle bir anahtarda veri yoksa ne yapılacak,
        if (savedPeriodLength != "") {
            Log.i("çekilen kayıt1 ", savedPeriodLength);
            return savedPeriodLength;

        } else return "";
    }
}