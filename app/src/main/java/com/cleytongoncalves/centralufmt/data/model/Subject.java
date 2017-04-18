package com.cleytongoncalves.centralufmt.data.model;

import android.support.annotation.IntDef;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToMany;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

@Entity
public class Subject {
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({ENROLLED, TAKEN, NOT_TAKEN}) @interface Status {}
	
	public static final transient int ENROLLED = 1;
	public static final transient int TAKEN = 2;
	public static final transient int NOT_TAKEN = 3;
	
	@Id
	private long code;
	
	private long courseId;
	
	@Index(unique = true) @NotNull
	private String title;
	
	private int courseLoad;
	
	private boolean optional;
	
	@NotNull
	private int status;
	
	private String term;
	
	@ToMany(referencedJoinProperty = "subjectId")
	private List<SubjectClass> classes;
	
	/**
	 * Used to resolve relations
	 */
	@Generated(hash = 2040040024)
	private transient DaoSession daoSession;
	/**
	 * Used for active entity operations.
	 */
	@Generated(hash = 1644932788)
	private transient SubjectDao myDao;
	
	public Subject(long courseId, long code, String title, int courseLoad, boolean optional,
	               int status) {
		this.courseId = courseId;
		this.code = code;
		this.title = title;
		this.courseLoad = courseLoad;
		this.optional = optional;
		this.status = status;
	}
	
	@Generated(hash = 158965352)
	public Subject(long code, long courseId, @NotNull String title, int courseLoad,
	               boolean optional,
	               int status, String term) {
		this.code = code;
		this.courseId = courseId;
		this.title = title;
		this.courseLoad = courseLoad;
		this.optional = optional;
		this.status = status;
		this.term = term;
	}
	
	@Generated(hash = 1617906264)
	public Subject() {
	}
	
	public long getCode() {
		return this.code;
	}
	
	public void setCode(long code) {
		this.code = code;
	}
	
	public long getCourseId() {
		return this.courseId;
	}
	
	public void setCourseId(long courseId) {
		this.courseId = courseId;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public int getCourseLoad() {
		return this.courseLoad;
	}
	
	public void setCourseLoad(int courseLoad) {
		this.courseLoad = courseLoad;
	}
	
	public boolean getOptional() {
		return this.optional;
	}
	
	public void setOptional(boolean optional) {
		this.optional = optional;
	}
	
	public int getStatus() {
		return this.status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public String getTerm() {
		return this.term;
	}
	
	public void setTerm(String term) {
		this.term = term;
	}
	
	/**
	 * To-many relationship, resolved on first access (and after reset).
	 * Changes to to-many relations are not persisted, make changes to the target entity.
	 */
	@Generated(hash = 1000189865)
	public List<SubjectClass> getClasses() {
		if (classes == null) {
			final DaoSession daoSession = this.daoSession;
			if (daoSession == null) {
				throw new DaoException("Entity is detached from DAO context");
			}
			SubjectClassDao targetDao = daoSession.getSubjectClassDao();
			List<SubjectClass> classesNew = targetDao._querySubject_Classes(code);
			synchronized (this) {
				if (classes == null) {
					classes = classesNew;
				}
			}
		}
		return classes;
	}
	
	/**
	 * Resets a to-many relationship, making the next get call to query for a fresh result.
	 */
	@Generated(hash = 681737750)
	public synchronized void resetClasses() {
		classes = null;
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
	@Generated(hash = 937984622)
	public void __setDaoSession(DaoSession daoSession) {
		this.daoSession = daoSession;
		myDao = daoSession != null ? daoSession.getSubjectDao() : null;
	}
}
