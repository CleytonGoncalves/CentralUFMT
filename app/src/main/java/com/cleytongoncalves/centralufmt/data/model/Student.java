package com.cleytongoncalves.centralufmt.data.model;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;

@SuppressWarnings("WeakerAccess") @Entity
public class Student {
	@Id
	private Long rga;
	
	private String fullName;
	
	private Long courseCode;
	
	@ToOne(joinProperty = "courseCode")
	private Course course;
	
	/**
	 * Used to resolve relations
	 */
	@Generated(hash = 2040040024)
	private transient DaoSession daoSession;
	
	/** Used for active entity operations. */
	@Generated(hash = 1943931642)
	private transient StudentDao myDao;
	
	@Generated(hash = 13676306)
	private transient Long course__resolvedKey;
	
	public Student(Long rga, String fullName) {
		this.rga = rga;
		this.fullName = fullName;
	}
	
	@Generated(hash = 914131345)
	public Student(Long rga, String fullName, Long courseCode) {
		this.rga = rga;
		this.fullName = fullName;
		this.courseCode = courseCode;
	}

	@Generated(hash = 1556870573)
	public Student() {
	}
	
	public Long getRga() {
		return this.rga;
	}
	
	public void setRga(Long rga) {
		this.rga = rga;
	}

	public String getFullName() {
		return this.fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	public String getFirstName() {
		return this.fullName.split(" ")[0];
	}
	
	public String getLastName() {
		String[] split = this.fullName.split(" ");
		return (split.length > 1) ? split[split.length - 1] : "";
	}
	
	public Long getCourseCode() {
		return this.courseCode;
	}
	
	public void setCourseCode(Long courseCode) {
		this.courseCode = courseCode;
	}
	
	/**
	 * To-one relationship, resolved on first access.
	 */
	@Generated(hash = 1362247244)
	public Course getCourse() {
		Long __key = this.courseCode;
		if (course__resolvedKey == null || ! course__resolvedKey.equals(__key)) {
			final DaoSession daoSession = this.daoSession;
			if (daoSession == null) {
				throw new DaoException("Entity is detached from DAO context");
			}
			CourseDao targetDao = daoSession.getCourseDao();
			Course courseNew = targetDao.load(__key);
			synchronized (this) {
				course = courseNew;
				course__resolvedKey = __key;
			}
		}
		return course;
	}
	
	/**
	 * called by internal mechanisms, do not call yourself.
	 */
	@Generated(hash = 1305619617)
	public void setCourse(Course course) {
		synchronized (this) {
			this.course = course;
			courseCode = course == null ? null : course.getCode();
			course__resolvedKey = courseCode;
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
	
	/** called by internal mechanisms, do not call yourself. */
	@Generated(hash = 1701634981)
	public void __setDaoSession(DaoSession daoSession) {
		this.daoSession = daoSession;
		myDao = daoSession != null ? daoSession.getStudentDao() : null;
	}
}