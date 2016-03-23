package haibuzou.rxjavabook.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import haibuzou.rxjavabook.R;
import haibuzou.rxjavabook.bean.AppInfo;


public class RxRecycleAdapter extends RecyclerView.Adapter<RxRecycleAdapter.RxViewHolder> {

    List<AppInfo> dataList;
    LayoutInflater inflater;

    public RxRecycleAdapter(List<AppInfo> dataList, Context context) {
        this.dataList = dataList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public RxViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RxViewHolder(inflater.inflate(R.layout.rx_recycle_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RxViewHolder holder, int position) {
        holder.nameTxt.setText(dataList.get(position).mName);
        if (null != dataList.get(position).mIcon)
            holder.logoImg.setImageDrawable(dataList.get(position).mIcon);
    }


    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class RxViewHolder extends RecyclerView.ViewHolder {

        TextView nameTxt;
        ImageView logoImg;

        public RxViewHolder(View itemView) {
            super(itemView);
            nameTxt = (TextView) itemView.findViewById(R.id.app_name);
            logoImg = (ImageView) itemView.findViewById(R.id.app_logo);
        }
    }
}
