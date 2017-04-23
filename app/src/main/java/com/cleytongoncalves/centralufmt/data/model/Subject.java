package com.cleytongoncalves.centralufmt.data.model;

import android.support.annotation.IntDef;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToMany;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

@SuppressWarnings("WeakerAccess") @Entity
public class Subject {
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({ENROLLED, PAST, FUTURE, OPTIONAL}) @interface Status {}
	public static final transient int ENROLLED = 1;
	public static final transient int PAST = 2;
	public static final transient int FUTURE = 3;
	public static final transient int OPTIONAL = 4;
	
	private Long courseCode;
	
	@Id
	private Long code;
	
	@Index(unique = true)
	private String title;
	
	@NotNull
	private String workLoad;
	
	@NotNull
	private String term;
	
	private int status;
	
	@ToMany(referencedJoinProperty = "subjectCode")
	private List<SubjectClass> classes;
	
	/**
	 * Used to resolve relations
	 */
	@Generated(hash = 2040040024)
	private transient DaoSession daoSession;
	/** Used for active entity operations. */
	@Generated(hash = 1644932788)
	private transient SubjectDao myDao;
	
	public Subject(Long code, String title, String courseLoad, String term, int status) {
		this.code = code;
		this.title = title;
		this.workLoad = courseLoad;
		this.term = term;
		this.status = status;
	}
	
	@Generated(hash = 1617906264)
	public Subject() {
	}
	
	@Generated(hash = 1700175837)
	public Subject(Long courseCode, Long code, String title, @NotNull String workLoad,
	               @NotNull String term, int status) {
		this.courseCode = courseCode;
		this.code = code;
		this.title = title;
		this.workLoad = workLoad;
		this.term = term;
		this.status = status;
	}

	public boolean isOptional() {
		return status == Subject.OPTIONAL;
	}
	
	public Long getCourseCode() {
		return this.courseCode;
	}
	
	public void setCourseCode(Long courseCode) {
		this.courseCode = courseCode;
	}
	
	public Long getCode() {
		return this.code;
	}
	
	public void setCode(Long code) {
		this.code = code;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getStatus() {
		return this.status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getTerm() {
		return isOptional() ? "" : this.term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	/**
	 * To-many relationship, resolved on first access (and after reset).
	 * Changes to to-many relations are not persisted, make changes to the target entity.
	 */
	@Keep
	public List<SubjectClass> getClasses() {
		if (status != Subject.ENROLLED) {
			throw new UnsupportedOperationException("Only enrolled subjects have classes");
		}
		
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
	
	/** Resets a to-many relationship, making the next get call to query for a fresh result. */
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
	
	public String getWorkLoad() {
		return this.workLoad;
	}
	
	public void setWorkLoad(String workLoad) {
		this.workLoad = workLoad;
	}
	
	/** called by internal mechanisms, do not call yourself. */
	@Generated(hash = 937984622)
	public void __setDaoSession(DaoSession daoSession) {
		this.daoSession = daoSession;
		myDao = daoSession != null ? daoSession.getSubjectDao() : null;
	}
}
