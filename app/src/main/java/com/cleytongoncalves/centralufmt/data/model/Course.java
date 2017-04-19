package com.cleytongoncalves.centralufmt.data.model;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.OrderBy;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;

@Entity @SuppressWarnings("WeakerAccess")
public class Course {
	@Id
	private Long code;
	
	@Index(unique = true)
	private String title;
	
	@NotNull
	private String type;
	
	private String currentTerm;
	
	@ToMany(referencedJoinProperty = "courseCode")
	@OrderBy(value = "status ASC")
	private List<Subject> curriculum;
	
	/**
	 * Used to resolve relations
	 */
	@Generated(hash = 2040040024)
	private transient DaoSession daoSession;
	
	/** Used for active entity operations. */
	@Generated(hash = 2063667503)
	private transient CourseDao myDao;
	
	@Generated(hash = 1256009704)
	public Course(Long code, String title, @NotNull String type,
	              String currentTerm) {
		this.code = code;
		this.title = title;
		this.type = type;
		this.currentTerm = currentTerm;
	}

	@Generated(hash = 1355838961)
	public Course() {
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

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCurrentTerm() {
		return this.currentTerm;
	}

	public void setCurrentTerm(String currentTerm) {
		this.currentTerm = currentTerm;
	}

	/**
	 * To-many relationship, resolved on first access (and after reset).
	 * Changes to to-many relations are not persisted, make changes to the target entity.
	 */
	@Generated(hash = 1977912531)
	public List<Subject> getCurriculum() {
		if (curriculum == null) {
			final DaoSession daoSession = this.daoSession;
			if (daoSession == null) {
				throw new DaoException("Entity is detached from DAO context");
			}
			SubjectDao targetDao = daoSession.getSubjectDao();
			List<Subject> curriculumNew = targetDao._queryCourse_Curriculum(code);
			synchronized (this) {
				if (curriculum == null) {
					curriculum = curriculumNew;
				}
			}
		}
		return curriculum;
	}
	
	/**
	 * Resets a to-many relationship, making the next get call to query for a fresh result.
	 */
	@Generated(hash = 393959870)
	public synchronized void resetCurriculum() {
		curriculum = null;
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
	@Generated(hash = 94420068)
	public void __setDaoSession(DaoSession daoSession) {
		this.daoSession = daoSession;
		myDao = daoSession != null ? daoSession.getCourseDao() : null;
	}
	
}