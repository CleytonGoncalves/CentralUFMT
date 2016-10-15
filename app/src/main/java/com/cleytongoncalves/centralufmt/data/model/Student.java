package com.cleytongoncalves.centralufmt.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value @AllArgsConstructor
public class Student implements Parcelable {
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
	String mFullName;
	String mRga;
	Curso mCurso;

	@SuppressWarnings("WeakerAccess")
	protected Student(Parcel in) {
		this.mFullName = in.readString();
		this.mRga = in.readString();
		this.mCurso = in.readParcelable(Curso.class.getClassLoader());
	}

	public static Student createStub() {
		return new Student("Não Logado", "", new Curso("", "", ""));
	}

	public String getFirstName() {
		return mFullName.split(" ")[0];
	}

	public String getLastName() {
		String[] names = mFullName.split(" ");
		return names.length > 1 ? names[names.length - 1] : "";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.mFullName);
		dest.writeString(this.mRga);
		dest.writeParcelable(this.mCurso, flags);
	}
}
