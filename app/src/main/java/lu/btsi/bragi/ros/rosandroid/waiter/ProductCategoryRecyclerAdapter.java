package lu.btsi.bragi.ros.rosandroid.waiter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import java8.util.stream.StreamSupport;
import lu.btsi.bragi.ros.models.pojos.Product;
import lu.btsi.bragi.ros.models.pojos.ProductCategory;
import lu.btsi.bragi.ros.rosandroid.Config;
import lu.btsi.bragi.ros.rosandroid.MainActivity;
import lu.btsi.bragi.ros.rosandroid.R;

import static java8.util.stream.Collectors.toList;

class ProductCategoryRecyclerAdapter extends RecyclerView.Adapter<ProductCategoryRecyclerAdapter.ViewHolder> {

    private List<ProductCategory> categories;
    private List<Product> products;
    private String baseURL;
    private final MainActivity mainActivity;

    ProductCategoryRecyclerAdapter(List<ProductCategory> categories, List<Product> products, String baseURL, MainActivity mainActivity) {
        this.categories = categories;
        this.products = products;
        this.baseURL = baseURL;
        this.mainActivity = mainActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ProductCategory category = categories.get(position);
        String url = "http://"+baseURL + ":8888"+ category.getImageUrl();
        ImageLoader.getInstance().displayImage(url, holder.image);
        StreamSupport.stream(category.getProductCategoryLocalized())
                .filter(pCL -> pCL.getLanguageCode().equals(Config.getInstance().getLanguage().getCode()))
                .findFirst()
                .ifPresent(pcl -> holder.title.setText(pcl.getLabel()));
        holder.products = StreamSupport.stream(products)
                .filter(p -> p.getProductCategory().equals(category))
                .collect(toList());
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image;
        List<Product> products;

        ViewHolder(View itemView) {
            super(itemView);

            image = (ImageView)itemView.findViewById(R.id.card_image);
            title = (TextView)itemView.findViewById(R.id.card_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = ViewHolder.this.getAdapterPosition();
                    WaiterProductsFragment waiterProductsFragment = new WaiterProductsFragment();
                    waiterProductsFragment.setProducts(products);
                    mainActivity.pushFragment(waiterProductsFragment);
                }
            });
        }
    }
}
