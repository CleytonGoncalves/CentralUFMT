package com.cleytongoncalves.centralufmt.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value @RequiredArgsConstructor
public class Course implements Parcelable {
	final private String mTitle;
	final private String mCode;
	final private String mType;
	final private String mCurrentTerm;
	@NonFinal @Setter List<Discipline> mEnrolledDisciplines;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.mTitle);
		dest.writeString(this.mCode);
		dest.writeString(this.mType);
		dest.writeString(this.mCurrentTerm);
		dest.writeList(this.mEnrolledDisciplines);
	}

	protected Course(Parcel in) {
		this.mTitle = in.readString();
		this.mCode = in.readString();
		this.mType = in.readString();
		this.mCurrentTerm = in.readString();
		this.mEnrolledDisciplines = new ArrayList<Discipline>();
		in.readList(this.mEnrolledDisciplines, Discipline.class.getClassLoader());
	}

	public static final Creator<Course> CREATOR = new Creator<Course>() {
		@Override
		public Course createFromParcel(Parcel source) {
			return new Course(source);
		}

		@Override
		public Course[] newArray(int size) {
			return new Course[size];
		}
	};
}