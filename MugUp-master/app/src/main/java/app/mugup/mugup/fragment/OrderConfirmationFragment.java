package app.mugup.mugup.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import app.mugup.mugup.R;

import static app.mugup.mugup.MainActivity.CURRENT_TAG;
import static app.mugup.mugup.MainActivity.TAG_ORDER_CONFIRMATION_FRAME;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OrderConfirmationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OrderConfirmationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderConfirmationFragment extends Fragment
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View mView;
    RelativeLayout subjectsSelection;

    private OnFragmentInteractionListener mListener;

    public OrderConfirmationFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OrderConfirmationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OrderConfirmationFragment newInstance(String param1, String param2)
    {
        OrderConfirmationFragment fragment = new OrderConfirmationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        CURRENT_TAG = TAG_ORDER_CONFIRMATION_FRAME;

        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_order_confirmation, container, false);
        mView = rootView;
        generateOrderConfirmationPage();
        return rootView;
    }

    private void generateOrderConfirmationPage()
    {
        final String id = getArguments().getString("ORDERID");
        final String status = getArguments().getString("STATUS");

        ImageView orderStatusImage = mView.findViewById(R.id.orderConfirmationTick);
        TextView orderStatus = mView.findViewById(R.id.orderStatus);
        TextView orderNumber = mView.findViewById(R.id.orderId);
        TextView orderStatusMessage = mView.findViewById(R.id.orderStatusMessage);
        Button viewLibraryButton = mView.findViewById(R.id.viewLibraryButton);

        orderNumber.setText("Order Number - #"+id);

        if(status.equals("SUCCESS"))
        {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Order Successful!");
            orderStatus.setText("Order Successful!");
            orderStatusImage.setImageResource(R.drawable.ic_order_confirmation_success);
            orderStatusMessage.setText("Your order has been confirmed. Click the button below to access your books.");
            viewLibraryButton.setVisibility(View.VISIBLE);
        }
        else
        {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Order Failed!");
            orderStatus.setText("Order Failed!");
            orderStatusImage.setImageResource(R.drawable.ic_order_confirmation_failure);
            orderStatusMessage.setText("Your order couldn't be completed. If money has been debited from your account, rest assured you will get access to your books shortly.");
        }

        viewLibraryButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Fragment fragment = new LibraryFragment();
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                fragmentTransaction.replace(R.id.frame, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commitAllowingStateLoss();
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri)
    {
        if (mListener != null)
        {
            mListener.onFragmentInteraction();
        }
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener)
        {
            mListener = (OnFragmentInteractionListener) context;
        }
        else
        {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
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
    public interface OnFragmentInteractionListener
    {
        // TODO: Update argument type and name
        void onFragmentInteraction();
    }
}
