package com.example.moviecollectionapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailsFragment extends Fragment
{
   // callback methods implemented by MainActivity  
   public interface DetailsFragmentListener
   {
      // called when a movie is deleted
      public void onMovieDeleted();
      
      // called to pass Bundle of movie's info for editing
      public void onEditMovie(Bundle arguments);
   }
   
   private DetailsFragmentListener listener;
   
   private long rowID = -1; // selected movie's rowID
   private TextView titleTextView; // displays movie's title
   private TextView yearTextView; // displays movie's year
   private TextView producerTextView; // displays movie's producer
   private TextView directorTextView; // displays movie's director
   private TextView genreTextView; // displays movie's genre
   private TextView actorTextView; // displays movie's actor
   private TextView actressTextView; // displays movie's actress
   
   // set DetailsFragmentListener when fragment attached   
   @Override
   public void onAttach(Activity activity)
   {
      super.onAttach(activity);
      listener = (DetailsFragmentListener) activity;
   }
   
   // remove DetailsFragmentListener when fragment detached
   @Override
   public void onDetach()
   {
      super.onDetach();
      listener = null;
   }

   // called when DetailsFragmentListener's view needs to be created
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState)
   {
      super.onCreateView(inflater, container, savedInstanceState);  
      setRetainInstance(true); // save fragment across config changes

      // if DetailsFragment is being restored, get saved row ID
      if (savedInstanceState != null) 
         rowID = savedInstanceState.getLong(MainActivity.ROW_ID);
      else 
      {
         // get Bundle of arguments then extract the movie's row ID
         Bundle arguments = getArguments(); 
         
         if (arguments != null)
            rowID = arguments.getLong(MainActivity.ROW_ID);
      }
         
      // inflate DetailsFragment's layout
      View view = 
         inflater.inflate(R.layout.fragment_details, container, false);               
      setHasOptionsMenu(true); // this fragment has menu items to display

      // get the EditTexts
      titleTextView = (TextView) view.findViewById(R.id.titleTextView);
      yearTextView = (TextView) view.findViewById(R.id.yearTextView);
      producerTextView = (TextView) view.findViewById(R.id.producerTextView);
      directorTextView = (TextView) view.findViewById(R.id.directorTextView);
      genreTextView = (TextView) view.findViewById(R.id.genreTextView);
      actorTextView = (TextView) view.findViewById(R.id.actorTextView);
      actressTextView = (TextView) view.findViewById(R.id.actressTextView);
      return view;
   }
   
   // called when the DetailsFragment resumes
   @Override
   public void onResume()
   {
      super.onResume();
      new LoadMovieTask().execute(rowID); // load movie at rowID
   } 

   // save currently displayed movie's row ID
   @Override
   public void onSaveInstanceState(Bundle outState) 
   {
       super.onSaveInstanceState(outState);
       outState.putLong(MainActivity.ROW_ID, rowID);
   }

   // display this fragment's menu items
   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
   {
      super.onCreateOptionsMenu(menu, inflater);
      inflater.inflate(R.menu.fragment_details_menu, menu);
   }

   // handle menu item selections
   @Override
   public boolean onOptionsItemSelected(MenuItem item) 
   {
      switch (item.getItemId())
      {
         case R.id.action_edit: 
            // create Bundle containing movie data to edit
            Bundle arguments = new Bundle();
            arguments.putLong(MainActivity.ROW_ID, rowID);
            arguments.putCharSequence("title", titleTextView.getText());
            arguments.putCharSequence("year", yearTextView.getText());
            arguments.putCharSequence("producer", producerTextView.getText());
            arguments.putCharSequence("director", directorTextView.getText());
            arguments.putCharSequence("genre", genreTextView.getText());
            arguments.putCharSequence("actor", actorTextView.getText());
            arguments.putCharSequence("actress", actressTextView.getText());            
            listener.onEditMovie(arguments); // pass Bundle to listener
            return true;
         case R.id.action_delete:
            deleteMovie();
            return true;
      }
      
      return super.onOptionsItemSelected(item);
   } 
   
   // performs database query outside GUI thread
   private class LoadMovieTask extends AsyncTask<Long, Object, Cursor> 
   {
      DatabaseConnector databaseConnector = 
         new DatabaseConnector(getActivity());

      // open database & get Cursor representing specified movie's data
      @Override
      protected Cursor doInBackground(Long... params)
      {
         databaseConnector.open();
         return databaseConnector.getOneMovie(params[0]);
      } 

      // use the Cursor returned from the doInBackground method
      @Override
      protected void onPostExecute(Cursor result)
      {
         super.onPostExecute(result);
         result.moveToFirst(); // move to the first item 
   
         // get the column index for each data item
         int titleIndex = result.getColumnIndex("title");
         int yearIndex = result.getColumnIndex("year");
         int producerIndex = result.getColumnIndex("producer");
         int directorIndex = result.getColumnIndex("director");
         int genreIndex = result.getColumnIndex("genre");
         int actorIndex = result.getColumnIndex("actor");
         int actressIndex = result.getColumnIndex("actress");
   
         // fill TextViews with the retrieved data
         titleTextView.setText(result.getString(titleIndex));
         yearTextView.setText(result.getString(yearIndex));
         producerTextView.setText(result.getString(producerIndex));
         directorTextView.setText(result.getString(directorIndex));
         genreTextView.setText(result.getString(genreIndex));
         actorTextView.setText(result.getString(actorIndex));
         actressTextView.setText(result.getString(actressIndex));
   
         result.close(); // close the result cursor
         databaseConnector.close(); // close database connection
      } // end method onPostExecute
   } // end class LoadMovieTask

   // delete a movie
   private void deleteMovie()
   {         
      // use FragmentManager to display the confirmDelete DialogFragment
      confirmDelete.show(getFragmentManager(), "confirm delete");
   } 

   // DialogFragment to confirm deletion of movie
   private DialogFragment confirmDelete = 
      new DialogFragment()
      {
         // create an AlertDialog and return it
         @Override
         public Dialog onCreateDialog(Bundle bundle)
         {
            // create a new AlertDialog Builder
            AlertDialog.Builder builder = 
               new AlertDialog.Builder(getActivity());
      
            builder.setTitle(R.string.confirm_title); 
            builder.setMessage(R.string.confirm_message);
      
            // provide an OK button that simply dismisses the dialog
            builder.setPositiveButton(R.string.button_delete,
               new DialogInterface.OnClickListener()
               {
                  @Override
                  public void onClick(
                     DialogInterface dialog, int button)
                  {
                     final DatabaseConnector databaseConnector = 
                        new DatabaseConnector(getActivity());
      
                     // AsyncTask deletes movie and notifies listener
                     AsyncTask<Long, Object, Object> deleteTask =
                        new AsyncTask<Long, Object, Object>()
                        {
                           @Override
                           protected Object doInBackground(Long... params)
                           {
                              databaseConnector.deleteMovie(params[0]); 
                              return null;
                           } 
      
                           @Override
                           protected void onPostExecute(Object result)
                           {                                 
                              listener.onMovieDeleted();
                           }
                        }; // end new AsyncTask
      
                     // execute the AsyncTask to delete movie at rowID
                     deleteTask.execute(new Long[] { rowID });               
                  } // end method onClick
               } // end anonymous inner class
            ); // end call to method setPositiveButton
            
            builder.setNegativeButton(R.string.button_cancel, null);
            return builder.create(); // return the AlertDialog
         }
      }; // end DialogFragment anonymous inner class
} // end class DetailsFragment
