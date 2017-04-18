package com.cleytongoncalves.centralufmt.data.model;


import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.converter.PropertyConverter;
import org.joda.time.Interval;

import timber.log.Timber;

@Entity
public final class ClassTime {
	@Id
	private long id;
	
	private long classId;
	
	@Convert(converter = IntervalConverterDB.class, columnType = String.class)
	Interval interval;
	
	@Generated(hash = 792691352)
	public ClassTime(long id, long classId, Interval interval) {
		this.id = id;
		this.classId = classId;
		this.interval = interval;
	}
	
	@Generated(hash = 1889268128)
	public ClassTime() {
	}
	
	public long getId() {
		return this.id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getClassId() {
		return this.classId;
	}
	
	public void setClassId(long classId) {
		this.classId = classId;
	}
	
	public Interval getInterval() {
		return this.interval;
	}
	
	public void setInterval(Interval interval) {
		this.interval = interval;
	}
	
	static class IntervalConverterDB implements PropertyConverter<Interval, String> {
		@Override
		public Interval convertToEntityProperty(String databaseValue) {
			if (databaseValue == null) { return null; }
			
			Interval interval = null;
			try {
				String[] split = databaseValue.split(" // ");
				long start = Long.parseLong(split[0]);
				long end = Long.parseLong(split[1]);
				interval = new Interval(start, end);
			} catch (Exception e) {
				Timber.wtf(e, "Error converting interval from database.");
			}
			
			return interval;
		}
		
		@Override
		public String convertToDatabaseValue(Interval entityProperty) {
			if (entityProperty == null) { return null; }
			
			long start = entityProperty.getStart().getMillis();
			long end = entityProperty.getEnd().getMillis();
			
			return String.valueOf(start) + " // " + String.valueOf(end);
		}
	}
}
