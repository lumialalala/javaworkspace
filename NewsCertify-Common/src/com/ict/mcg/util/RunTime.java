package com.ict.mcg.util;


/**
 * @author WuBo
 */
public class RunTime {
	private double mStart;
	private double mEnd;
	private double mTime;
	private String mName;

	public RunTime() {
		mName = "";
	}

	public RunTime(String name) {
		mName = name;
	}

	public String GetDate() {
		java.util.Calendar c = java.util.Calendar.getInstance();
		java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return (f.format(c.getTime()));
	}

	public double GetStartTime() {
		mStart = System.currentTimeMillis(); // start time
		return mStart;
	}

	public double GetEndTime() {

		mEnd = System.currentTimeMillis(); // end time
		return mEnd;
	}

	public double ComputeRunTime() {

		mTime = (mEnd - mStart) / 1000;
		if (mName != null) {
			// System.out.println(mName + " time is : " + mTime +"秒");
		} else
			System.out.println(mName + "RunTime is : " + mTime + "秒");
		return mTime;
	}

	public double ComputeRunTime(boolean print) {

		mTime = (mEnd - mStart) / 1000;
		if (print == true) {
			System.out.println(mName + "RunTime is : " + mTime + "秒");
		}
		return mTime;
	}

}
