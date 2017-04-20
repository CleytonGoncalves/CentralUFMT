package com.cleytongoncalves.centralufmt.data.local;


import android.content.Context;
import android.support.annotation.Nullable;

import com.cleytongoncalves.centralufmt.BuildConfig;
import com.cleytongoncalves.centralufmt.data.model.Course;
import com.cleytongoncalves.centralufmt.data.model.DaoMaster;
import com.cleytongoncalves.centralufmt.data.model.DaoSession;
import com.cleytongoncalves.centralufmt.data.model.Student;
import com.cleytongoncalves.centralufmt.data.model.Subject;
import com.cleytongoncalves.centralufmt.data.model.SubjectClass;
import com.cleytongoncalves.centralufmt.injection.ApplicationContext;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class DatabaseHelper {
	private static final String DB_NAME = "centralufmt-db";
	private final DaoSession mDaoSession;
	
	@Inject
	public DatabaseHelper(DaoSession daoSession) {
		mDaoSession = daoSession;
		
		//TODO: Remove query log on release
		if (BuildConfig.DEBUG) {
			QueryBuilder.LOG_SQL = true;
			QueryBuilder.LOG_VALUES = true;
		}
	}
	
	public void clearDb() {
		for (AbstractDao dao : mDaoSession.getAllDaos()) {
			dao.deleteAll();
		}
		
		mDaoSession.clear();
	}
	
	/* Student */
	
	public void insertStudent(final Student student) {
		mDaoSession.getStudentDao().insertOrReplace(student);
	}
	
	@Nullable
	public Student getStudent() {
		return mDaoSession.getStudentDao().queryBuilder().unique();
	}
	
	public boolean hasStudent() {
		return mDaoSession.getStudentDao().count() > 0;
	}
	
	/* Course */
	
	public void insertCourse(final Course course) {
		mDaoSession.getCourseDao().insertOrReplace(course);
	}
	
	@Nullable
	public Course getCourse() {
		return mDaoSession.getCourseDao().queryBuilder().unique();
	}
	
	/* Subject */
	
	public void insertSubjectList(final List<Subject> subjects) {
		mDaoSession.getSubjectDao().insertOrReplaceInTx(Collections.unmodifiableList(subjects));
	}
	
	@Nullable
	public List<Subject> getSubjectList() {
		return mDaoSession.getSubjectDao().queryBuilder().list();
	}
	
	/* Subject Class */
	
	public void insertSubjectClassList(final List<SubjectClass> subjectClasses) {
		mDaoSession.getSubjectClassDao()
		           .insertOrReplaceInTx(Collections.unmodifiableList(subjectClasses));
	}
	
	@Nullable
	public List<SubjectClass> getSubjectClassList() {
		return mDaoSession.getSubjectClassDao().queryBuilder().list();
	}
	
	
	public static class DbOpenHelper extends DaoMaster.DevOpenHelper {
		
		@Inject
		public DbOpenHelper(@ApplicationContext Context context) {
			super(context, DB_NAME);
		}
	}
}
