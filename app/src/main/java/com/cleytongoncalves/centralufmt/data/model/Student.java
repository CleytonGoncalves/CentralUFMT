package com.cleytongoncalves.centralufmt.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value @AllArgsConstructor
public class Student implements Parcelable {
	String mFullName;
	String mRga;
	Course mCourse;

	public String getFirstName() {
		return mFullName.split(" ")[0];
	}

	public String getLastName() {
		String[] names = mFullName.split(" ");
		return names.length > 1 ? names[names.length - 1] : "";
	}

	@SuppressWarnings("WeakerAccess")
	protected Student(Parcel in) {
		this.mFullName = in.readString();
		this.mRga = in.readString();
		this.mCourse = in.readParcelable(Course.class.getClassLoader());
	}

	public static final Parcelable.Creator<Student> CREATOR = new Parcelable.Creator<Student>() {
		@Override
		public Student createFromParcel(Parcel source) {
			return new Student(source);
		}

		@Override
		public Student[] newArray(int size) {
			return new Student[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.mFullName);
		dest.writeString(this.mRga);
		dest.writeParcelable(this.mCourse, flags);
	}
}
