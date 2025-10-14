package com.example.coin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CryptoAdapter(
    private val onItemClick: (CryptoCurrency) -> Unit
) : RecyclerView.Adapter<CryptoAdapter.CryptoViewHolder>() {

    private var cryptoList = listOf<CryptoCurrency>()

    inner class CryptoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.cryptoName)
        private val symbolTextView: TextView = itemView.findViewById(R.id.cryptoSymbol)
        private val priceTextView: TextView = itemView.findViewById(R.id.cryptoPrice)
        private val changeTextView: TextView = itemView.findViewById(R.id.cryptoChange)
        private val iconImageView: ImageView = itemView.findViewById(R.id.cryptoIcon)

        fun bind(crypto: CryptoCurrency) {
            nameTextView.text = crypto.name
            symbolTextView.text = crypto.symbol.uppercase()
            priceTextView.text = "$${String.format("%.2f", crypto.current_price)}"

            val change = crypto.price_change_percentage_24h
            changeTextView.text = "${String.format("%.2f", change)}%"

            if (change >= 0) {
                changeTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.green))
            } else {
                changeTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.red))
            }

            // Загрузка изображения
            Glide.with(itemView.context)
                .load(crypto.image)
                .into(iconImageView)

            itemView.setOnClickListener {
                onItemClick(crypto)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CryptoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_crypto, parent, false)
        return CryptoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CryptoViewHolder, position: Int) {
        holder.bind(cryptoList[position])
    }

    override fun getItemCount(): Int = cryptoList.size

    fun submitList(list: List<CryptoCurrency>) {
        cryptoList = list
        notifyDataSetChanged()
    }
}