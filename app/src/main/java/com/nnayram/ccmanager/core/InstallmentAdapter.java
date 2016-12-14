package com.nnayram.ccmanager.core;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.nnayram.ccmanager.R;
import com.nnayram.ccmanager.model.InstallmentCheckbox;

import java.util.ArrayList;

/**
 * Created by Rufo on 8/20/2016.
 */
public class InstallmentAdapter extends ArrayAdapter<InstallmentCheckbox> {
    private ArrayList<InstallmentCheckbox> installmentList;

    public InstallmentAdapter(Context context, int resource, ArrayList<InstallmentCheckbox> installmentList) {
        super(context, resource, installmentList);
        this.installmentList = new ArrayList<>();
        this.installmentList.addAll(installmentList);
    }

    private class ViewHolder {
        TextView installment;
        CheckBox name;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.cc_installment_info, null);

            holder = new ViewHolder();
            holder.installment = (TextView) convertView.findViewById(R.id.tv_cc_installment);
            holder.name = (CheckBox) convertView.findViewById(R.id.cb_cc_installment);
            convertView.setTag(holder);

            holder.name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    InstallmentCheckbox installment = (InstallmentCheckbox) cb.getTag();
                    installment.setSelected(cb.isChecked());
                }
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        InstallmentCheckbox installment = installmentList.get(position);
        holder.name.setText(installment.getName());
        holder.name.setChecked(installment.isSelected());
        holder.name.setTag(installment);

        return convertView;
    }

    public ArrayList<InstallmentCheckbox> getInstallmentList() {
        return installmentList;
    }
}
