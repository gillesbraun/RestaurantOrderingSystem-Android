package lu.btsi.bragi.ros.rosandroid.waiter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import java8.util.stream.StreamSupport;
import lu.btsi.bragi.ros.models.pojos.Product;
import lu.btsi.bragi.ros.models.pojos.ProductCategory;
import lu.btsi.bragi.ros.rosandroid.Config;
import lu.btsi.bragi.ros.rosandroid.MainActivity;
import lu.btsi.bragi.ros.rosandroid.R;
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionManager;

import static java8.util.stream.Collectors.toList;

class ProductCategoryRecyclerAdapter extends RecyclerView.Adapter<ProductCategoryRecyclerAdapter.ViewHolder> {

    public interface ProductCategoryClickedListener {
        void onProductCategoryClicked(ProductCategory category);
    }

    private final ArrayList<ProductCategory> categories = new ArrayList<>();
    private final ProductCategoryClickedListener listener;
    private final String baseURL = ConnectionManager.getInstance().getRemoteIPAdress();

    ProductCategoryRecyclerAdapter(ProductCategoryClickedListener listener) {
        this.listener = listener;
    }

    public void addItems(List<ProductCategory> items) {
        categories.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ProductCategory category = categories.get(position);
        String url = "http://" + baseURL + ":8888"+ category.getImageUrl();
        ImageLoader.getInstance().displayImage(url, holder.image);
        StreamSupport.stream(category.getProductCategoryLocalized())
                .filter(pCL -> pCL.getLanguageCode().equals(Config.getInstance().getLanguage().getCode()))
                .findFirst()
                .ifPresent(pcl -> holder.title.setText(pcl.getLabel()));
        holder.itemView.setOnClickListener(v -> listener.onProductCategoryClicked(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image;

        ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView)itemView.findViewById(R.id.card_image);
            title = (TextView)itemView.findViewById(R.id.card_title);
        }
    }
}
