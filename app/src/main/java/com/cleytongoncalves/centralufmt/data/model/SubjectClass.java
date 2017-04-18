package com.cleytongoncalves.centralufmt.data.model;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToMany;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;

@Entity
public final class SubjectClass {
	@Id
	private long id;
	
	private long subjectId;
	
	private Integer grade;
	
	private String group;
	
	private String classroom;
	
	private String crd;
	
	private Integer credit;
	
	@ToMany(referencedJoinProperty = "classId")
	private List<ClassTime> classTimes;
	
	public List<Interval> getIntervals() {
		List<Interval> intervals = new ArrayList<>(classTimes.size());
		
		for (int i = 0; i < classTimes.size(); i++) {
			intervals.add(classTimes.get(i).getInterval());
		}
		
		return intervals;
	}
	
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
	
	@Generated(hash = 428791142)
	public SubjectClass(long id, long subjectId, Integer grade, String group,
	                    String classroom, String crd, Integer credit) {
		this.id = id;
		this.subjectId = subjectId;
		this.grade = grade;
		this.group = group;
		this.classroom = classroom;
		this.crd = crd;
		this.credit = credit;
	}
	
	@Generated(hash = 1949292409)
	public SubjectClass() {
	}
	
	public long getId() {
		return this.id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getSubjectId() {
		return this.subjectId;
	}
	
	public void setSubjectId(long subjectId) {
		this.subjectId = subjectId;
	}
	
	public Integer getGrade() {
		return this.grade;
	}
	
	public void setGrade(Integer grade) {
		this.grade = grade;
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
	
	public Integer getCredit() {
		return this.credit;
	}
	
	public void setCredit(Integer credit) {
		this.credit = credit;
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
	@Generated(hash = 450549024)
	public List<ClassTime> getClassTimes() {
		if (classTimes == null) {
			final DaoSession daoSession = this.daoSession;
			if (daoSession == null) {
				throw new DaoException("Entity is detached from DAO context");
			}
			ClassTimeDao targetDao = daoSession.getClassTimeDao();
			List<ClassTime> classTimesNew = targetDao._querySubjectClass_ClassTimes(id);
			synchronized (this) {
				if (classTimes == null) {
					classTimes = classTimesNew;
				}
			}
		}
		return classTimes;
	}
	
	/**
	 * Resets a to-many relationship, making the next get call to query for a fresh result.
	 */
	@Generated(hash = 1785481954)
	public synchronized void resetClassTimes() {
		classTimes = null;
	}
	
	/**
	 * called by internal mechanisms, do not call yourself.
	 */
	@Generated(hash = 691510618)
	public void __setDaoSession(DaoSession daoSession) {
		this.daoSession = daoSession;
		myDao = daoSession != null ? daoSession.getSubjectClassDao() : null;
	}
	
}
