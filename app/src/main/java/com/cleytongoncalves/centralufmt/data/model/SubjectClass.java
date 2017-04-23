package com.cleytongoncalves.centralufmt.data.model;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.converter.PropertyConverter;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

@SuppressWarnings("WeakerAccess") @Entity
public final class SubjectClass {
	private Long subjectCode;
	
	@Id
	private Long id;
	
	private String group;
	
	private String classroom;
	
	private String crd;
	
	private String type;
	
	@Convert(converter = IntervalListConverter.class, columnType = String.class)
	private List<Interval> classTimes;
	
	@ToOne(joinProperty = "subjectCode")
	private Subject subject;
	
	/**
	 * Used to resolve relations
	 */
	@Generated(hash = 2040040024)
	private transient DaoSession daoSession;
	
	/** Used for active entity operations. */
	@Generated(hash = 313633891)
	private transient SubjectClassDao myDao;
	
	@Generated(hash = 711858396)
	private transient Long subject__resolvedKey;
	
	public SubjectClass(Long subjectCode, String group, String classroom, String crd,
	                    String type, List<Interval> classTimes) {
		this.subjectCode = subjectCode;
		this.group = group;
		this.classroom = classroom;
		this.crd = crd;
		this.type = type;
		this.classTimes = classTimes;
	}
	
	@Generated(hash = 535274261)
	public SubjectClass(Long subjectCode, Long id, String group, String classroom, String crd,
	                    String type, List<Interval> classTimes) {
		this.subjectCode = subjectCode;
		this.id = id;
		this.group = group;
		this.classroom = classroom;
		this.crd = crd;
		this.type = type;
		this.classTimes = classTimes;
	}

	@Generated(hash = 1949292409)
	public SubjectClass() {
	}
	
	public Long getSubjectId() {
		return this.subjectCode;
	}
	
	public void setSubjectId(Long subjectId) {
		this.subjectCode = subjectId;
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
	
	public List<Interval> getClassTimes() {
		return this.classTimes != null ? this.classTimes : Collections.emptyList();
	}
	
	public void setClassTimes(List<Interval> classTimes) {
		this.classTimes = classTimes;
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
	
	/** called by internal mechanisms, do not call yourself. */
	@Generated(hash = 691510618)
	public void __setDaoSession(DaoSession daoSession) {
		this.daoSession = daoSession;
		myDao = daoSession != null ? daoSession.getSubjectClassDao() : null;
	}
	
	
	static class IntervalListConverter implements PropertyConverter<List<Interval>, String> {
		@Override
		public List<Interval> convertToEntityProperty(String databaseValue) {
			String[] strArr = databaseValue.split(" && ");
			List<Interval> list = new ArrayList<>(strArr.length);
			
			for (String intervalStr : strArr) {
				list.add(convertStringToInterval(intervalStr));
			}
			
			return list;
		}
		
		@Override
		public String convertToDatabaseValue(List<Interval> list) {
			StringBuilder listStr = new StringBuilder();
			
			for (int i = 0, sz = list.size(); i < sz; i++) {
				if (i != 0) { listStr.append(" && "); }
				listStr.append(convertIntervalToString(list.get(i)));
			}
			
			return listStr.toString();
		}
		
		private Interval convertStringToInterval(String str) {
			if (str == null || str.isEmpty()) { return null; }
			
			Interval interval = null;
			try {
				String[] split = str.split("->");
				long start = Long.parseLong(split[0]);
				long end = Long.parseLong(split[1]);
				interval = new Interval(start, end);
			} catch (Exception e) {
				Timber.wtf(e, "Error converting String to Interval.");
			}
			
			return interval;
		}
		
		private String convertIntervalToString(Interval interval) {
			if (interval == null) { return ""; }
			
			long start = interval.getStart().getMillis();
			long end = interval.getEnd().getMillis();
			
			return String.valueOf(start) + "->" + String.valueOf(end);
		}
	}
	
}
