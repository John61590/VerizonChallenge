package com.johnbohne.verizonchallenge;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    TextView mErrorView;
    Context mContext;

    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.item_list);
        mRecyclerView.setNestedScrollingEnabled(false);
        mContext = this;
        GridLayoutManager gridLayoutManager;
//        if (getResources().getConfiguration().orientation == 1) { //portrait
            gridLayoutManager = new GridLayoutManager(this, 3);
//        } else {
//            gridLayoutManager = new GridLayoutManager(this, 5);
//        }
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mErrorView = (TextView) findViewById(R.id.error);


        assert mRecyclerView != null;


    }
    public void onStart() {
        super.onStart();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_STORAGE);
            }
        } else {
            //PERMISSION IS ALREADY GRANTED


            ArrayList<String> images = getAllShownImagesPath(this);

            if (images == null) {
                mRecyclerView.setVisibility(View.GONE);
                mErrorView.setVisibility(View.VISIBLE);
            } else {
                if (images.size() > 0) {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mRecyclerView.setAdapter(new ItemAdapter(this, images));
                } else {
                    mRecyclerView.setVisibility(View.GONE);
                    mErrorView.setVisibility(View.VISIBLE);
                }
            }
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    // permission was granted
                    ArrayList<String> images = getAllShownImagesPath(this);

                    mRecyclerView.setAdapter(new ItemAdapter(this, images));
                } else {


                    // permission denied, boo!
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /**
     * Getting All Images Path.
     *
     * @param activity
     *            the activity
     * @return ArrayList with images Path
     */
    private ArrayList<String> getAllShownImagesPath(Activity activity) {
        Cursor cursor;
        int column_index_data, column_index_thumbnail;
        ArrayList<String> listOfAllImages = new ArrayList<String>();


        Uri queryUri = MediaStore.Files.getContentUri("external");

        // Return only video and image metadata.
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.TITLE
        };


        cursor = activity.getContentResolver().query(queryUri, projection,
                selection, null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC");

        if (cursor == null) {
            return null;
        }
        column_index_data = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
        if (column_index_data == -1) {
            //no videos or images found
            return null;
        }
        while (cursor.moveToNext()) {
            listOfAllImages.add(cursor.getString(column_index_data));
        }

            cursor.close();


        return listOfAllImages;
    }




    public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
        private final String TAG = ItemAdapter.class.getSimpleName();
        private Context mContext;
        private ArrayList<String> mItems;



        public ItemAdapter(Context context, ArrayList<String> items) {
            mContext = context;
            mItems = items;

        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.result_layout, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        @Override
        public void onBindViewHolder(ItemViewHolder itemViewHolder, int position) {

            final ImageView image = itemViewHolder.getImageView();
            String path = mItems.get(position);

            //this works for both videos and images

                Glide.with(mContext)
                        .load(new File(path))
                        .placeholder(R.drawable.primary_placeholder_drawable)
                        .override(96, 96).centerCrop().into(image);

        }

        public class ItemViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            ItemViewHolder(View itemView) {
                super(itemView);

                imageView = (ImageView) itemView.findViewById(R.id.thumbnail);
            }

            public ImageView getImageView() {
                return imageView;
            }
        }
    }
}
