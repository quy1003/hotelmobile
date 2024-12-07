package com.example.hotelmobile;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.material.datepicker.CalendarConstraints;

import java.util.List;

public class CustomDateValidator implements CalendarConstraints.DateValidator {
    private final List<Long> disabledDates;

    public CustomDateValidator(List<Long> disabledDates) {
        this.disabledDates = disabledDates;
    }

    @Override
    public boolean isValid(long date) {
        return !disabledDates.contains(date);
    }

    // Parcelable implementation
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(disabledDates);
    }

    public static final Parcelable.Creator<CustomDateValidator> CREATOR = new Parcelable.Creator<CustomDateValidator>() {
        @Override
        public CustomDateValidator createFromParcel(Parcel source) {
            return new CustomDateValidator(source.readArrayList(Long.class.getClassLoader()));
        }

        @Override
        public CustomDateValidator[] newArray(int size) {
            return new CustomDateValidator[size];
        }
    };
}
