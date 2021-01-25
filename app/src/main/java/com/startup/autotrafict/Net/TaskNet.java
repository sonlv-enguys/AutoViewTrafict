package com.startup.autotrafict.Net;

import android.content.Context;

import com.telpoo.frame.model.BaseModel;
import com.telpoo.frame.model.TaskParams;
import com.telpoo.frame.net.BaseNetSupport;
import com.telpoo.frame.object.BaseObject;

/**
 * Created by naq on 05/04/2015.
 */
public class TaskNet extends MyTask {
    NetData dataApi;

    public TaskNet() {
        super();
    }

    public TaskNet(BaseModel model, int taskType, Context context) {
        super(model, taskType, context);
    }

    public TaskNet(BaseModel model, Context context) {
        super(model, context);
    }

    @Override
    protected Boolean doInBackground(TaskParams... params) {
        if (context == null) {
            return TASK_FAILED;
        }
        if (context != null && !BaseNetSupport.isNetworkAvailable(context)) {
            msg = "Không có kết nối internet";
            return TASK_FAILED;

        }

        switch (taskType) {
            case TASK_TASK:
                dataApi = NetSupport.getTasks(context);
                break;
            case TASK_LOGIN:
                dataApi = NetSupport.login(context,getParramObject());
                break;
        }
        return processData();
    }


    public boolean processData() {
        if (dataApi == null) return TASK_FAILED;
        if (dataApi.getcode() != 1) {
            msg = dataApi.getMsg();
            return TASK_FAILED;
        }
        setDataReturn(dataApi.getData());
        return TASK_DONE;
    }


    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

    }


    public TaskNet clone() {
        TaskNet task = new TaskNet();
        task.setAllData(this.getAllData());
        return task;
    }


    public BaseObject getParramObject() {
        BaseObject object = getTaskParramBaseObject("parram");
        if (object == null) object = new BaseObject();
        return object;
    }
}
