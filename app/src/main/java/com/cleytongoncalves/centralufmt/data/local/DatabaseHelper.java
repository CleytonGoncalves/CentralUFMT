package com.cleytongoncalves.centralufmt.data.local;


import android.content.Context;
import android.support.annotation.Nullable;

import com.cleytongoncalves.centralufmt.data.model.DaoMaster;
import com.cleytongoncalves.centralufmt.data.model.DaoSession;
import com.cleytongoncalves.centralufmt.data.model.Student;
import com.cleytongoncalves.centralufmt.injection.ApplicationContext;

import org.greenrobot.greendao.AbstractDao;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class DatabaseHelper {
	private final DaoSession mDaoSession;
	
	@Inject
	public DatabaseHelper(DaoSession daoSession) {
		mDaoSession = daoSession;
	}
	
	public void insertStudent(final Student student) {
		mDaoSession.getStudentDao().insertOrReplace(student);
	}
	
	@Nullable
	public Student getStudent() {
		return mDaoSession.getStudentDao().queryBuilder().unique();
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
