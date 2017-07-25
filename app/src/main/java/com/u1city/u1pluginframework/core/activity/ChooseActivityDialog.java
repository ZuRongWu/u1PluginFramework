package com.u1city.u1pluginframework.core.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.u1city.u1pluginframework.R;
import com.u1city.u1pluginframework.core.PluginIntent;
import com.u1city.u1pluginframework.core.PluginManager;
import com.u1city.u1pluginframework.core.pm.PackageManager;
import com.u1city.u1pluginframework.core.pm.PluginApk;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuzr on 2017/7/19.
 * 展示所有满足条件的activity列表
 */

public class ChooseActivityDialog extends FragmentActivity implements AdapterView.OnItemClickListener{
    public static final String KEY_ACTIVITIES = "key_activities";

    private List<ActivityInfo>  mActivities;
    private PluginManager mPluginManager;
    private PackageManager mPackageManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_dialog);
        mActivities = getIntent().getParcelableArrayListExtra(KEY_ACTIVITIES);
        if(mActivities == null){
            mActivities = new ArrayList<>(0);
        }
        mPluginManager = PluginManager.getInstance(this.getApplicationContext());
        mPackageManager = PackageManager.getInstance(this.getApplicationContext());
        ListView lvActivities = (ListView) findViewById(R.id.lv_activities);
        lvActivities.setAdapter(new MyAdapter());
        lvActivities.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ActivityInfo info = mActivities.get(i);
        Intent original = getIntent();
        //activities只要在这个activity有用，不需要传递给目标activity
        original.removeExtra(KEY_ACTIVITIES);
        PluginIntent intent = new PluginIntent(original);
        intent.setPluginName(info.packageName);
        intent.setPluginComponentName(info.name);
        mPluginManager.startPluginActivityForResult(this,intent,0);
        finish();
    }

    private class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mActivities.size();
        }

        @Override
        public Object getItem(int i) {
            return mActivities.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHandler hodler;
            if(view == null){
                view = View.inflate(ChooseActivityDialog.this,R.layout.item_choose_activity,null);
                hodler = new ViewHandler();
                hodler.tvItem = (TextView) view.findViewById(R.id.tv_item);
                view.setTag(hodler);
            }else {
                hodler = (ViewHandler) view.getTag();
            }
            ActivityInfo info = mActivities.get(i);
            PluginApk apk = mPackageManager.getPlugin(info.packageName);
            assert apk != null;
            Drawable logo = apk.getResources().getDrawable(info.logo);
            if(logo == null){
                logo = apk.getResources().getDrawable(apk.getApplicationInfo().logo);
            }
            String label = apk.getResources().getString(info.labelRes);
            if(TextUtils.isEmpty(label)){
                label = apk.getResources().getString(apk.getApplicationInfo().labelRes);
            }
            if(TextUtils.isEmpty(label)){
                label = "未知activity";
            }
            hodler.tvItem.setText(label);
            hodler.tvItem.setCompoundDrawablesWithIntrinsicBounds(logo,null,null,null);
            return view;
        }
    }

    private class ViewHandler {
        TextView tvItem;
    }
}
