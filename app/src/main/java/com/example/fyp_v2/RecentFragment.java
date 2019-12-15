package com.example.fyp_v2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp_v2.Class.Receipt;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RecentFragment extends Fragment{

    private RecyclerView mRecyclerView;
    private ReceiptAdapter mAdapter;
    private DatabaseReference mDatabseRef;
    private List<Receipt> mUploads;
    private DataSnapshot dataSnapshot;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_recent, container, false);

        mRecyclerView = rootView.findViewById(R.id.recyler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mUploads = new ArrayList<>();
        mDatabseRef = FirebaseDatabase.getInstance().getReference("Receipt").child(FirebaseAuth.getInstance().getUid());

        mDatabseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    Receipt receipt = postSnapshot.getValue(Receipt.class);
                    mUploads.add(receipt);
                }

                mAdapter = new ReceiptAdapter(getActivity(), mUploads, new ReceiptAdapter.OnReceiptListener() {
                    @Override
                    public void onReceiptClick(final int position) {

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                        alertDialogBuilder.setMessage("Are you sure wants to remove the record");
                        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String id = mUploads.get(position).getReceiptID();
                                DatabaseReference database = FirebaseDatabase.getInstance().getReference("Receipt").child(FirebaseAuth.getInstance().getUid()).child(id);
                                database.removeValue();
                                Log.i("ID",id);
                                Toast.makeText(getActivity(), "Remove Successfully", Toast.LENGTH_LONG).show();
                            }
                        });
                        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(getActivity(), "Failed Remove", Toast.LENGTH_LONG).show();
                            }
                        });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                });

                mRecyclerView.setAdapter(mAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

}





