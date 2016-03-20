package com.kevin.phototag;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;
import com.clarifai.api.Tag;
import com.clarifai.api.exception.ClarifaiException;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GameFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GameFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GameFragment extends Fragment {
    private static final String TAG = GameFragment.class.getSimpleName();
    private static final int CODE_PICK = 1;

    private ArrayList<String> tags1 = new ArrayList<String>();
    private ArrayList<String> tags2 = new ArrayList<String>();
    private Firebase myFirebaseRef;

    private ListView mLeft, mRight;
    private Button mTags, mGenerate;
    private FloatingActionButton mFab;

    static final int REQUEST_IMAGE_CAPTURE = 2;
    private Camera mCamera;
    private int cameraId = 0;

    private OnFragmentInteractionListener mListener;

    private final ClarifaiClient client = new ClarifaiClient(Credentials.CLIENT_ID,
            Credentials.CLIENT_SECRET);

    public GameFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static GameFragment newInstance(int sectionNumber) {
        GameFragment fragment = new GameFragment();
        Bundle args = new Bundle();
        args.putInt("section_number", sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Firebase.setAndroidContext(getContext());
        myFirebaseRef = new Firebase("https://imagesearch.firebaseio.com/");


        myFirebaseRef.child("Message").setValue("No More Favors");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_game2, container, false);
        mGenerate = (Button) rootView.findViewById(R.id.generate_tags);
        mFab = (FloatingActionButton) rootView.findViewById(R.id.picture_button);
        mFab.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {
                //starts an Intent to take a photo
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });
        mGenerate.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {
                generateWords();

            }
        });
        mLeft = (ListView) rootView.findViewById(R.id.left_words);
        mRight = (ListView) rootView.findViewById(R.id.right_words);
        mTags = (Button) rootView.findViewById(R.id.generate_tags);

        ArrayList<String> tags1 = new ArrayList<String>();
        ArrayList<String> tags2 = new ArrayList<String>();
        for(int i = 0; i<5; i++)
        {
            tags1.add(" ");
            tags2.add(" ");
        }
        ListAdapter customAdapter1 = new ListAdapter(getContext(), R.layout.word_list, tags1);
        ListAdapter customAdapter2 = new ListAdapter(getContext(), R.layout.word_list, tags2);

        mLeft.setAdapter(customAdapter1);
        mRight.setAdapter(customAdapter2);



        return rootView;
    }

    public void generateWords()
    {

        tags1.clear();
        tags2.clear();
        myFirebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                DataSnapshot tags = snapshot.child("Tags");
                for (int i = 0; i < 5; i++)
                {

                    Random rand = new Random();
                    int val = rand.nextInt((int) tags.getChildrenCount());
                    tags1.add((String)(tags.child(String.valueOf(val)).getValue()));
                    val = rand.nextInt((int) tags.getChildrenCount());
                    tags2.add((String)(tags.child(String.valueOf(val)).getValue()));
                }
                ListAdapter customAdapter1 = new ListAdapter(getContext(), R.layout.word_list, tags1);
                ListAdapter customAdapter2 = new ListAdapter(getContext(), R.layout.word_list, tags2);

                mLeft.setAdapter(customAdapter1);
                mRight.setAdapter(customAdapter2);


            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(requestCode == CODE_PICK && resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "User picked image" + intent.getData());
            Bitmap bitmap = loadBitmapFromUri(intent.getData());
            new AsyncTask<Bitmap, Void, RecognitionResult>() {
                @Override
                protected RecognitionResult doInBackground(Bitmap... bitmaps) {
                    return recognizeBitmap(bitmaps[0]);
                }

                @Override
                protected void onPostExecute(RecognitionResult result) {
                    updateUIForResult(result);
                }
            }.execute(bitmap);
        }
        else if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK)
        {
            Log.e("xxzcd","niggggas fuccckeed up");
            Bundle extras = intent.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");
            new AsyncTask<Bitmap, Void, RecognitionResult>() {
                @Override
                protected RecognitionResult doInBackground(Bitmap... bitmaps) {
                    return recognizeBitmap(bitmaps[0]);
                }

                @Override
                protected void onPostExecute(RecognitionResult result) {
                    updateUIForResult(result);
                }
            }.execute(bitmap);

        }
    }

    private double compareTags(ArrayList<Tag> userTags, ArrayList<String> serverTags){
        double score = 0;
        for(String s : serverTags){
            for(Tag t : userTags){
                if(t.getName().equals(s)){
                    score += weightedScore(t.getProbability()) * 1000;
                }
            }
        }

        return score;
    }

    private int weightedScore(double score){
        if(score > 0.95){
            return 2;
        } else if (score > 0.9){
            return 1;
        } else {
            return 0;
        }
    }

    private ArrayList<String> correctTags(ArrayList<Tag> userTags, ArrayList<String> serverTags){
        ArrayList<String> identical = new ArrayList<>();
        for(String s : serverTags){
            for(Tag t : userTags){
                if(t.getName().equals(s)){
                    identical.add(s);
                }
            }
        }

        return identical;
    }

    private Player setScoreAndSubmitted(Player player, boolean submitted, int score){
        player.scoreChange(score);
        player.submittedChange(submitted);
        return player;
    }

    /** Sends the given bitmap to Clarifai for recognition and returns the result. */
    private RecognitionResult recognizeBitmap(Bitmap bitmap) {
        try {
            // Scale down the image. This step is optional. However, sending large images over the
            // network is slow and  does not significantly improve recognition performance.
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 320,
                    320 * bitmap.getHeight() / bitmap.getWidth(), true);

            // Compress the image as a JPEG.
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 90, out);
            byte[] jpeg = out.toByteArray();

            // Send the JPEG to Clarifai and return the result.
            return client.recognize(new RecognitionRequest(jpeg)).get(0);
        } catch (ClarifaiException e) {
            Log.e(TAG, "Clarifai error", e);
            return null;
        }
    }

    /** Updates the UI by displaying tags for the given result. */
    private void updateUIForResult(RecognitionResult result) {
        if (result != null) {
            if (result.getStatusCode() == RecognitionResult.StatusCode.OK) {
                //get probabilities
                /*
                JsonObject fullResponse = result.getJsonResponse();

                JsonArray probabilities = fullResponse.getAsJsonObject("results")
                        .getAsJsonObject("tag").getAsJsonArray("probs");

                Iterator<JsonElement> probIterator = probabilities.iterator();

                ArrayList<String> probNumber = new ArrayList<>();

                while(probIterator.hasNext()){
                    probNumber.add(probIterator.next().getAsString());
                }

                LinkedHashMap<String, String> tagAndProb = new LinkedHashMap<>();
                */
                // Display the list of tags in the UI.
                StringBuilder b = new StringBuilder();
                for (Tag tag : result.getTags()) {
                    b.append(b.length() > 0 ? ", " : "").append(tag.getName()).append(" ").append((tag.getProbability()*100)).append("\n");
                }




            } else {
                Log.e(TAG, "Clarifai: " + result.getStatusMessage());

            }
        } else {

        }

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);

        }
    }
    private Bitmap loadBitmapFromUri(Uri uri){
        try {
            // The image may be large. Load an image that is sized for display. This follows best
            // practices from http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(uri), null, opts);
            int sampleSize = 1;

            opts = new BitmapFactory.Options();
            opts.inSampleSize = sampleSize;
            return BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(uri), null, opts);
        } catch (IOException e) {
            Log.e(TAG, "Error loading image: " + uri, e);
        }
        return null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
