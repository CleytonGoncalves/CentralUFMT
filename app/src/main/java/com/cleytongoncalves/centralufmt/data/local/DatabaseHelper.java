package com.cleytongoncalves.centralufmt.data.local;


import android.content.Context;
import android.support.annotation.Nullable;

import com.cleytongoncalves.centralufmt.BuildConfig;
import com.cleytongoncalves.centralufmt.data.model.ClassTime;
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
	
	public void insertStudent(final Student student) {
		mDaoSession.getStudentDao().save(student);
	}
	
	@Nullable
	public Student getStudent() {
		return mDaoSession.getStudentDao().queryBuilder().unique();
	}
	
	public boolean hasStudent() {
		return mDaoSession.getStudentDao().count() > 0;
	}
	
	public void insertCourse(final Course course) {
		mDaoSession.getCourseDao().save(course);
	}
	
	@Nullable
	public Course getCourse() {
		return mDaoSession.getCourseDao().queryBuilder().unique();
	}
	
	public void insertSubject(final Subject subject) {
		mDaoSession.getSubjectDao().save(subject);
	}
	
	public void insertSubjectList(final List<Subject> subjects) {
		mDaoSession.getSubjectDao().saveInTx(Collections.unmodifiableList(subjects));
	}
	
	@Nullable
	public List<Subject> getSubjectList() {
		return mDaoSession.getSubjectDao().queryBuilder().list();
	}
	
	public void insertSubjectClass(final SubjectClass subjectClass) {
		mDaoSession.getSubjectClassDao().save(subjectClass);
	}
	
	public void insertSubjectClassList(final List<SubjectClass> subjectClasses) {
		mDaoSession.getSubjectClassDao().saveInTx(Collections.unmodifiableList(subjectClasses));
	}
	
	@Nullable
	public List<SubjectClass> getSubjectClassList() {
		return mDaoSession.getSubjectClassDao().queryBuilder().list();
	}
	
	public void insertClassTime(final ClassTime classTime) {
		mDaoSession.getClassTimeDao().save(classTime);
	}
	
	public void insertClassTimeList(final List<ClassTime> classTimes) {
		mDaoSession.getClassTimeDao().saveInTx(Collections.unmodifiableList(classTimes));
	}
	
	@Nullable
	public List<ClassTime> getClassTimeList() {
		return mDaoSession.getClassTimeDao().queryBuilder().list();
	}
	
	public void clearDb() {
		for (AbstractDao dao : mDaoSession.getAllDaos()) {
			dao.deleteAll();
		}
		
		mDaoSession.clear();
	}
	
	public static class DbOpenHelper extends DaoMaster.DevOpenHelper {
		private static final String DB_NAME = "centralufmt-db";
		
		@Inject
		public DbOpenHelper(@ApplicationContext Context context) {
			super(context, DB_NAME);
		}
	}
}
