package app.mugup.mugup.fragment.profile;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import app.mugup.mugup.R;

import static app.mugup.mugup.app.Config.URL_ABOUT_US;
import static app.mugup.mugup.app.Config.URL_CONTACT_US;
import static app.mugup.mugup.app.Config.URL_DISCLAIMER;
import static app.mugup.mugup.app.Config.URL_INTELLECTUAL_PROPERTY;
import static app.mugup.mugup.app.Config.URL_PRIVACY_POLICY;
import static app.mugup.mugup.app.Config.URL_REFUND_POLICY;
import static app.mugup.mugup.app.Config.URL_TERMS_CONDITIONS;
import static app.mugup.mugup.fragment.profile.AboutFragment.ABOUT_CONTENT;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AboutContentsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AboutContentsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AboutContentsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public AboutContentsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AboutContentsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AboutContentsFragment newInstance(String param1, String param2) {
        AboutContentsFragment fragment = new AboutContentsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about_contents, container, false);
        final WebView wv = view.findViewById(R.id.wv_about_contents);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String url = "";
        switch (ABOUT_CONTENT){
            case "About MugUp":
                url = URL_ABOUT_US;
                break;
            case "Terms of Use":
                url = URL_TERMS_CONDITIONS;
                break;
            case "Privacy Policy":
                url = URL_PRIVACY_POLICY;
                break;
            case "Refund Policy":
                url = URL_REFUND_POLICY;
                break;
            case "Intellectual Property":
                url = URL_INTELLECTUAL_PROPERTY;
                break;
            case "Disclaimer":
                url = URL_DISCLAIMER;
                break;
            case "Contact Us":
                url = URL_CONTACT_US;
                break;
        }
        wv.loadUrl(url);

        // By default, redirects cause jump from WebView to default
        // system browser. Overriding url loading allows the WebView
        // to load the redirect into this screen.
        wv.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
