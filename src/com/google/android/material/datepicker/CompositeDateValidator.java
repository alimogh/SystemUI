package com.google.android.material.datepicker;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.core.util.Preconditions;
import com.google.android.material.datepicker.CalendarConstraints;
import java.util.ArrayList;
import java.util.List;
public final class CompositeDateValidator implements CalendarConstraints.DateValidator {
    public static final Parcelable.Creator<CompositeDateValidator> CREATOR = new Parcelable.Creator<CompositeDateValidator>() { // from class: com.google.android.material.datepicker.CompositeDateValidator.1
        @Override // android.os.Parcelable.Creator
        public CompositeDateValidator createFromParcel(Parcel parcel) {
            ArrayList readArrayList = parcel.readArrayList(CalendarConstraints.DateValidator.class.getClassLoader());
            Preconditions.checkNotNull(readArrayList);
            return new CompositeDateValidator(readArrayList);
        }

        @Override // android.os.Parcelable.Creator
        public CompositeDateValidator[] newArray(int i) {
            return new CompositeDateValidator[i];
        }
    };
    private final List<CalendarConstraints.DateValidator> validators;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    private CompositeDateValidator(List<CalendarConstraints.DateValidator> list) {
        this.validators = list;
    }

    @Override // com.google.android.material.datepicker.CalendarConstraints.DateValidator
    public boolean isValid(long j) {
        for (CalendarConstraints.DateValidator dateValidator : this.validators) {
            if (dateValidator != null && !dateValidator.isValid(j)) {
                return false;
            }
        }
        return true;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeList(this.validators);
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CompositeDateValidator)) {
            return false;
        }
        return this.validators.equals(((CompositeDateValidator) obj).validators);
    }

    @Override // java.lang.Object
    public int hashCode() {
        return this.validators.hashCode();
    }
}
