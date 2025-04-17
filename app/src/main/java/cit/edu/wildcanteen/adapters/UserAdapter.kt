package cit.edu.wildcanteen.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cit.edu.wildcanteen.R
import cit.edu.wildcanteen.UserInfo
import com.bumptech.glide.Glide

class UserAdapter(private val onItemClick: (UserInfo) -> Unit) :
    ListAdapter<UserInfo, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserViewHolder(itemView: View, private val onItemClick: (UserInfo) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val userName: TextView = itemView.findViewById(R.id.user_name)
        private val userAvatar: ImageView = itemView.findViewById(R.id.user_avatar)

        fun bind(user: UserInfo) {
            userName.text = user.name ?: "Unknown User"

            Glide.with(itemView.context)
                .load(user.profileImageUrl)
                .placeholder(R.drawable.hd_user)
                .error(R.drawable.hd_user)
                .circleCrop()
                .into(userAvatar)

            itemView.setOnClickListener {
                onItemClick(user)
            }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<UserInfo>() {
        override fun areItemsTheSame(oldItem: UserInfo, newItem: UserInfo): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: UserInfo, newItem: UserInfo): Boolean {
            return oldItem == newItem
        }
    }
}