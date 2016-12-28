package com.cleytongoncalves.centralufmt.data.model;

import org.immutables.value.Value;
import org.joda.time.Interval;

import java.util.List;

@Value.Immutable()
abstract class AbstractDiscipline implements DisciplineI {
	public static final int ENROLLED_STATUS = 0;
	public static final int NOT_ENROLLED_STATUS = 1;
	public static final int COMPLETED_STATUS = 2;

	@Override
	public abstract String getCode();

	@Override
	public abstract String getTitle();

	@Override
	public abstract String getCourseLoad();

	@Override
	public abstract String getTerm();

	@Override
	public abstract int getStatus();

	@Value.Immutable()
	static abstract class AbstractEnrolledDiscipline implements DisciplineI {

		@Override
		public abstract String getCode();

		@Override
		public abstract String getTitle();

		@Override
		public abstract String getCourseLoad();

		@Override
		public abstract String getTerm();

		public abstract String getGroup();

		public abstract String getRoom();

		public abstract String getCrd();

		public abstract String getType();

		public abstract List<Interval> getClassTimes();

		@Override
		public int getStatus() {
			return ENROLLED_STATUS;
		}
	}
}
