package com.example.android.booklistingapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Books>> {

    ///almost done i sent the data to another activity

    private String userInput;


    /**
     * TextView that is displayed when the list is empty
     */
    private TextView emptyview;


    /**
     * Constant value for the earthquake loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int EARTHQUAKE_LOADER_ID = 1;


    /**
     * Sample JSON response for a BOOK query
     */
    // private static final String BOOK_REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?q=android&maxResults=10";

    private String BOOK_REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?q=android&maxResults=10";


    /**
     * taking the query string from the user
     */

      private   String userUrl;
    /**
     * Adapter for the list of earthquakes
     */
    private customArrayAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // private static final String BOOK_REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?q=android&maxResults=10";


        emptyview = (TextView) findViewById(R.id.emptyState);


        // Create a new adapter that takes an empty list of earthquakes as input
        mAdapter = new customArrayAdapter(this, new ArrayList<Books>());

        ListView listView = (ListView) findViewById(R.id.myList);


        listView.setEmptyView(emptyview);

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        listView.setAdapter(mAdapter);

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected earthquake.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Find the current earthquake that was clicked on
                Books currentBooks = mAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                //   Uri earthquakeUri = Uri.parse(currentBooks.getmUrl());

                // Create a new intent to view the earthquake URI
                //    Intent websiteIntent = new Intent(Intent.ACTION_VIEW, earthquakeUri);

                // Send the intent to launch a new activity
                //   startActivity(websiteIntent);


                Bundle bundle = new Bundle();
                bundle.putString(myConstants.getTITLE(), currentBooks.getmTitle());
                bundle.putString(myConstants.getAUTHOR(), currentBooks.getmAuthor());
                bundle.putString(myConstants.getPUBLISHER(), currentBooks.getmPublisher());
                bundle.putString(myConstants.getDATE(), currentBooks.getmDate());
                bundle.putString(myConstants.getDESCRIPTION(), currentBooks.getmDescription());
                bundle.putString(myConstants.getURL(), currentBooks.getmUrl());
                Intent intent = new Intent(MainActivity.this, detail.class);
                intent.putExtras(bundle);
                startActivity(intent);


            }
        });


        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {

            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(EARTHQUAKE_LOADER_ID, null, this);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            emptyview.setText(R.string.no_internet_connection);
        }
    } // end of onCreate


    public void searchBooks(View view) {


        EditText editText = (EditText) findViewById(R.id.myEditText);
        userInput = editText.getText().toString();
        if (userInput.isEmpty()) {

            Toast.makeText(this,"You should search something first" , Toast.LENGTH_LONG).show();

        } else {
            // Base URI for the Books API.
            final String BOOK_BASE_URL = "https://www.googleapis.com/books/v1/volumes?q=";
            final String MAX_RESULTS = "&maxResults=10"; // Parameter that limits search results.


            userUrl = BOOK_BASE_URL + userInput + MAX_RESULTS;

            // Start the AsyncTask to fetch the earthquake data
            BooksAsyncTask task = new BooksAsyncTask();
            task.execute(userUrl);

        }
    }


    @NonNull
    @Override
    public Loader<List<Books>> onCreateLoader(int i, @Nullable Bundle bundle) {
        // TODO: Create a new loader for the given URL
        return new BooksLoader(this, userUrl);

    }


    @Override
    public void onLoadFinished(@NonNull Loader<List<Books>> loader, List<Books> earthquakes) {
        // TODO: Update the UI with the result

        // Hide loading indicator because the data has been loaded
        View loadingIndicator = findViewById(R.id.loading_spinner);
        loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "No books found."
        emptyview.setText(getString(R.string.no_books_found));

        // Clear the adapter of previous earthquake data
        mAdapter.clear();

        // If there is a valid list of {@link earthQuakeModel}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (earthquakes != null && !earthquakes.isEmpty()) {
            mAdapter.addAll(earthquakes);
        }

    }


    @Override
    public void onLoaderReset(@NonNull Loader<List<Books>> loader) {
        // TODO: Loader reset, so we can clear out our existing data.
        mAdapter.clear();

    }


    /**
     * {@link AsyncTask} to perform the network request on a background thread, and then
     * update the UI with the list of earthquakes in the response.
     * <p>
     * AsyncTask has three generic parameters: the input type, a type used for progress updates, and
     * an output type. Our task will take a String URL, and return an Earthquake. We won't do
     * progress updates, so the second generic is just Void.
     * <p>
     * We'll only override two of the methods of AsyncTask: doInBackground() and onPostExecute().
     * The doInBackground() method runs on a background thread, so it can run long-running code
     * (like network activity), without interfering with the responsiveness of the app.
     * Then onPostExecute() is passed the result of doInBackground() method, but runs on the
     * UI thread, so it can use the produced data to update the UI.
     */
    private class BooksAsyncTask extends AsyncTask<String, Void, List<Books>> {


        /**
         * This method runs on a background thread and performs the network request.
         * We should not update the UI from a background thread, so we return a list of
         * {@link Books}s as the result.
         */
        @Override
        protected List<Books> doInBackground(String... urls) {
            // Don't perform the request if there are no URLs, or the first URL is null.
            if (urls.length < 1 || urls[0] == null) {
                return null;
            }

            List<Books> result = QueryUtils.fetchBookData(urls[0]);
            return result;
        }


        /**
         * This method runs on the main UI thread after the background work has been
         * completed. This method receives as input, the return value from the doInBackground()
         * method. First we clear out the adapter, to get rid of earthquake data from a previous
         * query to USGS. Then we update the adapter with the new list of earthquakes,
         * which will trigger the ListView to re-populate its list items.
         */

        @Override
        protected void onPostExecute(List<Books> data) {
            // Clear the adapter of previous earthquake data
            mAdapter.clear();

            // If there is a valid list of {@link Earthquake}s, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (data != null && !data.isEmpty()) {
                mAdapter.addAll(data);
            }
        }
    }


}

