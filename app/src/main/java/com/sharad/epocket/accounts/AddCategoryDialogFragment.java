package com.sharad.epocket.accounts;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sharad.epocket.R;
import com.sharad.epocket.utils.Constant;
import com.sharad.epocket.widget.AutofitRecyclerView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Sharad on 24-Jul-16.
 */

public class AddCategoryDialogFragment extends DialogFragment {
    private static final String ARG_CATEGORY_ID   = "category_id";
    private static final String ARG_CATEGORY_TYPE = "category_type";
    private DataSourceCategory db;
    private ICategory category;
    private long id = Constant.INVALID_ID;

    private OnCategoryDialogListener onCategoryDialogListener;

    ImageButton bCategory;
    EditText eTitle;

    public AddCategoryDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param id Database id for editing, -1 for adding new.
     * @return A new instance of fragment AddBillDialogFragment.
     */
    public static AddCategoryDialogFragment newInstance(long id, int type) {
        AddCategoryDialogFragment fragment = new AddCategoryDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CATEGORY_ID, id);
        args.putInt(ARG_CATEGORY_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id = Constant.INVALID_ID;
        int type = ICategory.CATEGORY_TYPE_EXPENSE;
        if (getArguments() != null) {
            id = getArguments().getLong(ARG_CATEGORY_ID);
            type = getArguments().getInt(ARG_CATEGORY_TYPE);
        }

        db = new DataSourceCategory(getContext());
        if(id != Constant.INVALID_ID) {
            category = db.getCategory(id);
        } else {
            int colorList[] = getContext().getResources().getIntArray(R.array.light_palette);
            Random r = new Random();
            int index = r.nextInt(colorList.length - 1);
            int color = colorList[index];
            int imageIndex = CategoryImageList.RESOURCE_UNKNOWN;
            category = new ICategory(0, imageIndex, color, "", type, 0);
        }

        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Material_Light_Dialog_MinWidth);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.dialog_add_category, container, false);

        Button bSave = (Button) rootView.findViewById(R.id.save);
        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onSave(); }
        });

        ImageButton bClose = (ImageButton) rootView.findViewById(R.id.close);
        bClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { dismiss(); }
        });

        ImageButton bDelete = (ImageButton) rootView.findViewById(R.id.delete);
        bDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onDelete(); }
        });

        final AutofitRecyclerView recyclerView = (AutofitRecyclerView)rootView.findViewById(R.id.recyclerView);

        bCategory = (ImageButton) rootView.findViewById(R.id.category);
        bCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recyclerView.getVisibility() != View.VISIBLE) {
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        });

        eTitle = (EditText) rootView.findViewById(R.id.title);
        if(id != Constant.INVALID_ID) {
            bCategory.setImageResource(category.getImageResource());
            eTitle.setText(category.getTitle());
            bDelete.setVisibility(View.VISIBLE);
        }

        final ArrayList<ICategory> itemList = new ArrayList<>();
        for(int i=0; i<CategoryImageList.RESOURCE_COUNT; i++) {
            itemList.add(new ICategory(i));
        }
        CategoryRecyclerAdapter rcAdapter = new CategoryRecyclerAdapter(itemList);
        rcAdapter.setIconTint(ContextCompat.getColor(getContext(), R.color.secondary_text));
        rcAdapter.setOnItemClickListener(new CategoryRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                category.setImageIndex(position);
                bCategory.setImageResource(category.getImageResource());
                recyclerView.setVisibility(View.GONE);
            }

            @Override
            public void onItemLongClick(View view, int position) { }
        });
        recyclerView.setAdapter(rcAdapter);

        int color = ContextCompat.getColor(getContext(), R.color.transaction_expense);
        if(category.getType() == ICategory.CATEGORY_TYPE_INCOME) {
            color = ContextCompat.getColor(getContext(), R.color.transaction_income);
        }
        View accent = rootView.findViewById(R.id.accent);
        accent.setBackgroundColor(color);
        TextView title = (TextView) rootView.findViewById(R.id.title_text);
        title.setTextColor(color);
        bClose.setColorFilter(color);
        bDelete.setColorFilter(color);
        bCategory.setColorFilter(color);

        return rootView;
    }

    private void onDelete() {
        db.deleteCategory(id);
        if(onCategoryDialogListener != null) {
            onCategoryDialogListener.onCategoryDialogRemove(id);
        }
        dismiss();
    }

    private void onSave() {
        if(eTitle.getText().length() > 0) {
            category.setTitle(eTitle.getText().toString());
            if(id == Constant.INVALID_ID) {
                category.setId(db.insertCategory(category));
            } else {
                db.updateCategory(category);
            }
            if(onCategoryDialogListener != null) {
                onCategoryDialogListener.onCategoryDialogFinish(category);
            }
        }
        dismiss();
    }

    public void setOnCategoryDialogListener(OnCategoryDialogListener listener ) {
        onCategoryDialogListener = listener;
    }

    public interface OnCategoryDialogListener {
        void onCategoryDialogFinish(ICategory category);
        void onCategoryDialogRemove(long id);
    }
}
