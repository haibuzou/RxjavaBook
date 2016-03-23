package haibuzou.rxjavabook;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import haibuzou.rxjavabook.adapter.RxRecycleAdapter;
import haibuzou.rxjavabook.bean.AppInfo;
import haibuzou.rxjavabook.presenter.RxPresenter;
import haibuzou.rxjavabook.view.RxView;

public class MainActivity extends AppCompatActivity
        implements RxView,SwipeRefreshLayout.OnRefreshListener {

    RecyclerView recyclerView;
    RxPresenter rxPresenter;
    RxRecycleAdapter rxRecycleAdapter;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.rx_recycleview);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.rx_swipelayout);
        rxPresenter = new RxPresenter(this,this);
        swipeRefreshLayout.setOnRefreshListener(this);
        rxPresenter.findApp();
    }

    @Override
    public void onRefresh() {
        rxPresenter.findApp();
    }

    @Override
    public void setListItem(List<AppInfo> appList) {
        rxRecycleAdapter = new RxRecycleAdapter(appList,MainActivity.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(rxRecycleAdapter);
        rxRecycleAdapter.notifyDataSetChanged();
    }

    @Override
    public void showLoading() {
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void hideLoading() {
        swipeRefreshLayout.setRefreshing(false);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
