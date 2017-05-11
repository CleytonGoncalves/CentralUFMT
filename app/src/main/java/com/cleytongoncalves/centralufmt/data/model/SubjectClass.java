package com.cleytongoncalves.centralufmt.data.model;

import android.support.annotation.NonNull;

import com.cleytongoncalves.centralufmt.util.converter.DBConverterAsString;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;
import org.joda.time.Interval;

@SuppressWarnings("WeakerAccess") @Entity
public final class SubjectClass implements Comparable<SubjectClass> {
	private Long subjectCode;
	
	private Long scheduleId;
	
	@Id
	private Long id;
	
	private String group;
	
	private String classroom;
	
	private String crd;
	
	private String type;
	
	@Convert(converter = ClassTimeConverter.class, columnType = String.class)
	private ClassTime classTime;
	
	@ToOne(joinProperty = "subjectCode")
	private Subject subject;
	
	/**
	 * Used to resolve relations
	 */
	@Generated(hash = 2040040024)
	private transient DaoSession daoSession;
	
	/**
	 * Used for active entity operations.
	 */
	@Generated(hash = 313633891)
	private transient SubjectClassDao myDao;
	
	@Generated(hash = 711858396)
	private transient Long subject__resolvedKey;
	
	public SubjectClass(Long subjectCode, String group, String classroom, String crd,
	                    String type, ClassTime classTime) {
		this.subjectCode = subjectCode;
		this.group = group;
		this.classroom = classroom;
		this.crd = crd;
		this.type = type;
		this.classTime = classTime;
	}
	
	@Generated(hash = 600261141)
	public SubjectClass(Long subjectCode, Long scheduleId, Long id, String group,
	                    String classroom, String crd, String type, ClassTime classTime) {
		this.subjectCode = subjectCode;
		this.scheduleId = scheduleId;
		this.id = id;
		this.group = group;
		this.classroom = classroom;
		this.crd = crd;
		this.type = type;
		this.classTime = classTime;
	}
	
	@Generated(hash = 1949292409)
	public SubjectClass() {
	}
	
	public String getGroup() {
		return this.group;
	}
	
	public void setGroup(String group) {
		this.group = group;
	}
	
	public String getClassroom() {
		return this.classroom;
	}
	
	public void setClassroom(String classroom) {
		this.classroom = classroom;
	}
	
	public String getCrd() {
		return this.crd;
	}
	
	public void setCrd(String crd) {
		this.crd = crd;
	}
	
	public String getType() {
		return this.type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public Long getSubjectCode() {
		return this.subjectCode;
	}
	
	public void setSubjectCode(Long subjectCode) {
		this.subjectCode = subjectCode;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * To-one relationship, resolved on first access.
	 */
	@Generated(hash = 201052022)
	public Subject getSubject() {
		Long __key = this.subjectCode;
		if (subject__resolvedKey == null || ! subject__resolvedKey.equals(__key)) {
			final DaoSession daoSession = this.daoSession;
			if (daoSession == null) {
				throw new DaoException("Entity is detached from DAO context");
			}
			SubjectDao targetDao = daoSession.getSubjectDao();
			Subject subjectNew = targetDao.load(__key);
			synchronized (this) {
				subject = subjectNew;
				subject__resolvedKey = __key;
			}
		}
		return subject;
	}
	
	/**
	 * called by internal mechanisms, do not call yourself.
	 */
	@Generated(hash = 549336497)
	public void setSubject(Subject subject) {
		synchronized (this) {
			this.subject = subject;
			subjectCode = subject == null ? null : subject.getCode();
			subject__resolvedKey = subjectCode;
		}
	}
	
	/**
	 * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
	 * Entity must attached to an entity context.
	 */
	@Generated(hash = 128553479)
	public void delete() {
		if (myDao == null) {
			throw new DaoException("Entity is detached from DAO context");
		}
		myDao.delete(this);
	}
	
	/**
	 * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
	 * Entity must attached to an entity context.
	 */
	@Generated(hash = 1942392019)
	public void refresh() {
		if (myDao == null) {
			throw new DaoException("Entity is detached from DAO context");
		}
		myDao.refresh(this);
	}
	
	/**
	 * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
	 * Entity must attached to an entity context.
	 */
	@Generated(hash = 713229351)
	public void update() {
		if (myDao == null) {
			throw new DaoException("Entity is detached from DAO context");
		}
		myDao.update(this);
	}
	
	public Long getId() {
		return this.id;
	}
	
	public Long getScheduleId() {
		return this.scheduleId;
	}
	
	public void setScheduleId(Long scheduleId) {
		this.scheduleId = scheduleId;
	}
	
	public ClassTime getClassTime() {
		return this.classTime;
	}
	
	public void setClassTime(ClassTime classTime) {
		this.classTime = classTime;
	}
	
	@Override
	public int compareTo(@NonNull SubjectClass o) {
		return this.classTime.compareTo(o.getClassTime());
	}

	/** called by internal mechanisms, do not call yourself. */
	@Generated(hash = 691510618)
	public void __setDaoSession(DaoSession daoSession) {
		this.daoSession = daoSession;
		myDao = daoSession != null ? daoSession.getSubjectClassDao() : null;
	}
	
	static class ClassTimeConverter extends DBConverterAsString<ClassTime> {
		@Override
		public ClassTime convertToEntityProperty(String databaseValue) {
			String[] strArr = databaseValue.split(SEPARATOR);
			Interval interval = new Interval(Long.parseLong(strArr[0]), Long.parseLong(strArr[1]));
			return new ClassTime(interval);
		}
		
		@Override
		public String convertToDatabaseValue(ClassTime entityProperty) {
			Interval interval = entityProperty.getInterval();
			long start = interval.getStartMillis();
			long end = interval.getEndMillis();
			
			return String.valueOf(start) + SEPARATOR + String.valueOf(end);
		}
	}
	
}
