package haibuzou.rxjavabook;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import haibuzou.rxjavabook.adapter.RxRecycleAdapter;
import haibuzou.rxjavabook.bean.AppInfo;
import haibuzou.rxjavabook.presenter.RxPresenter;
import haibuzou.rxjavabook.view.RxView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,RxView,SwipeRefreshLayout.OnRefreshListener {

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
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
    }

    @Override
    public void showLoading() {
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void hideLoading() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.first_observable) {
            rxPresenter.findApp();
        } else if (id == R.id.senior_observable) {
            rxPresenter.findAppSenior();
        } else if (id == R.id.timer) {
            rxPresenter.findTimerApp();
        } else if (id == R.id.interval) {
            rxPresenter.findIntervalApp();
        } else if (id == R.id.range) {
            rxPresenter.findRangAppInfo();
        } else if (id == R.id.filter) {
            rxPresenter.findFilterApp();
        }else if (id == R.id.take) {
            rxPresenter.findTakeAppInfo();
        }else if (id == R.id.takeLast) {
            rxPresenter.findTakeLastAppInfo();
        }else if (id == R.id.Distinct) {
            rxPresenter.findDistinctAppInfo();
        }else if (id == R.id.DistinctUntilsChanged) {
            rxPresenter.findDistinctUntilsChangedAppInfo();
        }else if (id == R.id.first) {
            rxPresenter.findFirstAppInfo();
        }else if (id == R.id.firstOrDefault) {
            rxPresenter.findFirstOrDefaultAppInfo();
        }else if (id == R.id.skip) {
            rxPresenter.findSkipAppInfo();
        }else if (id == R.id.skipLast) {
            rxPresenter.findSkipLastAppInfo();
        }else if (id == R.id.elementAt) {
            rxPresenter.findElementAtAppInfo();
        }else if (id == R.id.Sample) {
            rxPresenter.findSampleAppInfo();
        }else if (id == R.id.TimeOut) {
            rxPresenter.findTimeOutAppInfo();
        }else if (id == R.id.debounce) {
            rxPresenter.findDebounceAppInfo();
        }else if (id == R.id.map) {
            rxPresenter.findMapAppInfo();
        }else if (id == R.id.flatmap) {
            rxPresenter.findFlatMapAppInfo();
        }else if (id == R.id.concatMap) {
            rxPresenter.findConcatMapAppInfo();
        }else if (id == R.id.flatMapIterable) {
            rxPresenter.findFlatMapIterableAppInfo();
        }else if (id == R.id.switchMap) {
            rxPresenter.findSwitchMapAppInfo();
        }else if (id == R.id.scan) {
            rxPresenter.findScanAppInfo();
        }else if (id == R.id.groupBy) {
            rxPresenter.findGroupByAppInfo();
        }else if (id == R.id.Buffer) {
            rxPresenter.findBufferAppInfo();
        }else if (id == R.id.window) {
            rxPresenter.findWindowAppInfo();
        }else if (id == R.id.cast) {
            rxPresenter.findCastAppInfo();
        }else if (id == R.id.merge) {
            rxPresenter.findMergeAppInfo();
        }else if (id == R.id.zip) {
            rxPresenter.findZipAppInfo();
        }else if (id == R.id.join) {
            rxPresenter.findJoinAppInfo();
        }else if (id == R.id.combineLatest) {
            rxPresenter.findComebineLastAppInfo();
        }else if (id == R.id.and_then_when) {
            rxPresenter.findAndThenWhenAppInfo();
        }else if (id == R.id.switchOnNext) {
            rxPresenter.findSwitchOnNextAppInfo();
        }else if (id == R.id.startWith) {
            rxPresenter.findStartWithAppInfo();
        }else if (id == R.id.SubscribeOnandObserveOn) {
            rxPresenter.dispatcher();
        }else if (id == R.id.long_task) {
            rxPresenter.longTask();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
