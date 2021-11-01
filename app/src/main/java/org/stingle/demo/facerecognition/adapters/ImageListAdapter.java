package org.stingle.demo.facerecognition.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.stingle.demo.R;
import org.stingle.demo.facerecognition.data.Image;

import java.util.UUID;

public class ImageListAdapter extends ListAdapter<Image, ImageListAdapter.ViewHolder> {
	private final LayoutInflater layoutInflater;

	private UUID personId;

	public ImageListAdapter(Context context) {
		super(new ImageDiffCallback());

		layoutInflater = LayoutInflater.from(context);
	}


	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ViewHolder(layoutInflater.inflate(R.layout.item_image, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		holder.imageView.setImageURI(getItem(position).uri);
	}

	static class ViewHolder extends RecyclerView.ViewHolder {

		SimpleDraweeView imageView;

		public ViewHolder(@NonNull View itemView) {
			super(itemView);

			imageView = itemView.findViewById(R.id.image);
		}
	}

	static class ImageDiffCallback extends DiffUtil.ItemCallback<Image> {

		@Override
		public boolean areItemsTheSame(@NonNull Image oldItem, @NonNull Image newItem) {
			return oldItem.equals(newItem);
		}

		@Override
		public boolean areContentsTheSame(@NonNull Image oldItem, @NonNull Image newItem) {
			return areItemsTheSame(oldItem, newItem);
		}
	}
}
