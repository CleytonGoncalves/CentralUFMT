package com.cleytongoncalves.centralufmt.data.model;


import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.ArrayList;
import java.util.List;

import static org.joda.time.DateTimeConstants.FRIDAY;
import static org.joda.time.DateTimeConstants.MONDAY;
import static org.joda.time.DateTimeConstants.SATURDAY;
import static org.joda.time.DateTimeConstants.SUNDAY;
import static org.joda.time.DateTimeConstants.THURSDAY;
import static org.joda.time.DateTimeConstants.TUESDAY;
import static org.joda.time.DateTimeConstants.WEDNESDAY;

@Entity
public final class Schedule {
	@Id
	private Long id;
	
	private transient List<SubjectClass> monday = new ArrayList<>();
	
	private transient List<SubjectClass> tuesday = new ArrayList<>();
	
	private transient List<SubjectClass> wednesday = new ArrayList<>();
	
	private transient List<SubjectClass> thursday = new ArrayList<>();
	
	private transient List<SubjectClass> friday = new ArrayList<>();
	
	private transient List<SubjectClass> saturday = new ArrayList<>();
	
	private transient List<SubjectClass> sunday = new ArrayList<>();
	
	@ToMany(referencedJoinProperty = "scheduleId")
	private List<SubjectClass> allClasses;
	
	/**
	 * Used to resolve relations
	 */
	@Generated(hash = 2040040024)
	private transient DaoSession daoSession;
	
	/**
	 * Used for active entity operations.
	 */
	@Generated(hash = 1493574644)
	private transient ScheduleDao myDao;
	
	public Schedule(List<SubjectClass> allClasses) {
		this.allClasses = allClasses;
		parseWeekdaysSchedule();
	}
	
	@Keep
	public Schedule(Long id) {
		this.id = id;
	}
	
	@Keep
	public Schedule() {
	}
	
	public boolean hasSaturdayClasses() {
		return saturday.isEmpty();
	}
	
	public boolean hasSundayClasses() {
		return sunday.isEmpty();
	}
	
	public boolean isEmpty() {
		return getAllClasses().isEmpty();
	}
	
	public List<SubjectClass> getWeekdayClasses(int weekdayConstant) {
		if (allClasses == null) { syncWeekdaysClasses(); }
		
		switch (weekdayConstant) {
			case MONDAY:
				return monday;
			case TUESDAY:
				return tuesday;
			case WEDNESDAY:
				return wednesday;
			case THURSDAY:
				return thursday;
			case FRIDAY:
				return friday;
			case SATURDAY:
				return saturday;
			case SUNDAY:
				return sunday;
			default:
				throw new IllegalArgumentException("Invalid weekday constant");
		}
	}
	
	private void syncWeekdaysClasses() {
		getAllClasses(); //Resolve the to-many relationship
		parseWeekdaysSchedule();
	}
	
	private void parseWeekdaysSchedule() {
		for (SubjectClass currClass : allClasses) {
			int weekday = currClass.getClassTime().getWeekday();
			getWeekdayClasses(weekday).add(currClass);
		}
	}
	
	public Long getId() {
		return this.id;
	}
	
	public void setId(Long id) {
		this.id = id;
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
	
	/**
	 * To-many relationship, resolved on first access (and after reset).
	 * Changes to to-many relations are not persisted, make changes to the target entity.
	 */
	@Generated(hash = 1782850011)
	public List<SubjectClass> getAllClasses() {
		if (allClasses == null) {
			final DaoSession daoSession = this.daoSession;
			if (daoSession == null) {
				throw new DaoException("Entity is detached from DAO context");
			}
			SubjectClassDao targetDao = daoSession.getSubjectClassDao();
			List<SubjectClass> allClassesNew = targetDao._querySchedule_AllClasses(id);
			synchronized (this) {
				if (allClasses == null) {
					allClasses = allClassesNew;
				}
			}
		}
		return allClasses;
	}
	
	/**
	 * Resets a to-many relationship, making the next get call to query for a fresh result.
	 */
	@Generated(hash = 1971335523)
	public synchronized void resetAllClasses() {
		allClasses = null;
	}
	
	/**
	 * called by internal mechanisms, do not call yourself.
	 */
	@Generated(hash = 502317300)
	public void __setDaoSession(DaoSession daoSession) {
		this.daoSession = daoSession;
		myDao = daoSession != null ? daoSession.getScheduleDao() : null;
	}
	
}
