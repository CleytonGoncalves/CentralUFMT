package com.cleytongoncalves.centralufmt.data.model;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToOne;

@Entity
public class Student {
	@Id
	private long rga;
	
	@Index(unique = true) @NotNull
	private String fullName;
	
	@ToOne
	private Course course;
	
	/**
	 * Used to resolve relations
	 */
	@Generated(hash = 2040040024)
	private transient DaoSession daoSession;
	
	/**
	 * Used for active entity operations.
	 */
	@Generated(hash = 1943931642)
	private transient StudentDao myDao;
	
	@Generated(hash = 191255678)
	public Student(long rga, @NotNull String fullName) {
		this.rga = rga;
		this.fullName = fullName;
	}
	
	@Generated(hash = 1556870573)
	public Student() {
	}
	
	@Generated(hash = 1209901689)
	private transient boolean course__refreshed;
	
	public String getFirstName() {
		return fullName.split(" ")[0];
	}

	public String getLastName() {
		String[] names = fullName.split(" ");
		return names.length > 1 ? names[names.length - 1] : "";
	}
	
	public long getRga() {
		return this.rga;
	}
	
	public void setRga(long rga) {
		this.rga = rga;
	}
	
	public String getFullName() {
		return this.fullName;
	}
	
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	/**
	 * To-one relationship, resolved on first access.
	 */
	@Generated(hash = 1251910757)
	public Course getCourse() {
		if (course != null || ! course__refreshed) {
			if (daoSession == null) {
				throw new DaoException("Entity is detached from DAO context");
			}
			CourseDao targetDao = daoSession.getCourseDao();
			targetDao.refresh(course);
			course__refreshed = true;
		}
		return course;
	}
	
	/**
	 * To-one relationship, returned entity is not refreshed and may carry only the PK property.
	 */
	@Generated(hash = 639660337)
	public Course peakCourse() {
		return course;
	}
	
	/**
	 * called by internal mechanisms, do not call yourself.
	 */
	@Generated(hash = 377950061)
	public void setCourse(Course course) {
		synchronized (this) {
			this.course = course;
			course__refreshed = true;
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
	
	/**
	 * called by internal mechanisms, do not call yourself.
	 */
	@Generated(hash = 1701634981)
	public void __setDaoSession(DaoSession daoSession) {
		this.daoSession = daoSession;
		myDao = daoSession != null ? daoSession.getStudentDao() : null;
	}
}
