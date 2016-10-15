package com.cleytongoncalves.centralufmt.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value @RequiredArgsConstructor
public class Curso implements Parcelable {
	public static final Parcelable.Creator<Curso> CREATOR = new Parcelable.Creator<Curso>() {
		@Override
		public Curso createFromParcel(Parcel source) {
			return new Curso(source);
		}

		@Override
		public Curso[] newArray(int size) {
			return new Curso[size];
		}
	};
	String mName;
	String mCode;
	String mType;
	@NonFinal @Setter String mCurrentSemesterCode;
	@NonFinal @Setter String mAvgFinishYears;
	@NonFinal @Setter String mMaxFinishYears;
	@NonFinal @Setter String mFinishedYears;
	/* COURSE LOAD IS GIVEN IN HOURS */
	@NonFinal @Setter String mEnrolledCourseLoad;
	@NonFinal @Setter String mObligatoryCourseLoad;
	@NonFinal @Setter String mTakenObligatoryCourseLoad;
	@NonFinal @Setter String mOptionalCourseLoad;
	@NonFinal @Setter String mTakenOptionalCourseLoad;
	@NonFinal @Setter String mComplementaryCourseLoad;
	@NonFinal @Setter String mTakenComplementaryCourseLoad;
	@NonFinal @Setter String mTakenElectiveCourseLoad;
	@NonFinal List<String> mEnrolledCourses;

	@SuppressWarnings("WeakerAccess")
	protected Curso(Parcel in) {
		this.mName = in.readString();
		this.mCode = in.readString();
		this.mType = in.readString();
		this.mCurrentSemesterCode = in.readString();
		this.mAvgFinishYears = in.readString();
		this.mMaxFinishYears = in.readString();
		this.mFinishedYears = in.readString();
		this.mEnrolledCourseLoad = in.readString();
		this.mObligatoryCourseLoad = in.readString();
		this.mTakenObligatoryCourseLoad = in.readString();
		this.mOptionalCourseLoad = in.readString();
		this.mTakenOptionalCourseLoad = in.readString();
		this.mComplementaryCourseLoad = in.readString();
		this.mTakenComplementaryCourseLoad = in.readString();
		this.mTakenElectiveCourseLoad = in.readString();
		this.mEnrolledCourses = in.createStringArrayList();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.mName);
		dest.writeString(this.mCode);
		dest.writeString(this.mType);
		dest.writeString(this.mCurrentSemesterCode);
		dest.writeString(this.mAvgFinishYears);
		dest.writeString(this.mMaxFinishYears);
		dest.writeString(this.mFinishedYears);
		dest.writeString(this.mEnrolledCourseLoad);
		dest.writeString(this.mObligatoryCourseLoad);
		dest.writeString(this.mTakenObligatoryCourseLoad);
		dest.writeString(this.mOptionalCourseLoad);
		dest.writeString(this.mTakenOptionalCourseLoad);
		dest.writeString(this.mComplementaryCourseLoad);
		dest.writeString(this.mTakenComplementaryCourseLoad);
		dest.writeString(this.mTakenElectiveCourseLoad);
		dest.writeStringList(this.mEnrolledCourses);
	}
}