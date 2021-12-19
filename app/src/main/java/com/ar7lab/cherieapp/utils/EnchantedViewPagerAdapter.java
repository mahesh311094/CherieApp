package com.ar7lab.cherieapp.utils;

import android.view.View;

import androidx.viewpager.widget.PagerAdapter;

import java.util.List;


public class EnchantedViewPagerAdapter extends PagerAdapter {

    private final List<?> list;
    private boolean enableCarrouselMode;

    public EnchantedViewPagerAdapter(List<?> list) {
        this.list = list;
        enableCarrouselMode = false;
    }

    @Override
    public int getCount() {
        if(enableCarrouselMode) {
            return list.size() * 500;
        }else{
            return list.size();
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return false;
    }

    public void enableCarrousel(){
        enableCarrouselMode = true;
        notifyDataSetChanged();
    }

    public void disableCarrousel(){
        enableCarrouselMode = false;
        notifyDataSetChanged();
    }

    public int getMiddlePosition(){
        if (list.size() == 0) {
            return 0;
        } else {
            return (list.size() * 500) / 2;
        }
    }

    public void removeItem(int position) {
        int itemPosition;

        if(enableCarrouselMode) {
            itemPosition = position % list.size();
        }else{
            itemPosition = position;
        }

        list.remove(list.get(itemPosition));
        notifyDataSetChanged();
    }
}
