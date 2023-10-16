package com.example.shoppingonline;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class ShoppingCartActivity extends AppCompatActivity implements View.OnClickListener{
    private final static String TAG = "ShoppingCartActivity";
    private ImageView iv_menu;
    private TextView tv_count;
    private TextView tv_total_price;
    private LinearLayout ll_content;
    private LinearLayout ll_cart;
    private LinearLayout ll_empty;
    private CartDBHelper mCartHelper; // 声明一个购物车数据库的帮助器对象
    private GoodsDBHelper mGoodsHelper; // 声明一个商品数据库的帮助器对象
    private int mCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);
        iv_menu = findViewById(R.id.iv_menu);
        TextView tv_title = findViewById(R.id.tv_title);
        tv_count = findViewById(R.id.tv_count);
        tv_total_price = findViewById(R.id.tv_total_price);
        ll_content = findViewById(R.id.ll_content);
        ll_cart = findViewById(R.id.ll_cart);
        ll_empty = findViewById(R.id.ll_empty);
        iv_menu.setOnClickListener(this);
        findViewById(R.id.btn_shopping_channel).setOnClickListener(this);
        findViewById(R.id.btn_settle).setOnClickListener(this);
        iv_menu.setVisibility(View.VISIBLE);
        tv_title.setText("购物车");
    }

    private void showCount(int count) {
        mCount = count;
        tv_count.setText("" + mCount);
        if (mCount == 0) {
            ll_content.setVisibility(View.GONE);
            ll_cart.removeAllViews();
            ll_empty.setVisibility(View.VISIBLE);
        } else {
            ll_content.setVisibility(View.VISIBLE);
            ll_empty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.iv_menu) {
            openOptionsMenu();
        }
        else if (v.getId() == R.id.btn_shopping_channel) {
            Intent intent =  new Intent(this, ShoppingChannelActivity.class);
            startActivity(intent);
        }
        else if (v.getId() == R.id.btn_settle) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("结算商品");
            builder.setMessage("客观抱歉，支付功能尚未开通，请下次再来");
            builder.setPositiveButton("我知道了",null);
            builder.create().show();
        }
    }
    private HashMap<Long, GoodsInfo> mGoodsMap = new HashMap<>();
    private HashMap<Integer, CartInfo> mCartGoods = new HashMap<>();
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        return super.onOptionsItemSelected(item);
        int id = item.getItemId();
        if(id == R.id.menu_shopping) {
            Intent intent = new Intent(this, ShoppingChannelActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.menu_clear) {
            mCartHelper.deleteAll();
            ll_cart.removeAllViews();
            SharedUtil.getInstance(this).writeShared("count", "0");
            showCount(0);
            mCartGoods.clear();
            mGoodsMap.clear();
            Toast.makeText(this, "购物车已清空",Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.menu_return) {
            finish();
        }
        return true;
    }
    private View mContextView;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        mContextView = v;
        getMenuInflater().inflate(R.menu.menu_goods, menu);
    }

    private void goDetail(long rowid) {
        Intent intent = new Intent(this, ShoppingDetailActivity.class);
        intent.putExtra("goods_id", rowid);
        startActivity(intent);
    }
    private ArrayList<CartInfo> mCartArray = new ArrayList<>();

    private void refreshTotalPrice() {
        int total_price = 0;
        for (CartInfo cartInfo : mCartArray) {
            GoodsInfo goodsInfo = mGoodsMap.get(cartInfo.goods_id);
            total_price += goodsInfo.price * cartInfo.count;
        }
        tv_total_price.setText(""+total_price);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        CartInfo info = mCartGoods.get(mContextView.getId());
        int id = item.getItemId();
        if(id == R.id.menu_detail) {
            goDetail(info.goods_id);
        }
        else if (id == R.id.menu_delete) {
            long goods_id = info.goods_id;
            mCartHelper.delete("goods_id=" + goods_id);
            ll_cart.removeView(mContextView);
            int left_count = mCount - info.count;
            for (int i=0;i< mCartArray.size();i++) {
                if(goods_id == mCartArray.get(i).goods_id) {
                    left_count = mCount - mCartArray.get(i).count;
                    mCartArray.remove(i);
                    break;
                }
            }
            SharedUtil.getInstance(this).writeShared("count", ""+left_count);
            showCount(left_count);
            Toast.makeText(this, "已从购物车删除"+mGoodsMap.get(goods_id).name, Toast.LENGTH_SHORT).show();
            mGoodsMap.remove(goods_id);
            refreshTotalPrice();
        }
        return true;
    }
}