package com.ar7lab.cherieapp.ui.signup

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.ar7lab.cherieapp.model.Country

/**
 * Country code adapter for get country dropdown
 * @property simple_spinner_dropdown_item is the xml file
 * */
class CountryCodeAdapter(context: Context, private var mCountryCodes: List<Country>) :
    ArrayAdapter<Country>(context, android.R.layout.simple_spinner_dropdown_item) {
    private var mSuggestions: ArrayList<Country> = ArrayList()
    private var tempCountryCodes = ArrayList(mCountryCodes)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        val holder: ViewHolder

        if (v == null) {
            v = LayoutInflater.from(context)
                .inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
            holder = ViewHolder()
            holder.tvName = v.findViewById(android.R.id.text1)
            v.tag = holder
        } else {
            holder = v.tag as ViewHolder
        }

        holder.tvName?.text = getItem(position).name

        return v!!
    }

    class ViewHolder {
        var tvName: TextView? = null
        var vLine: View? = null
    }

    override fun getItem(position: Int): Country {
        return if (mSuggestions.isEmpty()) mCountryCodes[position] else mSuggestions[position]
    }

    override fun getCount(): Int {
        return if (mSuggestions.isEmpty()) mCountryCodes.size else mSuggestions.size
    }

//    override fun getItemId(position: Int): Long {
//        return if (mSuggestions.isEmpty()) mCountries[position].countryPhoneCode else mSuggestions[position].countryPhoneCode
//    }

    override fun getFilter(): Filter {
        return filter
    }

    private var filter = object : Filter() {
        override fun convertResultToString(resultValue: Any?): CharSequence {
            return (resultValue as Country).name
        }

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            mSuggestions.clear()
            if (constraint != null) {
                for (item in tempCountryCodes) {
//                    if (item.name.toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                    mSuggestions.add(item)
//                    }
                }
                val filterResults = FilterResults()
                filterResults.values = mSuggestions
                filterResults.count = mSuggestions.size
                return filterResults
            } else {
                return FilterResults()
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            results?.values?.let {
                val items = it as ArrayList<*>
                if (results.count > 0) {
                    clear()
                    for (item in items) {
                        add(item as Country?)
                        notifyDataSetChanged()
                    }
                } else {
                    clear()
                    notifyDataSetChanged()
                }
            }
        }
    }
}