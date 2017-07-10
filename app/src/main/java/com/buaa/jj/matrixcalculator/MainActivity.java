package com.buaa.jj.matrixcalculator;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;

import java.util.LinkedList;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,AddMatrixFragment.FragmentInteraction{

    @Override
    public void process(int r,int c,int[][] n,String name) {
        binder.createMatrix(r,c,n,name);
        getFragmentManager().beginTransaction().remove(fragment).commit();
    }

    ServiceConnection conn=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder=((MatrixCalculator.MyBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    Fragment fragment;
    Context mainActivity =this;
    Intent MCService;
    MatrixCalculator binder;
    ListView listView;
    int fragmentStatus=0;
    int Id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MCService=new Intent(MainActivity.this,MatrixCalculator.class);
        startService(MCService);
        bindService(MCService,conn, BIND_AUTO_CREATE);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listView=(ListView) findViewById(R.id.list_item);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final PopupMenu popupMenu=new PopupMenu(mainActivity,view);
                if(fragmentStatus==0)
                    popupMenu.getMenuInflater().inflate(R.menu.activity_main_drawer,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int id = menuItem.getItemId();

                        if (id == R.id.addMatrix) {
                            AlertDialog.Builder addMatrix=new AlertDialog.Builder(mainActivity);
                            addMatrix.setTitle("Add Matrix");
                            final View dialogView= LayoutInflater.from(mainActivity).inflate(R.layout.add_matrix_dialog,null,false);
                            addMatrix.setView(dialogView);
                            addMatrix.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Bundle bundle=new Bundle();
                                    int[] n=new int[2];
                                    EditText editText=(EditText) dialogView.findViewById(R.id.editText);
                                    n[0]=Integer.parseInt(editText.getText().toString());
                                    editText=(EditText) dialogView.findViewById(R.id.editText2);
                                    n[1]=Integer.parseInt(editText.getText().toString());
                                    bundle.putIntArray("Row&Column",n);
                                    fragment =new AddMatrixFragment();
                                    fragment.setArguments(bundle);
                                    getFragmentManager().beginTransaction().replace(R.id.fragment_place,fragment).commit();
                                }
                            });
                            addMatrix.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            addMatrix.setCancelable(false);
                            addMatrix.show();
                        } else if (id == R.id.help) {
                            AlertDialog.Builder help=new AlertDialog.Builder(mainActivity);
                            help.setTitle("Help");
                            help.setMessage(R.string.Help);
                            help.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            help.setCancelable(false);
                            help.show();
                        } else if (id == R.id.about) {
                            AlertDialog.Builder about=new AlertDialog.Builder(mainActivity);
                            about.setTitle("About");
                            about.setMessage(R.string.About);
                            about.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            about.setCancelable(false);
                            about.show();
                        }else if (id==R.id.editMatrix){

                        }else if (id==R.id.deleteMatrix){
                            binder.removeMatrix(Id);
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        drawer.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                this.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                MatrixListAdapter matrixListAdapter =new MatrixListAdapter(binder.getMList(), mainActivity);
                listView.setAdapter(matrixListAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Id=i;
                        Matrix tmp=binder.getMList().get(i);
                        Bundle bundle=new Bundle();
                        bundle.putSerializable("Matrix",tmp);
                        fragment=new MatrixFragment();
                        fragment.setArguments(bundle);
                        getFragmentManager().beginTransaction().replace(R.id.fragment_place,fragment).commit();
                    }
                });
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public MatrixCalculator getService(){
        return binder;
    }
}
